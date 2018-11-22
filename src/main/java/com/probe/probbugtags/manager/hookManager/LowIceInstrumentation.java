package com.probe.probbugtags.manager.hookManager;

import android.app.Activity;
import android.app.Instrumentation;
import android.os.Bundle;

/**
 * 自定义 Instrumentation
 * Created by chengqianqian-xy on 2017/3/16.
 */

public class LowIceInstrumentation extends Instrumentation{

    @Override
    public void callActivityOnCreate(Activity activity, Bundle icicle) {
        super.callActivityOnCreate(activity, icicle);
        ActivityLifeManager.getInstance().onActivityCreated(activity, icicle);
    }

    @Override
    public void callActivityOnStart(Activity activity) {
        super.callActivityOnStart(activity);
        ActivityLifeManager.getInstance().onActivityStarted(activity);
    }

    @Override
    public void callActivityOnResume(Activity activity) {
        super.callActivityOnResume(activity);
        ActivityLifeManager.getInstance().onActivityResumed(activity);
    }

    @Override
    public void callActivityOnPause(Activity activity) {
        super.callActivityOnPause(activity);
        ActivityLifeManager.getInstance().onActivityPaused(activity);
    }

    @Override
    public void callActivityOnStop(Activity activity) {
        super.callActivityOnStop(activity);
        ActivityLifeManager.getInstance().onActivityStopped(activity);
    }

    @Override
    public void callActivityOnSaveInstanceState(Activity activity, Bundle outState) {
        super.callActivityOnSaveInstanceState(activity, outState);
        ActivityLifeManager.getInstance().onActivitySaveInstanceState(activity,outState);
    }

    @Override
    public void callActivityOnDestroy(Activity activity) {
        super.callActivityOnDestroy(activity);
        ActivityLifeManager.getInstance().onActivityDestroyed(activity);
    }
}
