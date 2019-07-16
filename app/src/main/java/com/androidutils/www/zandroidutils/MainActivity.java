package com.androidutils.www.zandroidutils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CompoundButton;

import com.zhaoyuntao.androidutils.camera.CameraView;
import com.zhaoyuntao.androidutils.component.FloatWindowHelper;
import com.zhaoyuntao.androidutils.component.LoggerView;
import com.zhaoyuntao.androidutils.component.ZSwitchButton;
import com.zhaoyuntao.androidutils.component.ZScaleBar;
import com.zhaoyuntao.androidutils.component.ZButton;
import com.zhaoyuntao.androidutils.component.ZVideoView;
import com.zhaoyuntao.androidutils.permission.runtime.Permission;
import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.T;
import com.zhaoyuntao.androidutils.tools.ZP;

public class MainActivity extends Activity {

    FloatWindowHelper floatWindowHelper;
    LoggerView contentLoggerView;

    CameraView cameraView;

//    AvcEncoder avcEncoder;

    ZVideoView zVideoView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        S.setFlag(true);

        initLogger();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ZP.p(activity(),  new ZP.CallBack() {
            @Override
            public void whenGranted() {

            }

            @Override
            public void whenDenied() {

            }
        },Permission.CAMERA,Permission.WRITE_EXTERNAL_STORAGE,Permission.READ_EXTERNAL_STORAGE,Permission.RECORD_AUDIO);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        final ZButton before=findViewById(R.id.before);
        final ZButton after=findViewById(R.id.after);

        ZSwitchButton switchButton=findViewById(R.id.switchbutton);
        switchButton.setOnPerformCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                S.s("isChecked---> by hand:"+isChecked);
                buttonView.setChecked(true);
            }
        });
        switchButton.setChecked(true);

        cameraView = findViewById(R.id.cameraview);
        cameraView.setCameraId(0);
        cameraView.setOverturn(false);
        cameraView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        cameraView.setCallBack(new CameraView.CallBack() {
            @Override
            public void whenGotBitmap(Bitmap bitmap, byte[] data) {
                before.setDrawable_back(bitmap);
                Bitmap b2=B.clip(bitmap.getWidth(),bitmap.getHeight()/2,bitmap);
                after.setDrawable_back(b2);
//                S.s("b2: w:"+b2.getWidth()+" h:"+b2.getHeight());
            }

            @Override
            public void whenCameraCreated() {

            }
        });

//        final TabDigit tabDigit = findViewById(R.id.tab);

        ZButton zButton = findViewById(R.id.zbutton1);
        zButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                tabDigit.setNextIndex();
                if (cameraView.isRecording()) {
                    T.t(activity(), "停止录像");
                    S.s("停止录像");
                    cameraView.stopRecord();
                } else {
                    T.t(activity(), "正在录像");
                    S.s("正在录像");
                    cameraView.startRecord(Environment.getExternalStorageDirectory().getAbsolutePath() + "/abcde/", "test1.mp4");
                }
            }

        });


        zVideoView = findViewById(R.id.zvideoview);


        zVideoView.setVideo(Environment.getExternalStorageDirectory().getAbsolutePath() + "/abcde/test1.mp4");
        zVideoView.play();


//
//        ZThread zThread = new ZThread(1) {
//            @Override
//            protected void todo() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        tabDigit.setNextIndex();
//                        WeatherTool.getWeather(new WeatherTool.CallBack() {
//
//                            @Override
//                            public void whenGotWeather(List<Weather> weathers) {
//
//                            }
//
//                            @Override
//                            public void whenFailed(String msg) {
//
//                            }
//                        });
//                    }
//                });
//            }
//        };
//        zThread.play();

        ZScaleBar scaleBar=findViewById(R.id.scalebar);
        scaleBar.setPercent(0.5f);
    }

    private void initLogger() {
        floatWindowHelper = FloatWindowHelper.getInstance(this);
        contentLoggerView = new LoggerView(this);
        contentLoggerView.setTextSize(B.sp2px(this, 5));
        floatWindowHelper.setDownListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contentLoggerView.scrollBottom();
            }
        });
        floatWindowHelper.setContentView(contentLoggerView);
    }

    private Context activity() {
        return this;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    public void finish() {

        super.finish();
    }

    @Override
    protected void onStop() {
        floatWindowHelper.cancel();
        super.onStop();
    }
}
