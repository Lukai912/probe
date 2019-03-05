package com.squareup.leakcanary;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public interface FragmentRefWatcher {
    void watchFragments(Activity activity);

    final class Helper {

        private static final String SUPPORT_FRAGMENT_REF_WATCHER_CLASS_NAME =
                "com.squareup.leakcanary.internal.SupportFragmentRefWatcher";

        public static void install(Context context, RefWatcher refWatcher) {
            List<FragmentRefWatcher> fragmentRefWatchers = new ArrayList<>();

//            if (SDK_INT >= O) {
//                fragmentRefWatchers.add(new AndroidOFragmentRefWatcher(refWatcher));
//            }

            try {
                Class<?> fragmentRefWatcherClass = Class.forName(SUPPORT_FRAGMENT_REF_WATCHER_CLASS_NAME);
                Constructor<?> constructor =
                        fragmentRefWatcherClass.getDeclaredConstructor(RefWatcher.class);
                FragmentRefWatcher supportFragmentRefWatcher =
                        (FragmentRefWatcher) constructor.newInstance(refWatcher);
                fragmentRefWatchers.add(supportFragmentRefWatcher);
            } catch (Exception ignored) {
            }

            if (fragmentRefWatchers.size() == 0) {
                return;
            }

            Helper helper = new Helper(fragmentRefWatchers);

            Application application = (Application) context.getApplicationContext();
            application.registerActivityLifecycleCallbacks(helper.activityLifecycleCallbacks);
        }

        private final Application.ActivityLifecycleCallbacks activityLifecycleCallbacks =
                new ActivityLifecycleCallbacksAdapter() {
                    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                        for (FragmentRefWatcher watcher : fragmentRefWatchers) {
                            watcher.watchFragments(activity);
                        }
                    }
                };

        private final List<FragmentRefWatcher> fragmentRefWatchers;

        private Helper(List<FragmentRefWatcher> fragmentRefWatchers) {
            this.fragmentRefWatchers = fragmentRefWatchers;
        }
    }
}
