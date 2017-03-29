/**
 * Cobub Razor
 * <p/>
 * An open source analytics android sdk for mobile applications
 *
 * @package Cobub Razor
 * @author WBTECH Dev Team
 * @copyright Copyright (c) 2011 - 2015, NanJing Western Bridge Co.,Ltd.
 * @license http://www.cobub.com/products/cobub-razor/license
 * @link http://www.cobub.com/products/cobub-razor/
 * @filesource
 * @since Version 0.1
 */
package com.csmijo.probbugtags.baseData;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.csmijo.probbugtags.utils.CommonUtil;
import com.csmijo.probbugtags.utils.Logger;

import java.io.File;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import static android.content.ContentValues.TAG;

/**
 * @author apple
 */
@SuppressLint("InlinedApi")
public class DeviceInfo {

    private static final String tag = "DeviceInfo";
    private static Context context;
    private static Location location;
    private static TelephonyManager telephonyManager;
    private static LocationManager locationManager;
    private static BluetoothAdapter bluetoothAdapter;
    private static SensorManager sensorManager;

    public static void init(Context context) {
        DeviceInfo.context = context;

        try {
            telephonyManager = (TelephonyManager) (DeviceInfo.context
                    .getSystemService(Context.TELEPHONY_SERVICE));
            locationManager = (LocationManager) DeviceInfo.context
                    .getSystemService(Context.LOCATION_SERVICE);
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        } catch (Exception e) {
            Logger.e(tag, e.toString());
        }
        getLocation();
    }

    public DeviceInfo() {
        super();
    }

    public static String getLanguage() {
        String language = Locale.getDefault().getLanguage();
        Logger.d(tag, "getLanguage()=" + language);
        if (language == null)
            return "";
        return language;
    }

    public static String getResolution() {

        DisplayMetrics displaysMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displaysMetrics);
        Logger.d(tag, "getResolution()=" + displaysMetrics.widthPixels + "x"
                + displaysMetrics.heightPixels);
        return displaysMetrics.widthPixels + "x" + displaysMetrics.heightPixels;
    }

    public static String getDeviceProduct() {
        String result = Build.PRODUCT;
        Logger.d(tag, "getDeviceProduct()=" + result);
        if (result == null)
            return "";
        return result;
    }

    public static boolean getBluetoothAvailable() {
        if (bluetoothAdapter == null)
            return false;
        else
            return true;
    }

    private static boolean isSimulator() {
        if (getDeviceIMEI().equals("000000000000000"))
            return true;
        else
            return false;
    }

    public static boolean getGravityAvailable() {
        try {
            // This code getSystemService(Context.SENSOR_SERVICE);
            // often hangs out the application when it runs in Android
            // Simulator.
            // so in simulator, this line will not be run.
            if (isSimulator())
                sensorManager = null;
            else
                sensorManager = (SensorManager) context
                        .getSystemService(Context.SENSOR_SERVICE);
            Logger.d(tag, "getGravityAvailable()");
            return (sensorManager == null) ? false : true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getOsVersion() {
        String result = Build.VERSION.RELEASE;
        Logger.d(tag, "getOsVersion()=" + result);
        if (result == null)
            return "";

        return result;
    }

    /**
     * Returns a constant indicating the device phone type. This indicates the
     * type of radio used to transmit voice calls.
     *
     * @return PHONE_TYPE_NONE //0 PHONE_TYPE_GSM //1 PHONE_TYPE_CDMA //2
     * PHONE_TYPE_SIP //3
     */
    public static int getPhoneType() {
        if (telephonyManager == null)
            return -1;
        int result = telephonyManager.getPhoneType();
        Logger.d(tag, "getPhoneType()=" + result);
        return result;
    }

    /**
     * get IMSI for GSM phone, return "" if it is unavailable.
     *
     * @return IMSI string
     */
    public static String getIMSI() {
        String result = "";
        try {
            if (!CommonUtil.checkPermissions(context,
                    Manifest.permission.READ_PHONE_STATE)) {
                Logger.e(tag,
                        "READ_PHONE_STATE permission should be added into AndroidManifest.xml.");
                return "";
            }
            result = telephonyManager.getSubscriberId();
            Logger.d(tag, "getIMSI()=" + result);
            if (result == null)
                return "";
            return result;

        } catch (Exception e) {
            Logger.e(tag, e);
        }

        return result;
    }

    public static String getWifiMac() {
        try {
            WifiManager wifiManager = (WifiManager) context
                    .getSystemService(Context.WIFI_SERVICE);
            WifiInfo wi = wifiManager.getConnectionInfo();
            String result = wi.getMacAddress();
            if (result == null)
                result = "";
            Logger.d(tag, "getWifiMac()=" + result);
            return result;
        } catch (Exception e) {
            Logger.e(tag, e);
            return "";
        }

    }

    public static String getDeviceTime() {
        try {
            Date date = new Date();
            SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.US);
            String result = localSimpleDateFormat.format(date);
            return result;
        } catch (Exception e) {
            return "";
        }
    }

    public static String getDeviceName() {
        try {
            String manufacturer = Build.MANUFACTURER;
            if (manufacturer == null)
                manufacturer = "";
            String model = Build.MODEL;
            if (model == null)
                model = "";

            if (model.startsWith(manufacturer)) {
                return capitalize(model).trim();
            } else {
                return (capitalize(manufacturer) + " " + model).trim();
            }
        } catch (Exception e) {
            Logger.e(tag, e);
            return "";
        }
    }

    public static String getNetworkTypeWIFI2G3G() {

        try {
            ConnectivityManager cm = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo ni = cm.getActiveNetworkInfo();
            String type = ni.getTypeName().toLowerCase(Locale.US);
            if (!type.equals("wifi")) {
                type = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
                        .getExtraInfo();
            }
            return type;
        } catch (Exception e) {
            return "";
        }
    }

    public static boolean getWiFiAvailable() {
        try {
            if (!CommonUtil.checkPermissions(context,
                    Manifest.permission.ACCESS_WIFI_STATE)) {
                Logger.e(tag,
                        "ACCESS_WIFI_STATE permission should be added into AndroidManifest.xml.");
                return false;
            }
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getTypeName().equals("WIFI")
                                && info[i].isConnected()) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getDeviceIMEI() {
        String result = "";
        try {
            if (!CommonUtil.checkPermissions(context,
                    Manifest.permission.READ_PHONE_STATE)) {
                Logger.e(tag,
                        "READ_PHONE_STATE permission should be added into AndroidManifest.xml.");
                return "";
            }
            result = telephonyManager.getDeviceId();
            Logger.d(tag, "getIMEI()=" + result);
            if (result == null)
                result = "";
        } catch (Exception e) {
            Logger.e(tag, e);
        }
        return result;
    }

    private static String getSSN() {
        String result = "";
        try {

            if (!CommonUtil.checkPermissions(context,
                    Manifest.permission.READ_PHONE_STATE)) {
                Logger.e(tag,
                        "READ_PHONE_STATE permission should be added into AndroidManifest.xml.");
                return "";
            }
            result = telephonyManager.getSimSerialNumber();
            if (result == null)
                result = "";
        } catch (Exception e) {
            Logger.e(tag, e);
        }
        return result;
    }


    public static String getDeviceId() {
        String result = null;
        result = getDeviceIMEI();
        if (!TextUtils.isEmpty(result)) {
            result = CommonUtil.md5Appkey(result);
        } else {
            result = "000000";
        }
        return result;
    }

    public static String getLatitude() {
        if (location == null)
            return "";
        return String.valueOf(location.getLatitude());
    }

    public static String getLongitude() {
        if (location == null)
            return "";
        return String.valueOf(location.getLongitude());

    }

    public static String getGPSAvailable() {
        if (location == null)
            return "false";
        else
            return "true";
    }

    private static void getLocation() {
        Logger.d(tag, "getLocation");
        try {
            List<String> matchingProviders = locationManager.getAllProviders();
            for (String prociderString : matchingProviders) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission
                        .ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(context, Manifest.permission
                                .ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                    return;
                }
                location = locationManager.getLastKnownLocation(prociderString);
                if (location != null)
                    break;
            }
        } catch (Exception e) {
            Logger.e(tag, e.toString());
        }
    }

    public static String getMCCMNC() {
        String result = "";
        try {
            String operator = telephonyManager.getNetworkOperator();
            if (operator == null)
                result = "";
            else
                result = operator;
        } catch (Exception e) {
            result = "";
            Logger.e(tag, e.toString());
        }
        return result;
    }


    /**
     * Calculates the free memory of the device. This is based on an inspection of the filesystem, which
     * in android devices is stored in RAM.
     *
     * @return Number of bytes available.
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());

        long blockSize;
        long availableBlocks;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();
            availableBlocks = statFs.getAvailableBlocksLong();
        } else {
            // no inspection deprecation
            blockSize = statFs.getBlockSize();
            availableBlocks = statFs.getAvailableBlocks();
        }

        return availableBlocks * blockSize/ 1024 / 1024;
    }


    /**
     * Calculates the total memory of the devices. This is based on an inspection of the filesystem, which
     * in android devices is stored in RAM.
     *
     * @return Total number of bytes.
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());

        long blockSize;
        long totalBlocks;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();
            totalBlocks = statFs.getBlockCountLong();
        } else {
            // no inspection deprecation
            blockSize = statFs.getBlockSize();
            totalBlocks = statFs.getBlockCount();
        }

        return totalBlocks * blockSize/ 1024 / 1024;
    }


    /**
     * Check if the device is currently running low on memory
     *
     * @param appContext
     * @return
     */
    public static Boolean isLowMemory(Context appContext) {
        try {
            ActivityManager activityManager = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);

            return memoryInfo.lowMemory;
        } catch (Exception e) {
            Logger.e(TAG, "Could not check lowMemory status");
        }
        return false;
    }


    private static final String[] ROOT_INDICATORS = new String[]{
            // Common binaries
            "/system/xbin/su",
            "/system/bin/su",
            // < Android 5.0
            "/system/app/Superuser.apk",
            "/system/app/SuperSU.apk",
            // >= Android 5.0
            "/system/app/Superuser",
            "/system/app/SuperSU",
            // Fallback
            "/system/xbin/daemonsu"
    };

    /**
     * Check if the current Android device is rooted
     *
     * @return
     */
    public static Boolean isRooted() {
        if (Build.TAGS != null && Build.TAGS.contains("test-keys")) {
            return true;
        }

        try {
            for (String candidate : ROOT_INDICATORS) {
                if (new File(candidate).exists()) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    /**
     * Gets information about the CPU/API
     *
     * @return
     */
    public static String[] getCpuAbi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return SupportedAbiWrapper.getSupportedAbis();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            return Abi2Wrapper.getAbi1andAbi2();
        }

        return new String[]{Build.CPU_ABI};
    }

    /**
     * Wrapper class to allow the test framework to use the correct version of the CPU / ABI
     */
    private static class SupportedAbiWrapper {
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public static String[] getSupportedAbis() {
            return Build.SUPPORTED_ABIS;
        }
    }

    /**
     * Wrapper class to allow the test framework to use the correct version of the CPU / ABI
     */
    private static class Abi2Wrapper {
        @TargetApi(Build.VERSION_CODES.FROYO)
        public static String[] getAbi1andAbi2() {
            return new String[]{Build.CPU_ABI, Build.CPU_ABI2};
        }
    }


    /**
     * Get the device orientation, eg. "landscape"
     */
    @Nullable
    public static String getOrientation(Context appContext) {
        String orientation = null;
        switch (appContext.getResources().getConfiguration().orientation) {
            case android.content.res.Configuration.ORIENTATION_LANDSCAPE:
                orientation = "landscape";
                break;
            case android.content.res.Configuration.ORIENTATION_PORTRAIT:
                orientation = "portrait";
                break;
            default:
                orientation = null;
                break;
        }
        return orientation;
    }

    /**
     * Get the current battery charge level, eg 0.3
     */
    @Nullable
    public static Float getBatteryLevel(Context appContext) {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = appContext.registerReceiver(null, ifilter);

            return batteryStatus.getIntExtra("level", -1) / (float) batteryStatus.getIntExtra("scale", -1);
        } catch (Exception e) {
            Logger.w(TAG, "Could not get batteryLevel");
        }
        return null;
    }


    /**
     * Is the device currently charging/full battery?
     */
    @Nullable
    public static Boolean isCharging(Context appContext) {
        try {
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = appContext.registerReceiver(null, ifilter);

            int status = batteryStatus.getIntExtra("status", -1);
            return (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
        } catch (Exception e) {
            Logger.w(TAG, "Could not get charging status");
        }
        return null;
    }

    /**
     * Returns the local ip address of the Telephony.
     *
     * @return local ip address
     */
    public static String getLocalIpAddress() {
        StringBuilder result = new StringBuilder();

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String address = inetAddress.getHostAddress();

                        boolean isIPv4 = address.indexOf(":") < 0;
                        if (isIPv4) {
                            result.append(address);
                        }
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return result.toString();
    }

    /**
     * Capitalize the first letter
     *
     * @param s model,manufacturer
     * @return Capitalize the first letter
     */
    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }

    }
}
