package com.zhaoyuntao.androidutils.tools;

import android.content.Context;

import com.zhaoyuntao.androidutils.permission.Action;
import com.zhaoyuntao.androidutils.permission.ZPermission;

import java.util.List;

public class ZP {

    public static void p(Context context, String[] permissions, final CallBack callBack) {
        p(context, callBack, permissions);
    }

    public static void p(Context context, final CallBack callBack, String... permissions) {
        if (permissions == null || permissions.length == 0) {
            return;
        }
        ZPermission.with(context).runtime().permission(permissions).onGranted(new Action<List<String>>() {
            @Override
            public void onAction(List<String> data) {
                if (callBack != null) {
                    callBack.whenGranted();
                }
            }
        }).onDenied(new Action<List<String>>() {
            @Override
            public void onAction(List<String> data) {
                if (callBack != null) {
                    callBack.whenDenied();
                }
            }
        }).start();
    }

    public static void p(Context context, String permission, final CallBack callBack) {
        p(context, callBack, new String[]{permission});
    }

    public interface CallBack {
        void whenGranted();

        void whenDenied();
    }
}
