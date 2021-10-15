package com.zach.cocurrency.core.ch4.case02;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @Description: 日志文件读取线程
 * @Author Zach
 * @Date 2021/10/15 8:18
 * Version :1.0
 */
public abstract class AbstractLogReader extends Thread {
    private final BufferedReader logFileReader;
    //表示日志文件是否读取结束
    protected volatile boolean endFlag = false;
    protected final int batchSize;

    public AbstractLogReader(InputStream in, int inputBufferSize, int batchSize) {
        logFileReader = new BufferedReader(new InputStreamReader(in));
        this.batchSize = batchSize;
    }
}
