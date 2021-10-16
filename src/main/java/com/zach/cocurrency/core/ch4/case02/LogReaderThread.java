package com.zach.cocurrency.core.ch4.case02;

import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * @Description: 日志读取线程实现类
 * @Author Zach
 * @Date 2021/10/16 16:32
 * Version :1.0
 */
public class LogReaderThread extends AbstractLogReader {
    //线程安全的队列
    final BlockingQueue<RecordSet> channel = new ArrayBlockingQueue<>(2);

    public LogReaderThread(InputStream in, int inputBufferSize, int batchSize) {
        super(in, inputBufferSize, batchSize);
    }

    @Override
    protected RecordSet nextBatch() throws InterruptedException {
        return (channel.take().isEmpty()) ? null : channel.take();
    }

    @Override
    protected void publish(RecordSet recordBatch) throws InterruptedException {
        //存入队列
        channel.put(recordBatch);
    }
}
