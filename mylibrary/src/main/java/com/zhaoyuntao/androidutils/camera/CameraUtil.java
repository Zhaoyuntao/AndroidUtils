package com.zhaoyuntao.androidutils.camera;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import androidx.annotation.RequiresApi;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.zhaoyuntao.androidutils.permission.runtime.Permission;
import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.ZP;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * 拍照工具类
 */
public class CameraUtil {
    private Camera camera;

    public static int w_preview = 640;
    public static int h_preview = 480;

    public static int w_picture = 640;
    public static int h_picture = 480;

    public static int w_bitmap;
    public static int h_bitmap;

    private MediaRecorder mRecorder;
    private boolean isRecording; // 记录是否正在录像,fasle为未录像, true 为正在录像

    //相机旋转角度
    private int angleCamera;
    /**
     * 升序
     */
    private CameraAscendSizeComparator ascendSizeComparator = new CameraAscendSizeComparator();
    private static CameraUtil instance = null;
    private int cameraId = 0;

    private NV21ToBitmap nv21ToBitmap;
    //左右翻转
    private boolean overturn;
    private float angle;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private CameraUtil(Context context) {
        nv21ToBitmap = new NV21ToBitmap(context);
    }

    public static CameraUtil getInstance(Context context) {
        if (instance == null) {
            synchronized (CameraUtil.class) {
                if (instance == null) {
                    instance = new CameraUtil(context);
                }
            }
        }
        return instance;
    }

    private SurfaceHolder holder;
    private Surface surface;

    public void setHolder(SurfaceHolder holder) {
        this.holder = holder;
        this.surface = holder.getSurface();
    }

    public void startRecord(String path, String name, boolean mic) {

        if (camera == null) {
            S.e("camera 不可用");
            return;
        }
        if (!isRecording) {
            //判断麦克风是否可用:
//            boolean mic = validateMicAvailability();


            // 判断sd卡是否存在
            boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
            File videoFolder = null;
            if (sdCardExist) {
                // 声明存放视频的文件夹的File对象
                videoFolder = new File(path);
                // 如果不存在此文件夹,则创建
                if (!videoFolder.exists()) {
                    videoFolder.mkdirs();
                }
                S.s("视频目录:[ " + videoFolder.getAbsolutePath() + " ]exists:" + videoFolder.exists());
                // 设置surfaceView不管理的缓冲区
//            hold.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            } else {
                S.s("sdcard 不存在！");
                return;
            }
            // 获取当前时间,作为视频文件的文件名
            // 声明视频文件对象
            File videoFile = new File(videoFolder.getAbsoluteFile(), name);
            S.s("path:" + path + " name:" + name);
            S.s("11");
            // 创建此视频文件
            try {
                videoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 关闭预览并释放资源
            camera.unlock();
            mRecorder = new MediaRecorder();
            mRecorder.setCamera(camera);
            mRecorder.setPreviewDisplay(surface); // 预览
            mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); // 视频源
            if (mic) {
                mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC); // 录音源为麦克风
            }
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4); // 输出格式为mp4
            mRecorder.setVideoSize(w_preview, h_preview); // 视频尺寸
//                        mRecorder.setVideoEncodingBitRate(2 * 1280 * 720); //设置视频编码帧率
            mRecorder.setVideoEncodingBitRate(1024); //设置视频编码帧率
            mRecorder.setVideoFrameRate(30); // 视频帧频率
            mRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT); // 视频编码
            if (mic) {
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT); // 音频编码
            }
            mRecorder.setMaxDuration(1800000); // 设置最大录制时间
            S.s("22");
            mRecorder.setOutputFile(videoFile.getAbsolutePath()); // 设置录制文件源
            try {
                mRecorder.prepare(); // 准备录像
            } catch (IOException e) {
                e.printStackTrace();
            }
            S.s("33");
            try {
                mRecorder.start(); // 开始录像
            } catch (Exception e) {
                e.printStackTrace();
                S.e(e);
            }
            isRecording = true; // 改变录制状态为正在录制
            camera.setPreviewCallback(previewCallback);
            S.s("已经开始录像");
        } else {
            S.e("正在录制中,无法再次录制");
        }
    }


    public boolean isRecording() {
        return isRecording;
    }

    public void setOverturn(boolean overturn) {
        this.overturn = overturn;
    }

    public void setBitmapAngle(float angle) {
        this.angle = angle;
    }

    public interface CallBack {
        void whenCreatedCamera();
    }


    public void openCamera(final Context context, final CallBack callBack) {
        openCamera(context, cameraId, callBack);
    }

    public void openCamera(final Context context, final int cameraId, final CallBack callBack) {
        this.cameraId = cameraId;
        Configuration configuration = context.getResources().getConfiguration();
        int orientation = configuration.orientation;
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //横屏
            S.s("横屏:");
        } else {
            //竖屏
            S.s("竖屏");
        }
        S.s("申请相机权限");
        ZP.p(context, new ZP.CallBack() {
            @Override
            public void whenGranted() {
                S.s("ZPermission: Camera权限已打开");
                camera = getCamera(cameraId);
                if (context instanceof Activity) {
                    Activity activity = (Activity) context;
                    setCameraDisplayOrientation(activity, cameraId, camera);
                }
                S.s("打开成功");
                if (callBack != null && camera != null) {
                    callBack.whenCreatedCamera();
                }
            }

            @Override
            public void whenDenied() {
                S.e("ZPermission: Camera权限未打开");
            }
        }, Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE, Permission.RECORD_AUDIO);

    }

    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                S.s("angle:" + 0);
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                S.s("angle:" + 90);
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                S.s("angle:" + 180);
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                S.s("angle:" + 270);
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    private Camera getCamera(int cameraId) {
        Camera camera = null;
        int numberOfCameras = Camera.getNumberOfCameras();
        if (numberOfCameras > 0 && cameraId < numberOfCameras) {
            S.s("摄像头可用数量:" + numberOfCameras);
            try {
                S.s("正在打开相机:" + cameraId);
                camera = Camera.open(cameraId);

                S.s("打开相机:" + cameraId + ",开始设置相机");
                setupCamera(camera);
                S.s("设置成功");
            } catch (Exception e) {
                S.e("getCamera err: " + e);
                S.e(e);
            }
        } else {
            S.e("摄像头不可用:" + cameraId);
        }
        return camera;
    }

    /**
     * 设置surfaceView的尺寸 因为camera默认是横屏，所以取得支持尺寸也都是横屏的尺寸
     * 我们在startPreview方法里面把它矫正了过来，但是这里我们设置设置surfaceView的尺寸的时候要注意 previewSize.height<previewSize.width
     * previewSize.width才是surfaceView的高度
     * 一般相机都是屏幕的宽度 这里设置为屏幕宽度 高度自适应 你也可以设置自己想要的大小
     */
    private void setupCamera(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        //根据屏幕尺寸获取最佳 大小
        Size s = getSize(parameters.getSupportedPreviewSizes(), w_preview, h_preview);
        w_preview = s.width;
        h_preview = s.height;
        S.s("preview最终选择:w:" + w_preview + " h:" + h_preview);
        parameters.setPreviewSize(w_preview, h_preview);

        s = getSize(parameters.getSupportedPictureSizes(), w_picture, h_picture);
        w_picture = s.width;
        h_picture = s.height;
        S.s("picture最终选择:w:" + w_picture + " h:" + h_picture);
        parameters.setPictureSize(w_picture, h_picture);
        parameters.setRotation(180);
        //不自动对焦
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_FIXED);
        //自动对焦
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.setParameters(parameters);

    }

    public interface CameraCallBack {

        void whenGotBitmap(Bitmap bitmap, byte[] data);
    }

    CameraCallBack cameraCallBack;

    public void setCameraCallBack(CameraCallBack cameraCallBack) {
        this.cameraCallBack = cameraCallBack;
    }

    Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if ((w_bitmap == 0 || h_bitmap == 0) && camera != null) {
                Camera.Parameters parameters = camera.getParameters();
                if (parameters != null) {
                    Size size = parameters.getPreviewSize();
                    S.s("size.w:" + size.width + " h:" + size.height);
                    if (size.width != 0 && size.height != 0) {
                        w_bitmap = size.width;
                        h_bitmap = size.height;
                    }
                }
            }
            Bitmap bitmap = nv21ToBitmap.nv21ToBitmap(data, w_bitmap, h_bitmap);
            if (angle != 0) {
                bitmap = B.rotate(bitmap, angle);
            }
            if (overturn) {
                bitmap = B.reverse(bitmap, 0);
            }
            if (cameraCallBack != null) {
                cameraCallBack.whenGotBitmap(bitmap, data);
            }
        }
    };

    /**
     * 开始预览
     */
    public void startPreview(Context activity) {

        if (camera == null) {
            S.e("camera is null, can not startPreview!");
            return;
        }
        camera.stopPreview();

        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
            S.s("camera.setPreviewDisplay err:");
            S.s(e);
        }
        if (activity instanceof Activity) {
            S.s("**********************************************setCameraDisplayOrientation");
            Activity activity1 = (Activity) activity;
//            setCameraDisplayOrientation(activity1,cameraId,camera);
        } else {
            S.e("***********-----------------*******************setCameraDisplayOrientation");

        }
        camera.setPreviewCallback(previewCallback);
        camera.startPreview();


    }

    public class NV21ToBitmap {
        private RenderScript rs;
        private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
        private Type.Builder yuvType, rgbaType;
        private Allocation in, out;

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        public NV21ToBitmap(Context context) {
            rs = RenderScript.create(context);
            yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        }

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        public Bitmap nv21ToBitmap(byte[] nv21, int width, int height) {
            if (yuvType == null) {
                yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
                in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
                rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
                out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
            }
            in.copyFrom(nv21);
            yuvToRgbIntrinsic.setInput(in);
            yuvToRgbIntrinsic.forEach(out);
            Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            out.copyTo(bmpout);
            return bmpout;
        }
    }


    private int getRecorderRotation(int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        return info.orientation;
    }

    /**
     * 获取所有支持的返回视频尺寸
     *
     * @param list      list
     * @param minHeight minHeight
     * @return Size
     */
    private Size getPropVideoSize(List<Size> list, int minHeight) {
        Collections.sort(list, ascendSizeComparator);

        int i = 0;
        for (Size s : list) {
            if ((s.height >= minHeight)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    /**
     * 释放相机资源
     */
    public void releaseCamera() {
        stopRecord();
        if (camera != null) {
            camera.lock();
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
            S.s("相机已释放");
        }
    }

    public void stopRecord() {

        try {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        isRecording = false;
        S.s("停止录像");
    }

    /**
     * 根据 宽度和高度找到是否有相等的 尺寸  如果没有 就获取最小的 值
     *
     * @param list list
     * @return size
     */
    public Size getSize(List<Size> list, int w, int h) {
        Collections.sort(list, ascendSizeComparator);

        S.s("----------------------------------------------------------");
        for (int x = 0; x < list.size(); x++) {
            Size s = list.get(x);
            S.s("支持:w:" + s.width + " h:" + s.height);
            if ((s.width >= w) && (s.height >= h)) {
                return s;
            }
        }
        S.s("----------------------------------------------------------");
        //如果没找到，就选最小的size 0
        return list.get(list.size());
    }

    public Size getPropPictureSize(List<Size> list, float th, int minWidth) {
        Collections.sort(list, ascendSizeComparator);

        int i = 0;
        for (Size s : list) {
            if ((s.width >= minWidth) && equalRate(s, th)) {
                S.s("PictureSize : w = " + s.width + "h = " + s.height);
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;//如果没找到，就选最小的size
        }
        return list.get(i);
    }

    /**
     * 升序 按照高度
     */
    private class CameraAscendSizeComparatorForHeight implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            if (lhs.height == rhs.height) {
                return 0;
            } else if (lhs.height > rhs.height) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private boolean equalRate(Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        return Math.abs(r - rate) <= 0.03;
    }

    /**
     * 降序
     */
    private class CameraDropSizeComparator implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width < rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /**
     * 升序
     */
    private class CameraAscendSizeComparator implements Comparator<Size> {
        @Override
        public int compare(Size lhs, Size rhs) {
            if (lhs.width == rhs.width) {
                return 0;
            } else if (lhs.width > rhs.width) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    /**
     * 打印支持的previewSizes
     *
     * @param params
     */
    private void printSupportPreviewSize(Camera.Parameters params) {
        List<Size> previewSizes = params.getSupportedPreviewSizes();
        for (int i = 0; i < previewSizes.size(); i++) {
            Size size = previewSizes.get(i);
        }

    }

    /**
     * 打印支持的pictureSizes
     *
     * @param params
     */
    private void printSupportPictureSize(Camera.Parameters params) {
        List<Size> pictureSizes = params.getSupportedPictureSizes();
        for (int i = 0; i < pictureSizes.size(); i++) {
            Size size = pictureSizes.get(i);
        }
    }

    /**
     * 打印支持的聚焦模式
     *
     * @param params params
     */
    private void printSupportFocusMode(Camera.Parameters params) {
        List<String> focusModes = params.getSupportedFocusModes();
        for (String mode : focusModes) {
            S.e("printSupportFocusMode: " + mode);
        }
    }

    /**
     * 打开闪关灯
     *
     * @param mCamera camera
     */
    public void turnLightOn(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null) {
            return;
        }
        String flashMode = parameters.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            }
        }
    }

    /**
     * 自动模式闪光灯
     *
     * @param mCamera camera
     */
    public void turnLightAuto(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        if (flashModes == null) {
            return;
        }
        String flashMode = parameters.getFlashMode();
        if (!Camera.Parameters.FLASH_MODE_AUTO.equals(flashMode)) {
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            }
        }
    }

    /**
     * 关闭闪光灯
     *
     * @param mCamera camera
     */
    public void turnLightOff(Camera mCamera) {
        if (mCamera == null) {
            return;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        String flashMode = parameters.getFlashMode();
        if (flashModes == null) {
            return;
        }
        if (!Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            }
        }
    }
}
