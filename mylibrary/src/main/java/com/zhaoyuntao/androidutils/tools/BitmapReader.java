package com.zhaoyuntao.androidutils.tools;

import android.graphics.Bitmap;

public class BitmapReader extends Thread {
    B.CallBack callBack;
    String filename;

    public BitmapReader(String filename,B.CallBack callBack) {
        this.filename=filename;
        this.callBack = callBack;
    }

    @Override
    public void run() {
        Bitmap bitmap = B.getBitmap(filename);
        callBack.whenBitmapReady(bitmap);
    }


}
