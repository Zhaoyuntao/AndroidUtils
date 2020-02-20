package com.zhaoyuntao.androidutils.tools.device;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Point;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import androidx.annotation.RequiresPermission;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Locale;
import java.util.regex.Pattern;

public class DevicesUtils {
    public static boolean legacyDevices() {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.GINGERBREAD_MR1;
    }

    public static boolean modernDevices() {
        return Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1;
    }

    public static boolean honeyCombDevices() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean lollipopDevices() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static boolean kitkatDevices() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean jellyBeanMR1Devices() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    public static boolean jellyBeanMR2Devices() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    public static boolean jellyBeanDevices() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean marshmallowDevices() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    public static boolean oreoDevices() {
        // return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O;
        return Build.VERSION.SDK_INT >= 26;
    }

    static int numOfCpuCores = -1;

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     * @return The number of cores, or 1 if failed to get result
     */
    public static int getNumCores() {
        if (numOfCpuCores > 0) {
            return numOfCpuCores;
        }

        //Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                //Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            numOfCpuCores = files.length > 0 ? files.length : 1;
        } catch (Exception e) {
            numOfCpuCores = -1;
        }

        if (numOfCpuCores <= 0) {
            numOfCpuCores = Runtime.getRuntime().availableProcessors();
        }

        if (numOfCpuCores <= 0) {
            //Default to return 1 core
            numOfCpuCores = 1;
        }

        return numOfCpuCores;
    }

    static int memoryMB = -1;

    public static boolean lowPhysicalMemoryDevices(Context context) {
        if (lowRamDevice(context)) {
            return true;
        }
        if (memoryMB == -1) {
            memoryMB = (int) (getPhysicalMemoryKBs() / 1024);
        }

        return (memoryMB < 300);
    }
    static long sPhysicalMemory = 0L;

    public static Long getPhysicalMemoryKBs() {
        // read /proc/meminfo to find MemTotal 'MemTotal: 711480 kB'
        // This operation would complete in fixed time

        if (sPhysicalMemory == 0L) {
            final String PATTERN = "MemTotal:";

            InputStream inStream = null;
            InputStreamReader inReader = null;
            BufferedReader inBuffer = null;

            try {
                inStream = new FileInputStream("/proc/meminfo");
                inReader = new InputStreamReader(inStream);
                inBuffer = new BufferedReader(inReader);

                String s;
                while ((s = inBuffer.readLine()) != null && s.length() > 0) {
                    if (s.startsWith(PATTERN)) {
                        String memKBs = s.substring(PATTERN.length()).trim();
                        memKBs = memKBs.substring(0, memKBs.indexOf(' '));
                        sPhysicalMemory = Long.parseLong(memKBs);
                        break;
                    }
                }
            } catch (Exception e) {
            } finally {
                silentlyClose(inStream);
                silentlyClose(inReader);
                silentlyClose(inBuffer);
            }
        }

        return sPhysicalMemory;
    }

    public static void silentlyClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable e) {
            }
        }
    }

    public static boolean lowPhysicalMemoryDevicesmLessThan3G(Context context) {
        if (lowPhysicalMemoryDevices(context)) {
            return true;
        }

        return (memoryMB < 3000);
    }

    public static boolean lowPhysicalMemoryDevicesmLessThan4G(Context context) {
        if (lowPhysicalMemoryDevices(context)) {
            return true;
        }

        return (memoryMB < 4500);
    }

    public static boolean isLowMemory(Context context) {
        ActivityManager manager = (ActivityManager) context.getApplicationContext()
                .getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(info);
        return info.lowMemory;
    }

    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    private static Boolean sLowRamDevice;

    private static boolean lowRamDevice(Context context) {
        if (sLowRamDevice == null) {
            if (kitkatDevices()) {
                ActivityManager am = (ActivityManager) context.getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                if (am != null) {
                    sLowRamDevice = am.isLowRamDevice();
                }
            } else {
                sLowRamDevice = false;
            }
        }

        return sLowRamDevice;
    }

    public static boolean extremeLowMemoryDevices(Context context) {
        return getHeapSize(context) <= 20;
    }

    public static boolean lowMemoryDevices(Context context) {
        return getHeapSize(context) < 30;
    }

    static int sHeapSize = -1;

    public static int getHeapSize(Context context) {
        if (sHeapSize <= 0) {
            ActivityManager am = (ActivityManager) context.getApplicationContext().getSystemService(
                    Context.ACTIVITY_SERVICE);
            sHeapSize = am.getMemoryClass();
        }
        return sHeapSize;
    }

    static String deviceDesc = null;

    public static String getDeviceDescription() {
        if (deviceDesc == null) {
            StringBuffer sb = new StringBuffer();

            sb.append('\n');
            sb.append('\t').append("Build.MANUFACTURER\t").append(Build.MANUFACTURER).append('\n');
            sb.append('\t').append("Build.MODEL\t").append(Build.MODEL).append('\n');
            sb.append('\t').append("Build.PRODUCT\t").append(Build.PRODUCT).append('\n');
            sb.append('\t').append("Build.DEVICE\t").append(Build.DEVICE).append('\n');
            sb.append('\t').append("Build.BOARD\t").append(Build.BOARD).append('\n');
            sb.append('\t').append("Build.BRAND\t").append(Build.BRAND).append('\n');
            sb.append('\t').append("Build.CPU_ABI\t").append(Build.CPU_ABI).append('\n');
            sb.append('\t').append("Build.DISPLAY\t").append(Build.DISPLAY).append('\n');
            sb.append('\t').append("Build.FINGERPRINT\t").append(Build.FINGERPRINT).append('\n');
            sb.append('\t').append("Build.HARDWARE\t").append(Build.HARDWARE).append('\n');
            sb.append('\t').append("Build.RADIO\t").append(Build.RADIO).append('\n');
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                sb.append('\t').append("Build.SERIAL\t").append(getSN()).append('\n');
            }
            sb.append('\t').append("Build.TAGS\t").append(Build.TAGS).append('\n');
            sb.append('\t').append("Build.TYPE\t").append(Build.TYPE).append('\n');
            sb.append('\t').append("Build.SDK_INT\t").append(Build.VERSION.SDK_INT).append('\n');

            deviceDesc = sb.toString();
        }

        return deviceDesc;
    }

    private static HashSet<String> jZPMagicNotSupportedSet = null;
    private static Boolean isJZPMagicNotSupported = null;

    public static boolean isJZPMagicNotSupported() {
        if (jZPMagicNotSupportedSet == null || isJZPMagicNotSupported == null) {
            jZPMagicNotSupportedSet = new HashSet<>();
//            jZPMagicNotSupportedSet.add("samsung/j2ltedd/j2lte:5.1.1/LMY47X/J200GDDU2AQD2:user/release-keys");
//            final String fingerprint = Build.FINGERPRINT;
//            isJZPMagicNotSupported = jZPMagicNotSupportedSet.contains(fingerprint);
            jZPMagicNotSupportedSet.add("samsung/SM-J200G");
            final String manufacturer = Build.MANUFACTURER;
            final String model = Build.MODEL;
            isJZPMagicNotSupported = jZPMagicNotSupportedSet.contains(manufacturer + "/" + model);
        }
        return isJZPMagicNotSupported;
    }

    public static boolean isMeizuM9() {
        // bad example: Meizu M9
        final String fingerprint = Build.FINGERPRINT;
        return fingerprint != null ? (fingerprint.indexOf("meizu_m9") != -1) : false;
    }

    public static boolean isMeizuMX2() {
        final String fingerprint = Build.FINGERPRINT;
        return fingerprint != null ? (fingerprint.indexOf("meizu_mx2") != -1) : false;
    }

    private static Boolean isHuaweiC8812E = null;

    public static boolean isHuaweiC8812E() {
        if (isHuaweiC8812E == null) {
            final String fingerPrint = Build.FINGERPRINT;
            isHuaweiC8812E = fingerPrint != null ? fingerPrint.contains("HuaweiC8812E") : false;
        }
        return isHuaweiC8812E;
    }

    private static Boolean isHuaweiU8825D = null;

    public static boolean isHuaweiU8825D() {
        if (isHuaweiU8825D == null) {
            final String fingerPrint = Build.FINGERPRINT;
            isHuaweiU8825D = fingerPrint != null ? fingerPrint.contains("HuaweiU8825D") : false;
        }
        return isHuaweiU8825D;
    }

    static Boolean isSamsungGalaxyNote = null;

    public static boolean isGalaxyNote() {
        if (isSamsungGalaxyNote == null) {
            final String FINGERPRINT = "samsung/GT-N7000/GT-N7000:2.3.5/GINGERBREAD/ZSKJ6:user/release-keys";
            final String MODEL = "GT-N7000";
            if (Build.FINGERPRINT.equals(FINGERPRINT) || Build.FINGERPRINT.contains(MODEL)) {
                isSamsungGalaxyNote = true;
            } else {
                isSamsungGalaxyNote = false;
            }
        }

        return isSamsungGalaxyNote;
    }

    static Boolean isSamsungDevice = null;

    public static boolean isSamsung() {
        if (isSamsungDevice == null) {
            isSamsungDevice = Build.FINGERPRINT.toLowerCase().contains("samsung");
        }

        return isSamsungDevice;
    }

    static Boolean mIsHuaWeiDevice = null;

    public static boolean isHuaWei() {
        if (mIsHuaWeiDevice == null) {
            mIsHuaWeiDevice = Build.MANUFACTURER.toLowerCase().contains("huawei") || Build.FINGERPRINT.toLowerCase(Locale.US).contains("huawei");
        }
        return mIsHuaWeiDevice;
    }

    public static boolean isHuaWei10Above() {
        return Build.VERSION.SDK_INT >= 29 && isHuaWei();
    }

    static Boolean isHtcG14 = null;

    public static boolean isHtcG14() {
        if (isHtcG14 == null) {
            // bad official rom: could not use hardware acc or BubbleTextViews are in trouble
            final String FINGERPRINT = "htccn_chs_cu/htc_pyramid/pyramid:4.0.3/IML74K/357408.14:user/release-keys";
            if (Build.FINGERPRINT.equals(FINGERPRINT)) {
                isHtcG14 = true;
            } else {
                isHtcG14 = false;
            }
        }

        return isHtcG14;
    }

    static Boolean isI9100 = null;

    public static boolean isI9100() {
        if (isI9100 == null) {
            final String FINGERPRINT = "samsung/GT-I9100/GT-I9100:4.0.3/IML74K/ZSLPE:user/release-keys";
            final String MODEL = "GT-I9100";
            if (Build.FINGERPRINT.equals(FINGERPRINT) || Build.FINGERPRINT.contains(MODEL)) {
                isI9100 = true;
            } else {
                isI9100 = false;
            }
        }
        return isI9100;
    }

    static Boolean isZTEModernDevice = null;

    public static boolean isZTEModernDevice() {
        if (isZTEModernDevice == null) {
            final String MODEL = "ZTE";
            if (modernDevices() && Build.FINGERPRINT.contains(MODEL) || Build.MODEL.contains(MODEL)) {
                isZTEModernDevice = true;
            } else {
                isZTEModernDevice = false;
            }
        }
        return isZTEModernDevice;
    }

    static String sMainIMEI = null;

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public static String getIMEI(Context context) {
        if (sMainIMEI == null || sMainIMEI.length() < 4) {
            TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(
                    Context.TELEPHONY_SERVICE);

            try {
                // need android.permission.READ_PHONE_STATE
                sMainIMEI = tm.getDeviceId();
            } catch (Exception e) {
                sMainIMEI = "No-Permission-IMEI: " + Build.FINGERPRINT;
            }
        }

        return sMainIMEI;
    }

    static String sMainIMSI = null;

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public static String getIMSI(Context context) {
        if (sMainIMSI == null || sMainIMSI.length() < 4) {
            TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(
                    Context.TELEPHONY_SERVICE);

            try {
                // need android.permission.READ_PHONE_STATE
                sMainIMSI = tm.getSubscriberId();
            } catch (Exception e) {
                sMainIMSI = "No-Permission-IMSI: " + Build.FINGERPRINT;
            }
        }

        return sMainIMSI;
    }

    static String sSerialNumber = null;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static String getSN() {
        if (TextUtils.isEmpty(sSerialNumber)) {
            try {
                sSerialNumber = Build.SERIAL;
            } catch (Exception e) {
                sSerialNumber = "Serial-Not-Available";
            }
        }

        return sSerialNumber;
    }

    static String sPhoneNumber = null;

    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public static String getPhoneNumber(Context context) {
        if (TextUtils.isEmpty(sPhoneNumber)) {
            TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(
                    Context.TELEPHONY_SERVICE);

            try {
                // need android.permission.READ_PHONE_STATE
                sPhoneNumber = tm.getLine1Number();
            } catch (Exception e) {
            }

            if (TextUtils.isEmpty(sPhoneNumber)) {
                sPhoneNumber = "";
            }
        }

        return sPhoneNumber;
    }

    static String sSimContryCode = null;

    public static String getSimCountryCode(Context context) {
        if (sSimContryCode == null) {
            TelephonyManager tm = (TelephonyManager) context.getApplicationContext().getSystemService(
                    Context.TELEPHONY_SERVICE);
            try {
                // need android.permission.READ_PHONE_STATE
                sSimContryCode = tm.getSimCountryIso();
            } catch (Exception e) {
            }
            if (TextUtils.isEmpty(sSimContryCode)) {
                sSimContryCode = "";
            }
        }
        return sSimContryCode;
    }

    /**
     * Gets the state of Airplane Mode.
     *
     * @param context
     * @return true if enabled.
     */
    @SuppressLint("NewApi")
    public static boolean isAirplaneModeOn(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return Settings.System.getInt(context.getContentResolver(), Settings.System.AIRPLANE_MODE_ON, 0) != 0;
        } else {
            return Settings.Global.getInt(context.getContentResolver(), Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
        }
    }

    private static Boolean sIsLenovoK900 = null;

    public static boolean isLenovoK900() {
        //        [ro.build.fingerprint]: [Lenovo/K900/K900:4.2.1/JOP40D/K900_1_S_2_009_0145_130508:user/releasekey]
        //        [ro.product.device]: [K900]
        //        [ro.product.manufacturer]: [LENOVO]

        if (sIsLenovoK900 == null) {
            if (Build.FINGERPRINT != null && Build.FINGERPRINT.contains("Lenovo/K900")) {
                sIsLenovoK900 = true;
            } else if ("K900".equalsIgnoreCase(Build.DEVICE) && "LENOVO".equalsIgnoreCase(Build.MANUFACTURER)) {
                sIsLenovoK900 = true;
            } else {
                sIsLenovoK900 = false;
            }
        }

        return sIsLenovoK900;
    }

    private static Boolean sIsMX4Pro = null;

    public static boolean isMeizuMX4Pro() {
        if (sIsMX4Pro == null) {
            if ("mx4pro".equalsIgnoreCase(Build.DEVICE) && "Meizu".equalsIgnoreCase(Build.MANUFACTURER)) {
                sIsMX4Pro = true;
            } else {
                sIsMX4Pro = false;
            }
        }
        return sIsMX4Pro;
    }

    private static Boolean sIsXiaomi = null;

    // detects mi-phone or miui rom
    public static boolean isXiaomi() {
        if (sIsXiaomi == null) {
            boolean isXiaomi = false;

            if (Build.FINGERPRINT != null) {
                if (Build.FINGERPRINT.toLowerCase().contains("xiaomi")) {
                    isXiaomi = true;
                }
            }

            if (Build.PRODUCT != null) {
                if (Build.PRODUCT.toLowerCase().contains("xiaomi")) {
                    isXiaomi = true;
                }
            }

            if (Build.MANUFACTURER != null) {
                if (Build.MANUFACTURER.toLowerCase().contains("xiaomi")) {
                    isXiaomi = true;
                }
            }

            sIsXiaomi = isXiaomi;
        }

        return sIsXiaomi;
    }

    private static Boolean sIsNexus6 = null;

    public static boolean isNexus6() {
        if (sIsNexus6 == null) {
            if ("nexus 6".equalsIgnoreCase(Build.MODEL) && "motorola".equalsIgnoreCase(Build.MANUFACTURER)) {
                sIsNexus6 = true;
            } else {
                sIsNexus6 = false;
            }
        }
        return sIsNexus6;
    }

    public static String getWifiMac(Context context) {
        try {
            String ret = null;

            WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(android.content.Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            if (wifiInfo != null) {
                ret = wifiInfo.getMacAddress();
            }
            return ret;
        } catch (Exception e) {
            return "";
        }
    }

    public static String getMmcID() {
        // read '/sys/block/mmcblk%d/device/cid'
        // available before SD card mounted, available on user build devices.
        // This operation would complete in fixed time
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < 4; i++) {
            String command = String.format("/system/bin/cat /sys/block/mmcblk%d/device/cid", i);

            try {
                java.lang.Process p = Runtime.getRuntime().exec(command);

                InputStream inStream = p.getInputStream();
                InputStreamReader inReader = new InputStreamReader(inStream);
                BufferedReader inBuffer = new BufferedReader(inReader);

                String s;
                while ((s = inBuffer.readLine()) != null && s.length() > 0) {
                    result.append(s).append(" ");
                }
            } catch (Exception e) {
            }
        }

        return result.toString();
    }

    public static String getFreeMemoryKBs() {
        // read /proc/meminfo to find memfree 'MemFree: 143632 kB'
        // This operation would complete in fixed time
        String command = "/system/bin/cat /proc/meminfo";

        try {
            java.lang.Process p = Runtime.getRuntime().exec(command);

            InputStream inStream = p.getInputStream();
            InputStreamReader inReader = new InputStreamReader(inStream);
            BufferedReader inBuffer = new BufferedReader(inReader);

            String s;
            while ((s = inBuffer.readLine()) != null && s.length() > 0) {
                if (s.startsWith("MemFree:")) {
                    return s;
                }
            }
        } catch (Exception e) {
        }
        return "unknown";
    }

    /**
     * 得到屏幕的物理尺寸，由于该尺寸是在出厂时，厂商写死的，所以仅供参考
     * 计算方法：获取到屏幕的分辨率:point.x和point.y，再取出屏幕的DPI（每英寸的像素数量），
     * 计算长和宽有多少英寸，即：point.x / dm.xdpi，point.y / dm.ydpi，屏幕的长和宽算出来了，
     * 再用勾股定理，计算出斜角边的长度，即屏幕尺寸。
     *
     * @param context
     * @return
     */
    public static double getPhysicsScreenSize(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            manager.getDefaultDisplay().getRealSize(point);
        }
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int densityDpi = dm.densityDpi;//得到屏幕的密度值，但是该密度值只能作为参考，因为他是固定的几个密度值。
        double x = Math.pow(point.x / dm.xdpi, 2);//dm.xdpi是屏幕x方向的真实密度值，比上面的densityDpi真实。
        double y = Math.pow(point.y / dm.ydpi, 2);//dm.xdpi是屏幕y方向的真实密度值，比上面的densityDpi真实。
        double screenInches = Math.sqrt(x + y);
        return screenInches;
    }

    public static double getScaledPhysicsScreenSize(Context context, double thumbnailSubScale) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            manager.getDefaultDisplay().getRealSize(point);
        }
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int densityDpi = dm.densityDpi;//得到屏幕的密度值，但是该密度值只能作为参考，因为他是固定的几个密度值。
        double mScreenWidth = point.x / dm.xdpi;//dm.xdpi是屏幕x方向的真实密度值，比上面的densityDpi真实。
//        double y = Math.pow(point.y / dm.ydpi, 2);//dm.xdpi是屏幕y方向的真实密度值，比上面的densityDpi真实。
//        double screenInches = Math.sqrt(x + y);
        double mThumbnailWidth = mScreenWidth / thumbnailSubScale;
        double mThumbnailHeight = mScreenWidth * (1f / thumbnailSubScale) * (4f / 3f);
        double screenInches = Math.sqrt(Math.pow(mThumbnailWidth, 2) + Math.pow(mThumbnailHeight, 2));
        return screenInches;
    }

    /**
     * 获取手机型号
     *
     * @return 手机型号
     */
    public static String getDeviceModel() {
        return android.os.Build.MODEL;
    }
}
