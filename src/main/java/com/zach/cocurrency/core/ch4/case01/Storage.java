package com.zach.cocurrency.core.ch4.case01;

import com.zach.cocurrency.utils.Tools;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Description: 文件存储组件
 * @Author Zach
 * @Date 2021/10/13 8:02
 * Version :1.0
 */
public class Storage implements Closeable {
    private final RandomAccessFile storeFile;
    private final FileChannel storeChannel;
    private final AtomicLong totalWrites = new AtomicLong(0);

    public Storage(long fileSize, String fileShortName) throws IOException {
        String fullFileName = System.getProperty("java.io.tmpdir") + "/"
                + fileShortName;
        String localFileName = createFile(fileSize, fullFileName);
        storeFile = new RandomAccessFile(localFileName, "rw");
        storeChannel = storeFile.getChannel();
    }

    public long getTotalWrites() {
        return totalWrites.get();
    }

    /**
     * 将data中指定的数据写入文件中
     *
     * @param offset     写入数据在整个文件中的起始偏移位置
     * @param byteBuffer byteBuf必须在该方法调用前执行byteBuf.flip()
     * @return
     * @throws IOException
     */
    public int store(long offset, ByteBuffer byteBuffer) throws IOException {
        storeChannel.write(byteBuffer, offset);
        int limit = byteBuffer.limit();
        totalWrites.addAndGet(limit);
        return limit;
    }

    private String createFile(long fileSize, String fullFileName) throws IOException {
        File file = new File(fullFileName);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        try {
            raf.setLength(fileSize);
        } finally {
            Tools.silentClose(raf);
        }
        return fullFileName;

    }

    @Override
    public void close() throws IOException {
        if (storeChannel.isOpen()) {
            Tools.silentClose(storeChannel, storeFile);
        }
    }
}
