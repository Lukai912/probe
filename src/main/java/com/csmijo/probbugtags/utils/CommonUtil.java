package com.csmijo.probbugtags.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import com.csmijo.probbugtags.BugTagAgent;
import com.csmijo.probbugtags.baseData.AppInfo;
import com.csmijo.probbugtags.baseData.DeviceInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.ref.WeakReference;
import java.security.MessageDigest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

/**
 * Created by chengqianqian-xy on 2016/6/20.
 */
public class CommonUtil {
    private static final String TAG = "CommonUtil";

    /**
     * check permission
     *
     * @param context
     * @param permission
     * @return
     */
    public static boolean checkPermissions(Context context, String permission) {
//        PackageManager pm = context.getPackageManager();
//        return pm.checkPermission(permission, context.getPackageName()) ==
//                PackageManager.PERMISSION_GRANTED;
        boolean result = ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
        return result;
    }


    /**
     * 检查是否有权限还没有授权
     *
     * @param context
     * @return
     */
    public static boolean hasLackPermissions(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(context.getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }
        return false;
    }


    /**
     * Testing equipment networking and networking WIFI
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvailable(Context context) {
        if (checkPermissions(context, "android.permission.INTERNET")) {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (null == connectivityManager) {
                return false;
            }

            NetworkInfo info = connectivityManager.getActiveNetworkInfo();
            if (null != info && info.isAvailable()) {
                Logger.i(TAG, info.getTypeName() + " Network is available");
                return true;
            } else {
                Logger.i(TAG, "Network is not available");
                return false;
            }
        } else {
            Logger.e(TAG, "android.permission.INTERNET permission should be added into AndroidManifest.xml.");
            return false;
        }
    }

    /**
     * Determine the current network type
     *
     * @param context
     * @return
     */
    public static boolean isNetworkTypeWifi(Context context) {

        if (checkPermissions(context, "android.permission.INTERNET")) {
            ConnectivityManager cManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cManager == null)
                return false;

            NetworkInfo info = cManager.getActiveNetworkInfo();

            if (info != null && info.isAvailable()
                    && info.getType() == ConnectivityManager.TYPE_WIFI) {
                Logger.i(TAG, "Active Network type is wifi");
                return true;
            } else {
                Logger.i(TAG, "Active Network type is not wifi");
                return false;
            }
        } else {
            Logger.e(
                    TAG,
                    "android.permission.INTERNET permission should be added into AndroidManifest.xml.");
            return false;
        }

    }

    /**
     * @param saveObject
     * @param fileSuffix
     * @param context
     */
    public static void saveInfoToFile(String type, JSONObject saveObject, String fileSuffix, Context
            context) {
        JSONArray newDataArray = new JSONArray();
        try {
            newDataArray.put(0, saveObject);
            JSONObject newSaveObject = new JSONObject();
            newSaveObject.put(type, newDataArray);

            String cacheFile = context.getCacheDir() + fileSuffix;
            Thread thread = new SaveInfo(newSaveObject, cacheFile);
            thread.run();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * return UserIdentifier
     */
    public static String getUserIdentifier(Context context) {
        try {
            return SharedPrefUtil.getValue(context, "identifier", "");
        } catch (Exception e) {
            Logger.e(TAG, e);
            return "";
        }
    }

    /**
     * Get the current send model
     *
     * @param context
     * @return
     */
    public static BugTagAgent.SendPolicy getReportPolicyMode(Context context) {
        return Constants.mReportPolicy;
    }


    /**
     * get currnet activity's name
     *
     * @param context
     * @return
     */
    public static String getActivityName(Context context) {
        if (context == null) {
            return "";
        }
        if (context instanceof Activity) {
            WeakReference<Context> wContext = new WeakReference<Context>(context);

            String name = "";
            try {
                name = ((Activity) wContext.get()).getComponentName()
                        .getShortClassName();
            } catch (Exception e) {
                Logger.e("can not get name", e.toString());
            }

            wContext.clear();

            return name;
        } else {
            ActivityManager am = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            if (checkPermissions(context, "android.permission.GET_TASKS")) {
                ComponentName cn = am.getRunningTasks(1).get(0).topActivity;

                String name = cn.getShortClassName();


                return name;
            } else {
                Logger.e("lost permission", "android.permission.GET_TASKS");

                return "";
            }
        }
    }

    /**
     * Get the version number of the current program
     *
     * @param context
     * @return
     */

    public static String getCurVersion(Context context) {
        String curversion = "";
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            curversion = pi.versionName;
            if (curversion == null || curversion.length() <= 0) {
                return "";
            }
        } catch (Exception e) {
            Logger.e(TAG, e.toString());
        }
        return curversion;
    }

    /**
     * Get the current networking
     *
     * @param context
     * @return WIFI or MOBILE
     */
    public static String getNetworkType(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        int type = manager.getNetworkType();
        String typeString = "UNKNOWN";
        if (type == TelephonyManager.NETWORK_TYPE_CDMA) {
            typeString = "CDMA";
        }
        if (type == TelephonyManager.NETWORK_TYPE_EDGE) {
            typeString = "EDGE";
        }
        if (type == TelephonyManager.NETWORK_TYPE_EVDO_0) {
            typeString = "EVDO_0";
        }
        if (type == TelephonyManager.NETWORK_TYPE_EVDO_A) {
            typeString = "EVDO_A";
        }
        if (type == TelephonyManager.NETWORK_TYPE_GPRS) {
            typeString = "GPRS";
        }
        if (type == TelephonyManager.NETWORK_TYPE_HSDPA) {
            typeString = "HSDPA";
        }
        if (type == TelephonyManager.NETWORK_TYPE_HSPA) {
            typeString = "HSPA";
        }
        if (type == TelephonyManager.NETWORK_TYPE_HSUPA) {
            typeString = "HSUPA";
        }
        if (type == TelephonyManager.NETWORK_TYPE_UMTS) {
            typeString = "UMTS";
        }
        if (type == TelephonyManager.NETWORK_TYPE_UNKNOWN) {
            typeString = "UNKNOWN";
        }
        if (type == TelephonyManager.NETWORK_TYPE_1xRTT) {
            typeString = "1xRTT";
        }
        if (type == 11) {
            typeString = "iDen";
        }
        if (type == 12) {
            typeString = "EVDO_B";
        }
        if (type == 13) {
            typeString = "LTE";
        }
        if (type == 14) {
            typeString = "eHRPD";
        }
        if (type == 15) {
            typeString = "HSPA+";
        }

        return typeString;
    }

    public static boolean isNewSession(Context context) {
        try {
            long currenttime = System.currentTimeMillis();
            long session_save_time = SharedPrefUtil.getValue(context, "session_save_time", 0);
            Logger.i(TAG, "currenttime=" + currenttime);
            Logger.i(TAG, "session_save_time=" + session_save_time);
            if (currenttime - session_save_time > Constants.kContinueSessionMillis) {
                Logger.i(TAG, "return true,create new session.");
                return true;
            }
            Logger.i(TAG, "return false.At the same session.");
            return false;
        } catch (Exception e) {
            Logger.e(TAG, e);
            return true;
        }
    }


    public static String md5Appkey(String str) {
        try {
            MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
            localMessageDigest.update(str.getBytes());
            byte[] arrayOfByte = localMessageDigest.digest();
            StringBuffer localStringBuffer = new StringBuffer();
            for (int i = 0; i < arrayOfByte.length; i++) {
                int j = 0xFF & arrayOfByte[i];
                if (j < 16)
                    localStringBuffer.append("0");
                localStringBuffer.append(Integer.toHexString(j));
            }
            return localStringBuffer.toString();
        } catch (Exception e) {
            Logger.e(TAG, e);
        }
        return "";
    }

    /**
     * create sessionID
     *
     * @param context
     * @return sessionId
     * @throws ParseException
     */
    public static String generateSession(Context context) throws ParseException {

        String sessionId = "";
        String str = AppInfo.getAppKey();
        if (str != null) {
            str = str + DeviceInfo.getDeviceTime();
            sessionId = CommonUtil.md5Appkey(str);
            SharedPrefUtil.setValue(context, "session_id", sessionId);

            saveSessionTime(context);
            return sessionId;
        }
        return sessionId;
    }

    public static void saveSessionTime(Context context) {
        SharedPrefUtil.setValue(context, "session_save_time", System.currentTimeMillis());
    }


    /**
     * 判断登录是否还有效
     *
     * @param context
     */
    public static boolean isOnLine(Context context) {
        try {
            long currenttime = System.currentTimeMillis();
            long login_save_time = SharedPrefUtil.getValue(context, "login_save_time", 0);
            Logger.i(TAG, "currenttime=" + currenttime);
            Logger.i(TAG, "login_save_time=" + login_save_time);
            if (currenttime - login_save_time > Constants.liveTimeMillis) {
                Logger.i(TAG, "return true, please login again.");
                return false;
            }
            Logger.i(TAG, "return false,has login.");
            return true;
        } catch (Exception e) {
            Logger.e(TAG, e);
            return false;
        }
    }

    public static void savePageName(Context context, String pageName) {
        SharedPrefUtil.setValue(context, "CurrentPage", pageName);
    }

    public static String getFormatTime(long timestamp) {
        try {
            Date date = new Date(timestamp);
            SimpleDateFormat localSimpleDateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.US);
            String result = localSimpleDateFormat.format(date);
            return result;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 返回该设备在此程序上的随机数。
     *
     * @param context Context对象。
     * @return 表示该设备在此程序上的随机数。
     */
    @SuppressLint("NewApi")
    public synchronized static String getSALT(Context context) {
        String file_name = context.getPackageName().replace(".", "");
        String sdCardRoot = Environment.getExternalStorageDirectory()
                .getAbsolutePath();
        int apiLevel = Integer.parseInt(android.os.Build.VERSION.SDK);
        File fileFromSDCard = new File(sdCardRoot + File.separator, "."
                + file_name);
        File fileFromDData = new File(context.getFilesDir(), file_name);// 获取data/data/<package>/files
        // 4.4之後 /storage/emulated/0/Android/data/<package>/files
        if (apiLevel >= 19) {
            sdCardRoot = context.getExternalFilesDir(null).getAbsolutePath();
            fileFromSDCard = new File(sdCardRoot, file_name);
        }

        String saltString = "";
        if (Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            // sdcard存在
            if (!fileFromSDCard.exists()) {

                String saltId = getSaltOnDataData(fileFromDData, file_name);
                try {
                    writeToFile(fileFromSDCard, saltId);
                } catch (Exception e) {
                    Logger.e(TAG, e);
                }
                return saltId;

            } else {
                // SD卡上存在salt
                saltString = getSaltOnSDCard(fileFromSDCard);
                writeToFile(fileFromDData, saltString);
                return saltString;
            }

        } else {
            // sdcard 不可用
            return getSaltOnDataData(fileFromDData, file_name);
        }

    }

    private static String getSaltOnSDCard(File fileFromSDCard) {
        // TODO Auto-generated method stub

        String saltString = readSaltFromFile(fileFromSDCard);
        return saltString;
    }

    private static String getSaltOnDataData(File fileFromDData, String file_name) {

        if (!fileFromDData.exists()) {
            String uuid = getUUID();
            writeToFile(fileFromDData, uuid);
            return uuid;
        }
        return readSaltFromFile(fileFromDData);


    }

    private static String getUUID() {
        // TODO Auto-generated method stub
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 读出保存在程序文件系统中的表示该设备在此程序上的唯一标识符。。
     *
     * @param file 保存唯一标识符的File对象。
     * @return 唯一标识符。
     * @throws IOException IO异常。
     */
    private static String readSaltFromFile(File file) {
        RandomAccessFile accessFile = null;
        byte[] bs = null;
        try {
            accessFile = new RandomAccessFile(file, "r");
            bs = new byte[(int) accessFile.length()];
            accessFile.readFully(bs);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (accessFile != null) {
                try {
                    accessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (bs != null) {
            return new String(bs);
        } else {
            return "";
        }


    }

    /**
     * 将表示此设备在该程序上的唯一标识符写入程序文件系统中
     *
     * @param file 保存唯一标识符的File对象。
     * @throws IOException IO异常。
     */
    private static void writeToFile(File file, String uuid) {
        FileOutputStream out = null;
        try {
            file.createNewFile();

            out = new FileOutputStream(file);

            out.write(uuid.getBytes());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


    }
}
