package com.zhaoyuntao.androidutils.tools;

import android.app.Activity;
import android.app.Service;
import android.os.Vibrator;

/**
 * Created by zhaoyuntao on 2017/2/23.
 * 震动单元
 */

public class VibratorTool {

    /**
     * long[] pattern  quiet and vibrat, like this
     *
     * boolean isRepeat
     */

    public static void vibrate(final Activity activity, long milliseconds) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(milliseconds);
    }

    public static void vibrate(final Activity activity, long[] pattern, boolean isRepeat) {
        Vibrator vib = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);
        vib.vibrate(pattern, isRepeat ? 1 : -1);
    }

}
