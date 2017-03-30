package com.csmijo.probbugtags.utils;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by chengqianqian-xy on 2017/3/29.
 */

public class DataContentProvider extends ContentProvider {


    DBHelper mDBHelper;

    public static final String AUTHORITY = "com.csmijo.datacontentprovider";

    private static UriMatcher uriMatcher;

    private static final int INFO_DIR = 0;
    private static final int INFO_ITEM = 1;


    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "info", INFO_ITEM);
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new DBHelper(getContext(), "Infos.db", null, 1);
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //查询数据库
        SQLiteDatabase database = mDBHelper.getReadableDatabase();
        Cursor cursor = null;
        switch (uriMatcher.match(uri)) {

            case INFO_ITEM:
                cursor = database.query("Infos", projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                break;
        }
        return cursor;
    }


    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //添加数据
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        Uri uriReturn = null;
        switch (uriMatcher.match(uri)) {

            case INFO_ITEM:
                long newItemId = database.insert("Infos", null, values);
                uriReturn = Uri.parse("content://" + AUTHORITY + "/info/" + newItemId);
                break;
            default:
                break;
        }
        return uriReturn;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // 删除数据
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        int deleteRows = 0;
        switch (uriMatcher.match(uri)) {

            case INFO_ITEM:
                deleteRows = database.delete("Infos", selection, selectionArgs);
                break;
            default:
                break;
        }

        return deleteRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        //更新数据
        SQLiteDatabase database = mDBHelper.getWritableDatabase();
        int updateRows = 0;
        switch (uriMatcher.match(uri)) {

            case INFO_ITEM:
                /*String itemId = uri.getPathSegments().get(1);
                updateRows = database.update("Infos", values, "id=?", new String[]{itemId});*/
                updateRows = database.update("Infos", values, selection, selectionArgs);
                break;
            default:
                break;
        }
        return updateRows;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch (uriMatcher.match(uri)) {
            case INFO_DIR:
                return "vnd.android.cursor.dir/vnd.com.csmijo.datacontentprovider.info";
            case INFO_ITEM:
                return "vnd.android.cursor.item/vnd.com.csmijo.datacontentprovider.info";
        }
        return null;
    }
}
