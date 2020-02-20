
package com.zhaoyuntao.androidutils.tools;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import com.zhaoyuntao.androidutils.tools.thread.TP;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    public interface IHashState {
        public void onProgress(String file, long processed, long total);

        public void onFinish(String file, String hash);
    }

    public static String getStringMD5(String input) {
        return getBytesHash("MD5", input.getBytes());
    }

    public static String getStringSHA1(String input) {
        return getBytesHash("SHA1", input.getBytes());
    }

    public static String getStringUTF8MD5(String input) {
        try {
            return getBytesHash("MD5", input.getBytes("utf8"));
        } catch (UnsupportedEncodingException e) {
            return getBytesHash("MD5", input.getBytes());
        }
    }

    public static String getStringUTF8SHA1(String input) {
        try {
            return getBytesHash("SHA1", input.getBytes("utf8"));
        } catch (UnsupportedEncodingException e) {
            return getBytesHash("SHA1", input.getBytes());
        }
    }

    public static String getBytesMD5(byte[] input) {
        return getBytesHash("MD5", input, 0, input.length);
    }

    public static String getBytesMD5(byte[] input, int offset, int length) {
        return getBytesHash("MD5", input, offset, length);
    }

    public static String getBytesSHA1(byte[] input) {
        return getBytesHash("SHA1", input, 0, input.length);
    }

    public static String getBytesSHA1(byte[] input, int offset, int length) {
        return getBytesHash("SHA1", input, offset, length);
    }

    public static byte[] getBytesMD5Bytes(byte[] input) {
        return getBytesHashBytes("MD5", input, 0, input.length);
    }

    public static byte[] getBytesMD5Bytes(byte[] input, int offset, int length) {
        return getBytesHashBytes("MD5", input, offset, length);
    }

    public static byte[] getBytesSHA1Bytes(byte[] input) {
        return getBytesHashBytes("SHA1", input, 0, input.length);
    }

    public static byte[] getBytesSHA1Bytes(byte[] input, int offset, int length) {
        return getBytesHashBytes("SHA1", input, offset, length);
    }

    public static void updateDigest(MessageDigest md, long val) {
        byte[] vals = new byte[16]; // 8 bytes for long
        for (int i = 0; i < vals.length; i++) {
            vals[i] = (byte) ((val >> (4 * i)) & 0xff);
        }
        md.update(vals);
    }

    public static void updateDigest(MessageDigest md, int val) {
        byte[] vals = new byte[8]; // 4 bytes for long
        for (int i = 0; i < vals.length; i++) {
            vals[i] = (byte) ((val >> (4 * i)) & 0xff);
        }
        md.update(vals);
    }

    private static String getBytesHash(String algo, byte[] input) {
        return getBytesHash(algo, input, 0, input.length);
    }

    private static String getBytesHash(String algo, byte[] input, int offset, int length) {
        byte[] bytes = getBytesHashBytes(algo, input, offset, length);
        if (bytes != null) {
            return binaryToHexString(bytes);
        } else {
            return String.valueOf(input.hashCode());
        }
    }

    private static byte[] getBytesHashBytes(String algo, byte[] input, int offset, int length) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algo);
            md.reset();
            md.update(input, offset, length);

            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static void getFileMD5(String file, IHashState cb) {
        getFileHash("MD5", file, cb);
    }

    public static void getFileSHA1(String file, IHashState cb) {
        getFileHash("SHA1", file, cb);
    }

    private static void getFileHash(final String algo, final String file, final IHashState cb) {
        if (cb == null) {
            return;
        }

        final File f = new File(file);
        if (!f.exists() || !f.canRead()) {
            cb.onFinish(file, null);
            return;
        }

        TP.runOnPool(new Runnable() {
            @Override
            public void run() {
                MessageDigest md;
                try {
                    md = MessageDigest.getInstance(algo);
                    md.reset();

                    InputStream is = new FileInputStream(f);
                    byte[] buffer = new byte[4 * 1024]; // optimal size is 4K
                    long available = is.available();
                    long processed = 0;

                    int read = 0;
                    do {
                        read = is.read(buffer);
                        if (read > 0) {
                            md.update(buffer, 0, read);
                            processed += read;
                            cb.onProgress(file, processed, available);
                        }
                    } while (read > 0);

                    is.close();

                    byte[] b = md.digest();
                    cb.onFinish(file, binaryToHexString(b));
                } catch (Exception e) {
                    cb.onFinish(file, null);
                }
            }
        });
    }

    public static String binaryToHexString(byte[] messageDigest) {
        // Create Hex String
        StringBuffer hexString = new StringBuffer();
        for (int i = 0; i < messageDigest.length; i++) {
            int by = 0xFF & messageDigest[i];
            if (by < 0x10) {
                hexString.append("0" + Integer.toHexString(by));
            } else if (by >= 0x10) {
                hexString.append(Integer.toHexString(by));
            }
        }
        return hexString.toString();
    }

    private static PackageManager sPM = null;

    public static String getApkPublicKey(Context context, String algo, String pkgName, int sigID) {
        if (sPM == null) {
            sPM = context.getPackageManager();
        }
        try {
            PackageInfo pi = sPM.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
            return getBytesHash(algo, pi.signatures[sigID].toByteArray());
        } catch (Exception e) {
            return null;
        }
    }

    public static String getApkPublicKeyMD5(Context context, String pkgName) {
        return getApkPublicKey(context, "MD5", pkgName, 0);
    }

    public static String getApkPublicKeySHA1(Context context, String pkgName) {
        return getApkPublicKey(context, "SHA1", pkgName, 0);
    }

    public static byte[] getStringUTF8MD5Bytes(String input) {
        if (TextUtils.isEmpty(input)) {
            return null;
        }

        byte[] src = null;

        try {
            src = input.getBytes("utf8");
        } catch (UnsupportedEncodingException e) {
            src = input.getBytes();
        }

        return getBytesHashBytes("MD5", src, 0, src.length);
    }

    public static byte[] getStringUTF8SHA1Bytes(String input) {
        if (TextUtils.isEmpty(input)) {
            return null;
        }

        byte[] src = null;

        try {
            src = input.getBytes("utf8");
        } catch (UnsupportedEncodingException e) {
            src = input.getBytes();
        }

        return getBytesHashBytes("SHA1", src, 0, src.length);
    }
}
