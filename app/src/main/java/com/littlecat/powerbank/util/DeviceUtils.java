package com.littlecat.powerbank.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.NetworkInfo.State;
import android.os.Build;
//import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;

/**
 * Created by zhangzhilai on 9/5/14.
 */
public class DeviceUtils {
    private static final String TAG = "DeviceUtils";

    /**
     * 系统版本号
     */
    public static String getSystemVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * mac地址
     *
     * @param con
     * @return
     */
    public static String getLocalMacAddress(Context con) {
        WifiManager wifi = (WifiManager) con.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifi.getConnectionInfo();
        return info.getMacAddress();
    }

    /**
     * 检测是否有网络连接
     *
     * @param ctx
     * @return true 连接可用。 false 连接不可用。
     */
    public static boolean isNetwork(Context ctx) {
        boolean netIsEnabled = true;
        if (checkNetworkInfo(ctx).equals("00")) {
            netIsEnabled = false;
        }
        return netIsEnabled;
    }

    /**
     * 检测网络状态<br>
     * 返回一个"00"形式的字符串 <li>"00":3G无效，WLAN无效 <li>"10":3G有效，WLAN无效 <li>"01":3G无效，WLAN有效 <li>"11":3G有效，WLAN有效
     *
     * @param ctx Context
     */
    public static String checkNetworkInfo(Context ctx) {
        String g3 = "0";
        String wlan = "0";
        ConnectivityManager conMan = null;
        try {
            conMan = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (Exception e) {
//            LogUtils.e(e.toString());
        }
        if (conMan == null)
            return "00";
        State mobile = null;
        State wifi = null;
        try {
            // mobile 3G Data Network
            mobile = conMan.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
        } catch (Exception e) {
        }
        try {
            // wifi
            wifi = conMan.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
        } catch (Exception e) {
//            LogUtils.e("获取wifi状态失败，请检查权限");
        }
        if (mobile == State.CONNECTED || mobile == State.CONNECTING)
            g3 = "1";
        if (wifi == State.CONNECTED || wifi == State.CONNECTING)
            wlan = "1";
        // 如果3G网络和wifi网络都未连接，且不是处于正在连接状态 则进入Network Setting界面 由用户配置网络连接
        // ctx.startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));//进入无线网络配置界面
        return g3 + wlan;
    }

    /*
    获取应用包相关信息
     */
    public static PackageInfo getPackgeInfo(final Context con) {
        PackageManager packageManager = con.getPackageManager();
        PackageInfo packageInfo = null;
        try {
            packageInfo = packageManager.getPackageInfo(con.getPackageName(), 0);
        } catch (NameNotFoundException e) {
            Log.e(TAG, e.toString());
        }
        return packageInfo;
    }

    /**
     * 获得应用版本名
     */
    public static String getVersionName(final Context con) {
        return getPackgeInfo(con).versionName;
    }

    /**
     * 获得应用版本号
     */
    public static int getVersionCode(final Context con) {
        return getPackgeInfo(con).versionCode;
    }

    /**
     * 获得应用包名
     */
    public static String getPackageName(final Context con) {
        return getPackgeInfo(con).packageName;
    }

    /**
     *
     */
    public static String getPackageChannel(Context context) {
        String packageChannel = null;
        PackageManager localPackageManager = context.getPackageManager();
        try {
            ApplicationInfo localApplicationInfo = localPackageManager
                    .getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            if (localApplicationInfo != null) {
                packageChannel = localApplicationInfo.metaData.getString("UMENG_CHANNEL");
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageChannel;
    }

    //获取屏幕的相关属性
    public static DisplayMetrics getDisplayMetrics(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm;
    }

    //获取屏幕的宽度
    public static int getScreenWidth(Context context) {
        return getDisplayMetrics(context).widthPixels;
    }

    //获取屏幕的高度
    public static int getScreenHeight(Context context) {
        return getDisplayMetrics(context).heightPixels;
    }

    //获取屏幕的密度
    public static float getDensity(Context context) {
        return getDisplayMetrics(context).density;
    }

    //dp转像素px
    public static int dp2px(Context context, int dp) {
        return (int) (dp * getDensity(context));
    }

    //px转dp
    public static int px2dp(Context context, int px) {
        return (int) (px / getDensity(context));
    }

    public static String getImei(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(context, Manifest.permission.READ_PHONE_STATE, 0x12);
//                return null;
//            }
        }
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String imei = telephonyManager.getDeviceId();
        return imei;
    }

    public static int getLocalVersion(Context context) {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }
}