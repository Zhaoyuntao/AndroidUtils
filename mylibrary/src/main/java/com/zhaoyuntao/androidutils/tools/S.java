package com.zhaoyuntao.androidutils.tools;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.util.Log;


import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 */
public class S {

    public static final boolean DEBUG = true;
    /**
     * 日志tag
     */
    protected final String tag = "abcd";
    protected final String tag1 = "abcde";
    protected final String tag2 = "abcdef";
    protected final String tag3 = "abcdefg";

    private static final int I = 0;
    private static final int E = 1;
    private static final int D = 2;
    private static final int V = 3;
    private static final int L = 4;
    private static final int DEBUGD = 10;
    private static final int DEBUGE = 11;

    public static byte[] longToByteArr(long b) {
        byte[] tmp = new byte[8];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = (byte) ((b >> (8 * i)) & 0xff);
        }
        return tmp;
    }

    public static long byteArrToLong(byte[] c) {
        long tmp = 0;
        if (c != null && c.length == 8) {
            for (int i = 0; i < 8; i++) {
                tmp |= ((c[i] & 0xff) << (8 * i));
            }
        }
        return tmp;
    }

    public static byte[] intToByteArr(int b) {
        byte[] tmp = new byte[4];
        for (int i = 0; i < tmp.length; i++) {
            tmp[i] = (byte) ((b >> (8 * i)) & 0xff);
        }
        return tmp;
    }

    public static int byteArrToInt(byte[] c) {
        int tmp = 0;
        if (c != null && c.length == 4) {
            for (int i = 0; i < 4; i++) {
                tmp |= ((c[i] & 0xff) << (8 * i));
            }
        }
        return tmp;
    }

    /**
     * log记录相关----------------------------------------------------------------------------
     */
    public static class LogItem {
        public long time;
        public String log;
        public int type;

        public LogItem(String log) {
            this(log, I);
        }

        public LogItem(String log, int type) {
            this.log = log;
            this.time = S.currentTimeMillis();
            this.type = type;
        }

        @Override
        public String toString() {
            return log;
        }
    }

    private static S s;
    /**
     * 日志缓存
     */
    private List<LogItem> list = new ArrayList<>();
    /**
     * 最大缓存数
     */
    private int maxSize = 2000;

    public static CallBack callBack;

    private S() {

    }

    public interface CallBack {
        void whenLog(LogItem logItem);
    }

    private static class SHandler extends Handler {
        private final WeakReference<CallBack> callBackWeakReference;

        public SHandler(CallBack callBack) {
            callBackWeakReference = new WeakReference<>(callBack);
        }

        @Override
        public void handleMessage(Message msg) {

            if (msg.obj instanceof LogItem) {
                LogItem logItem = (LogItem) msg.obj;
                CallBack callBack = callBackWeakReference.get();
                if (callBack != null) {
                    callBack.whenLog(logItem);
                }
            }
        }
    }

    ;

    private SHandler sHandler = new SHandler(callBack);

    private static S getS() {
        synchronized (S.class) {
            if (s == null) {
                synchronized (S.class) {
                    s = new S();
                }
            }
        }
        return s;
    }

    /**
     * 日志开关
     */
    private boolean flag = true;
    /**
     * 日志缓存开关
     */
    private boolean cache = true;

    /**
     * 设置日志开关
     *
     * @param flag
     */
    public static void setFlag(boolean flag) {
        getS().flag = flag;
    }

    /**
     * 设置日志缓存开关
     *
     * @param cache
     */
    public static void setCache(boolean cache) {
        getS().cache = cache;
    }

    /**
     * 设置日志最大缓存数
     *
     * @param maxSize
     */
    public static void setMaxSize(int maxSize) {
        getS().maxSize = maxSize;
    }

    /**
     * 清空日志缓存
     */
    public static void clearLog() {
        synchronized (Lock.class) {
            getS().list.clear();
        }
    }

    private void log(String tag, Object o, int depth, int type) {
        if (o == null) {
            o = "null";
        } else if (o instanceof Exception) {
            o = ((Exception) (o)).getMessage();
            if (o == null) {
                o = "null";
            }
        }
        String tagTmp = "|" + tag + "|   ";

        final int depthDefault = 3;

        final Throwable t = new Throwable();
        final StackTraceElement[] elements = t.getStackTrace();

        if (depth > 20) {
            depth = 20;
        }
        int depthNow = depthDefault + depth + 1;
        String usingSourceL = "";
        int offsetSpaceCount = 0;

        while (depthNow-- > depthDefault) {
            if (elements.length <= depthNow) {
                continue;
            }

            StackTraceElement[] traceElements = Thread.currentThread().getStackTrace();
            StringBuilder taskName = new StringBuilder();
            if (traceElements.length > 6) {
                StackTraceElement traceElement = traceElements[depthNow + 2];
                taskName.append("(").append(traceElement.getFileName()).append(":").append(traceElement.getLineNumber()).append(")  ");
                taskName.append(traceElement.getMethodName());
                if (depthNow == depthDefault) {
                    usingSourceL = taskName.toString();
                }
            } else {
                String callerClassName = elements[depthNow].getClassName();
                String callerMethodName = elements[depthNow].getMethodName();
                String callerLineNumber = String.valueOf(elements[depthNow].getLineNumber());

                int pos = callerClassName.lastIndexOf('.');
                if (pos >= 0) {
                    callerClassName = callerClassName.substring(pos + 1);
                }

                taskName.append("<").append(callerClassName).append(".").append(callerMethodName).append(" ").append(callerLineNumber).append("> ");
                if (depthNow == depthDefault) {
                    usingSourceL = "(" + callerClassName + ".java:" + callerLineNumber + ")";
                }
            }
            if (depth > 0) {
                if (offsetSpaceCount > 0) {
                    taskName.insert(0, "∟");
                } else {
                    taskName.insert(0, " ");
                }
            }
            for (int i = 0; i < offsetSpaceCount; i++) {
                taskName.insert(0, "  ");
            }
            offsetSpaceCount++;
            if (type != L) {
                Log.d(tagTmp, taskName.toString());
            }
        }
        if (offsetSpaceCount == 1) {
            offsetSpaceCount = 0;
        }
        StringBuilder offset = new StringBuilder();
        for (int i = 0; i < offsetSpaceCount; i++) {
            offset.insert(0, "  ");
        }
        switch (type) {
            case E:
                Log.e(tagTmp, offset + o.toString());
                break;
            case D:
                Log.i(tagTmp, offset + o.toString());
                break;
            case L:
                Log.i(tagTmp, o.toString() + "    " + usingSourceL + "");
                break;
        }

        LogItem logItem = new LogItem(String.valueOf(o), type);
        addLogSelf(tag, logItem);
    }

    private void addLogSelf(String tag, LogItem logItem) {
        if (logItem != null) {
            synchronized (Lock.class) {
                if (cache) {
                    if (maxSize > 0 && list.size() > maxSize) {
                        list.remove(0);
                    }
                    s.list.add(logItem);
                }
            }
            if (isNotEmpty(tag) && (logItem.type == DEBUGE || logItem.type == DEBUGD)) {
                Message message = new Message();
                message.obj = logItem;
                sHandler.sendMessage(message);
            }
        }
    }

    public static void addLog(String tag, LogItem logItem) {
        getS().addLogSelf(tag, logItem);
    }

    /**
     * 获取log缓存
     *
     * @return
     */
    public static List<LogItem> getLogCache() {
        List<LogItem> logItems = new ArrayList<>();
        synchronized (Lock.class) {
            for (int i = 0; i < logItems.size(); i++) {
                logItems.add(s.list.get(i));
            }
        }
        return logItems;
    }

    class Lock {
    }
    //私有log函数--------------------------------------

    private void s_self(Object o, int type) {
        log(tag, o, 0, type);
    }

    private void s_self(String tag, Object o, int type) {
        log(tag, o, 0, type);
    }

    private void ss_self(Object o, int type) {
        log(tag1, o, 1, type);
    }

    private void sss_self(Object o, int type) {
        log(tag2, o, 2, type);
    }

    private void ssss_self(Object o, int type) {
        log(tag3, o, 3, type);
    }

    private void e_self(Object o, int type) {
        log(tag, o, 0, type);
    }

    private void e_self(String tag, Object o, int type) {
        log(tag, o, 0, type);
    }

    private void ee_self(Object o, int type) {
        log(tag1, o, 1, type);
    }

    private void eee_self(Object o, int type) {
        log(tag2, o, 2, type);
    }

    private void eeee_self(Object o, int type) {
        log(tag3, o, 3, type);
    }

    private void s_self_custom(Object o, int depth, int type) {
        log(tag, o, depth, type);
    }

    private void e_self_custom(Object o, int depth, int type) {
        log(tag, o, depth, type);
    }


    //对外提供------------------------------------------
    //normal log just show

    public static void l() {
        getS().s_self("----------------", D);
    }

    public static void ll() {
        getS().s_self("--------------------------------", D);
    }

    public static void lll() {
        getS().s_self("----------------------------------------------------------------", D);
    }

    public static void s(Object o) {
        getS().s_self(o, D);
    }

    public static void sd(Object o, int depth) {
        getS().s_self_custom(o, depth, D);
    }

    public static void sd(Object o) {
        getS().s_self_custom(o, 20, D);
    }

    public static void s(String tag, Object o) {
        getS().s_self(tag, o, D);
    }

    public static void ss(Object o) {
        getS().ss_self(o, D);
    }

    public static void sss(Object o) {
        getS().sss_self(o, D);
    }

    public static void ssss(Object o) {
        getS().ssss_self(o, D);
    }

    public static void s_d(Object o) {
        getS().s_self(o, DEBUGD);
    }

    //debug log for callback using.

    public static void s_d(String tag, Object o) {
        getS().s_self(tag, o, DEBUGD);
    }

    public static void ss_d(Object o) {
        getS().ss_self(o, DEBUGD);
    }

    public static void sss_d(Object o) {
        getS().sss_self(o, DEBUGD);
    }

    public static void ssss_d(Object o) {
        getS().ssss_self(o, DEBUGD);
    }

    //normal log just show

    public static void e(Object o) {
        getS().e_self(o, E);
    }

    public static void ed(Object o, int depth) {
        getS().e_self_custom(o, depth, E);
    }

    public static void ed(Object o) {
        getS().e_self_custom(o, 20, E);
    }

    public static void e(String tag, Object o) {
        getS().e_self(tag, o, E);
    }

    public static void ee(Object o) {
        getS().ee_self(o, E);
    }

    public static void eee(Object o) {
        getS().eee_self(o, E);
    }

    public static void eeee(Object o) {
        getS().eeee_self(o, E);
    }

    //debug log for callback using.

    public static void e_d(Object o) {
        getS().e_self(o, DEBUGE);
    }

    public static void e_d(String tag, Object o) {
        getS().e_self(tag, o, DEBUGE);
    }

    public static void ee_d(Object o) {
        getS().ee_self(o, DEBUGE);
    }

    public static void eee_d(Object o) {
        getS().eee_self(o, DEBUGE);
    }

    public static void eeee_d(Object o) {
        getS().eeee_self(o, DEBUGE);
    }

    //------------------------------------------------------其他功能-------------------------------------------------------------
    /**
     * ip正则表达式
     */
    private final static String ip_regular = "((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))" + "\\" + ".){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))";
    /**
     * 端口号正则表达式
     */
    private final static String port_regular = "^([1-9][0-9]{0," + "3}|[1-5][0-9]{4}|6[0-4][0-9]{3}|65[0-4][0-9]{2}|655[0-2][0-9]{1}|6553[0-5])$";
    /**
     * url网址正则表达式
     */
    private final static String reg_url = "(http|ftp|https):\\/\\/[\\w\\-_]+(\\.[\\w\\-_]+)+([\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])?";

    /**
     * @param str
     * @return
     */
    public static boolean isEmpty(CharSequence str) {
        return TextUtils.isEmpty(str);
    }

    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    public static boolean isIp(String ip) {
        if (S.isEmpty(ip)) {
            return false;
        }
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

    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * local to utc
     *
     * @return
     */
    public static String local2UTC() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("gmt"));
        String gmtTime = sdf.format(new Date());
        return gmtTime;
    }

    /**
     * utc to local
     *
     * @param utcTime
     * @return
     */
    public static String utc2Local(String utcTime) {
        SimpleDateFormat utcFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//UTC
        utcFormater.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date gpsUTCDate = null;
        try {
            gpsUTCDate = utcFormater.parse(utcTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        SimpleDateFormat localFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//local
        localFormater.setTimeZone(TimeZone.getDefault());
        String localTime = localFormater.format(gpsUTCDate.getTime());
        return localTime;
    }

    public static long currentTimeSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public static String formatDate(long time, String formation) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(formation);
        return simpleDateFormat.format(new Date(time));
    }

    public static String now() {
        return formatDate(currentTimeMillis(), "yyyy-MM-dd hh:mm:ss:ss");
    }

    public static String now_hms() {
        return formatDate(currentTimeMillis(), "hh:mm:ss");
    }

    public static String formatNumber(double number) {
        return formatNumber(number, "#.#");
    }

    public static String formatNumber(double number, String formation) {
        DecimalFormat decimalFormat = new DecimalFormat(formation);
        return decimalFormat.format(number);
    }

    public static String getRegexString(String srcString, String regexString) {
        Pattern pattern = Pattern.compile(regexString);
        Matcher m = pattern.matcher(srcString);
        while (m.find()) {
            return m.group(1);
        }
        return "";
    }


    public static String format(String dateSrc, String format_src, String format_des) {
        if (S.isEmpty(dateSrc) || S.isEmpty(format_src) || S.isEmpty(format_des)) {
            return null;
        }
        SimpleDateFormat simpleDateFormat_src = new SimpleDateFormat(format_src);
        SimpleDateFormat simpleDateFormat_des = new SimpleDateFormat(format_des);
        try {
            Date date_src = simpleDateFormat_src.parse(dateSrc);
            return simpleDateFormat_des.format(date_src);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * more days of date2 than date1
     *
     * @return
     */
    public static long differentDays(String day_small, String day_big) {
        if (day_small == null || day_big == null || !Pattern.compile("\\d{8}").matcher(day_small).matches() || !Pattern.compile("\\d{8}").matcher(day_big).matches()) {
            return -1;
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        long count = 0;
        try {
            Date date_before = simpleDateFormat.parse(day_small);
            Date date_after = simpleDateFormat.parse(day_big);
            count = (date_after.getTime() - date_before.getTime()) / 86400000;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return count;
    }
}
