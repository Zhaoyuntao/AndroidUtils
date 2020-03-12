package com.androidutils.www.zandroidutils;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.zhaoyuntao.androidutils.permission.runtime.Permission;
import com.zhaoyuntao.androidutils.permission.PermissionSettings;
import com.zhaoyuntao.androidutils.tools.ZP;

import java.util.List;

public class Main3Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        ZP.p(Main3Activity.this, new ZP.RequestResult() {
            @Override
            public void onGranted(List<String> permissions) {

            }

            @Override
            public void onDenied(List<String> permissions) {

            }

            @Override
            public void onDeniedNotAsk(PermissionSettings permissionSettings) {

            }
        },Permission.CAMERA);
    }
}
