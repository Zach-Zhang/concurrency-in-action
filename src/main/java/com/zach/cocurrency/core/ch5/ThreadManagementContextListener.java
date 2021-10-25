package com.zach.cocurrency.core.ch5;

import com.zach.cocurrency.utils.Debug;
import com.zach.cocurrency.utils.Tools;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/10/24 21:44
 * Version :1.0
 */
@WebListener
public class ThreadManagementContextListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent ctxEvent) {
        //停止所有登记的线程
        ThreadTerminationRegistry.INSTANCE.clearThreads();
    }

    @Override
    public void contextInitialized(ServletContextEvent ctxEvt) {
        //创建并启动一个数据库监控线程
        AbstractMonitorThread databaseMonitorThread = new AbstractMonitorThread(2000) {
            @Override
            protected void doMonitor() {
                Debug.info("Monitoring database...");
                // 模拟实际的时间消耗
                Tools.randomPause(100);
            }
        };
        databaseMonitorThread.start();
    }

    /**
     * 监控线程
     */
    static abstract class AbstractMonitorThread extends Thread {
        //监控周期
        private final long interval;
        //线程暂停标记
        final AtomicBoolean terminationToken = new AtomicBoolean(false);

        public AbstractMonitorThread(long interval) {
            this.interval = interval;
            //设置为守护线程!
            setDaemon(true);
            ThreadTerminationRegistry.Handler handler = () -> {
                terminationToken.set(true);
                AbstractMonitorThread.this.interrupt();
            };
            ThreadTerminationRegistry.INSTANCE.register(handler);
        }

        @Override
        public void run() {
            try {
                while (!terminationToken.get()) {
                    doMonitor();
                    Thread.sleep(interval);
                }
            } catch (InterruptedException e) {

            }
            Debug.info("terminated:%s", Thread.currentThread());
        }

        // 子类覆盖该方法来实现监控逻辑
        protected abstract void doMonitor();
    }
}
