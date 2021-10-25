package com.zach.cocurrency.core.ch5;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/10/24 10:34
 * Version :1.0
 */
public class TaskRunner {
    protected final BlockingQueue<Runnable> channel;
    protected volatile Thread workerThread;

    public TaskRunner(BlockingQueue<Runnable> channel) {
        this.channel = channel;
        this.workerThread = new WorkerThread();
    }

    public TaskRunner() {
        this(new LinkedBlockingDeque<>());
    }

    public void init() {
        final Thread t = workerThread;
        if (null != t) {
            t.start();
        }
    }

    public void submit(Runnable task) throws InterruptedException {
        channel.put(task);
    }

    class WorkerThread extends Thread {
        @Override
        public void run() {

            Runnable task = null;
            try {

                for (; ; ) {
                    System.out.println("A:"+Thread.currentThread().getName() + ":" + Thread.currentThread().isInterrupted());
                    task = channel.take();
                    task.run();
                }
            } catch (InterruptedException e) {
                // e.printStackTrace();
                System.out.println("D:"+Thread.currentThread().getName() + ":" + Thread.currentThread().isInterrupted());
            }
        }

    }
}
