package com.csmijo.probbugtags.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

/**
 * Created by chengqianqian-xy on 2017/3/29.
 */

public class SharedPrefUtil {

    public static void setValue(Context context, String key, long value) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://"+context.getPackageName() + ".com.csmijo.datacontentprovider/info");
        Logger.i("sharedPrefUtil", "setValue");
        ContentValues values = new ContentValues();
        values.put(key, value);


        Cursor cursor = resolver.query(uri, new String[]{"id"}, null, null, "id desc");
        int id = -100;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                if (cursor.moveToNext()) {
                    id = cursor.getInt(cursor.getColumnIndex("id"));
                }
            }
            cursor.close();
        }


        if (id != -100) {
            //update
            resolver.update(uri, values, "id = ?", new String[]{id + ""});
        } else {
            //insert
            resolver.insert(uri, values);
        }


    }

    public static void setValue(Context context, String key, String value) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://"+context.getPackageName() + ".com.csmijo.datacontentprovider/info");

        ContentValues values = new ContentValues();
        values.put(key, value);
        // resolver.insert(uri, values);
        Logger.i("sharedPrefUtil", "setValue");
        Cursor cursor = resolver.query(uri, new String[]{"id"}, null, null, "id desc");
        int id = -100;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                if (cursor.moveToNext()) {
                    id = cursor.getInt(cursor.getColumnIndex("id"));
                }
            }
            cursor.close();
        }


        if (id != -100) {
            //update
            resolver.update(uri, values, "id = ?", new String[]{id + ""});
        } else {
            //insert
            resolver.insert(uri, values);
        }
    }

    public static void setValue(Context context, String key, Boolean value) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://"+context.getPackageName() + ".com.csmijo.datacontentprovider/info");
        Logger.i("sharedPrefUtil", "setValue");
        int insertValue = 0;
        if (value) {
            insertValue = 1;
        }

        ContentValues values = new ContentValues();
        values.put(key, insertValue);
        //resolver.insert(uri, values);

        Cursor cursor = resolver.query(uri, new String[]{"id"}, null, null, "id desc");
        int id = -100;
        if (cursor != null) {
            if (cursor.getCount() > 0) {
                if (cursor.moveToNext()) {
                    id = cursor.getInt(cursor.getColumnIndex("id"));
                }
            }
            cursor.close();
        }


        if (id != -100) {
            //update
            resolver.update(uri, values, "id = ?", new String[]{id + ""});
        } else {
            //insert
            resolver.insert(uri, values);
        }
    }

    public static long getValue(Context context, String key, long defaultValue) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://"+context.getPackageName() + ".com.csmijo.datacontentprovider/info");
        Logger.i("sharedPrefUtil", "getValue");
        Cursor cursor = resolver.query(uri, new String[]{key}, key + " IS NOT NULL", null, "id desc");
        long value = defaultValue;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.isFirst()) {
                    value = cursor.getLong(cursor.getColumnIndex(key));
                    break;
                }
            }
            cursor.close();
        }
        return value;
    }

    public static String getValue(Context context, String key, String defaultValue) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://"+context.getPackageName() + ".com.csmijo.datacontentprovider/info");
        Logger.i("sharedPrefUtil", "getValue");
        Cursor cursor = resolver.query(uri, new String[]{key}, key + " IS NOT NULL", null, "id desc");
        String value = defaultValue;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.isFirst()) {
                    value = cursor.getString(cursor.getColumnIndex(key));
                    break;
                }
            }
            cursor.close();
        }
        return value;
    }

    public static Boolean getValue(Context context, String key, Boolean defaultValue) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://"+context.getPackageName() + ".com.csmijo.datacontentprovider/info");
        Logger.i("sharedPrefUtil", "getValue");
        Cursor cursor = resolver.query(uri, new String[]{key}, key + " IS NOT NULL", null, "id desc");
        boolean value = defaultValue;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.isFirst()) {
                    int tmpValue = cursor.getInt(cursor.getColumnIndex(key));
                    if (tmpValue == 0) {
                        value = false;
                    } else if (tmpValue == 1) {
                        value = true;
                    }
                    break;
                }
            }
            cursor.close();
        }
        return value;
    }

    public static void removeKey(Context context, String key) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://"+context.getPackageName() + ".com.csmijo.datacontentprovider/info");

        resolver.delete(uri, key, null);
    }

    public static void clear(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Uri uri = Uri.parse("content://"+context.getPackageName() + ".com.csmijo.datacontentprovider/info");

        resolver.delete(uri, null, null);
    }

}
