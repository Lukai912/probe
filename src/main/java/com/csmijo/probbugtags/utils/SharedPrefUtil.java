package com.csmijo.probbugtags.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by chengqianqian-xy on 2016/7/4.
 */
public class SharedPrefUtil {

    //获取SharedPreferences实例
    private static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences("probbug_SharedPref", Context.MODE_PRIVATE);
    }

    //获取Editor实例
    private static SharedPreferences.Editor getEditor(Context context) {
        return getSharedPreferences(context).edit();
    }


    public static void setValue(Context context,String key, long value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putLong(key, value);
        editor.commit();
    }

    public static void removeKey(Context context,String key) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(key);
        editor.commit();
    }

    public static void setValue(Context context,String key, String value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putString(key, value);
        editor.commit();
    }

    public static void setValue(Context context,String key, Boolean value) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.putBoolean(key, value);
        editor.commit();
    }

    public static long getValue(Context context,String key, long defaultValue) {
        return getSharedPreferences(context).getLong(key, defaultValue);
    }

    public static String getValue(Context context,String key, String defaultValue) {
        return getSharedPreferences(context).getString(key, defaultValue);
    }

    public static Boolean getValue(Context context,String key, Boolean defaultValue) {
        return getSharedPreferences(context).getBoolean(key, defaultValue);
    }

    public static void clear(Context context) {
        SharedPreferences.Editor editor = getEditor(context);
        editor.clear();
        editor.commit();
    }

}
