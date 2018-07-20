package com.androidutils.www.zandroidutils;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.zhaoyuntao.androidutils.component.ZButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final ZButton zButton = findViewById(R.id.zbutton);
        zButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(zButton.isEnabled()){
                    zButton.setEnabled(false);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    zButton.setEnabled(true);
                                }
                            });
                        }
                    }).start();
                }
            }
        });
    }
}
