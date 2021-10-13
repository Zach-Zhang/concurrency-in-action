package com.zach.cocurrency.core.ch4;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/10/13 8:02
 * Version :1.0
 */
public class Storage implements  Closeable {
    private final RandomAccessFile storeFile;
    private final FileChannel storeChannel;
    private final AtomicLong totalWrites = new AtomicLong(0);

    public Storage(long fileSize, String fileShortName) throws FileNotFoundException {
        String fullFileName = System.getProperty("java.io.tmpdir") + "/"
                + fileShortName;
        String localFileName=createFile(fileSize,fullFileName);
        storeFile=new RandomAccessFile(localFileName,"rw");
        storeChannel = storeFile.getChannel();
    }

    private String createFile(long fileSize, String fullFileName) {
        return null;

    }

    @Override
    public void close() throws IOException {

    }
}
