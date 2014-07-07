package com.rox.app.sync.core;

/**
 * Created by Rox on 2014/7/2.
 * 乐观锁，记录修改前的锁版本和准备提交的锁版本
 */
public class OptimisticLock {
    private long old;
    private long now;

    public OptimisticLock() {
    }

    public long getOld() {
        return old;
    }

    public void setOld(long old) {
        this.old = old;
        this.now = old;
    }

    public long getNow() {
        return now;
    }

    public void increase() {
        now = old + 1;
    }

    public boolean isChanged() {
        return old != now;
    }

    public void reset() {
        now = old;
    }

    public String toString() {
        return String.valueOf(now);
    }
}
