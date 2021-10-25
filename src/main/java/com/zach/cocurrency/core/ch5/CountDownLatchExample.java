package com.zach.cocurrency.core.ch5;

import com.zach.cocurrency.utils.Debug;
import com.zach.cocurrency.utils.Tools;

import java.util.concurrent.CountDownLatch;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/10/22 8:15
 * Version :1.0
 */
public class CountDownLatchExample {
    private static CountDownLatch latch = new CountDownLatch(4);
    private static int data;

    public static void main(String[] args) throws InterruptedException {
        Thread workerThread = new Thread(()->{
            for (int i = 1; i < 10; i++) {
                data=i;
                System.out.println("执行了第"+i+"次");
                latch.countDown();
                Tools.randomPause(1000);
            }
        });
        workerThread.start();
        latch.await();
        Debug.info("It's done. data=%d", data);
    }
}
