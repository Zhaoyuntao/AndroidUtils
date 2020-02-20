package com.zhaoyuntao.androidutils.tools;

import android.content.Intent;
import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

public class ChooseFileUtils {

    public static void chooseFile(final AppCompatActivity activity, final FileGetter fileGetter) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*").addCategory(Intent.CATEGORY_OPENABLE);
        AvoidActivityResultFragment.getFragment(activity).startActivityForResult(intent, 1001, new AvoidActivityResultFragment.CallBack() {
            @Override
            public void onActivityResult(int resultCode, Intent data) {
                if (resultCode == AppCompatActivity.RESULT_OK) {
                    Uri uriFile = data.getData();
                    if (uriFile != null) {
                        if (fileGetter != null) {
                            fileGetter.whenGetFile(uriFile);
                        }
                    }
                }
            }
        });
    }

    interface FileGetter {
        void whenGetFile(Uri uri);
    }
}
