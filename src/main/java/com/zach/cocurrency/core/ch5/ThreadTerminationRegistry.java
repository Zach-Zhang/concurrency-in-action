package com.zach.cocurrency.core.ch5;

import java.util.HashSet;
import java.util.Set;

/**
 * @Description: 线程终止登记注册表
 * @Author Zach
 * @Date 2021/10/24 21:47
 * Version :1.0
 */
public enum ThreadTerminationRegistry {
    INSTANCE;
    private final Set<Handler> handlers = new HashSet<Handler>();

    public synchronized void register(Handler handler) {
        handlers.add(handler);
    }

    public void clearThreads() {
        final Set<Handler> handlersSnapshot;
        synchronized (this) {
            handlersSnapshot = new HashSet<>(handlers);
        }
        for (Handler handler : handlersSnapshot) {
            try {
                handler.terminate();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 线程终止处理器
     * <p>
     * 封装了有关线程停止的知识
     *
     * @author Viscent Huang
     */
    public static interface Handler {
        void terminate();
    }
}
