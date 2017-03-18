package com.bluetoothchat.www.bluetoothchat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by SS on 17-2-8.
 */
public class SaveDate extends SQLiteOpenHelper {
    private static final String CREATE_BOOK = "CREATE TABLE info("
            +"_id INTEGER PRIMARY KEY AUTOINCREMENT,"
            +"time TEXT,"
            +"content TEXT,"
            +"current INTEGER)";
    public SaveDate(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_BOOK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
