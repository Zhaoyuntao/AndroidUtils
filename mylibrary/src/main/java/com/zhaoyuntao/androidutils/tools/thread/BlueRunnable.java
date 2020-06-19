package com.zhaoyuntao.androidutils.tools.thread;

import com.zhaoyuntao.androidutils.tools.S;

/**
 * created by zhaoyuntao
 * on 2020/6/19
 * description:
 */
public class BlueRunnable implements Runnable {
    private String name;
    private long timeStart;
    private long timeEnd;
    private Runnable runnable;

    public BlueRunnable(Runnable runnable, String name) {
        if (runnable == null) {
            S.e("BlueRunnable ERROR:runnable is null!");
            throw new RuntimeException();
        }
        this.runnable = runnable;
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    @Override
    public void run() {
        timeStart = System.currentTimeMillis();
        runnable.run();
        timeEnd = System.currentTimeMillis();
        long spendTime = timeEnd - timeStart;
        S.s("BlueRunnable[" + name + "] spend time:" + spendTime + "ms");
    }

    public long getTimeStart() {
        return timeStart;
    }

    public long getTimeEnd() {
        return timeEnd;
    }
}
