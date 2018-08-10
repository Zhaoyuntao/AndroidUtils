package com.zhaoyuntao.androidutils.tools;


/**
 * Created by zhaoyuntao on 2018/7/4.
 */

public abstract class ZThread extends Thread {
    private float frame = 1;
    private float frame_real;
    private boolean flag = true;

    private boolean pause;

    private Sleeper sleeper = new Sleeper();

    public ZThread(float frame) {
        this.frame = frame;
    }

    @Override
    public void run() {
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
            long time_start = System.currentTimeMillis();
            todo();
            long time_end = System.currentTimeMillis();
            long during = time_end - time_start;
            double during2 = (1000d / frame);
            long rest = (long) (during2 - during);
            if (rest > 0) {
                try {
                    Thread.sleep(rest);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
            time_end = System.currentTimeMillis();
            during = time_end - time_start;
            frame_real = (float) (((double) (int) ((double) 1000 / during * 10)) / 10);
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
        flag = false;
        interrupt();
        pause = false;
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
}
