package com.zach.cocurrency.core.ch5;

import com.zach.cocurrency.utils.Debug;

import java.util.Random;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/10/17 15:44
 * Version :1.0
 */
public class TimeoutWaitExample {
    private static final Object lock = new Object();
    private static boolean ready = false;
    protected static final Random random = new Random();
    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(() -> {
            for (; ; ) {
                synchronized (lock) {
                    ready = random.nextInt(100) < 5 ? true : false;
                    if (ready) {
                        lock.notify();
                    }
                }
            }
        });
        t.setDaemon(true);
        t.start();
        waiter(1000);
    }

    private static void waiter(final long timeout) throws InterruptedException {
        if (timeout < 0) {
            throw new IllegalArgumentException();
        }

        long start = System.currentTimeMillis();
        synchronized (lock) {
            while (!ready) {
                long now = System.currentTimeMillis();
                long waitTime = timeout - (now - start);
                Debug.info("Remaining time to wait:%sms", waitTime);
                if (waitTime <= 0) {
                    //等待超时退出
                    break;
                }
                lock.wait(waitTime);
            }

            if (ready) {
                //执行目标动作
                guardedAction();
            } else {
                //等待超时,保护条件未成立
                Debug.error("Wait timed out,unable to execution target action!");
            }
        }


    }

    private static void guardedAction() {
        Debug.info("Take some action.");
    }
}
