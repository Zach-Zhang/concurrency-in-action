package com.zach.cocurrency.core.ch2;

import com.zach.cocurrency.utils.Tools;

/**
 * @Description:
 * 一个线程终止后该线程对共享变量的更新对于调用该线程的join方法的线程而言是可见的
 * @Author Zach
 * @Date 2021/9/29 7:27
 * Version :1.0
 */
public class ThreadJoinVisibility {
    //线程间的共享变量
    static int data = 0;

    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            //当前线程休眠R秒
            Tools.randomPause(50);
            data = 1;
        });
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(data);
    }
}
