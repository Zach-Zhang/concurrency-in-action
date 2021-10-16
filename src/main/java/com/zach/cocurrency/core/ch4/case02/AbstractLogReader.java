package com.zach.cocurrency.core.ch4.case02;

import com.zach.cocurrency.utils.Tools;

import java.io.BufferedReader;
import java.io.IOException;
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

    protected RecordSet getNextToFill() {
        return new RecordSet(batchSize);
    }

    //获取下一个记录集
    protected abstract RecordSet nextBatch() throws InterruptedException;
    // 发布指定的记录集
    protected abstract void publish(RecordSet recordBatch)
            throws InterruptedException;
    @Override
    public void run() {

        try {
            while (true){
                RecordSet recordSet = getNextToFill();
                recordSet.reset();
                boolean flag = doFill(recordSet);
                publish(recordSet);
                if(flag){
                    if(!recordSet.isEmpty()){
                        publish(new RecordSet(1));
                    }
                    endFlag=true;
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            Tools.silentClose(logFileReader);
        }
    }
    protected boolean doFill(final RecordSet recordSet) throws IOException {
        final int capacity = recordSet.capacity;
        String record;
        for (int i = 0; i < capacity; i++) {
            record = logFileReader.readLine();
            if (null == record) {
                return true;
            }
            // 将读取到的日志记录存入指定的记录集
            recordSet.putRecord(record);
        }
        return false;
    }

}
