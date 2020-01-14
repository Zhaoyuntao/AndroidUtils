package com.zhaoyuntao.androidutils.tools;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.zhaoyuntao.androidutils.permission.FileProvider;

import java.io.File;


public class ShareUtils {

    public static final String MIME_TEXT = "text/*";
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String MIME_TEXT_RTF = "text/rtf";
    public static final String MIME_TEXT_HTML = "text/html";
    public static final String MIME_TEXT_JSON = "text/json";

    public static final String MIME_IMAGE = "image/*";
    public static final String MIME_IMAGE_JPG = "image/jpg";
    public static final String MIME_IMAGE_PNG = "image/png";
    public static final String MIME_IMAGE_GIF = "image/gif";

    public static final String MIME_VIDEO = "video/*";
    public static final String MIME_VIDEO_MP4 = "video/mp4";
    public static final String MIME_VIDEO_3GP = "video/3gp";

    public static final String MIME_PDF = "application/pdf";

    public static final String MINE_ALL = "*/*";

    public static void shareText(Context context, String content) {
        shareText(context, null, null, content, MIME_TEXT);
    }


    /**
     * type="text/plain" 纯文本分享
     *
     * @param context
     * @param content
     * @param packageName
     */
    public static void shareText(Context context, String content, String packageName) {
        shareText(context, null, null, content, MIME_TEXT_PLAIN, packageName);
    }

    public static void shareText(Context context, String title, String subject, String content) {
        shareText(context, title, subject, content, MIME_TEXT);
    }

    public static void shareText(Context context, String title, String subject, String content, String mimeType) {
        shareText(context, title, subject, content, MIME_TEXT, null);
    }

    public static void shareText(Context context, String title, String subject, String content, String mimeType, String packageName) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        if (!TextUtils.isEmpty(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (!TextUtils.isEmpty(title)) {
            intent.putExtra(Intent.EXTRA_TITLE, title);
        }
        if (!TextUtils.isEmpty(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }

        if (!TextUtils.isEmpty(packageName)) {
            intent.setPackage(packageName);
        }

        context.startActivity(Intent.createChooser(intent, title));
    }

    public static void shareImg(Context context, Uri uri, String mimeType) {
        shareImg(context, null, null, null, uri, mimeType);
    }

    public static void shareImg(String packageName, Uri uri, Context context) {
        shareImg(context, null, null, null, uri, MIME_IMAGE, packageName);
    }


    /**
     * 分享图片 兼容 10.0
     *
     * @param context
     * @param snapshot
     */
    public static void shareImg(Context context, Bitmap snapshot) {
        Uri uri = B.bitmapToUri(snapshot, context);
        shareImg(context, uri);
    }


    public static void shareImg(Context context, Uri uri) {
        shareImg(context, null, null, null, uri, MIME_IMAGE);
    }

    public static void shareImg(Context context, String title, Uri uri) {
        shareImg(context, title, null, null, uri, MIME_IMAGE);
    }

    public static void shareImg(Context context, String title, String subject, Uri uri) {
        shareImg(context, title, subject, null, uri, MIME_IMAGE);
    }

    public static void shareImg(Context context, String title, String subject, String content, Uri uri) {
        shareImg(context, title, subject, content, uri, MIME_IMAGE);
    }

    public static void shareImg(Context context, String title, String subject, String content, Uri uri, String mimeType, String packageName) {

        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (!TextUtils.isEmpty(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (!TextUtils.isEmpty(title)) {
            intent.putExtra(Intent.EXTRA_TITLE, title);
        }
        if (!TextUtils.isEmpty(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }

        if (!TextUtils.isEmpty(packageName)) {
            intent.setPackage(packageName);
        }

        context.startActivity(Intent.createChooser(intent, title));
    }


    public static void shareVideo(Context context, File file) {
        shareImg(context, null, null, null, ShareUtils.FileToUri(context, file), MIME_VIDEO);
    }

    public static void shareVideo(Context context, Uri uri) {
        shareImg(context, null, null, null, uri, MIME_VIDEO);
    }

    public static void shareVideo(Context context, String title, Uri uri) {
        shareImg(context, title, null, null, uri, MIME_VIDEO);
    }

    public static void shareVideo(Context context, String title, String subject, Uri uri) {
        shareImg(context, title, subject, null, uri, MIME_VIDEO);
    }

    public static void shareVideo(Context context, String title, String subject, String content, Uri uri) {
        shareImg(context, title, subject, content, uri, MIME_VIDEO);
    }

    private static void shareImg(Context context, String title, String subject, String content, Uri uri, String mimeVideo) {
        shareImg(context, title, subject, content, uri, mimeVideo, null);
    }

    public static void shareVideo(Context context, String title, String subject, String content, Uri uri, String mimeType) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        if (!TextUtils.isEmpty(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (!TextUtils.isEmpty(title)) {
            intent.putExtra(Intent.EXTRA_TITLE, title);
        }
        if (!TextUtils.isEmpty(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }
        context.startActivity(Intent.createChooser(intent, title));
    }

    public static void shareFile(Context mContext, File file) {

        shareFile(mContext, null, null, null, FileToUri(mContext, file), MINE_ALL);
    }

    public static void shareFile(Context context, Uri uri) {
        shareFile(context, null, null, null, uri, MINE_ALL);
    }

    public static void shareFile(Context context, String title, Uri uri) {
        shareFile(context, title, null, null, uri, MINE_ALL);
    }

    public static void shareFile(Context context, String title, String content, Uri uri) {
        shareFile(context, title, null, content, uri, MINE_ALL);
    }

    public static void shareFile(Context context, String title, String subject, String content, Uri uri) {
        shareFile(context, title, subject, content, uri, MINE_ALL);
    }

    public static void shareFile(Context context, String title, String subject, String content, Uri uri, String mimeType) {
        if (uri == null) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType(mimeType);
        intent.putExtra(Intent.EXTRA_STREAM, uri);

        if (!TextUtils.isEmpty(subject)) {
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }
        if (!TextUtils.isEmpty(title)) {
            intent.putExtra(Intent.EXTRA_TITLE, title);
        }
        if (!TextUtils.isEmpty(content)) {
            intent.putExtra(Intent.EXTRA_TEXT, content);
        }
        context.startActivity(Intent.createChooser(intent, title));
    }

    public static Uri FileToUri(Context context, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
        } else {
            uri = Uri.fromFile(file);
        }

        return uri;
    }

}
