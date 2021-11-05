package com.zach.cocurrency.core.ch8;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/10/30 19:47
 * Version :1.0
 */
@WebListener
public class AppListener implements ServletContextListener {
    final static Logger LOGGER = Logger.getAnonymousLogger();

    @Override
    public void contextInitialized(ServletContextEvent contextEvent) {
        Thread.UncaughtExceptionHandler ueh = new LoggingUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(ueh);
        //启动若干工作者线程
        startServices();
    }

    static class LoggingUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
            String threadInfo = "Thread[" + t.getName() + "," + t.getId() + ","
                    + t.getThreadGroup().getName() + ",@" + t.hashCode() + "]";
            //将线程异常终止的相关信息记录到日志中
            LOGGER.log(Level.SEVERE, threadInfo + "terminated: ", e);
        }
    }

    protected void startServices() {
        // 省略其他代码
    }

    protected void stopServices() {
        // 省略其他代码
    }

    @Override
    public void contextDestroyed(ServletContextEvent contextEvent) {
        Thread.setDefaultUncaughtExceptionHandler(null);
        stopServices();
    }
}
