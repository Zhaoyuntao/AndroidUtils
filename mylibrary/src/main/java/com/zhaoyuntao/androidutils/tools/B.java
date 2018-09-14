package com.zhaoyuntao.androidutils.tools;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.format.DateFormat;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 */
public class B {

    private static Map<String, Bitmap> bitmaps = new HashMap<>();

    public static void close() {
        bitmaps.clear();
    }


    /**
     * @param bitmap_src
     * @return Bitmap
     * @author caizhiming
     */
    public static Bitmap getBitmap_circle(Bitmap bitmap_src) {
        if (bitmap_src == null) {
            return null;
        }
        Paint paint = new Paint();
        int w_bitmap = bitmap_src.getWidth();
        int h_bitmap = bitmap_src.getHeight();
        int doubleRadius = w_bitmap;
        if (w_bitmap > h_bitmap) {
            doubleRadius = h_bitmap;
        }
        Bitmap bitmap_des = Bitmap.createBitmap(w_bitmap, h_bitmap, Bitmap.Config.ARGB_8888);
        Canvas canvas_des = new Canvas(bitmap_des);
        final Rect rect = new Rect(0, 0, bitmap_src.getWidth(), bitmap_src.getHeight());
        paint.setAntiAlias(true);
        canvas_des.drawARGB(0, 0, 0, 0);

        canvas_des.drawCircle(w_bitmap / 2, h_bitmap / 2, doubleRadius / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas_des.drawBitmap(bitmap_src, rect, rect, paint);

        Bitmap bitmap_des_final = Bitmap.createBitmap(bitmap_des, (w_bitmap - doubleRadius) / 2, (h_bitmap - doubleRadius) / 2, doubleRadius, doubleRadius);
        bitmap_des.recycle();
        return bitmap_des_final;
    }

    public static Bitmap getBitmap_circle_addBorder(Bitmap bitmap_src) {
        if (bitmap_src == null) {
            return null;
        }
        Paint paint = new Paint();
        int w_bitmap = bitmap_src.getWidth();
        int h_bitmap = bitmap_src.getHeight();
        int doubleRadius = w_bitmap;
        if (w_bitmap > h_bitmap) {
            doubleRadius = h_bitmap;
        }
        Bitmap bitmap_des = Bitmap.createBitmap(w_bitmap, h_bitmap, Bitmap.Config.ARGB_8888);
        Canvas canvas_des = new Canvas(bitmap_des);
        Rect rect_src = new Rect();
        rect_src.set(0, 0, w_bitmap, h_bitmap);
        Rect rect_des = new Rect();
        rect_des.set(0, 0, w_bitmap, h_bitmap);
        canvas_des.drawBitmap(bitmap_src, rect_src, rect_des, paint);
        paint.setStrokeWidth(2);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        canvas_des.drawCircle(w_bitmap / 2, h_bitmap / 2, doubleRadius / 2, paint);
        return bitmap_des;
    }

    public static Bitmap getBitmap_rect(Bitmap bitmap_src, Rect rect_src, Rect rect_des) {
        if (bitmap_src == null) {
            return null;
        }
        Bitmap bitmap_des = Bitmap.createBitmap(rect_des.width(), rect_des.height(), Bitmap.Config.ARGB_8888);
        Canvas canvas_des = new Canvas(bitmap_des);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setStyle(Paint.Style.FILL);
        canvas_des.drawBitmap(bitmap_src, rect_src, rect_des, p);
        return bitmap_des;
    }

    public static Bitmap getBitmap_polygon(Bitmap bitmap_src, Path path) {
        return getBitmap_polygon(bitmap_src, path, 0, 0);
    }

    public static Bitmap getBitmap_polygon(Bitmap bitmap_src, Path path, int x_scale, int y_scale) {
        if (bitmap_src == null) {
            return null;
        }
        Bitmap bitmap_des = Bitmap.createBitmap(bitmap_src.getWidth(), bitmap_src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas_des = new Canvas(bitmap_des);
        Paint p = new Paint();
        p.setAntiAlias(true);
        path.close();
        p.setStyle(Paint.Style.FILL);
        p.setStrokeWidth(3);
        canvas_des.save();
        canvas_des.translate(-x_scale, -y_scale);
        canvas_des.drawPath(path, p);
        canvas_des.restore();
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas_des.drawBitmap(bitmap_src, 0, 0, p);
        p.setXfermode(null);
        return bitmap_des;
    }

    public static Bitmap getBitmap_polygon(Bitmap bitmap_src, int[][] positions) {
        if (bitmap_src == null) {
            return null;
        }
        Bitmap bitmap_des = Bitmap.createBitmap(bitmap_src.getWidth(), bitmap_src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas_des = new Canvas(bitmap_des);
        Paint p = new Paint();
        p.setAntiAlias(true);
        Path path = new Path();
        for (int i = 0; i < positions.length; i++) {
            if (positions[i].length > 1) {
                float x = positions[i][0];
                float y = positions[i][1];
                if (i == 0) {
                    path.moveTo(x, y);
                } else if (i == (positions.length - 1)) {
                    path.lineTo(x, y);
                }
            }
        }
        path.close();
        p.setStyle(Paint.Style.FILL);
        p.setStrokeWidth(3);
        canvas_des.drawPath(path, p);
        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas_des.drawBitmap(bitmap_src, 0, 0, p);
        p.setXfermode(null);
        return bitmap_des;
    }

    /**
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     */
    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    /**
     * @param pxValue
     * @return
     */
    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    /**
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        //canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public static Drawable bitmapToDrawable(Resources res, Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        return new BitmapDrawable(res, bitmap);
    }

    /**
     * @param bitmap
     * @param percent
     * @return
     */
    public static Bitmap getBitmapByPercent(Bitmap bitmap, float percent) {
        return getBitmapByPercent(bitmap, percent, percent);
    }

    public static Bitmap getBitmapByPercent(Bitmap bitmap, float percent_w, float percent_h) {
        Matrix matrix = new Matrix();
        matrix.postScale(percent_w, percent_h); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    public static Bitmap getBitmapByWH(Bitmap bitmap, int w_des, int h_des) {
        int w_bitmap = bitmap.getWidth();
        int h_bitmap = bitmap.getHeight();
        float percnet_w = (float) w_des / w_bitmap;
        float percnet_h = (float) h_des / h_bitmap;
        return getBitmapByPercent(bitmap, percnet_w, percnet_h);
    }

    public static Bitmap getBitmapById_Percent(Context context, int drawableId, int num) {
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inPreferredConfig = Bitmap.Config.RGB_565;
        if (num > 0) {
            option.inSampleSize = num;
        }
        if (bitmaps.containsKey(String.valueOf(drawableId))) {
            return bitmaps.get(String.valueOf(drawableId));
        } else {
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableId, option);
            bitmaps.put(String.valueOf(drawableId), bitmap);
            return bitmap;
        }
    }

    public static Drawable getDrawableById_byPercent(Context context, int drawableId, int percent) {
        return bitmapToDrawable(context.getResources(), getBitmapById_Percent(context, drawableId,
                percent));
    }

    public static Drawable getDrawableById(Context context, int drawableId) {
        return getDrawableById_byPercent(context, drawableId, 1);
    }

    public static Bitmap rotate(Bitmap bitmap, float angle) {
        if (bitmap == null) {
            return null;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

    public static int getIdByName(Context context, String name) {
        int resID = context.getResources().getIdentifier(name, "drawable", context.getPackageName());
        return resID;
    }

    public static byte[] bitmapToBytes(Bitmap bm) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }


    public static Bitmap getBitmapById(Context context, int drawableId) {
        return getBitmapById_Percent(context, drawableId, 1);
    }


    /**
     */
    public static String bitmapToFile(Bitmap bitmap) {
        return bitmapToFile(bitmap, Bitmap.CompressFormat.JPEG);
    }

    static int i = 0;

    /**
     */
    public static String bitmapToFile(Bitmap bitmap, Bitmap.CompressFormat compressFormat) {
        String sdPath = Environment.getExternalStorageDirectory().getPath();
        String name = new DateFormat().format("yyyyMMddhhmmss", Calendar.getInstance(Locale.CHINA)) + (i++ + ".jpg");
        String picPath = sdPath + "/" + name;
        File file = new File(picPath);
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        switch (compressFormat) {
            case PNG:
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                break;
            case JPEG:
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                break;
            case WEBP:
                bitmap.compress(Bitmap.CompressFormat.WEBP, 100, outputStream);
                break;
            default:
                SS.e("B:con not compress this bitmap");
                break;
        }
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return picPath;
    }
}
