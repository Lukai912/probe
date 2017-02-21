package com.csmijo.probbugtags;

import android.app.Activity;
import android.app.Application;
import android.content.Context;

import com.csmijo.probbugtags.collector.BoundedLinkedList;
import com.csmijo.probbugtags.utils.Constants;
import com.squareup.leakcanary.AndroidExcludedRefs;
import com.squareup.leakcanary.DisplayLeakService;
import com.squareup.leakcanary.ExcludedRefs;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.io.FileInputStream;
import java.io.IOException;

public class ApplicationInit {

    private static RefWatcher refWatcher;
    private static Activity currentActivity = null;
    public static BoundedLinkedList<String> recentActivities;

    public static void onCreateInit(Application mApplication) {
        // 不监听特定的对象
        ExcludedRefs excludedRefs = AndroidExcludedRefs
                .createAndroidDefaults()
                .build();

        refWatcher = LeakCanary.install(mApplication, DisplayLeakService.class,
                excludedRefs);

        String processName = getCurrentProcessName();

        if (null != processName) {
            boolean defaultProcess = processName.equalsIgnoreCase(mApplication
                    .getPackageName());
            if (defaultProcess) {
                recentActivities = new BoundedLinkedList<>(5);

                BugTagAgent.init(mApplication.getApplicationContext());
                BugTagAgent.setDebugEnabled(true);
                BugTagAgent.setDebugLevel(Constants.Verbose);

                BugTagAgent.updateOnlineConfig(mApplication.getApplicationContext());
//                BugTagAgent.startPerformService(mApplication.getApplicationContext());
//                BugTagAgent.startBugTagFab(mApplication.getApplicationContext());

                // 根据需求添加
                /*
                 * UmsAgent.postTags(this, "test tags");
				 * UmsAgent.postPushID(this, "cid"); UmsAgent.bindUserid(this,
				 * "user id");
				 */
            }
        }

    }

    /**
     * @param context
     */
    public static void watch(Context context) {
        refWatcher.watch(context);
    }

    /**
     * 返回当前的进程名
     *
     * @return
     */
    public static String getCurrentProcessName() {
        FileInputStream in = null;
        try {
            String fn = "/proc/self/cmdline";
            in = new FileInputStream(fn);
            byte[] buffer = new byte[256];
            int len = 0;
            int b;
            while ((b = in.read()) > 0 && len < buffer.length) {
                buffer[len++] = (byte) b;
            }
            if (len > 0) {
                return new String(buffer, 0, len, "UTF-8");
            }
        } catch (Throwable e) {
        } finally {

            try {
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return null;
    }

    public static Activity getCurrentActivity() {
        return currentActivity;
    }

    public static void setCurrentActivity(Activity mCurrentActivity) {
        currentActivity = mCurrentActivity;
    }

    public static void clearReferences(Activity activity) {
        if (activity.equals(currentActivity)) {
            recentActivities.add(currentActivity.getComponentName().getClassName());
            ApplicationInit.setCurrentActivity(null);
        }
    }

}
