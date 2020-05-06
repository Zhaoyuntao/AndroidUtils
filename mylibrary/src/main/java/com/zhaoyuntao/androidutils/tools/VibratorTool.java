package com.zhaoyuntao.androidutils.tools;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Created by zhaoyuntao on 2017/2/23.
 * 震动单元
 */

public class VibratorTool {

    public static void vibrate(Context context) {
        vibrate(context,-1);
    }
    public static void vibrate(Context context,long milliseconds) {
        if (context == null) {
            return;
        }
        if(milliseconds<=0){
            milliseconds = 30;
        }
        Vibrator vib = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
        if (vib != null) {
            if (Build.VERSION.SDK_INT >= 26) {
                vib.vibrate(VibrationEffect.createOneShot(milliseconds, 5));
            } else {
                vib.vibrate(milliseconds);
            }
        }
    }
}
