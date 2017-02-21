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

package com.csmijo.probbugtags;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;

import com.csmijo.probbugtags.baseData.AppInfo;
import com.csmijo.probbugtags.baseData.DeviceInfo;
import com.csmijo.probbugtags.manager.ConfigManager;
import com.csmijo.probbugtags.manager.MyCrashHandler;
import com.csmijo.probbugtags.manager.UploadHistoryLog;
import com.csmijo.probbugtags.manager.UploadLeakDumpHistory;
import com.csmijo.probbugtags.manager.UsinglogManager;
import com.csmijo.probbugtags.utils.CommonUtil;
import com.csmijo.probbugtags.utils.Constants;
import com.csmijo.probbugtags.utils.Logger;
import com.squareup.leakcanary.internal.LeakCanaryInternals;

import java.io.File;
import java.io.FileFilter;
import java.lang.ref.WeakReference;

import static android.content.ContentValues.TAG;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;


public class BugTagAgent {

    private static final String tag = "BugTagAgent";
    public static Handler handler;
    private static Context mContext;

    static {
        HandlerThread localHandlerThread = new HandlerThread("BugTagAgent");
        localHandlerThread.start();
        handler = new Handler(localHandlerThread.getLooper());
    }

    public BugTagAgent() {
        // TODO Auto-generated constructor stub
    }

    public static void init(final Context context) {
        mContext = context.getApplicationContext();
        AppInfo.init(mContext);
        DeviceInfo.init(mContext);
        BugTagAgent.postHistoryLog(mContext);
        BugTagAgent.postLeakDumpHistory(mContext);
        BugTagAgent.onError(mContext);
    }


    /**
     * @param context
     */
    public static void postHistoryLog(final Context context) {
        Logger.i(tag, "postHistoryLog");
        if (CommonUtil.isNetworkAvailable(context)) {
            Thread thread = new UploadHistoryLog(context);
            handler.post(thread);
        }
    }

    /**
     * 上传leak dump file
     *
     * @param context
     */
    public static void postLeakDumpHistory(Context context) {
        Logger.i(tag, "postLeakDumpHistory");
        if (CommonUtil.isNetworkAvailable(context) && CommonUtil.isNetworkTypeWifi(context)) {
            Thread thread = new UploadLeakDumpHistory(context);
            handler.post(thread);
        }else{
            alertToUpload(LeakCanaryInternals.storageDirectory());
        }
    }

    //提示用户发送
    private static void alertToUpload(File parentFile) {
        // 提示上传或者删除
        final File[] existsFiles = parentFile.listFiles(
                new FileFilter() {
                    @Override
                    public boolean accept(File file) {
                        return !file.isDirectory() && (file.getName().endsWith(".zip") || file.getName().endsWith(".hprof"));
                    }
                }
        );

        BugTagAgent.handler.post(new Runnable() {
            @Override
            public void run() {
                if (existsFiles.length >= 3) {
                    //AlertDialog提示
                    AlertDialog.Builder builder = new AlertDialog.Builder(ApplicationInit.getCurrentActivity());
                    builder.setMessage("存在多个dump文件，请打开wifi并重启App完成上传，否则会删除文件！是否上传？");
                    builder.setPositiveButton("是", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.setNegativeButton("否", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (existsFiles != null) {
                                for (File file : existsFiles) {
                                    Logger.i(TAG, "delete " + file.getName());
                                    file.delete();
                                }
                            }
                        }
                    });
                    builder.show().setCanceledOnTouchOutside(false);
                }
            }
        });
    }


    public static void onResume(Activity activity) {
        final WeakReference<Activity> wActivity = new WeakReference<Activity>(activity);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Logger.i(tag, "Call onResume()");

                Activity activity = wActivity.get();
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    ApplicationInit.setCurrentActivity(activity);
                    UsinglogManager usinglogManager = new UsinglogManager(mContext);
                    usinglogManager.onResume();

                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    String[] permissions = new String[]{
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_PHONE_STATE
                    };

                    if (CommonUtil.hasLackPermissions(activity, permissions)) {
                        ActivityCompat.requestPermissions(activity, permissions, 1);
                    }
                }

            }
        };
        handler.post(runnable);
    }

    public static void onPause(Activity activity) {
        final WeakReference<Activity> wActivity = new WeakReference<Activity>(activity);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Logger.i(tag, "Call onPause()");

                Activity activity = wActivity.get();
                ApplicationInit.clearReferences(activity);
                UsinglogManager usinglogManager = new UsinglogManager(mContext);
                usinglogManager.onPause();

            }
        };
        handler.post(runnable);
    }

    public static void onDestory(Activity activity) {
        final WeakReference<Activity> wActivity = new WeakReference<Activity>(activity);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Logger.i(tag, "Call onDestory()");

                Activity activity = wActivity.get();
                ApplicationInit.clearReferences(activity);
                if (SDK_INT < ICE_CREAM_SANDWICH) {
                    ApplicationInit.watch(activity.getApplicationContext());
                }
            }
        };
        handler.post(runnable);
    }

    /**
     * Call this function to send the uncatched crash exception stack
     * information to server
     *
     * @param context
     */
    static void onError(Context context) {
        final WeakReference<Context> wContext = new WeakReference<Context>(mContext);
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Logger.i(tag, "Call onError()");
                MyCrashHandler crashHandler = MyCrashHandler.getInstance();
                crashHandler.init(wContext.get().getApplicationContext());
//                Thread.setDefaultUncaughtExceptionHandler(crashHandler);
            }
        });
        handler.post(thread);
    }

    /**
     * @param isEnableDebug
     */
    public static void setDebugEnabled(boolean isEnableDebug) {
        Constants.DebugEnabled = isEnableDebug;
    }

    /**
     * @param level
     */
    public static void setDebugLevel(int level) {
        Constants.DebugLevel = level;
    }

    /**
     * @param context
     */
    public static void updateOnlineConfig(final Context context) {
        final WeakReference<Context> wContext = new WeakReference<Context>(mContext);
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Logger.i(tag, "Call updaeOnlineConfig");
                ConfigManager cm = new ConfigManager(wContext.get().getApplicationContext());
                cm.updateOnlineConfig();
            }
        });
        handler.post(thread);
    }

    /**
     * @param isUpdateonlyWifi
     */
    public static void setUpdateOnlyWifi(boolean isUpdateonlyWifi) {
        Constants.mUpdateOnlyWifi = isUpdateonlyWifi;
        Logger.i(tag,
                "setUpdateOnlyWifi = " + String.valueOf(isUpdateonlyWifi));
    }

    /**
     * Setting data transmission mode
     *
     * @param context
     * @param sendPolicy
     */
    public static void setDefaultReportPolicy(Context context,
                                              SendPolicy sendPolicy) {
        Constants.mReportPolicy = sendPolicy;
        Logger.i(tag,
                "setDefaultReportPolicy = " + String.valueOf(sendPolicy));
    }


    public static void postWebPage(final String pageName) {
        final WeakReference<Context> wContext = new WeakReference<Context>(mContext);
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Logger.i(tag, "Call postWebPage()");

                UsinglogManager usinglogManager = new UsinglogManager(wContext.get());
                usinglogManager.onWebPage(pageName);
            }
        });
        handler.post(thread);
    }


    public enum SendPolicy {
        BATCH, REALTIME
    }
}
