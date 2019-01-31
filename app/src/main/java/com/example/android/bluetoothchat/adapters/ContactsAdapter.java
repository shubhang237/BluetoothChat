package com.example.android.bluetoothchat.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.TextView;

import com.example.android.bluetoothchat.R;
import com.example.android.bluetoothchat.models.Contact;

import java.util.ArrayList;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    LayoutInflater layoutInflater;
    ArrayList<Contact> list;


    public ContactsAdapter(Context context, ArrayList<Contact> contactList) {

        this.list = contactList;
        this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {


        View view = layoutInflater.inflate(R.layout.contact_item, parent, false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int i) {
       holder.name.setText(list.get(i).name);
       holder.phoneNo.setText(list.get(i).phone);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ContactsViewHolder extends RecyclerView.ViewHolder{

        TextView name,phoneNo;
        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            phoneNo = itemView.findViewById(R.id.phoneNo);
        }
    }
}
