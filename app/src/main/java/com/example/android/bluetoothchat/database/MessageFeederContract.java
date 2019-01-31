package com.example.android.bluetoothchat.database;

import android.provider.BaseColumns;

public final class MessageFeederContract {

   private MessageFeederContract() {
    }

    public static  class MessageFeedEntry implements BaseColumns{
       public static final String TABLE_NAME = "message";
       public static final String COLUMN_NAME_TEXT = "text";
       public static final String COLUMN_NAME_SENDER = "sender";
       public static final String COLUMN_NAME_RECEIVER = "receiver";
       public static final String COLUMN_NAME_TIME = "time";
    }
}

