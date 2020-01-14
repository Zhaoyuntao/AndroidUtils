package com.zhaoyuntao.androidutils.tools.progress;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;

import com.zhaoyuntao.androidutils.tools.HashUtils;
import com.zhaoyuntao.androidutils.tools.S;

import java.io.IOException;
import java.util.List;

public class ProcessUtils {
    static String sMyProcessName = null;
    static String sMyProcessTag = null;

//    public static void ensureMainProcess(Context context) {
//        if (!getProcessName(context).equals(Utilities.sMainProcessName)) {
//            throw new IllegalStateException("ensureMainProcess: process environment check failed");
//        }
//    }

    public static String getProcessName(Context context) {
        if (sMyProcessName == null) {
            int pid = android.os.Process.myPid();
            ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningAppProcessInfo> runningAppProcesses = mActivityManager.getRunningAppProcesses();
            if (runningAppProcesses == null) {
                return "unknown";
            }
            for (ActivityManager.RunningAppProcessInfo appProcess : runningAppProcesses) {
                if (appProcess.pid == pid) {
                    sMyProcessName = appProcess.processName;
                    return sMyProcessName;
                }
            }
        }

        return sMyProcessName == null ? "unknown" : sMyProcessName;
    }

    public static String getProcessTag(Context context) {
        if (sMyProcessTag == null) {
            sMyProcessTag = HashUtils.getStringMD5(getProcessName(context)).substring(0, 4);
        }
        return sMyProcessTag;
    }

    public static String getSafeProcessName(Context context) {
        String proc = getProcessName(context);
        return proc.replace(':', '-');
    }

    public static void killSelf() {
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    /**
     * 清除应用缓存的用户数据，同时停止所有服务和Alarm定时task
     * Runtime.getRuntime().exec(cmd)
     * @param packageName
     * @return
     */
    public static Process clearAppUserData(String packageName) {
        Process p = execRuntimeProcess("pm clear " + packageName);
        if (p == null) {
            S.ed("Clear app data packageName:" + packageName + ", FAILED !");
        } else {
            S.sd("Clear app data packageName:" + packageName + ", SUCCESS !");
        }
        return p;
    }

    public static Process execRuntimeProcess(String command) {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(command);
        } catch (IOException e) {
           S.ed("exec Runtime command:" + command + ", IOException" + e);
            e.printStackTrace();
        }
        S.sd("exec Runtime command:" + command + ", Process:" + p);
        return p;
    }

}
