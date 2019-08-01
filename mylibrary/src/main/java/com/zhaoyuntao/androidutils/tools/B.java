package com.zhaoyuntao.androidutils.tools;

import android.app.Activity;
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
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.format.DateFormat;
import android.view.Display;
import android.view.WindowManager;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

/**
 *
 */
public class B {
    public static String path_system = Environment.getExternalStorageDirectory().getAbsolutePath();

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

    /**
     * int颜色转16进制字符串格式
     *
     * @param color
     * @return
     */
    public static String toHexColorString(int color) {
        StringBuffer sb = new StringBuffer();
        String R = Integer.toHexString(Color.red(color));
        String G = Integer.toHexString(Color.green(color));
        String B = Integer.toHexString(Color.blue(color));
        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;
        sb.append("#");
        sb.append(R);
        sb.append(G);
        sb.append(B);
        return sb.toString();
    }

    /**
     * 字符串是否符合16进制颜色格式
     *
     * @param hexColorString
     * @return
     */
    public static boolean isLegalHexColorString(String hexColorString) {
        String rex = "^#([0-9a-fA-F]{6})|#([0-9a-fA-F]{8})$";
        return Pattern.compile(rex).matcher(hexColorString).matches();
    }

    /**
     * 16进制颜色字符串转int
     *
     * @param hexColorString
     * @return
     */
    public static int hexColorToIntColor(String hexColorString) {
        if (isLegalHexColorString(hexColorString)) {
            return Color.parseColor(hexColorString);
        } else {
            return Color.WHITE;
        }
    }


    /**
     * 镜像翻转图片
     *
     * @param bitmap
     * @param i      0:HORIZONTAL other:VERTICAL
     * @return
     */
    public static Bitmap reverse(Bitmap bitmap, int i) {
        Matrix m = new Matrix();
        if (i == 0) {
            m.setScale(-1, 1);//水平翻转
        } else {
            m.setScale(1, -1);//垂直翻转
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        //生成的翻转后的bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, m, true);
    }

    /**
     * 将图片修剪成一个给定size的新图片,不会失真, 但是会被裁剪, 原始图片先缩放,然后再裁剪
     *
     * @param w
     * @param h
     * @return
     */
    public static Bitmap clip(int w, int h, Bitmap bitmap) {
        float propprtion_src = (float) bitmap.getWidth() / bitmap.getHeight();
        float propprtion_des = (float) w / h;
        //取材宽度
        int h_tmp = 0;
        int w_tmp = 0;
        //宽图片变成窄图片
        if (propprtion_src > propprtion_des) {
            h_tmp = bitmap.getHeight();
            w_tmp = (int) (h_tmp * propprtion_des);
        } else {
            w_tmp = bitmap.getWidth();
            h_tmp = (int) (w_tmp / propprtion_des);
        }
        //计算需要裁剪的区域的x开始坐标和y开始坐标
        int x_start = (int) ((bitmap.getWidth() - w_tmp) / 2f);
        int y_start = (int) ((bitmap.getHeight() - h_tmp) / 2f);
        //进行裁剪
        Bitmap bitmap_des = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap_des);

        Rect rect_draw = new Rect();
        rect_draw.set(0, 0, w, h);

        Rect rect_src = new Rect();
        rect_src.set(x_start, y_start, x_start + w_tmp, y_start + h_tmp);

        Paint paint = new Paint();
        canvas.drawBitmap(bitmap, rect_src, rect_draw, paint);

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
     *
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     *
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

    public static int getColor(int colorId, Context context) {
        return ContextCompat.getColor(context, colorId);
    }

    /**
     * 随机获取一个颜色值
     *
     * @return
     */
    public static int getRandomColor() {
        return Color.rgb(new Random().nextInt(256), new Random().nextInt(256), new Random().nextInt(256));
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
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), drawableId, option);
        return bitmap;
    }

    public static Drawable getDrawableById_byPercent(Context context, int drawableId, int percent) {
        return bitmapToDrawable(context.getResources(), getBitmapById_Percent(context, drawableId, percent));
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
        byte[] data = baos.toByteArray();
        try {
            baos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    public static Bitmap bytesToBitmap(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }


    public static void saveBitmap_JPEG(Bitmap bitmap, String bitmapName, String dirChild, int quality) {
        saveBitmap(bitmap, bitmapName, dirChild, Bitmap.CompressFormat.JPEG, quality);
    }

    public static void saveBitmap_PNG(Bitmap bitmap, String bitmapName, String dirChild, int quality) {
        saveBitmap(bitmap, bitmapName, dirChild, Bitmap.CompressFormat.PNG, quality);
    }

    public static void saveBitmap(Bitmap bitmap, String bitmapName, String dirChild, Bitmap.CompressFormat compressFormat, int quality) {
        if (bitmap == null || S.isEmpty(bitmapName)) {
            return;
        }
        bitmapName = bitmapName.trim();
        switch (compressFormat) {
            case PNG:
                if (!bitmapName.endsWith(".png")) {
                    bitmapName += ".png";
                }
                break;
            case JPEG:
                if (!bitmapName.endsWith(".jpg")) {
                    bitmapName += ".jpg";
                }
                break;
            default:
                S.e("B.saveBitmap:err:不支持的图片格式!" + compressFormat);
                return;
        }
        dirChild = dirChild.trim();
        if (dirChild.startsWith("/")) {
            dirChild = dirChild.substring(1);
        }
        if (dirChild.endsWith("/")) {
            dirChild = dirChild.substring(dirChild.length() - 1);
        }
        String path_pic = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + dirChild + "/";
        File dir = new File(path_pic);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        bitmapName = bitmapName.trim();
        if (bitmapName.startsWith("/")) {
            bitmapName = bitmapName.substring(1);
        }
        if (bitmapName.endsWith("/")) {
            bitmapName = bitmapName.substring(bitmapName.length() - 1);
        }

        File f = new File(path_pic, bitmapName);
        if (f.exists()) {
            f.delete();
            try {
                f.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            FileOutputStream out = new FileOutputStream(f);
            bitmap.compress(compressFormat, quality, out);
            out.flush();
            out.close();
            S.s("bitmap[" + f.getAbsolutePath() + "]保存成功");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            S.e(e);
        } catch (IOException e) {
            e.printStackTrace();
            S.e(e);
        }
    }

    public static void getBitmapByThread(String filename, CallBack callBack) {
        BitmapReader bitmapReader = new BitmapReader(filename, callBack);
        bitmapReader.start();
    }


    public static Bitmap compress(Bitmap bitmap, int kbs) {
        int size_bitmap = bitmap.getByteCount();
        S.s("压缩前:" + bitmap.getByteCount());
        int maxSize = kbs * 1024;
        S.s("要求大小:" + maxSize);
        //计算压缩比例
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        if (kbs <= 0) {
            options.inSampleSize = 1;
        } else {
            float percent = ((float) size_bitmap) / maxSize;
            S.s("percent:" + percent);
            int sampleSize = (int) Math.sqrt(percent) + 1;
            S.s("sampleSize:" + sampleSize);
            options.inSampleSize = sampleSize;
        }
        options.inJustDecodeBounds = false;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] data = byteArrayOutputStream.toByteArray();
        try {
            byteArrayOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bitmap bitmap2 = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        S.s("压缩后:" + bitmap2.getByteCount() + " 压缩比:" + (float) size_bitmap / bitmap2.getByteCount() + "  w:" + bitmap2.getWidth() + " h:" + bitmap2.getHeight());
        return bitmap2;
    }

    public static Bitmap getBitmap(String bitmapName) {
        bitmapName = bitmapName.trim();
        if (bitmapName.startsWith("/")) {
            bitmapName = bitmapName.substring(1);
        }
        if (bitmapName.endsWith("/")) {
            bitmapName = bitmapName.substring(bitmapName.length() - 1);
        }
        if (S.isNotEmpty(bitmapName)) {
            S.s("getBitmap:正在读取文件:" + path_system + "/" + bitmapName);
            Bitmap bitmap = BitmapFactory.decodeFile(path_system + "/" + bitmapName);
            return bitmap;
        } else {
            S.e("getBitmap:face.bitmapName is null");
        }
        return null;
    }

    public interface CallBack {

        void whenBitmapReady(Bitmap bitmap);
    }

    public static Bitmap getBitmapById(Context context, int drawableId) {
        return getBitmapById_Percent(context, drawableId, 1);
    }


    /**
     *
     */
    public static String bitmapToFile(Bitmap bitmap) {
        return bitmapToFile(bitmap, Bitmap.CompressFormat.JPEG);
    }

    static int i = 0;

    /**
     *
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
                S.e("B:con not compress this bitmap");
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

    public static int[] getScreenWH(Context context) {
        WindowManager windowmanager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display d = windowmanager.getDefaultDisplay();
        Point p = new Point();
        d.getSize(p);
        int w_screen = p.x; // 屏幕宽（像素）
        int h_screen = p.y;
        return new int[]{w_screen, h_screen};
    }

    /**
     * 通过uri获取图片并进行压缩
     *
     * @param uri
     */
    public static Bitmap getBitmapFormUri(Activity ac, Uri uri) throws IOException {
        InputStream input = ac.getContentResolver().openInputStream(uri);
        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();
        int originalWidth = onlyBoundsOptions.outWidth;
        int originalHeight = onlyBoundsOptions.outHeight;
        if ((originalWidth == -1) || (originalHeight == -1)) return null;
        //图片分辨率以480x800为标准
        float hh = 800f;//这里设置高度为800f
        float ww = 480f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (originalWidth > originalHeight && originalWidth > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (originalWidth / ww);
        } else if (originalWidth < originalHeight && originalHeight > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (originalHeight / hh);
        }
        if (be <= 0) be = 1;
        //比例压缩
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = be;//设置缩放比例
        bitmapOptions.inDither = true;//optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        input = ac.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();

        return compressImage(bitmap);//再进行质量压缩
    }

    /**
     * 质量压缩方法:100kb
     *
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {  //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            //第一个参数 ：图片格式 ，第二个参数： 图片质量，100为最高，0为最差  ，第三个参数：保存压缩后的数据的流
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
}
