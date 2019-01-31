package com.example.android.bluetoothchat.models;

import java.sql.Time;
import java.util.Date;

public class Message {

    public String text;
    public String sender;
    String receiver;
    public String systemTime;

    public Message(String message, String sender, String receiver, String systemTime) {
        this.text = message;
        this.sender = sender;
        this.receiver = receiver;
        this.systemTime = systemTime;
    }

    public String getSender() {
        return sender;
    }

    public Message() {


    }
}
