package com.zach.cocurrency.core.ch5;

import com.zach.cocurrency.utils.Debug;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/10/24 18:02
 * Version :1.0
 */
public class TerminateTaskRunner implements TaskRunnerSpec {
    protected final BlockingQueue<Runnable> channel;

    //线程停止的标记
    protected volatile boolean inUse = true;

    //待处理任务计数器
    public final AtomicInteger reservations = new AtomicInteger(0);

    private volatile Thread workerThread;

    public TerminateTaskRunner(BlockingQueue<Runnable> channel) {
        this.channel = channel;
        this.workerThread = new WorkerThread();
    }

    @Override
    public void init() {
        final Thread t = workerThread;
        if (null != t) {
            t.start();
        }
    }

    public void shutdown() {
        Debug.info("Shutting down service...");
        inUse = false;
        final Thread t = workerThread;
        if (null != t) {
            t.interrupt();
        }
    }

    public void cancelTask() {
        Debug.info("Canceling in progress task...");
        workerThread.interrupt();
    }

    @Override
    public void submit(Runnable task) throws InterruptedException {
        channel.put(task);
        reservations.incrementAndGet();
    }

    private class WorkerThread extends Thread {

        @Override
        public void run() {
            try {
                Runnable task ;
                for(;;){
                    if(!inUse && reservations.get()<=0){
                        break;
                    }
                    task=channel.take();
                    try {
                        task.run();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    reservations.decrementAndGet();
                }
            } catch (InterruptedException e) {
               workerThread=null;
            }
            Debug.info("worker thread terminated.");
        }
    }
}
