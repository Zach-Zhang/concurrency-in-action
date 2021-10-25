package com.zach.cocurrency.core.ch5;

import com.zach.cocurrency.utils.Debug;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/10/24 16:55
 * Version :1.0
 */
public class MayNotBeTerminatedDemo {
    public static void main(String[] args) throws InterruptedException {
        TaskRunner tr = new TaskRunner();
        tr.init();

        tr.submit(() -> {
            Debug.info("before doing task");
            try {
                System.out.println("B:"+Thread.currentThread().getName() + ":" + Thread.currentThread().isInterrupted());
                Thread.sleep(1000);

            } catch (InterruptedException e) {
                //这会导致线程中断标记被清除
                System.out.println("E:"+Thread.currentThread().getName() + ":" + Thread.currentThread().isInterrupted());
            }
            Debug.info("after doing task");
        });
        tr.workerThread.interrupt();
        System.out.println("C:"+Thread.currentThread().getName() + ":" + Thread.currentThread().isInterrupted());
        System.out.println(tr.workerThread.getName());
    }
}
