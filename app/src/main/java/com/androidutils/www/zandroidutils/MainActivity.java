package com.androidutils.www.zandroidutils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.CompoundButton;

import com.zhaoyuntao.androidutils.component.FloatWindowHelper;
import com.zhaoyuntao.androidutils.component.LoggerView;
import com.zhaoyuntao.androidutils.component.ZSwitchButton;
import com.zhaoyuntao.androidutils.component.ZScaleBar;
import com.zhaoyuntao.androidutils.component.ZButton;
import com.zhaoyuntao.androidutils.component.ZVideoView;
import com.zhaoyuntao.androidutils.net.Msg;
import com.zhaoyuntao.androidutils.net.ZSocket;
import com.zhaoyuntao.androidutils.permission.runtime.Permission;
import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.T;
import com.zhaoyuntao.androidutils.tools.ZP;

public class MainActivity extends Activity {

    FloatWindowHelper floatWindowHelper;
    LoggerView contentLoggerView;

    ZVideoView zVideoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        S.setFlag(true);

        initLogger();
        ZSocket.getInstance().setPort(6655);
        ZSocket.getInstance().setPortOfFileServer(6658);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        zVideoView=findViewById(R.id.videoview);
//        zVideoView.setVideo(Environment.getExternalStorageDirectory().getAbsolutePath()+"/zsocketcache/cateye1564989444792.mp4");

        ZP.p(activity(), new ZP.CallBack() {
            @Override
            public void whenGranted() {

            }

            @Override
            public void whenDenied() {

            }
        }, Permission.CAMERA, Permission.WRITE_EXTERNAL_STORAGE, Permission.READ_EXTERNAL_STORAGE, Permission.RECORD_AUDIO);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        final ZButton progress = findViewById(R.id.progress);

        ZSwitchButton switchButton = findViewById(R.id.switchbutton);
        switchButton.setOnPerformCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                S.s("isChecked---> by hand:" + isChecked);
                if (isChecked) {
                    progress.showProgress(true);
                } else {
                    progress.showProgress(false);
                }
            }
        });
        switchButton.setChecked(false);

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
//                ZSocket.getInstance().DEBUG().addAnswer("hello", new ZSocket.Answer() {
//                    @Override
//                    public String getAnswer(String param) {
//                        S.s("params:"+param);
//                        return "hi";
//                    }
//                }).ask("hello","你好", new ZSocket.AskResult() {
//                    @Override
//                    public void whenTimeOut() {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                T.t(activity(),"请求超时");
//                            }
//                        });
//                    }
//
//                    @Override
//                    public void whenGotResult(final Msg msg) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                T.t(activity(),new String(msg.msg));
//                            }
//                        });
//                    }
//                });

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
                ZSocket.getInstance().ask("hello", new ZSocket.AskResult() {
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




        ZScaleBar scaleBar = findViewById(R.id.scalebar);
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
        ZSocket.close();
        super.finish();
    }

    @Override
    protected void onStop() {
        floatWindowHelper.cancel();
        super.onStop();
    }
}
