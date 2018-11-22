package com.csmijo.probbugtags.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by chengqianqian-xy on 2017/3/29.
 */

public class DBHelper extends SQLiteOpenHelper {

    private Context mContext;

    private static final String CREATE_INFOS = "create table Infos (" +
            "id integer primary key autoincrement, " +
            "identifier text, " +
            "session_id text, " +
            "session_save_time integer, " +
            "CurrentPage text, " +
            "recent_activity_names text, " +
            "userName text, " +
            "login_save_time integer, " +
            "isLogin integer, " +
            "assignUserNames text" +
            ")";

    private static final String TAG = DBHelper.class.getSimpleName();

    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_INFOS);
        Logger.i(TAG, "onCreate: create successed");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
