package com.zhaoyuntao.androidutils.tools.thread;


import android.util.Log;

import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.Sleeper;

import java.util.Timer;

/**
 * Created by zhaoyuntao on 2018/7/4.
 */

public abstract class ZThread extends Thread {
    private volatile float frame;
    private volatile float frame_real;
    private volatile boolean flag = true;
    private volatile boolean isStart;
    private volatile boolean pause;
    private volatile long timeStart;

    private Sleeper sleeper = new Sleeper();

    public ZThread(float frame) {
        this.frame = frame;
        setPriority(10);
    }

    public ZThread() {
        this(1000f);
    }

    @Override
    public synchronized void start() {
        if (isStart) {
            return;
        }
        if (frame <= 0) {
            Log.e("ZThread", "frame must be bigger than 0");
            return;
        }
        isStart = true;
        timeStart = S.currentTimeMillis();
        super.start();
    }

    protected void init() {
    }

    @Override
    public void run() {
        init();
        while (flag) {
            if (pause) {
                synchronized (sleeper) {
                    try {
                        sleeper.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                }
            }
            double interval = (1000d / frame);
            //计算当前开始时间
            long time_start = S.currentTimeMillis();
            //计算本次循环的最快结束时间
            long time_end = (long) (time_start + interval);
            todo();
            long time_now = S.currentTimeMillis();
            long rest = time_end - time_now;
            if (rest > 0) {
                try {
                    Thread.sleep(rest);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
            //计算频率
            long time_end2 = S.currentTimeMillis();
            long during = time_end2 - time_start;
            frame_real = (long) (10000d / during) / 10f;
            Log.i("ZThread", "frame of zthread:" + frame_real);
//            S.s("time_start:" + time_start + "=======time_end:" + time_end + "==============time_end2:" );//+ time_end2 + "==========================during:" + during + "=============rest:" + rest + "============" + frame_real);
        }
    }

    /**
     * frame of real
     *
     * @return
     */
    public float getFrame_real() {
        return frame_real;
    }

    // TODO: 2018/7/4
    protected abstract void todo();

    public void close() {
        Log.i("ZThread", "zthread close");
        flag = false;
        pause = false;
        interrupt();
    }

    public boolean isClose() {
        return !flag || isInterrupted();
    }

    public void pauseThread() {
        pause = true;
    }

    public void resumeThread() {
        pause = false;
        synchronized (sleeper) {
            sleeper.notifyAll();
        }
    }

    public boolean isPause() {
        return pause;
    }

    public void setFrame(float frame) {
        this.frame = frame;
    }

    public long getTimeStart() {
        return timeStart;
    }
}
