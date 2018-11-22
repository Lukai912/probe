package com.csmijo.probbugtags.hookTest2;

import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;

import com.csmijo.probbugtags.BugTagAgent;
import com.csmijo.probbugtags.utils.Logger;
import com.squareup.leakcanary.AndroidExcludedRefs;
import com.squareup.leakcanary.DisplayLeakService;
import com.squareup.leakcanary.ExcludedRefs;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by chengqianqian-xy on 2017/3/16.
 */

public class LifecycleAgent {

    public static final String TAG = LifecycleAgent.class.getSimpleName();

    public static void init(Application app) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            // 4.0以上使用系统自带的Activity生命周期监听方式
            // Make sure you don't get installed twice.
            UpIceActivityLifeCallBack upIceActivityLifeCallBack = new UpIceActivityLifeCallBack();
            app.unregisterActivityLifecycleCallbacks(upIceActivityLifeCallBack);
            app.registerActivityLifecycleCallbacks(upIceActivityLifeCallBack);
        } else {
            // 4.0以下，替换Instrumentation，实现Activity的生命周期监听
            replaceInstrumentation();
        }

        // 不监听特定的对象
        ExcludedRefs excludedRefs = AndroidExcludedRefs
                .createAndroidDefaults()
                .build();
        // install LeakCanary
        final RefWatcher refWatcher = LeakCanary.install(app, DisplayLeakService.class,
                excludedRefs);

        //最终两种方式生命周期统一回调
        ActivityLifeManager.getInstance().addIActivityLifeChange(new IActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                Logger.i(TAG, "onActivityCreated: " + activity.getLocalClassName().toString());
            }

            @Override
            public void onActivityStarted(Activity activity) {
                Logger.i(TAG, "onActivityStarted: " + activity.getLocalClassName().toString());
            }

            @Override
            public void onActivityResumed(Activity activity) {
                Logger.i(TAG, "onActivityResumed: " + activity.getLocalClassName().toString());
                BugTagAgent.onResume(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                Logger.i(TAG, "onActivityPaused: " + activity.getLocalClassName().toString());
                BugTagAgent.onPause(activity);
            }

            @Override
            public void onActivityStopped(Activity activity) {
                Logger.i(TAG, "onActivityStopped: " + activity.getLocalClassName().toString());
            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
                Logger.i(TAG, "onActivitySaveInstanceState: " + activity.getLocalClassName().toString());
            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                Logger.i(TAG, "onActivityDestroyed: " + activity.getLocalClassName().toString());
                refWatcher.watch(activity);
            }
        });

    }

    /**
     * 替换系统默认的Instrumentation
     */
    private static void replaceInstrumentation() {
        try {
            // 加载activity thread 的class
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");

            // 找到方法currentActivityThread
            Method method = activityThreadClass.getDeclaredMethod("currentActivityThread");
            // 由于这个方法是静态的，所以传入Null就行了
            Object currentActivityThread = method.invoke(null);

            // 把之前ActivityThread中的mInstrumentation替换成我们的Instrumentation
            Field field = activityThreadClass.getDeclaredField("mInstrumentation");
            field.setAccessible(true);

            LowIceInstrumentation mInstrumentation = new LowIceInstrumentation();
            field.set(currentActivityThread, mInstrumentation);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

    }

}
