package com.androidutils.www.zandroidutils;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.zhaoyuntao.androidutils.component.FloatWindowHelper;
import com.zhaoyuntao.androidutils.component.LoggerView;
import com.zhaoyuntao.androidutils.component.ZButton;
import com.zhaoyuntao.androidutils.component.ZDialog;
import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.T;
import com.zhaoyuntao.androidutils.tools.ZThread;

public class MainActivity extends Activity {

    ZThread zThread;
    FloatWindowHelper floatWindowHelper;
    LoggerView contentLoggerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        S.setFlag(true);

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


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ZButton zButton = findViewById(R.id.zbutton);
        final ZButton zButton0 = findViewById(R.id.zbutton0);
        final ZButton zButton1 = findViewById(R.id.zbutton1);
        final ZButton zButton2 = findViewById(R.id.zbutton2);
        final ZButton zButton3 = findViewById(R.id.zbutton3);
        zButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ZDialog(activity()).setTitle("暂停当前清扫任务?").setMessage("oasdbasdwuhdqwd")

                        .setButton2OnClickListener(new ZDialog.OnClickListener() {
                    @Override
                    public void onClick(ZDialog dialog) {

                    }
                })
 .setButton1OnClickListener("ok", new ZDialog.OnClickListener() {
                    @Override
                    public void onClick(ZDialog dialog) {

                    }
                })
//                        .setButton3OnClickListener(new ZDialog.OnClickListener() {
//                    @Override
//                    public void onClick(ZDialog dialog) {
//
//                    }
//                })
 .show();
            }
        });
        zButton.addFriend(zButton0).addFriend(zButton1).addFriend(zButton2);//.addFriend(zButton3);
        zButton.setText_center("打开");
        zButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatWindowHelper.show();
            }
        });
        zButton0.setText_center("最小化");
        zButton0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatWindowHelper.miniSize();
            }
        });
        zButton1.setText_center("关闭");
        zButton1.setOnClickListener(new View.OnClickListener() {
            boolean issmall = true;

            @Override
            public void onClick(View v) {
                floatWindowHelper.cancel();
            }
        });

        final LoggerView loggerView = findViewById(R.id.log);
        loggerView.setTextSize(5);
        S.callBack = new S.CallBack() {
            @Override
            public void whenLog(S.LogItem logItem) {
                contentLoggerView.appendLog(logItem.toString(), B.getRandomColor());
                loggerView.appendLog(logItem.toString(), B.getRandomColor());
            }
        };
        final int time = 50;
        final long now = S.currentTimeMillis();

        zThread = new ZThread(8) {
            @Override
            protected void todo() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        S.ss_d("=======> 1651135135");
                        if ((S.currentTimeMillis() - now) / 1000 > time) {
//                            S.ss_d("zthread close.......\nzthread has closed");
                            zThread.close();
                        }
                    }
                });
            }
        };
        zThread.start();
    }

    private Context activity() {

        return this;
    }

    @Override
    protected void onStart() {
        super.onStart();
        floatWindowHelper.requestPermission(activity());
        floatWindowHelper.show();
    }


    @Override
    public void finish() {

        zThread.close();
        super.finish();
    }

    @Override
    protected void onStop() {
        floatWindowHelper.cancel();
        super.onStop();
    }
}
