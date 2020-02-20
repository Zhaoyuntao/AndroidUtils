package com.zhaoyuntao.androidutils.tools.thread;

import android.util.Log;

import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.device.DevicesUtils;

import java.util.concurrent.ThreadPoolExecutor;

public class ThreadManager {
    private static final String TAG = "ThreadManager";
    private final static EasyThread io;
    private final static EasyThread scheduleIo;
    private final static EasyThread cache;
    private final static EasyThread calculator;
    private final static EasyThread file;
    private final static DefaultCallback callback = S.DEBUG ? new DefaultCallback() : null;

    public static EasyThread getIO() {
        return io;
    }
    public static EasyThread getScheduleIo() {
        return scheduleIo;
    }

    public static EasyThread getCache() {
        return cache;
    }

    public static EasyThread getCalculator() {
        return calculator;
    }

    public static EasyThread getFile() {
        return file;
    }

    public static EasyThread newSingle(String name) {
        return EasyThread.Builder.createSingle().setName(name).setCallback(callback).build();
    }

    static {
        DevicesUtils.getNumCores();
        int coreSize =Runtime.getRuntime().availableProcessors();
        io = EasyThread.Builder.createFixed(coreSize * 3).setName("IO").setPriority(7).setCallback(callback).build();
        cache = EasyThread.Builder.createCacheable().setName("cache").setCallback(callback).build();
        calculator = EasyThread.Builder.createFixed(coreSize + 1).setName("calculator").setPriority(Thread.MAX_PRIORITY).setCallback(callback).build();
        file = EasyThread.Builder.createFixed(4).setName("file").setPriority(3).setCallback(callback).build();
        scheduleIo = EasyThread.Builder.createScheduled(coreSize * 3).setName("scheduleIo").setPriority(7).setCallback(callback).build();
    }

    private static class DefaultCallback implements Callback {

        @Override
        public void onError(String threadName, Throwable t) {
        }

        @Override
        public void onCompleted(String threadName) {
            showActiveCount("onCompleted");
        }

        @Override
        public void onStart(String threadName) {

        }
    }

    private static void showActiveCount(String tag) {
        if (S.DEBUG) {
            String tmp = TAG + " " + tag;
            Log.i(tmp, "io thread active count:" + getActiveCount(io) + "  " + getPoolSize(io) + " ");
            Log.i(tmp, "scheduleIo thread active count:" + getActiveCount(scheduleIo) + "  " + getPoolSize(scheduleIo) + " ");
            Log.i(tmp, "cache thread active count:" + getActiveCount(cache) + "  " + getPoolSize(cache) + " ");
            Log.i(tmp, "calculator thread active count:" + getActiveCount(calculator) + "  " + getPoolSize(calculator) + " ");
            Log.i(tmp, "file thread active count:" + getActiveCount(file) + "  " + getPoolSize(file) + " ");
        }
    }

    private static int getActiveCount(EasyThread easyThread) {
        return ((ThreadPoolExecutor) easyThread.getExecutor()).getActiveCount();
    }

    private static int getPoolSize(EasyThread easyThread) {
        return ((ThreadPoolExecutor) easyThread.getExecutor()).getPoolSize();
    }
}
