package com.zhaoyuntao.androidutils.tools;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.XmlResourceParser;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import com.zhaoyuntao.androidutils.tools.thread.ZThread;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.START_TAG;

public class F {

    /**
     * 同步拆分文件
     *
     * @param file
     * @param size
     * @return
     */
    public static byte[][] slipFile(File file, int size) {
        byte[][] packages = new byte[0][];
        try {
            RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");
            while (true) {
                byte[] data = new byte[size];
                int num = randomAccessFile.read(data);
                if (num == -1) {
                    break;
                }
                if (num < data.length) {
                    data = Arrays.copyOf(data, num);
                }
                packages = Arrays.copyOf(packages, packages.length + 1);
                packages[packages.length - 1] = data;
            }
//            S.s("文件大小:" + count);
//            S.s("拆分总数:" + packages.length + " 每份:" + size + " 总和:" + (packages.length * size - tail));
            randomAccessFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }
        return packages;
    }


    /**
     * 异步拆分文件
     *
     * @param file
     * @param size
     * @param callBack
     */
    public static ZThread slipFile(final File file, final int size, final CallBack callBack) {
        ZThread zThread = new ZThread() {
            private FileInputStream fileInputStream;
            private RandomAccessFile randomAccessFile;
            private int count;
            private int index;
            private int position;
            private int filesize;

            @Override
            protected void init() {
                try {
                    fileInputStream = new FileInputStream(file);
                    filesize = fileInputStream.available();
//                    S.s("文件大小:" + filesize);
                    fileInputStream.close();
                    count = filesize / size;
                    if (filesize % size > 0) {
                        count += 1;//尾部的余数也算一个片段
                    }

                    randomAccessFile = new RandomAccessFile(file, "r");
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected void todo() {
                byte[] data = new byte[size];
                int num = 0;
                try {
                    num = randomAccessFile.read(data);
                    if (num == -1) {
                        close();
                        return;
                    }
                    if (num < data.length) {
                        data = Arrays.copyOf(data, num);
                    }
//                    S.s("得到第[" + index + "/" + count + "]个片段,大小:" + num + "写入位置计算:" + position);
                    if (callBack != null) {
                        callBack.whenGotPiece(Arrays.copyOf(data, data.length), index++, count, position, filesize, this);
                    }
                    position += num;
                } catch (IOException e) {
                    e.printStackTrace();
                    close();
                }
            }

            @Override
            public void close() {
                if (randomAccessFile != null) {
                    try {
                        randomAccessFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                S.s("end , zthread.close");
                super.close();
            }
        };
        zThread.start();
        return zThread;
    }


    /**
     * 拆分byte arr,同步
     *
     * @param data
     * @param size
     * @return
     */
    public static byte[][] slipByteArrays(byte[] data, int size) {
        int length = data.length;
        int onePiece = size;
        int count = length / onePiece;
        if (length % onePiece > 0) {
            count++;
        }
        byte[][] packages = new byte[count][];
        for (int i = 0; i < packages.length; i++) {
            byte[] tmp = null;
            if (i == packages.length - 1) {
                tmp = Arrays.copyOfRange(data, i * onePiece, data.length);
            } else {
                tmp = Arrays.copyOfRange(data, i * onePiece, (i + 1) * onePiece);
            }
            packages[i] = tmp;
        }
        return packages;
    }

    /**
     * 组合byte arr,同步
     *
     * @param datas
     * @return
     */
    public static byte[] assembleByteArrays(byte[][] datas) {
        byte[] data = new byte[0];

        for (int i = 0; i < datas.length; i++) {
            byte[] tmp = datas[i];
            if (tmp != null) {
                data = Arrays.copyOf(data, data.length + tmp.length);
                System.arraycopy(tmp, 0, data, data.length - tmp.length, tmp.length);
            }
        }
        return data;
    }


    /**
     * 组合文件,异步
     *
     * @param path
     * @param filename
     * @param datas
     * @param result
     */
    public static void assembleFile(final String path, final String filename, final byte[][] datas, final Result result) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String pathTmp = path;
                String filenameTmp = filename;
                byte[] fileBytes = assembleByteArrays(datas);
                if (pathTmp.startsWith("/")) {
                    pathTmp = pathTmp.substring(1);
                }
                if (pathTmp.endsWith("/")) {
                    pathTmp = pathTmp.substring(pathTmp.length() - 1);
                }
                if (filenameTmp.startsWith("/")) {
                    filenameTmp = filenameTmp.substring(1);
                }
                if (filenameTmp.endsWith("/")) {
                    filenameTmp = filenameTmp.substring(filenameTmp.length() - 1);
                }
                File file = new File(pathTmp, filenameTmp);
                FileOutputStream fileOutputStream = null;
                try {
                    fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(fileBytes);
                    fileOutputStream.flush();
                    if (result != null) {
                        result.whenSuccess();
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    if (result != null) {
                        result.whenFailed(e.getMessage());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    if (result != null) {
                        result.whenFailed(e.getMessage());
                    }
                } finally {
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();

    }


    public interface CallBack {
        void whenGotPiece(byte[] data, int index, int count, int position, long filesize, ZThread zThread);

    }

    public interface FileInfo {
        void whenGotFileInfo(byte[] info);
    }

    public interface Result {
        void whenSuccess();

        void whenFailed(String msg);
    }

    private static final String META_DATA_FILE_PROVIDER_PATHS = "android.support.FILE_PROVIDER_PATHS";
    private static final HashMap<String, PathStrategy> sCache = new HashMap<>();

    public static Uri getUriForFile(Context context, String authority, File file) {
        final PathStrategy strategy = getPathStrategy(context, authority);
        return strategy.getUriForFile(file);
    }

    private static PathStrategy getPathStrategy(Context context, String authority) {
        PathStrategy strategy;
        synchronized (sCache) {
            strategy = sCache.get(authority);
            if (strategy == null) {
                try {
                    strategy = parsePathStrategy(context, authority);
                } catch (IOException e) {
                    throw new IllegalArgumentException(
                            "Failed to parse " + META_DATA_FILE_PROVIDER_PATHS + " meta-data", e);
                } catch (XmlPullParserException e) {
                    throw new IllegalArgumentException(
                            "Failed to parse " + META_DATA_FILE_PROVIDER_PATHS + " meta-data", e);
                }
                sCache.put(authority, strategy);
            }
        }
        return strategy;
    }

    private static final String ATTR_NAME = "name";
    private static final String ATTR_PATH = "path";
    private static final String TAG_ROOT_PATH = "root-path";
    private static final String TAG_FILES_PATH = "files-path";
    private static final String TAG_CACHE_PATH = "cache-path";
    private static final String TAG_EXTERNAL = "external-path";
    private static final String TAG_EXTERNAL_FILES = "external-files-path";
    private static final String TAG_EXTERNAL_CACHE = "external-cache-path";
    private static final String TAG_EXTERNAL_MEDIA = "external-media-path";
    private static final File DEVICE_ROOT = new File("/");

    private static PathStrategy parsePathStrategy(Context context, String authority)
            throws IOException, XmlPullParserException {
        final SimplePathStrategy strategy = new SimplePathStrategy(authority);
        final ProviderInfo info = context.getPackageManager()
                .resolveContentProvider(authority, PackageManager.GET_META_DATA);
        final XmlResourceParser in = info.loadXmlMetaData(context.getPackageManager(), META_DATA_FILE_PROVIDER_PATHS);
        if (in == null) {
            throw new IllegalArgumentException("Missing " + META_DATA_FILE_PROVIDER_PATHS + " meta-data");
        }

        int type;
        while ((type = in.next()) != END_DOCUMENT) {
            if (type == START_TAG) {
                final String tag = in.getName();

                final String name = in.getAttributeValue(null, ATTR_NAME);
                String path = in.getAttributeValue(null, ATTR_PATH);

                File target = null;
                if (TAG_ROOT_PATH.equals(tag)) {
                    target = DEVICE_ROOT;
                } else if (TAG_FILES_PATH.equals(tag)) {
                    target = context.getFilesDir();
                } else if (TAG_CACHE_PATH.equals(tag)) {
                    target = context.getCacheDir();
                } else if (TAG_EXTERNAL.equals(tag)) {
                    target = Environment.getExternalStorageDirectory();
                } else if (TAG_EXTERNAL_FILES.equals(tag)) {
                    File[] externalFilesDirs = getExternalFilesDirs(context, null);
                    if (externalFilesDirs.length > 0) {
                        target = externalFilesDirs[0];
                    }
                } else if (TAG_EXTERNAL_CACHE.equals(tag)) {
                    File[] externalCacheDirs = getExternalCacheDirs(context);
                    if (externalCacheDirs.length > 0) {
                        target = externalCacheDirs[0];
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && TAG_EXTERNAL_MEDIA.equals(tag)) {
                    File[] externalMediaDirs = context.getExternalMediaDirs();
                    if (externalMediaDirs.length > 0) {
                        target = externalMediaDirs[0];
                    }
                }

                if (target != null) {
                    strategy.addRoot(name, buildPath(target, path));
                }
            }
        }

        return strategy;
    }

    static class SimplePathStrategy implements PathStrategy {

        private final String mAuthority;
        private final HashMap<String, File> mRoots = new HashMap<String, File>();

        SimplePathStrategy(String authority) {
            mAuthority = authority;
        }

        void addRoot(String name, File root) {
            if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("Name must not be empty");
            }

            try {
                root = root.getCanonicalFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to resolve canonical path for " + root, e);
            }

            mRoots.put(name, root);
        }

        @Override
        public Uri getUriForFile(File file) {
            String path;
            try {
                path = file.getCanonicalPath();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to resolve canonical path for " + file);
            }

            Map.Entry<String, File> mostSpecific = null;
            for (Map.Entry<String, File> root : mRoots.entrySet()) {
                final String rootPath = root.getValue().getPath();
                boolean invalidMost = mostSpecific == null ||
                        rootPath.length() > mostSpecific.getValue().getPath().length();
                if (path.startsWith(rootPath) && invalidMost) {
                    mostSpecific = root;
                }
            }

            if (mostSpecific == null) {
                throw new IllegalArgumentException("Failed to find configured root that contains " + path);
            }

            final String rootPath = mostSpecific.getValue().getPath();
            if (rootPath.endsWith("/")) {
                path = path.substring(rootPath.length());
            } else {
                path = path.substring(rootPath.length() + 1);
            }

            path = Uri.encode(mostSpecific.getKey()) + '/' + Uri.encode(path, "/");
            return new Uri.Builder().scheme("content").authority(mAuthority).encodedPath(path).build();
        }

        @Override
        public File getFileForUri(Uri uri) {
            String path = uri.getEncodedPath();

            final int splitIndex = path.indexOf('/', 1);
            final String tag = Uri.decode(path.substring(1, splitIndex));
            path = Uri.decode(path.substring(splitIndex + 1));

            final File root = mRoots.get(tag);
            if (root == null) {
                throw new IllegalArgumentException("Unable to find configured root for " + uri);
            }

            File file = new File(root, path);
            try {
                file = file.getCanonicalFile();
            } catch (IOException e) {
                throw new IllegalArgumentException("Failed to resolve canonical path for " + file);
            }

            if (!file.getPath().startsWith(root.getPath())) {
                throw new SecurityException("Resolved path jumped beyond configured root");
            }
            return file;
        }
    }

    private static int modeToMode(String mode) {
        int modeBits;
        if ("r".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_ONLY;
        } else if ("w".equals(mode) || "wt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE |
                    ParcelFileDescriptor.MODE_TRUNCATE;
        } else if ("wa".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_WRITE_ONLY | ParcelFileDescriptor.MODE_CREATE |
                    ParcelFileDescriptor.MODE_APPEND;
        } else if ("rw".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE;
        } else if ("rwt".equals(mode)) {
            modeBits = ParcelFileDescriptor.MODE_READ_WRITE | ParcelFileDescriptor.MODE_CREATE |
                    ParcelFileDescriptor.MODE_TRUNCATE;
        } else {
            throw new IllegalArgumentException("Invalid mode: " + mode);
        }
        return modeBits;
    }

    private static File buildPath(File base, String... segments) {
        File cur = base;
        for (String segment : segments) {
            if (segment != null) {
                cur = new File(cur, segment);
            }
        }
        return cur;
    }

    private static String[] copyOf(String[] original, int newLength) {
        final String[] result = new String[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    private static Object[] copyOf(Object[] original, int newLength) {
        final Object[] result = new Object[newLength];
        System.arraycopy(original, 0, result, 0, newLength);
        return result;
    }

    private static File[] getExternalFilesDirs(Context context, String type) {
        if (Build.VERSION.SDK_INT >= 19) {
            return context.getExternalFilesDirs(type);
        } else {
            return new File[]{context.getExternalFilesDir(type)};
        }
    }

    public static File[] getExternalCacheDirs(Context context) {
        if (Build.VERSION.SDK_INT >= 19) {
            return context.getExternalCacheDirs();
        } else {
            return new File[]{context.getExternalCacheDir()};
        }
    }

    interface PathStrategy {

        Uri getUriForFile(File file);

        File getFileForUri(Uri uri);
    }
}
