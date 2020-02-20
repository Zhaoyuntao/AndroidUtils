package com.zhaoyuntao.androidutils.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import java.lang.reflect.Method;


public class NetworkUtil {

    public static final String NET_UNKNOWN = "none";

    // wifi, cmwap, ctwap, uniwap, cmnet, uninet, ctnet,3gnet,3gwap
    // 其中3gwap映射为uniwap
    public static final String NET_WIFI = "wifi";
    public static final String NET_CMWAP = "cmwap";
    public static final String NET_UNIWAP = "uniwap";
    public static final String NET_CTWAP = "ctwap";
    public static final String NET_CTNET = "ctnet";

    /**
     * 判断当前网络是否为wifi网络
     *
     * @param context
     * @return
     */
    public static boolean isWifiConnect(Context context) {
        String netType = "";
        if (context != null) {
            ConnectivityManager nmgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo active = nmgr.getActiveNetworkInfo();
            if (active == null) {

            } else {
                netType = NetworkUtil.getNetType(active);
            }
        }
        return NET_WIFI.equals(netType);
    }


    /**
     * 获取网络类型参数，包括cmwap,uniwap,ctwap,wifi,cmnet,ctnet,uninet,3gnet
     * 由于底层msc无法处理3gwap，3gwap映射为uniwap
     *
     * @param info
     * @return
     */
    public static String getNetType(NetworkInfo info) {
        if (info == null) return NET_UNKNOWN;

        try {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) return NET_WIFI;
            else {
                String extra = info.getExtraInfo().toLowerCase();
                if (TextUtils.isEmpty(extra)) return NET_UNKNOWN;
                // 3gwap由于底层msc兼容不了，转换为uniwap
                if (extra.startsWith("3gwap") || extra.startsWith(NET_UNIWAP)) {
                    return NET_UNIWAP;
                } else if (extra.startsWith(NET_CMWAP)) {
                    return NET_CMWAP;
                } else if (extra.startsWith(NET_CTWAP)) {
                    return NET_CTWAP;
                } else if (extra.startsWith(NET_CTNET)) {
                    return NET_CTNET;
                } else return extra;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NET_UNKNOWN;
    }

    /**
     * 获取网络类型详细信息，包括EDGE、CDMA-EvDo rev.A、HSDPA等
     *
     * @param info
     * @return 以字符串加int类型进行组合，如EDGE;2
     */
    public static String getNetSubType(NetworkInfo info) {
        if (info == null) return NET_UNKNOWN;
        try {
            if (info.getType() == ConnectivityManager.TYPE_WIFI) return NET_UNKNOWN;
            else {
                String subtype = "";
                subtype += info.getSubtypeName();
                subtype += ";" + info.getSubtype();
                return subtype;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return NET_UNKNOWN;
    }


    /**
     * 判断网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * IP转整型
     *
     * @param ip
     * @return
     */
    public static long ip2int(String ip) {
        String[] items = ip.split("\\.");
        return Long.valueOf(items[0]) << 24 | Long.valueOf(items[1]) << 16 | Long.valueOf(items[2]) << 8 | Long.valueOf(items[3]);
    }

    /**
     * 整型转IP
     *
     * @param ipInt
     * @return
     */
    public static String int2ip(long ipInt) {
        if(ipInt==-1){
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 判断wifi ap是否启用
     *
     * @param context
     * @return
     */
    public static boolean isWifiApEnabled(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            Method method = manager.getClass().getMethod("isWifiApEnabled");
            return (Boolean) method.invoke(manager);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getSSID(Context context) {

        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = manager.getConnectionInfo();
            return wifiInfo.getSSID().substring(1, wifiInfo.getSSID().length() - 1);
        } catch (Exception e) {
            return null;
        }
    }

    public static int getIP(Context context) {
        if(context==null){
            return -1;
        }
        try {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiManager.getConnectionInfo();
            return info.getIpAddress();
        } catch (Exception e) {
            return -1;
        }
    }

    public static String getIp(Context context){
        return int2ip(getIP(context));
    }
}
