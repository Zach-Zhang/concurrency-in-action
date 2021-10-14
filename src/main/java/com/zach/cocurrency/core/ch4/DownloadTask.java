package com.zach.cocurrency.core.ch4;

import com.zach.cocurrency.utils.Debug;
import com.zach.cocurrency.utils.Tools;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description: 下载子任务
 * @Author Zach
 * @Date 2021/10/13 22:04
 * Version :1.0
 */
public class DownloadTask implements Runnable {
    private final long lowerBound;
    private final long upperBound;
    private final DownLoadBuffer downLoadBuffer;
    private final URL requestUrl;
    private final AtomicBoolean cancelFlag;

    public DownloadTask(long lowerBound, long upperBound, URL requestUrl,
                        Storage storage, AtomicBoolean cancelFlag) {
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.requestUrl = requestUrl;
        this.downLoadBuffer = new DownLoadBuffer(lowerBound, upperBound, storage);
        this.cancelFlag = cancelFlag;
    }
    //对HTTP发起分段请求
    private static InputStream issueRequest(URL requestURL, long lowerBound, long upperBound) throws IOException {
        Thread me = Thread.currentThread();
        Debug.info(me + "->[" + lowerBound + "," + upperBound + "]");
        final HttpURLConnection conn;
        InputStream in = null;
        conn = (HttpURLConnection) requestURL.openConnection();
        String strConnTimeout = System.getProperty("x.dt.conn.timeout");
        int connTimeout = null == strConnTimeout ? 60000 : Integer.parseInt(strConnTimeout);
        conn.setConnectTimeout(connTimeout);

        String strReadTimeout = System.getProperty("x.dt.read.timeout");
        int readTimeout = null == strReadTimeout ? 60000 : Integer.parseInt(strReadTimeout);
        conn.setReadTimeout(readTimeout);

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Connection", "Keep-alive");
        // Range: bytes=0-1024
        conn.setRequestProperty("Range", "bytes=" + lowerBound + "-" + upperBound);
        conn.setDoInput(true);
        conn.connect();

        int statusCode = conn.getResponseCode();
        if (HttpURLConnection.HTTP_PARTIAL != statusCode) {
            conn.disconnect();
            throw new IOException("Server exception,status code:" + statusCode);
        }

        Debug.info(me + "-Content-Range:" + conn.getHeaderField("Content-Range")
                + ",connection:" + conn.getHeaderField("connection"));

        in = new BufferedInputStream(conn.getInputStream()) {
            @Override
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    conn.disconnect();
                }
            }
        };

        return in;
    }

    @Override
    public void run() {
        if (cancelFlag.get()) {
            return;
        }
        ReadableByteChannel channel = null;
        try {
            channel = Channels.newChannel(issueRequest(requestUrl, lowerBound, upperBound));
            ByteBuffer buf = ByteBuffer.allocate(1024);
            while (!cancelFlag.get() && channel.read(buf) > 0) {
                //从网络读取的数据写入缓冲区
                downLoadBuffer.write(buf);
                buf.clear();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (channel != null) {
                Tools.silentClose(channel, downLoadBuffer);
            }

        }
    }
}
