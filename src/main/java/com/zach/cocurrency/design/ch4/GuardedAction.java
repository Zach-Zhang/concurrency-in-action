package com.zach.cocurrency.design.ch4;

import java.util.concurrent.Callable;

/**
 * @author Zhangshengzhi
 * @version 1.0.0
 * @Description
 * @createTime 2021年12月08日 12:59:00
 */
public abstract class GuardedAction<V> implements Callable<V> {
    protected final Predicate guard;

    public GuardedAction(Predicate guard) {
        this.guard = guard;
    }
}
