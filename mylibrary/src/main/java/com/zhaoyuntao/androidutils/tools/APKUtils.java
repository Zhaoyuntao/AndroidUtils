package com.zhaoyuntao.androidutils.tools;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by zhaoyuntao on 2018/7/25.
 */

public class APKUtils {
    public static int getVersionCode(Context mContext) {
        int versionCode = 0;

        try {
            versionCode = mContext.getPackageManager().
                    getPackageInfo(mContext.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    public static String getVerName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().
                    getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }
}
