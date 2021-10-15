package com.zach.cocurrency.core.ch4.case02;

/**
 * @Description:日志记录集。 包含若干条日志记录。
 * @Author Zach
 * @Date 2021/10/15 8:25
 * Version :1.0
 */
public class RecordSet {
    public final int capacity;
    final String[] records;
    int readIndex = 0;
    int writeIndex = 0;

    public RecordSet(int capacity) {
        this.capacity = capacity;
        records = new String[capacity];
    }

    public String nextRecord() {
        return (readIndex < writeIndex) ? (records[readIndex++]) : "";
    }

    public boolean putRecord(String line) {
        if (writeIndex == capacity) {
            return true;
        }
        records[writeIndex++] = line;
        return false;
    }

    public void reset() {
        readIndex = 0;
        writeIndex = 0;
        for (int i = 0, len = records.length; i < len; i++) {
            records[i] = null;
        }
    }

    public boolean isEmpty() {
        return 0 == writeIndex;
    }
}
