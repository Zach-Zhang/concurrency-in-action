package com.zach.cocurrency.core.ch4.case01;

import com.zach.cocurrency.utils.Debug;
import com.zach.cocurrency.utils.Tools;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description: 大文件下载器, 基于数据分割并发化策略
 * @Author Zach
 * @Date 2021/10/13 8:00
 * Version :1.0
 */

public class BigFileDownloader {
    protected final URL requestURL;
    protected final long fileSize;

    protected final Storage storage;
    protected final AtomicBoolean taskCanceled = new AtomicBoolean(false);

    public BigFileDownloader(String strURL) throws Exception {
        requestURL = new URL(strURL);
        fileSize = retieveFileSize(requestURL);
        String fileName = strURL.substring(strURL.lastIndexOf('/') + 1);
        storage = new Storage(fileSize, fileName);
    }

    /**
     * 下载指定文件
     *
     * @param taskCount      任务个数
     * @param reportInterval 下载报告周期
     */
    public void download(int taskCount, long reportInterval) throws InterruptedException {
        long chunkSizePerThread = fileSize / taskCount;
        //下载数据段的起始字节
        long lowerBound = 0;
        //下载数据段的结束字节
        long upperBound = 0;


        for (int i = taskCount - 1; i >= 0; i--) {
            lowerBound = i * chunkSizePerThread;
            if (i == taskCount - 1) {
                upperBound = fileSize;
            } else {
                upperBound = lowerBound + chunkSizePerThread - 1;
            }
            DownloadTask dt = new DownloadTask(lowerBound, upperBound, requestURL, storage, taskCanceled);
            dispatchWork(dt, i);
        }
        //定时报告下载进度
        reportProgress(reportInterval);
        //清理程序占用的资源
        doCleanup();
    }

    private void reportProgress(long reportInterval) throws InterruptedException {
        int completion = 0;
        float lastCompletion;
        while (!taskCanceled.get()){
            lastCompletion = completion;
            completion = (int)(storage.getTotalWrites()*100/fileSize);
            if(completion==100){
                break;
            }else if(completion-lastCompletion>=1){
                Debug.info("Completion:%s%%", completion);
                if(completion>=90){
                    reportInterval=1000;
                }
            }
            Thread.sleep(reportInterval);
        }
        Debug.info("Completion:%s%%", completion);
    }

    protected void dispatchWork(final DownloadTask dt, int workerIndex) {
        //创建下载线程
        Thread workerThread = new Thread(() -> {
            try {
                dt.run();
            } catch (Exception e) {
                e.printStackTrace();
                //取消整个文件的下载
                cancelDownload();
            }
        });
        workerThread.setName("downloader-" + workerIndex);
        workerThread.start();
    }

    protected void doCleanup() {
        Tools.silentClose(storage);
    }

    protected void cancelDownload() {
        if (taskCanceled.compareAndSet(false, true)) {
            doCleanup();
        }
    }

    // 根据指定的URL获取相应文件的大小
    private static long retieveFileSize(URL requestURL) throws Exception {
        long size = -1;
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) requestURL.openConnection();

            conn.setRequestMethod("HEAD");
            conn.setRequestProperty("Connection", "Keep-alive");
            conn.connect();
            int statusCode = conn.getResponseCode();
            if (HttpURLConnection.HTTP_OK != statusCode) {
                throw new Exception("Server exception,status code:" + statusCode);
            }

            String cl = conn.getHeaderField("Content-Length");
            size = Long.valueOf(cl);
        } finally {
            if (null != conn) {
                conn.disconnect();
            }
        }
        return size;
    }

}
