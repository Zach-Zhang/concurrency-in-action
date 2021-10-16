package com.zach.cocurrency.core.ch4.case02;

import com.zach.cocurrency.utils.Tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/10/16 15:50
 * Version :1.0
 */
public class SimpleStatTask extends AbstractStatTask {
    private final InputStream in;

    public SimpleStatTask(InputStream in, int sampleInterval, int traceIdDiff,
                          String expectedOperationName, String expectedExternalDeviceList) {
        super(sampleInterval, traceIdDiff, expectedOperationName,
                expectedExternalDeviceList);
        this.in = in;
    }

    @Override
    protected void doCalculate() throws IOException, InterruptedException {
        String strBufferSize = System.getProperty("x.input.buffer");
        int inputBufferSize = strBufferSize != null ? Integer.valueOf(strBufferSize) : 8192 * 4;
        final BufferedReader logFileReader = new BufferedReader(new InputStreamReader(in), inputBufferSize);
        String record;
        try {
            while ((record = logFileReader.readLine()) != null) {
                recordProcessor.process(record);
            }
        } finally {
            Tools.silentClose(logFileReader);
        }
    }
}
