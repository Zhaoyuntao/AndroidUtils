package com.zhaoyuntao.androidutils.permission.bridge;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.zhaoyuntao.androidutils.permission.ZPermission;

class Messenger extends BroadcastReceiver {

    public static void send(Context context) {
        Intent broadcast = new Intent(ZPermission.bridgeAction(context));
        context.sendBroadcast(broadcast);
    }

    private final Context mContext;
    private final Callback mCallback;

    public Messenger(Context context, Callback callback) {
        this.mContext = context;
        this.mCallback = callback;
    }

    public void register() {
        IntentFilter filter = new IntentFilter(ZPermission.bridgeAction(mContext));
        mContext.registerReceiver(this, filter);
    }

    public void unRegister() {
        try {
            mContext.unregisterReceiver(this);
        } catch (Exception ignore) {

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        mCallback.onCallback();
    }

    public interface Callback {

        void onCallback();
    }
}