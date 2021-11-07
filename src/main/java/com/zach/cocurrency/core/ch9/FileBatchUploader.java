package com.zach.cocurrency.core.ch9;

import com.zach.cocurrency.utils.Debug;
import com.zach.cocurrency.utils.Tools;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPReply;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/11/5 7:59
 * Version :1.0
 */
public class FileBatchUploader implements Closeable {
    private final String ftpServer;
    private final String userName;
    private final String password;
    private final String targetRemoteDir;
    private final FTPClient ftp = new FTPClient();
    private final CompletionService<File> completionService;
    private final ExecutorService worker;
    private final ExecutorService dispatcher;

    public FileBatchUploader(String ftpServer, String userName, String password, String targetRemoteDir) {
        this.ftpServer = ftpServer;
        this.userName = userName;
        this.password = password;
        this.targetRemoteDir = targetRemoteDir;
        this.worker = Executors.newSingleThreadExecutor();
        this.dispatcher = Executors.newSingleThreadExecutor();
        this.completionService = new ExecutorCompletionService<>(worker);
    }

    public void uploadFiles(final Set<File> files) {
        dispatcher.submit(() -> {
            try {
                doUploadFiles(files);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void doUploadFiles(Set<File> files) throws InterruptedException {
        //批量提交文件长传任务
        for (final File file : files) {
            completionService.submit(new UploadTask(file));
        }
        Set<File> md5Files = new HashSet<File>();
        for (File file : files) {
            try {
                Future<File> future = completionService.take();
                File uploadedFile = future.get();
                File md5File = generateMD5(moveToSuccessDir(uploadedFile));
                md5Files.add(md5File);
            } catch (ExecutionException | InterruptedException | IOException | NoSuchAlgorithmException e) {
                e.printStackTrace();
                moveToDeadDir(file);
            }
        }
        for (File file : md5Files) {
            // 上传相应的MD5文件
            completionService.submit(new UploadTask(file));
        }
        //检查md5上传的结果
        int successUploaded = md5Files.size();
        for (int i = 0; i < successUploaded; i++) {
            Future<File> future = completionService.take();
            try {
                File uploadFile = future.get();
                md5Files.remove(uploadFile);
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }
        //将剩余(即未上成功)md5的文件移动到相应的备份目录中
        for (File file : md5Files) {
            moveToDeadDir(file);
        }
    }

    private File generateMD5(File file) throws IOException, NoSuchAlgorithmException {
        String md5 = Tools.md5sum(file);
        File md5File = new File(file.getAbsolutePath() + ".md5");
        Files.write(Paths.get(md5File.getAbsolutePath()), md5.getBytes("UTF-8"));
        return md5File;
    }

    private static File moveToSuccessDir(File file) {
        File targetFile = null;
        try {
            targetFile = moveFile(file, Paths.get(file.getParent(), "..", "backup", "success"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return targetFile;
    }

    private static File moveToDeadDir(File file) {
        File targetFile = null;
        try {
            targetFile = moveFile(file, Paths.get(file.getParent(), "..", "backup", "dead"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return targetFile;
    }

    private static File moveFile(File srcFile, Path destPath) throws IOException {
        Path sourcePath = Paths.get(srcFile.getAbsolutePath());
        if (!Files.exists(destPath)) {
            Files.createDirectories(destPath);
        }
        Path destFile = destPath.resolve(srcFile.getName());
        Files.move(sourcePath, destFile,
                StandardCopyOption.REPLACE_EXISTING);
        return destFile.toFile();
    }


    @Override
    public void close() throws IOException {
        dispatcher.shutdown();
        try {
            dispatcher.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        worker.shutdown();
        try {
            worker.awaitTermination(60, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Tools.silentClose(() -> {
            if (ftp.isConnected()) {
                ftp.disconnect();
            }
        });
    }
    // 初始化FTP客户端
    public void init() throws Exception {
        FTPClientConfig config = new FTPClientConfig();
        ftp.configure(config);
        int reply;
        ftp.connect(ftpServer);
        Debug.info("FTP Reply:%s", ftp.getReplyString());
        reply = ftp.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new Exception("FTP server refused connection.");
        }
        boolean isOK = ftp.login(userName, password);
        if (isOK) {
            Debug.info("FTP Reply:%s", ftp.getReplyString());
        } else {
            throw new Exception("Failed to login." + ftp.getReplyString());
        }
        reply = ftp.cwd(targetRemoteDir);
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftp.disconnect();
            throw new Exception("Failed to change working directory.reply:"
                    + reply);
        } else {
            Debug.info("FTP Reply:%s", ftp.getReplyString());
        }
        ftp.setFileType(FTP.ASCII_FILE_TYPE);
    }
    private class UploadTask implements Callable<File> {
        private final File file;

        public UploadTask(File file) {
            this.file = file;
        }

        @Override
        public File call() throws Exception {
            Debug.info("uploading %s", file.getCanonicalPath());
            upload(file);
            return file;
        }
    }

    // 将指定的文件上传至FTP服务器
    protected void upload(File file) throws Exception {
        boolean isOK;
        try (InputStream dataIn = new BufferedInputStream(new FileInputStream(file))) {
            isOK = ftp.storeFile(file.getName(), dataIn);
        }
        if (!isOK) {
            throw new IOException("Failed to upload " + file + ",reply:" + ","
                    + ftp.getReplyString());
        }
    }
}
