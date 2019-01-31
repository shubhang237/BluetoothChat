package com.example.android.bluetoothchat.models;

public class device {
    public int status;
    public String address;
    public String name;

    public device(int b, String s,String address) {
        this.status = b;
        this.name = s;
        this.address = address;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setName(String name) {
        this.name = name;
    }
}
