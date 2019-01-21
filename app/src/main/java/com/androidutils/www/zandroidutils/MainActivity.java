package com.androidutils.www.zandroidutils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.zhaoyuntao.androidutils.component.FingerControl;
import com.zhaoyuntao.androidutils.component.ZButton;
import com.zhaoyuntao.androidutils.tools.SS;
import com.zhaoyuntao.androidutils.tools.T;
import com.zhaoyuntao.androidutils.tools.ZThread;

import java.io.IOException;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ZButton zButton = findViewById(R.id.zbutton);
        final ZButton zButton0 = findViewById(R.id.zbutton0);
        final ZButton zButton1 = findViewById(R.id.zbutton1);
        final ZButton zButton2 = findViewById(R.id.zbutton2);
        final ZButton zButton3 = findViewById(R.id.zbutton3);
        final FingerControl fingercontrol = findViewById(R.id.fingercontrol);
        fingercontrol.setCallBack(new FingerControl.CallBack() {
            @Override
            public void send(float x, float y) {
                SS.s("x:" + x + " y:" + y);
            }

        });
        zButton.addFriend(zButton0).addFriend(zButton1).addFriend(zButton2).addFriend(zButton3);
        zButton3.setChoosen(true);
        zButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fingercontrol.setFrame(1);
            }
        });



    }
}
