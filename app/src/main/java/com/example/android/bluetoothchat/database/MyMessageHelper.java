package com.example.android.bluetoothchat.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.android.bluetoothchat.database.DbCommands.SQL_CREATE_TABLE;
import static com.example.android.bluetoothchat.database.DbCommands.SQL_DROP_TABLE;

public class MyMessageHelper extends SQLiteOpenHelper {

    public MyMessageHelper(Context context) {
        super(context, "message",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DROP_TABLE);
        onCreate(db);
    }
}
