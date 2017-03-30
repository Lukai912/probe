package com.csmijo.probbugtags.manager.hookManager;

import android.app.Activity;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

/**
 * 生命周期管理类
 * Created by chengqianqian-xy on 2017/3/16.
 */

public class ActivityLifeManager implements IActivityLifecycleCallbacks {

    private static volatile ActivityLifeManager activityManager;
    private List<IActivityLifecycleCallbacks> mLifeChanges = new ArrayList<>();

    public static ActivityLifeManager getInstance() {
        if (activityManager == null) {
            synchronized (ActivityLifeManager.class) {
                if (activityManager == null) {
                    activityManager = new ActivityLifeManager();
                }
            }
        }
        return activityManager;
    }

    private ActivityLifeManager() {

    }

    public void addIActivityLifeChange(IActivityLifecycleCallbacks lis) {
        mLifeChanges.add(lis);
    }


    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        for (IActivityLifecycleCallbacks lis : mLifeChanges) {
            lis.onActivityCreated(activity, savedInstanceState);
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        for (IActivityLifecycleCallbacks lis : mLifeChanges) {
            lis.onActivityStarted(activity);
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        for (IActivityLifecycleCallbacks lis : mLifeChanges) {
            lis.onActivityResumed(activity);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        for (IActivityLifecycleCallbacks lis : mLifeChanges) {
            lis.onActivityPaused(activity);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        for (IActivityLifecycleCallbacks lis : mLifeChanges) {
            lis.onActivityStopped(activity);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        for (IActivityLifecycleCallbacks lis : mLifeChanges) {
            lis.onActivitySaveInstanceState(activity, outState);
        }
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        for (IActivityLifecycleCallbacks lis : mLifeChanges) {
            lis.onActivityDestroyed(activity);
        }
    }
}
