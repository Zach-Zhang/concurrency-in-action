package com.zach.cocurrency.core.ch2;

import com.zach.cocurrency.utils.Tools;

/**
 * @Description:
 * 父线程在启动子线程之前对共享变量的更新对于子线程来说是可见的
 * @Author Zach
 * @Date 2021/9/29 7:17
 * Version :1.0
 */
public class ThreadStartVisibility {
    //线程间的共享变量
    static int data = 0;

    public static void main(String[] args) {
        Thread thread = new Thread(() -> {
            //当前线程休眠R秒
            Tools.randomPause(50);
            System.out.println(data);
        });
        // 在子线程thread启动前更新变量data的值
        data = 1;
        thread.start();

        //当前线程休眠R秒
        Tools.randomPause(50);
// 在子线程thread启动后更新变量data的值
        data = 2;
    }
}
