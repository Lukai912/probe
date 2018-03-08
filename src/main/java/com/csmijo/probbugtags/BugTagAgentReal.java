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
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;

import com.csmijo.probbugtags.baseData.AppInfo;
import com.csmijo.probbugtags.baseData.DeviceInfo;
import com.csmijo.probbugtags.bean.MyMessage;
import com.csmijo.probbugtags.manager.ClientdataManager;
import com.csmijo.probbugtags.manager.ConfigManager;
import com.csmijo.probbugtags.manager.MyCrashHandler;
import com.csmijo.probbugtags.manager.UploadHistoryLog;
import com.csmijo.probbugtags.manager.UploadLeakDumpHistory;
import com.csmijo.probbugtags.manager.UsinglogManager;
import com.csmijo.probbugtags.service.UploadReportService;
import com.csmijo.probbugtags.utils.CommonUtil;
import com.csmijo.probbugtags.utils.Constants;
import com.csmijo.probbugtags.utils.Logger;
import com.csmijo.probbugtags.utils.RetrofitClient;
import com.csmijo.probbugtags.utils.SharedPrefUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class BugTagAgentReal implements AnrInspector.ANRListener {

    private static final String tag = "BugTagAgentReal";
    public static Handler handler;
    private static Context mContext;
    private static ExecutorService anrCollector = Executors.newSingleThreadExecutor();
    static {
        HandlerThread localHandlerThread = new HandlerThread("BugTagAgentReal");
        localHandlerThread.start();
        handler = new Handler(localHandlerThread.getLooper());
    }

    public BugTagAgentReal() {

    }

    public static void init(final Context context) {
        mContext = context.getApplicationContext();
        AppInfo.init(mContext);
        DeviceInfo.init(mContext);
        BugTagAgentReal.onError(mContext);
        anrCollector.execute(new AnrInspector().setANRListener(new BugTagAgentReal()));
    }

    public static void postOnInit(Context context) {
        mContext = context.getApplicationContext();
        BugTagAgentReal.postHistoryLog(mContext);
        BugTagAgentReal.postLeakDumpHistory(mContext);
    }


    /**
     * @param context
     */
    public static void postHistoryLog(final Context context) {
        Logger.d(tag, "postHistoryLog");
        Thread thread = new UploadHistoryLog(context);
        handler.postDelayed(thread,10000);
    }

    /**
     * 上传leak dump file
     *
     * @param context
     */
    public static void postLeakDumpHistory(Context context) {
        Logger.d(tag, "postLeakDumpHistory");
        Thread thread = new UploadLeakDumpHistory(context);
        handler.postDelayed(thread,15000);
    }


    public static void onResume(Activity activity) {
        final WeakReference<Activity> wActivity = new WeakReference<Activity>(activity);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Logger.d(tag, "Call onResume()");

                Activity activity = wActivity.get();

                ApplicationInit.setCurrentActivity(activity);
                UsinglogManager usinglogManager = new UsinglogManager(mContext);
                usinglogManager.onResume();

            }
        };
        handler.post(runnable);
    }


    public static void onPause(Activity activity) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Logger.d(tag, "Call onPause()");

                UsinglogManager usinglogManager = new UsinglogManager(mContext);
                usinglogManager.onPause();

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
    public static void onError(Context context) {
        Logger.d(tag, "Call onError()");
        MyCrashHandler crashHandler = MyCrashHandler.getInstance();
        crashHandler.init(context.getApplicationContext());
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

//        Thread thread = new Thread(new Runnable() {
//            @Override
//            public void run() {
//                Logger.d(tag, "Call updaeOnlineConfig");
//                ConfigManager cm = new ConfigManager(context);
//                cm.updateOnlineConfig();
//            }
//        });
//        handler.post(thread);
    }

    /**
     * @param isUpdateonlyWifi
     */
    public static void setUpdateOnlyWifi(boolean isUpdateonlyWifi) {
        Constants.mUpdateOnlyWifi = isUpdateonlyWifi;
        Logger.d(tag,
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
        Logger.d(tag,
                "setDefaultReportPolicy = " + String.valueOf(sendPolicy));
    }


    public static void postWebPage(final String pageName) {
        final WeakReference<Context> wContext = new WeakReference<Context>(mContext);
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                Logger.d(tag, "Call postWebPage()");

                UsinglogManager usinglogManager = new UsinglogManager(wContext.get());
                usinglogManager.onWebPage(pageName);
            }
        });
        handler.post(thread);
    }

    @Override
    public void onAppNotResponding(ANRError error) {
        Logger.e("ANR-Watchdog", "Detected Application Not Responding!");
        final String trace = error.getStackTrace("anr",error.getCause());
        try {
            final JSONObject clientInfObject = new ClientdataManager(mContext).prepareClientdataJSON();
            clientInfObject.put("ANRThreadTrace",trace);
            Activity activity = ApplicationInit.getCurrentActivity();
            if (null != activity) {
                clientInfObject.put("activities", activity.getComponentName().getClassName());
            } else {
                clientInfObject.put("activities", "");
            }
            clientInfObject.put("time",CommonUtil.getFormatTime(System.currentTimeMillis()));
            clientInfObject.put("recentActivities", SharedPrefUtil.getValue(mContext, "recent_activity_names", ""));
            if (CommonUtil.getReportPolicyMode(mContext) == BugTagAgentReal.SendPolicy.REALTIME
                    && CommonUtil.isNetworkAvailable(mContext)) {
                Intent intent = new Intent("anr");
                intent.putExtra("content", clientInfObject.toString());
                intent.setClass(mContext.getApplicationContext(), UploadReportService.class);
                mContext.startService(intent);
            } else {
                CommonUtil.saveInfoToFile("anrInfo", clientInfObject,
                        "/cobub.cache", mContext);
            }

            anrCollector.execute(new AnrInspector().setANRListener(new BugTagAgentReal()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    public enum SendPolicy {
        BATCH, REALTIME
    }
}
