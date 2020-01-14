package com.zhaoyuntao.androidutils.tools.thread;

/**
 * created by zhaoyuntao
 * on 2019-12-13
 * description:
 */
public class SThread extends Thread {

    public SThread() {
    }

    public SThread(Runnable target) {
        super(target);
    }

    public SThread(ThreadGroup group, Runnable target) {
        super(group, target);
    }

    public SThread(String name) {
        super(name);
    }

    public SThread(ThreadGroup group, String name) {
        super(group, name);
    }

    public SThread(Runnable target, String name) {
        super(target, name);
    }

    public SThread(ThreadGroup group, Runnable target, String name) {
        super(group, target, name);
    }

    public SThread(ThreadGroup group, Runnable target, String name, long stackSize) {
        super(group, target, name, stackSize);
    }
}
