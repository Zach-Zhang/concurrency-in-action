package com.zach.cocurrency.core.ch5;

/**
 * @Description:
 * @Author Zach
 * @Date 2021/10/24 18:03
 * Version :1.0
 */
public interface TaskRunnerSpec {
    public void init();

    public void submit(Runnable task) throws InterruptedException;
}
