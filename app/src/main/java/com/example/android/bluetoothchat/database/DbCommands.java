package com.example.android.bluetoothchat.database;

import static com.example.android.bluetoothchat.database.MessageFeederContract.MessageFeedEntry.COLUMN_NAME_RECEIVER;
import static com.example.android.bluetoothchat.database.MessageFeederContract.MessageFeedEntry.COLUMN_NAME_SENDER;
import static com.example.android.bluetoothchat.database.MessageFeederContract.MessageFeedEntry.COLUMN_NAME_TEXT;
import static com.example.android.bluetoothchat.database.MessageFeederContract.MessageFeedEntry.COLUMN_NAME_TIME;
import static com.example.android.bluetoothchat.database.MessageFeederContract.MessageFeedEntry.TABLE_NAME;

public class DbCommands {

    static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + " ( "+
            COLUMN_NAME_TEXT + " TEXT, " + COLUMN_NAME_SENDER + " TEXT, " + COLUMN_NAME_RECEIVER + " TEXT, " +
            COLUMN_NAME_TIME + " TEXT );";

    static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS"+TABLE_NAME+";";
}
