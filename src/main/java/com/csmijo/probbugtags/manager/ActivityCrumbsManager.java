package com.csmijo.probbugtags.manager;

import android.app.Activity;

import com.csmijo.probbugtags.collector.BoundedLinkedList;
import com.csmijo.probbugtags.utils.Logger;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * Created by chengqianqian-xy on 2017/3/27.
 */

public class ActivityCrumbsManager {

    private static ActivityCrumbsManager instance = null;
    public  BoundedLinkedList<String> recentActivities = null;
    private WeakReference<Activity> sCurrentActivityWeakRef = null;
    private static final String TAG = ActivityCrumbsManager.class.getSimpleName();

    private ActivityCrumbsManager(){
        recentActivities = new BoundedLinkedList<>(5);
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
            Logger.d(TAG,"sCurrentActivityWeakRef not null , currentActivity = "+ currentActivity.getLocalClassName());
        }else {
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
                Logger.d(TAG,"sCurrentActivityWeakRef null , currentActivity = "+ currentActivity.getLocalClassName());
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
            }
        }
        return currentActivity;
    }

    public void setCurrentActivity(Activity mCurrentActivity) {
        sCurrentActivityWeakRef = new WeakReference<Activity>(mCurrentActivity);
        if (mCurrentActivity != null) {
            recentActivities.add(mCurrentActivity.getComponentName().getClassName());
        }
    }

    public BoundedLinkedList<String> getRecentActivities() {
        return recentActivities;
    }


}
