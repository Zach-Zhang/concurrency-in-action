package com.zach.cocurrency.design.ch4;


import com.zach.cocurrency.utils.Debug;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Zhangshengzhi
 * @version 1.0.0
 * @Description
 * @createTime 2021年12月08日 13:06:00
 */
public class ConditionVarBlocker implements Blocker {
    private final Lock lock;
    private final Condition condition;
    private final boolean allowAccess2Lock;

    public ConditionVarBlocker(Lock lock, boolean allowAccess2Lock) {
        this.lock = lock;
        this.condition = lock.newCondition();
        this.allowAccess2Lock = allowAccess2Lock;
    }

    public ConditionVarBlocker(boolean allowAccess2Lock) {
        this(new ReentrantLock(), allowAccess2Lock);
    }

    public ConditionVarBlocker(Lock lock) {
        this(lock, true);
    }

    public ConditionVarBlocker() {
        this(false);
    }

    public Lock getLock() {
        if (allowAccess2Lock) {
            return this.lock;
        }
        throw new IllegalStateException("Access to the lock disallowed.");
    }

    @Override
    public <V> V callWithGuard(GuardedAction<V> guardedAction) throws Exception {
        V result;
        try {
            lock.lockInterruptibly();
            final Predicate guard = guardedAction.guard;
            while (!guard.evaluate()) {
                Debug.info("waiting...");
                condition.await();
            }
            result = guardedAction.call();
            return result;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void signalAfter(Callable<Boolean> stateOperation) throws Exception {
        try {
            lock.lockInterruptibly();
            if(stateOperation.call()){
                condition.signal();
            }
        }finally {
            lock.unlock();
        }
    }

    @Override
    public void signal() throws InterruptedException {
        lock.lockInterruptibly();
        try {
            condition.signal();

        } finally {
            lock.unlock();
        }
    }

    @Override
    public void broadcastAfter(Callable<Boolean> stateOperation) throws Exception {
        lock.lockInterruptibly();
        try {
            if (stateOperation.call()) {
                condition.signalAll();
            }
        } finally {
            lock.unlock();
        }

    }
}
