package com.example.android.bluetoothchat.adapters;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.bluetoothchat.R;
import com.example.android.bluetoothchat.models.device;

import java.util.ArrayList;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceListViewHolder>{


    LayoutInflater inflater;
    private ArrayList<device> list;

    public DeviceListAdapter(Context context, ArrayList<device> list)
    {
        this.list = list;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public DeviceListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = inflater.inflate(R.layout.user_status,parent,false);
        return new DeviceListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceListViewHolder holder, int i) {
        if(list.get(i).status == 1)
            holder.status.setImageResource(R.drawable.ic_fiber_manual_record_red_24dp);
        else if(list.get(i).status == 2)
            holder.status.setImageResource(R.drawable.ic_fiber_manual_record_green_24dp);
        if(list.get(i).name == null){
            holder.device_name.setText("Unknown Device");
        }
        else
        holder.device_name.setText(list.get(i).name);
        holder.device_address.setText(list.get(i).address);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class DeviceListViewHolder extends RecyclerView.ViewHolder {

        TextView device_name,device_address;
        ImageView status;

        public DeviceListViewHolder(@NonNull View itemView) {
            super(itemView);
            device_name = itemView.findViewById(R.id.device_name);
            status = itemView.findViewById(R.id.status);
            device_address = itemView.findViewById(R.id.device_address);
        }
    }

}

