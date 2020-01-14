package com.androidutils.www.zandroidutils;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.zhaoyuntao.androidutils.permission.ZPermission;
import com.zhaoyuntao.androidutils.permission.runtime.Permission;
import com.zhaoyuntao.androidutils.tools.S;
import com.zhaoyuntao.androidutils.tools.ZP;

public class Main3Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        ZP.p(Main3Activity.this, Permission.READ_PHONE_STATE, new ZP.CallBack() {
            @Override
            public void whenGranted() {
//                ZP.p(Main3Activity.this,
//                        Permission.ACCESS_FINE_LOCATION,
//                        new ZP.CallBack() {
//                            @Override
//                            public void whenGranted() {
//                                S.s("2");
//                            }
//
//                            @Override
//                            public void whenDenied() {
//
//                            }
//                        });
            }

            @Override
            public void whenDenied() {

            }
        });
    }
}
