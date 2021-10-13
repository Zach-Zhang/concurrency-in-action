package com.zach.cocurrency.core.ch4;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * @author Zach Zhang
 * @version 1.0.0
 * @ClassName DownLoadBuffer.java
 * @Description
 * @createTime 2021年10月13日 13:37:00
 */
public class DownLoadBuffer implements Closeable {
    /**
     * 当前Buffer中缓冲的数据相对于整个存储文件的位置偏移
     */
    private long globalOffset;
    private long upperBound;
    private int offset = 0;
    private final ByteBuffer byteBuffer;
    private final Storage storage;

    public DownLoadBuffer(long globalOffset, long upperBound, final Storage storage) {
        this.globalOffset = globalOffset;
        this.upperBound = upperBound;
        this.byteBuffer = ByteBuffer.allocate(1024 * 1024);
        this.storage = storage;
    }

    public void write(ByteBuffer buf) throws IOException {
        int length = byteBuffer.position();
        final int capacity = byteBuffer.capacity();
        //当缓存区已满,或者剩余容量不够容纳新数据
        if (offset + length > capacity || length == capacity) {
            flush();
        }
        byteBuffer.position(offset);
        buf.flip();
        byteBuffer.put(buf);
        offset += length;
    }

    public void flush() throws IOException {
        byteBuffer.flip();
        int length = storage.store(globalOffset, byteBuffer);
        byteBuffer.clear();
        globalOffset += length;
        offset = 0;
    }

    @Override
    public void close() throws IOException {
        if (globalOffset < upperBound) {
            flush();
        }
    }
}
