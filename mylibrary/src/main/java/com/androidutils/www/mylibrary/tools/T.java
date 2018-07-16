package com.androidutils.www.mylibrary.tools;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.Toast;

/**
 */
public class T {

    private static boolean access = true;

    public static void s(Context context, String content) {
        S.s(content);
        t(context, content);
    }
    public static void e(Context context, String content) {
        S.e(content);
        t(context, content);
    }

    public static void t(Context context, final Object o) {
        t(context, o, Toast.LENGTH_SHORT);
    }

    @SuppressLint("ShowToast")
    public static void t(Context context, Object o, int duration) {
        if (!access) {
            return;
        }
        if (context == null) {
            return;
        }
        final Context context_tmp = context;
        final Object o_tmp = o;
        final int duration_tmp = duration;
        if (context_tmp instanceof Activity) {
            ((Activity) context_tmp).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    s_p(context_tmp, o_tmp, duration_tmp);
                }
            });
        } else {
            s_p(context_tmp, o_tmp, duration_tmp);
        }
    }

    private static void s_p(final Context context, final Object o, final int duration) {
        String content;
        if (o != null) {
            content = o.toString();
        } else {
            content = "null";
        }
        Toast toast = new Toast(context);
        View view = Toast.makeText(context, "", Toast.LENGTH_SHORT).getView();
        toast.setView(view);
        toast.setText(content);
        toast.setDuration(duration);
        toast.show();
    }

    public static void t(Context context, int content) {
        t(context, content + "");
    }

    public static void t(Context context, short content) {
        t(context, content + "");
    }

    public static void t(Context context, long content) {
        t(context, content + "");
    }

    public static void t(Context context, float content) {
        t(context, content + "");
    }

    public static void t(Context context, byte content) {
        t(context, content + "");
    }

    public static void t(Context context, boolean content) {
        t(context, content + "");
    }

}
