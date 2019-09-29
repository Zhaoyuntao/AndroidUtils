package com.zhaoyuntao.androidutils.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;

import com.zhaoyuntao.androidutils.permission.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class TakePictureUtils {
    public static void takePhoto(final AppCompatActivity activity, String authority, final PhotoGetter photoGetter) {
        String imgName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "pic_" + S.currentTimeMillis() + ".jpg";
        final Uri uriImg = FileProvider.getUriForFile(activity, authority, new File(imgName));
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImg);
        AvoidActivityResultFragment.getFragment(activity).startActivityForResult(intent, 1002, new AvoidActivityResultFragment.CallBack() {
            @Override
            public void onActivityResult(int resultCode, Intent data) {
                if (resultCode == Activity.RESULT_OK) {
                    Bitmap bitmap = getCompressBitmap(activity, uriImg);
                    if (bitmap != null) {
                        if (photoGetter != null) {
                            photoGetter.whenGetPhoto(uriImg, bitmap);
                        }
                    }
                }
            }
        });
    }

    public static Bitmap getCompressBitmap(Context context, String filepath, String authorityOfProvider) {
        Uri uri = FileProvider.getUriForFile(context, authorityOfProvider, new File(filepath));
        return getCompressBitmap(context, uri);
    }

    public static Bitmap getCompressBitmap(Context context, Uri uri) {
        InputStream input = null;
        try {
            input = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            S.e(e);
            e.printStackTrace();
        }
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                S.e(e);
                e.printStackTrace();
            }
        }
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1)) {
            return null;
        }
        float hDes = 800;
        float wDes = 480;
        //proportion
        int inSampleSize = 1;
        if (originalWidth > originalHeight && originalWidth > wDes) {
            inSampleSize = (int) (originalWidth / wDes);
        } else if (originalWidth < originalHeight && originalHeight > hDes) {
            inSampleSize = (int) (originalHeight / hDes);
        }
        if (inSampleSize <= 0) {
            inSampleSize = 1;
        }
        //compress
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = inSampleSize;
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        try {
            input = context.getContentResolver().openInputStream(uri);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        if (input != null) {
            try {
                input.close();
            } catch (IOException e) {
                S.e(e);
                e.printStackTrace();
            }
        }
        return bitmap;
    }

    interface PhotoGetter {
        void whenGetPhoto(Uri uri, Bitmap bitmap);
    }
}
