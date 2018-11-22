package com.probe.probbugtags.manager;

import android.app.Activity;

import com.probe.probbugtags.ApplicationInit;
import com.probe.probbugtags.utils.Logger;
import com.probe.probbugtags.utils.SharedPrefUtil;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by chengqianqian-xy on 2017/3/27.
 */

public class ActivityCrumbsManager {

    private static ActivityCrumbsManager instance = null;
    private WeakReference<Activity> sCurrentActivityWeakRef = null;
    private int callTimes = 0;

    private static final String TAG = ActivityCrumbsManager.class.getSimpleName();

    private ActivityCrumbsManager() {

    }

    public static ActivityCrumbsManager getInstance() {
        if (instance == null) {
            synchronized (ActivityCrumbsManager.class) {
                if (instance == null) {
                    instance = new ActivityCrumbsManager();
                }
            }
        }
        return instance;
    }

    public Activity getCurrentActivity() {
        Activity currentActivity = null;
        if (sCurrentActivityWeakRef != null) {
            currentActivity = sCurrentActivityWeakRef.get();
            Logger.d(TAG, "sCurrentActivityWeakRef not null , currentActivity = " + currentActivity.getLocalClassName());
        } else {
            //1 获取ActivityThread中所有的ActivityRecord
            //2 从ActivityRecord中获取状态不是pause的Activity并返回
            try {
                Class activityThreadClass = Class.forName("android.app.ActivityThread");
                Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(null);
                Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
                activitiesField.setAccessible(true);

                Map activities = (Map) activitiesField.get(activityThread);
                for (Object activityRecord : activities.values()) {
                    Class activityRecordClass = activityRecord.getClass();
                    Field pausedField = activityRecordClass.getDeclaredField("paused");
                    pausedField.setAccessible(true);

                    if (!pausedField.getBoolean(activityRecord)) {
                        Field activityField = activityRecordClass.getDeclaredField("activity");
                        activityField.setAccessible(true);
                        currentActivity = (Activity) activityField.get(activityRecord);
                    }
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return currentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        sCurrentActivityWeakRef = new WeakReference<Activity>(mCurrentActivity);

        // 第一次清空sqlite中的recent_activity_names
        String processName = ApplicationInit.getCurrentProcessName();
        if (null != processName) {
            boolean defaultProcess = processName.equalsIgnoreCase(mCurrentActivity.getApplicationContext().getPackageName());
            if (defaultProcess && callTimes == 0) {
                SharedPrefUtil.setValue(mCurrentActivity.getApplicationContext(), "recent_activity_names", "");
                callTimes++;
            }
        }

        StringBuffer recentActivityNames = new StringBuffer();

        if (mCurrentActivity != null) {
            String recentNames = SharedPrefUtil.getValue(mCurrentActivity.getApplicationContext(), "recent_activity_names", "");
            if (recentNames != null && recentNames.trim().length() > 0) {
                String[] activityNames = recentNames.split(",");
                if (activityNames.length >= 5) {
                    for (int i = 1; i < 5; i++) {
                        recentActivityNames.append(activityNames[i]).append(",");
                    }
                    recentActivityNames.append(mCurrentActivity.getComponentName().getClassName());
                } else {
                    recentActivityNames.append(recentNames);
                    recentActivityNames.append(mCurrentActivity.getComponentName().getClassName()).append(",");
                }
            } else {
                recentActivityNames.append(mCurrentActivity.getComponentName().getClassName()).append(",");
            }

            SharedPrefUtil.setValue(mCurrentActivity.getApplicationContext(), "recent_activity_names", recentActivityNames.toString());
        }

    }
}
