package com.androidutils.www.zandroidutils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.zhaoyuntao.androidutils.camera.CameraView;
import com.zhaoyuntao.androidutils.component.FloatWindowHelper;
import com.zhaoyuntao.androidutils.component.LoggerView;
import com.zhaoyuntao.androidutils.component.ZSwitchButton;
import com.zhaoyuntao.androidutils.component.ZScaleBar;
import com.zhaoyuntao.androidutils.component.ZButton;
import com.zhaoyuntao.androidutils.component.ZVideoView;
import com.zhaoyuntao.androidutils.net.Msg;
import com.zhaoyuntao.androidutils.net.ZSocket;
import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.QRCodeTool;
import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.ZMusicPlayer;
import com.zhaoyuntao.androidutils.tools.T;

import java.util.Arrays;

public class MainActivity extends Activity {

    FloatWindowHelper floatWindowHelper;
    LoggerView contentLoggerView;

    ZVideoView zVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        S.setFlag(true);
        S.s(Arrays.toString(B.getScreenWH(this)));
        initLogger();
        ZSocket.getInstance().setPort(6655);
        ZSocket.getInstance().setPortOfFileServer(6658);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        zVideoView=findViewById(R.id.videoview);
//        zVideoView.setVideo(Environment.getExternalStorageDirectory().getAbsolutePath()+"/zsocketcache/cateye1564989444792.mp4");

//        ZP.p(activity(), new ZP.CallBack() {
//            @Override
//            public void whenGranted() {
//
//            }
//
//            @Override
//            public void whenDenied() {
//
//            }
//        }, Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE, Permission.RECORD_AUDIO);
//        ZP.requestPermission(getApplicationContext(), new ZP.RequestResult() {
//            @Override
//            public void onGranted(List<String> permissions) {
//
//            }
//
//            @Override
//            public void onDenied(List<String> permissions) {
//
//            }
//
//            @Override
//            public void onDeniedNotAsk(PermissionSettings permissionSettings) {
//
//            }
//        },Permission.CAMERA);


        ZSwitchButton switchButton = findViewById(R.id.switchbutton);
        switchButton.setOnPerformCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                S.s("isChecked---> by hand:" + isChecked);
            }
        });
        switchButton.setChecked(true);

        final ZButton zButton = findViewById(R.id.zbutton1);
        zButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final long time_start=S.currentTimeMillis();
//                ZSocket.getInstance().DEBUG().setFilePathProcessor(new ZSocket.FilePathProcessor() {
//                    @Override
//                    public String getFilePath(String filename) {
//                        if(filename.endsWith(".mp4")){
//                            return B.path_system+"/abcsss/";
//                        }
//                        return B.path_system;
//                    }
//                }).downloadFile("dp.mp4", new ZSocket.FileDownloadResult() {
//                    @Override
//                    public void whenTimeOut() {
//                        T.t(activity(),"请求超时");
//                    }
//
//                    @Override
//                    public void whenFileNotFind(String filename) {
//                        S.e("文件未找到,无法下载:"+filename);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                T.t(activity(),"文件不存在,无法下载");
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void whenDownloadCompleted(final String filename) {
//                        final long during=S.currentTimeMillis()-time_start;
//                        S.s("文件下载完毕:" + filename+" 耗时:"+during);
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                T.t(activity(),"文件下载完毕:" + filename+" 耗时:"+during);
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void whenDownloading(String filename, float percent) {
//                        S.s("正在下载文件:" + percent);
//                    }
//
//                    @Override
//                    public void whenStartDownloading(String filename, long filesize) {
//                        S.s("开始下载文件:"+filename+" 文件大小:"+((double)filesize/1024/1024)+"Mb ["+filesize+"]");
//                    }
//                });
                ZSocket.getInstance().DEBUG().addAnswer("hello", new ZSocket.Answer() {
                    @Override
                    public String getAnswer(String param) {
                        S.s("params:"+param);
                        return "hi";
                    }
                });

//                ZSocket.getInstance().setReceiver(new ZSocket.ReceiverResult() {
//                    @Override
//                    public void whenGotResult(Msg msg) {
//                        S.s("接到消息:"+new String(msg.msg));
//                    }
//                });
//                ZSocket.getInstance().send("hello");

//                ZSocket.getInstance().DEBUG().addAnswer(new ZSocket.Answer() {
//                    @Override
//                    public String getAsk() {
//                        return "hello";
//                    }
//
//                    @Override
//                    public String getAnswer() {
//                        return "hi";
//                    }
//                });
                ZSocket.getInstance().DEBUG().ask("hello", new ZSocket.AskResult() {
                    @Override
                    public void whenTimeOut() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                T.t(activity(),"请求超时");
                            }
                        });
                    }

                    @Override
                    public void whenGotResult(Msg msg) {
                        if (msg.type == Msg.ANSWER) {
                            final String content = new String(msg.msg);
                            S.s("接到消息:" + content);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    T.t(activity(),content);
                                }
                            });
                        }
                    }
                });
            }

        });
        ZButton z1=findViewById(R.id.z1);
        z1.setChoosen(true);
        ZButton z2=findViewById(R.id.z2);
        z1.addFriend(z2);
        ZButton center=findViewById(R.id.center);
        center.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ZMusicPlayer.playSound(activity(),R.raw.a);
            }
        });
        ZButton center2=findViewById(R.id.center2);
        center2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ZMusicPlayer.isPlayingMusic()){
                    ZMusicPlayer.releaseMusic();
                }else{
//                    ZMusicPlayer.playMusicResId(activity(),R.raw.music);
                    ZMusicPlayer.playMusicAssets(activity(),"music.ogg",false);
                }
            }
        });
        ZButton center3=findViewById(R.id.center3);
        center3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ZMusicPlayer.isPlayingMusic()){
                    ZMusicPlayer.pauseMusic();
                }else{
                    ZMusicPlayer.resumeMusic();
                }
            }
        });

        Bitmap bitmap= QRCodeTool.getQRCodeBitmap("hello,ZButton",100,100);
        zButton.setDrawable_center(bitmap);
        final TextView textView=findViewById(R.id.showprogress);
        final ZScaleBar scaleBar = findViewById(R.id.scalebar);
        scaleBar.setPercent(0.5f);
        scaleBar.setCallBack(new ZScaleBar.CallBack() {
            @Override
            public void whenScaleStart(float percent) {

            }

            @Override
            public void whenScale(float percent) {
//                S.s("percent:"+percent);
            }

            @Override
            public void whenScaleEnd(float percent) {

                textView.setText(" "+(int)(percent*100)+"%");
            }
        });
        textView.setText(" "+(int)(scaleBar.getPercent()*100)+"%");
        final ZScaleBar scaleBar2 = findViewById(R.id.scalebar2);
        final TextView textView2=findViewById(R.id.showprogress2);
        scaleBar2.setPercent(0.23f);
        scaleBar2.setCallBack(new ZScaleBar.CallBack() {
            @Override
            public void whenScaleStart(float percent) {

            }

            @Override
            public void whenScale(float percent) {
//                S.s("percent:"+percent);
                textView2.setText(" "+(int)(percent*100)+"%");
            }

            @Override
            public void whenScaleEnd(float percent) {

                textView2.setText(" "+(int)(percent*100)+"%");
            }
        });

        textView2.setText(" "+(int)(scaleBar2.getPercent()*100)+"%");

        final ZButton zButton2=findViewById(R.id.zb2);

//        CameraView cameraView=findViewById(R.id.cameraview);
//        cameraView.setAngle(90);
//        cameraView.setCallBack(new CameraView.CallBack() {
//            @Override
//            public void whenGotBitmap(Bitmap bitmap, byte[] data) {
//                zButton2.setDrawable_back(bitmap);
//            }
//
//            @Override
//            public void whenCameraCreated() {
//
//            }
//
//            @Override
//            public void whenNoPermission() {
//                T.t(MainActivity.this,"no camera permission");
//            }
//        });

//        S.lll();
//        S.ll();
//        S.l();
//        S.e("1");
//        S.ed("1");
//        S.s("111");
//        S.sd("111");
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
        ZSocket.close();
        super.finish();
    }

    @Override
    protected void onStop() {
        floatWindowHelper.cancel();
        super.onStop();
    }
}
