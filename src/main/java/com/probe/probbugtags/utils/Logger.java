package com.csmijo.probbugtags.utils;

import android.util.Log;

/**
 * Created by chengqianqian-xy on 2016/5/30.
 */
public class Logger {

    public static final String TAG_PREFIX = "tanzhen_";

    public static void v(String tag, String msg) {

        if (!Constants.DebugEnabled) {
            return;
        }

        if (Constants.DebugLevel > Constants.Verbose)
            return;

        Log.v(TAG_PREFIX + tag, msg);
    }

    public static void d(String tag, String msg) {

        if (!Constants.DebugEnabled)
            return;

        if (Constants.DebugLevel > Constants.Debug)
            return;

        Log.d(TAG_PREFIX + tag, msg);
    }

    public static void i(String tag, String msg) {

        if (!Constants.DebugEnabled)
            return;

        if (Constants.DebugLevel > Constants.Info)
            return;

        Log.i(TAG_PREFIX + tag, msg);
    }

    public static void w(String tag, String msg) {

        if (!Constants.DebugEnabled)
            return;

        if (Constants.DebugLevel > Constants.Warn)

            Log.w(TAG_PREFIX + tag, msg);
    }

    public static void e(String tag, String msg) {

        if (!Constants.DebugEnabled)
            return;

        if (Constants.DebugLevel > Constants.Error)
            return;
        Log.e(TAG_PREFIX + tag, msg);
    }

    public static void e(String tag, Exception e) {
        if (!Constants.DebugEnabled)
            return;

        if (Constants.DebugLevel > Constants.Error)
            return;

        Log.e(TAG_PREFIX + tag, e.toString());
        e.printStackTrace();
    }

}
