package com.example.android.bluetoothchat;


import android.Manifest;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.bluetoothchat.adapters.ContactsAdapter;
import com.example.android.bluetoothchat.models.Contact;

import java.util.ArrayList;
import java.util.HashMap;

public class StatusFragment extends Fragment {

    HashMap<String,Contact> contacts;
    ArrayList<Contact>ContactList;
    ContactsAdapter contactAdapter;
    RecyclerView recyclerView;

    public StatusFragment() {
          ContactList = new ArrayList<>();
          contacts = new HashMap<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View fragment =  inflater.inflate(R.layout.fragment_status, container, false);
        getPermissions();
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.contactList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactAdapter = new ContactsAdapter(getContext(),ContactList);
        recyclerView.setAdapter(contactAdapter);
        getAllContacts(getContext().getContentResolver());
    }

    private void getPermissions() {
        int perm = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS);
        perm += ContextCompat.checkSelfPermission(getContext(),Manifest.permission.WRITE_CONTACTS);
        if(perm < 0){
            ActivityCompat.requestPermissions(getActivity(),new String[]{
                    Manifest.permission.READ_CONTACTS,
                    Manifest.permission.WRITE_CONTACTS
            },121);

        }
    }


    public  void getAllContacts(ContentResolver cr) {

        Cursor phones = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
        int count = 0;
        while (phones.moveToNext())
        {
            Log.d("Device",""+count++);
            String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            Contact c = new Contact(name,phoneNumber);
             if(!contacts.containsKey(name)){
                 contacts.put(name,c);
                 ContactList.add(c);
             }

        }
        contactAdapter.notifyDataSetChanged();
        phones.close();
    }
}










