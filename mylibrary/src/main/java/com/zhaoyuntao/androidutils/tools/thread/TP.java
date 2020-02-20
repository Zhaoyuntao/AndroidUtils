package com.zhaoyuntao.androidutils.tools.thread;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.zhaoyuntao.androidutils.BuildConfig;
import com.zhaoyuntao.androidutils.tools.S;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * created by zhaoyuntao
 * on 2020-01-14
 * description:
 */
public class TP {
    private static Handler sUiHandler;

    public static boolean isUiThread() {
        final Looper myLooper = Looper.myLooper();
        final Looper mainLooper = Looper.getMainLooper();
        return myLooper != null && myLooper == mainLooper;
    }

    public static void init() {
        if (sUiHandler == null) {
            sUiHandler = new Handler(Looper.getMainLooper());
        }
    }

    public static void runOnUiSafely(ZRunnable r) {
        init();
        sUiHandler.post(r);
    }

    public static void runOnUiDelayedSafely(ZRunnable r, long delay) {
        init();
        sUiHandler.postDelayed(r, delay);
    }

    public static void removeFromUi(Runnable r) {
        init();
        sUiHandler.removeCallbacks(r);
    }

    public static void runOnUiWithPriority(Runnable r) {
        init();
        sUiHandler.postAtFrontOfQueue(r);
    }

    //--------------------------------------------------------------------------------------------------
    public static void runOnPool(Runnable runnable) {
        if (S.DEBUG)
            ThreadManager.getIO().execute(new ShowExceptionRunnable(runnable));
        else
            ThreadManager.getIO().execute(runnable);
    }

    public static void runOnPoolDelayed(Runnable runnable, final int delay) {
        if (S.DEBUG)
            ((ScheduledThreadPoolExecutor) ThreadManager.getScheduleIo().getExecutor()).schedule(new ShowExceptionRunnable(runnable), delay, TimeUnit.MILLISECONDS);
        else
            ((ScheduledThreadPoolExecutor) ThreadManager.getScheduleIo().getExecutor()).schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public static void removeFromPool(Runnable runnable) {
        ((ThreadPoolExecutor) ThreadManager.getIO().getExecutor()).remove(runnable);
        ((ScheduledThreadPoolExecutor) ThreadManager.getScheduleIo().getExecutor()).remove(runnable);
    }

    public static void runThread(Runnable runnable) {
        runThread(runnable, "");
    }

    public static void runThread(Runnable runnable, String threadName) {

        if (TextUtils.isEmpty(threadName)) {
            threadName = "tN" + System.currentTimeMillis();
        }
        if (BuildConfig.DEBUG) {
            try {
                int stackTraceCount = 3;
                StackTraceElement[] elements = new Throwable().getStackTrace();
                String callerClassName = elements.length > stackTraceCount ? elements[stackTraceCount].getClassName() : "NA";
                String callerLineNumber = elements.length > stackTraceCount ? String.valueOf(elements[stackTraceCount].getLineNumber()) : "NA";

                int pos = callerClassName.lastIndexOf('.');
                if (pos >= 0) {
                    callerClassName = callerClassName.substring(pos + 1);
                }

                threadName += callerClassName + "_" + callerLineNumber;
            } catch (Exception e) {
                //do nothing
            }
        }

        new SThread(runnable, threadName).start();
    }
}
