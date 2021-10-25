package com.zach.cocurrency.core.ch5.case2;

import com.zach.cocurrency.utils.Debug;

import java.util.concurrent.CountDownLatch;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/10/22 8:27
 * Version :1.0
 */
public abstract class AbstractService implements Service {
    protected boolean started = false;
    protected final CountDownLatch latch;

    public AbstractService(CountDownLatch latch) {
        this.latch = latch;
    }

    protected abstract void doStart() throws Exception;

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isStarted() {
        return started;
    }

    class ServiceStarter extends Thread {
        @Override
        public void run() {
            final String serviceName = AbstractService.this.getClass().getSimpleName();
            Debug.info("Starting %s", serviceName);

            try {
                doStart();
                started = true;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                //计数器递减的方法要放在finally块中执行,防止由于程序报错计数器无法递减到0
                //而导致等待线程一直处于WAITING状态
                latch.countDown();
                Debug.info("Done Starting %s", serviceName);
            }
        }
    }
}
