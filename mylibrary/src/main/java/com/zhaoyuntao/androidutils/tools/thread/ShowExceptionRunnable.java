
package com.zhaoyuntao.androidutils.tools.thread;

import com.zhaoyuntao.androidutils.tools.S;

public class ShowExceptionRunnable implements Runnable {
    private final long MIN_JOB_DURATION_TO_LOG = 5000;

    private boolean mCheckDuration;

    private final Runnable mOrigin;
    private Exception mStackTrace;

    public ShowExceptionRunnable(Runnable ori) {
        if (ori == null) {
            throw new NullPointerException("invalid argument: ori=null");
        }

        mOrigin = ori;
        mCheckDuration = true;
        mStackTrace = new Exception("Stack trace of " + ori);
    }

    public ShowExceptionRunnable(Runnable ori, boolean checkDuration) {
        if (ori == null) {
            throw new NullPointerException("invalid argument: ori=null");
        }

        mOrigin = ori;
        mCheckDuration = checkDuration;
        if (mCheckDuration) {
            mStackTrace = new Exception("Stack trace of " + ori);
        }
    }

    @Override
    public void run() {
        long start = mCheckDuration ? System.currentTimeMillis() : 0;
        try {
            mOrigin.run();
        } catch (final Throwable e) {
            S.e("Throwable catched during execution: " + mOrigin, e);
            if (mCheckDuration) {
                S.e("Job posted in: " + mStackTrace);
            }
            // request processs termination
            throw new RuntimeException(e);
        } finally {
            if (mCheckDuration) {
                long end = System.currentTimeMillis();

                if (end - start > MIN_JOB_DURATION_TO_LOG) {
                    S.s("Job: "
                                    + mOrigin
                                    + " takes too long to complete: "
                                    + (end - start)
                                    + "ms. Long task should in seperate Thread instead of ThreadPool.\nTask originally created at: ",
                            mStackTrace);
                }
            }

            mStackTrace = null;
        }
    }

    @Override
    public String toString() {
        return "ShowExceptionRunnable: {" + mOrigin.toString() + "}";
    }
}
