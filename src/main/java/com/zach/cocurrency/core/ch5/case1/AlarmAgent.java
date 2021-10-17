package com.zach.cocurrency.core.ch5.case1;

import com.zach.cocurrency.utils.Debug;
import com.zach.cocurrency.utils.Tools;

import java.util.Random;

/**
 * @Description: 告警代理
 * @Author Zach
 * @Date 2021/10/17 8:34
 * Version :1.0
 */
public class AlarmAgent {
    //保存该类的唯一实例
    private final static AlarmAgent INSTANCE = new AlarmAgent();

    //是否连接上告警服务器
    private boolean connectedToServer = false;

    //心跳线程,用于检测告警代理与告警服务器的网络连接是否正常
    private final HeartbeatThread heartbeatThread = new HeartbeatThread();

    private AlarmAgent() {
        // 什么也不做
    }

    public static AlarmAgent getInstance() {
        return INSTANCE;
    }

    public void init() {
        connectToServer();
        heartbeatThread.setDaemon(true);
        heartbeatThread.start();
    }

    private void connectToServer() {
        //创建并启动网络连接线程,在该线程中与告警服务器建立连接
        new Thread(this::doConnect).start();
    }

    private void doConnect() {
        //模拟实际操作耗时
        Tools.randomPause(100);
        synchronized (this) {
            connectedToServer = true;
            //唤醒告警发送的线程
            notify();
        }
    }

    public void sendAlarm(String message) throws InterruptedException {
        synchronized (this){
            //使当前线程等待直到告警代理与告警服务器的连接建立完毕
            while (!connectedToServer){
                wait();
            }
            doSendAlarm(message);
        }
    }

    private void doSendAlarm(String message) {
        Debug.info("Alarm sent:%s", message);
    }

    class HeartbeatThread extends Thread {
        @Override
        public void run() {
            try {
                //留一定的时间给网络连接线程与告警服务器建立连接
                Thread.sleep(1000);
                while (true) {
                    if (checkConnection()) {
                        connectedToServer = true;
                    } else {
                        connectedToServer = false;
                        Debug.info("Alarm agent was disconnected from server.");

                        //检测到连接中断,重新建立连接
                        connectToServer();
                    }
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // 检测与告警服务器的网络连接情况
        private boolean checkConnection() {
            boolean isConnected = true;
            final Random random = new Random();

            // 模拟随机性的网络断链
            int rand = random.nextInt(1000);
            if (rand <= 500) {
                isConnected = false;
            }
            return isConnected;
        }
    }
}
