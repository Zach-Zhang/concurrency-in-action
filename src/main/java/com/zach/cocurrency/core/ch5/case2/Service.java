package com.zach.cocurrency.core.ch5.case2;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/10/22 8:27
 * Version :1.0
 */
public interface Service {
    void start();
    void stop();
    boolean isStarted();
}
