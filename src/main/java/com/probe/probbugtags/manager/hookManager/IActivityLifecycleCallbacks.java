package com.probe.probbugtags.manager.hookManager;

import android.app.Activity;
import android.os.Bundle;

/**
 * 4.0以下自定义activity生命周期回调接口
 * Created by chengqianqian-xy on 2017/3/16.
 */

public interface IActivityLifecycleCallbacks {

    void onActivityCreated(Activity activity, Bundle savedInstanceState);
    void onActivityStarted(Activity activity);
    void onActivityResumed(Activity activity);
    void onActivityPaused(Activity activity);
    void onActivityStopped(Activity activity);
    void onActivitySaveInstanceState(Activity activity, Bundle outState);
    void onActivityDestroyed(Activity activity);
}
