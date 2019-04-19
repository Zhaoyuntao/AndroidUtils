package com.androidutils.www.zandroidutils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.zhaoyuntao.androidutils.component.FloatWindowHelper;
import com.zhaoyuntao.androidutils.component.LoggerView;
import com.zhaoyuntao.androidutils.component.ZButton;
import com.zhaoyuntao.androidutils.tools.B;
import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.T;
import com.zhaoyuntao.androidutils.tools.ZThread;

public class MainActivity extends AppCompatActivity {

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
        zButton.addFriend(zButton0).addFriend(zButton1).addFriend(zButton2);//.addFriend(zButton3);
        zButton.setText_center("打开");
        zButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                floatWindowHelper.show(true);
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
            boolean issmall=true;
            @Override
            public void onClick(View v) {
                    floatWindowHelper.cancel();
            }
        });

        final LoggerView loggerView = findViewById(R.id.log);
        loggerView.setTextSize(B.sp2px(this, 5));
        S.callBack=new S.CallBack() {
            @Override
            public void whenLog(S.LogItem logItem) {
                contentLoggerView.appendLog(logItem.toString(),B.getRandomColor());
                loggerView.appendLog(logItem.toString(),B.getRandomColor());
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
                        S.addLog(new S.LogItem("=======> 1651135135"));
                        if((S.currentTimeMillis()-now)/1000>time){
                            S.addLog(new S.LogItem("zthread close.......\nzthread has closed"));
                            zThread.close();
                        }
                    }
                });
            }
        };
        zThread.start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        floatWindowHelper.show(false);
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
