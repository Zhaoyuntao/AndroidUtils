package com.androidutils.www.mylibrary.tools;

import android.util.Log;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Pattern;

/**
 */
public class S {

    /**
     */
    private final static String ip_regular = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))" + "\\" + ".){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))";
    /**
     */
    private final static String port_regular = "^([1-9][0-9]{0," + "3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]{1}|6553[0-5])$";
    /**
     */
    private final static String reg_url = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";
    private static final String tag = "abcd";
    private static final String tag2 = "sss";
    private static final String tag3 = "sssss";

    public static boolean isEmpty(String str) {
        return str == null || str.trim().equals("") || str.equalsIgnoreCase("null");
    }

    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    public static void s(Object text, boolean flag) {
        if (flag) {
            s(text);
        }
    }

    public static void s(String tag, Object o) {
        tag = S.tag + tag + "[" + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date(System.currentTimeMillis())) + "]";
        if (o != null) {
            Log.i(tag, o.toString());
        } else {
            Log.i(tag, "null");
        }
    }

    public static void s(Object text) {
        if (text != null) {
            Log.i(tag, text.toString());
        } else {
            Log.i(tag, "null");
        }
    }


    public static void sss(Object text) {
        if (text != null) {
            Log.i(tag2, text.toString());
        } else {
            Log.i(tag2, "null");
        }
    }

    public static void sssss(Object text) {
        if (text != null) {
            Log.i(tag3, text.toString());
        } else {
            Log.i(tag3, "null");
        }
    }

    public static void v(Object text) {
        if (text != null) {
            Log.i(tag, text.toString());
        } else {
            Log.i(tag, "null");
        }
    }

    public static void d(String text) {
        if (text != null) {
            Log.d(tag, text.toString());
        } else {
            Log.d(tag, "null");
        }
    }

    public static void e(Object text) {
        if (text != null) {
            Log.e(tag, text.toString());
        } else {
            Log.e(tag, "null");
        }
    }

    public static void e(Exception e) {

        if (e != null) {
            e.printStackTrace();
            Log.e(tag, e.toString());
        } else {
            Log.e(tag, "null");
        }
    }

    public static boolean isIp(String ip) {
        if (Pattern.compile(ip_regular).matcher(ip + "").matches()) {
            return true;
        } else {
            // e("错误的ip格式");
            return false;
        }
    }

    public static boolean isUrl(String url) {
        if (S.isEmpty(url)) {
            return false;
        }
        if (Pattern.compile(reg_url).matcher(url + "").matches()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isPort(int port) {
        if (Pattern.compile(port_regular).matcher(port + "").matches()) {
            return true;
        } else {
            return false;
        }
    }


    public static <T> T[] concat(T[] first, T[] second) {
        T[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static int[] concat(int[] first, int[] second) {
        return concat(first, second, second.length);
    }

    public static int[] concat(int[] first, int[] second, int length_second) {
        int[] result = Arrays.copyOf(first, first.length + length_second);
        System.arraycopy(second, 0, result, first.length, length_second);
        return result;
    }

    public static byte[] concat(byte[] first, byte[] second) {
        return concat(first, second, second.length);
    }

    public static byte[] concat(byte[] first, byte[] second, int length_second) {
        byte[] result = Arrays.copyOf(first, first.length + length_second);
        System.arraycopy(second, 0, result, first.length, length_second);
        return result;
    }

    public static <T> void cpArr(T[] src, int srcPos, T[] dest, int destPos, int length) {
        try {
            System.arraycopy(src, srcPos, dest, destPos, length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static byte[] copyOf(byte[] arr, int i) {
        try {
            return Arrays.copyOf(arr, i);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] copyOfRange(byte[] arr, int start, int end) {
        try {
            return Arrays.copyOfRange(arr, start, end);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String addTab(String name, int num) {

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); ) {
            if (i + num < name.length()) {
                sb.append(name.substring(i, i + num) + "\n");
                i += num;
            } else {
                sb.append(name.substring(i, name.length()));
                break;
            }
        }
        return sb.toString();
    }

    public static String a2s(byte[] arr_head) {
        return Arrays.toString(arr_head);
    }

    public static void cpArr(int[] src, int srcPos, int[] dest, int destPos, int length) {
        try {
            System.arraycopy(src, srcPos, dest, destPos, length);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void cpArr(byte[] src, int srcPos, byte[] dest, int destPos, int length) {
        try {
            System.arraycopy(src, srcPos, dest, destPos, length);
        } catch (Exception e) {
//            S.e("Arr copy err:ArryaIndexOutOfBoudsException");
//			e.printStackTrace();
        }
    }

    public static void e(String abcdef, Object e) {
        e(e);
    }

    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public static String format(long time, String formation) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formation);
        return simpleDateFormat.format(new Date(time));
    }

    public static String now() {
        return format(currentTimeMillis(), "yyyy-MM-dd hh:mm:ss:ss");
    }

    public static String now_hms() {
        return format(currentTimeMillis(), "hh:mm:ss");
    }

    public static String format(double number) {
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return decimalFormat.format(number);
    }
}
