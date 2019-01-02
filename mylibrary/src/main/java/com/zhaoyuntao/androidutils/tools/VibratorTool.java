package com.zhaoyuntao.androidutils.tools;

import android.app.Activity;
import android.app.Service;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import static android.content.Context.VIBRATOR_SERVICE;

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
        Vibrator vib = (Vibrator) activity.getSystemService(VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 26) {
            vib.vibrate(VibrationEffect.createOneShot(milliseconds,10));
        } else {
            vib.vibrate(milliseconds);
        }
    }
}
