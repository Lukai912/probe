package com.csmijo.probbugtags.hookTest2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

/**
 * 4.0以上系统Activity生命周期回调接口
 * Created by chengqianqian-xy on 2017/3/16.
 */

@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class UpIceActivityLifeCallBack implements Application.ActivityLifecycleCallbacks {
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        ActivityLifeManager.getInstance().onActivityCreated(activity, savedInstanceState);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        ActivityLifeManager.getInstance().onActivityStarted(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        ActivityLifeManager.getInstance().onActivityResumed(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        ActivityLifeManager.getInstance().onActivityPaused(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        ActivityLifeManager.getInstance().onActivityStopped(activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        ActivityLifeManager.getInstance().onActivitySaveInstanceState(activity, outState);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        ActivityLifeManager.getInstance().onActivityDestroyed(activity);
    }
}
