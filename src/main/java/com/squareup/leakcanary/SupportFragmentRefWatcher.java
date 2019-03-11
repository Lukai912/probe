package com.squareup.leakcanary;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;

public class SupportFragmentRefWatcher implements FragmentRefWatcher {
    private final RefWatcher refWatcher;
    public SupportFragmentRefWatcher(RefWatcher refWatcher) {
        this.refWatcher = refWatcher;
    }

    private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks =
            new FragmentManager.FragmentLifecycleCallbacks() {

                @Override public void onFragmentViewDestroyed(FragmentManager fm, Fragment fragment) {
                    View view = fragment.getView();
                    if (view != null) {
                        Log.d("probe","viewDestroyWatch:"+fragment.getClass().getName());
                        refWatcher.watch(view);
                    }
                }

                @Override public void onFragmentDestroyed(FragmentManager fm, Fragment fragment) {
                    Log.d("probe","fragmentDesWatch:"+fragment.getClass().getName());
                    refWatcher.watch(fragment);
                }
            };

    @Override public void watchFragments(Activity activity) {
        if (activity instanceof FragmentActivity) {
            FragmentManager supportFragmentManager =
                    ((FragmentActivity) activity).getSupportFragmentManager();
            supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true);
        }
    }
}
