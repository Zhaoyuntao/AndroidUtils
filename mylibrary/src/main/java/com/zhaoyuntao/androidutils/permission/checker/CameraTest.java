package com.zhaoyuntao.androidutils.permission.checker;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;

import com.zhaoyuntao.androidutils.tools.S;

class CameraTest implements PermissionTest {

    private Context mContext;

    CameraTest(Context context) {
        this.mContext = context;
    }

    @Override
    public boolean test() throws Throwable {
        int numberOfCameras = Camera.getNumberOfCameras();
        S.s("numberOfCameras:" + numberOfCameras);
        PackageManager packageManager = mContext.getPackageManager();
        if (numberOfCameras <= 0 || !packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            S.e("ZPermission Error:没有可用摄像头");
            return false;
        }
        Camera camera = null;
        for (int i = 0; i < numberOfCameras; i++) {
            try {
                camera = Camera.open(i);
                Camera.Parameters parameters = camera.getParameters();
                camera.setParameters(parameters);
                camera.setPreviewCallback(PREVIEW_CALLBACK);
                camera.startPreview();
                S.s("ZPermission camera test:摄像头[" + i + "]可用");
                return true;
            } catch (Throwable e) {
                S.e("ZPermission camera test:摄像头[" + i + "]不可用");
            } finally {
                if (camera != null) {
                    camera.stopPreview();
                    camera.setPreviewCallback(null);
                    camera.release();
                }
            }
        }
        return false;
    }

    private static final Camera.PreviewCallback PREVIEW_CALLBACK = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
        }
    };
}