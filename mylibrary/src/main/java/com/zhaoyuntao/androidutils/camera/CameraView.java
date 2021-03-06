package com.zhaoyuntao.androidutils.camera;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.zhaoyuntao.androidutils.component.ZButton;
import com.zhaoyuntao.androidutils.permission.runtime.Permission;
import com.zhaoyuntao.androidutils.permission.PermissionSettings;
import com.zhaoyuntao.androidutils.tools.ZP;

import java.util.List;


public class CameraView extends ZButton {

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private CameraUtil camera;

    private CallBack callBack;

    private boolean pause;

    private int cameraId;
    //左右翻转
    private boolean overTurn;

    private float angle;

    private boolean alreadyInit;

    /**
     * 暂停预览画面
     */
    public void pause() {
        pause = true;
    }

    /**
     * 继续预览画面
     */
    public void resume() {
        pause = false;
    }

    public CameraView(Context context) {
        super(context);
        init();
    }

    public CameraView(Context context, CallBack callBack) {
        super(context);
        this.callBack = callBack;
        init();
    }

    public CameraView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CameraView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void startRecord(String path, String name) {
        camera.startRecord(path, name, true);
    }

    public void startRecord(String path, String name, boolean mic) {
        camera.startRecord(path, name, mic);
    }

    public void stopRecord() {
        camera.stopRecord();
    }

    public boolean isRecording() {
        return camera.isRecording();
    }

    @Override
    protected void underCanvas() {
        super.underCanvas();

        surfaceView = new SurfaceView(getContext());
        surfaceView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(surfaceView);
    }

    private void init() {
        synchronized (CameraView.class) {
            if (alreadyInit) {
                return;
            }
            //人脸框线条的宽度
            setStyle_bitmap_back(ZButton.style_bitmap_back_clip);
            holder = surfaceView.getHolder();
            ZP.p(getContext(), new ZP.RequestResult() {
                @Override
                public void onGranted(List<String> permissions) {
                    alreadyInit = true;
                    initCamera();
                }

                @Override
                public void onDenied(List<String> permissions) {
                    if (callBack != null) {
                        callBack.whenNoPermission();
                    }
                }

                @Override
                public void onDeniedNotAsk(PermissionSettings permissionSettings) {
                    if (callBack != null) {
                        callBack.whenNoPermission();
                    }
                }
            }, Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE, Permission.RECORD_AUDIO);
        }
    }

    private void initCamera() {
        camera = CameraUtil.getInstance(getContext());
        camera.setHolder(holder);

        final CameraUtil.CameraCallBack cameraCallBack = new CameraUtil.CameraCallBack() {
            @Override
            public void whenGotBitmap(Bitmap bitmap, byte[] data) {
                if (!pause) {
                    setDrawable_back(bitmap);
                    setDrawable_back_click(bitmap);
                    if (callBack != null) {
                        callBack.whenGotBitmap(bitmap, data);
                    }
                }
            }
        };

        camera.setCameraCallBack(cameraCallBack);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(final SurfaceHolder holder) {
                if (camera != null) {
                    camera.openCamera(getContext(), cameraId, new CameraUtil.CallBack() {
                        @Override
                        public void whenCreatedCamera() {
                            if (camera != null) {
                                camera.startPreview(getContext());
                            }
                            if (callBack != null) {
                                callBack.whenCameraCreated();
                            }
                        }
                    });
                    camera.setOverturn(overTurn);
                    camera.setBitmapAngle(angle);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }
        });
    }

    @Override
    protected void drawCanvas(Canvas canvas) {
        super.drawCanvas(canvas);
        int w_view = getWidth();
        int h_view = getHeight();
        if (w_view == 0 || h_view == 0) {
            return;
        }

    }

    public void setCallBack(CallBack callBack) {
        this.callBack = callBack;
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.releaseCamera();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        releaseCamera();
    }

    public void setCameraId(int cameraId) {
        this.cameraId = cameraId;
    }

    public void setOverturn(boolean overturn) {
        this.overTurn = overturn;
        if (this.camera != null) {
            camera.setOverturn(overturn);
        }
    }

    public void setAngle(float angle) {
        this.angle = angle;
        if (this.camera != null) {
            camera.setBitmapAngle(angle);
        }
    }

    public interface CallBack {
        void whenGotBitmap(Bitmap bitmap, byte[] data);

        void whenCameraCreated();

        void whenNoPermission();
    }
}
