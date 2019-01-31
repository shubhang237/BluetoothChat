package com.example.android.bluetoothchat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bluetoothchat.adapters.ClickListener;
import com.example.android.bluetoothchat.adapters.RecyclerTouchListener;

import java.sql.Array;
import java.util.ArrayList;

public class CallsFragment extends Fragment {


    ArrayList<String> numList;
    String num;


    public CallsFragment() {
        num = "";
        // Required empty public constructor
        numList = new ArrayList<>();
        for(int i=1;i<=9;i++){
            numList.add(""+i);
        }
        numList.add("*");
        numList.add("0");
        numList.add("#");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment = inflater.inflate(R.layout.fragment_calls, container, false);
        getPermissions();
        final TextView dialedNumber = fragment.findViewById(R.id.dialedNumber);
        final RecyclerView dialPad = fragment.findViewById(R.id.dialPad);
        dialPad.setLayoutManager(new GridLayoutManager(getContext(),3));
        DialAdapter dialAdapter = new DialAdapter(numList);
        dialPad.setAdapter(dialAdapter);
        dialPad.addOnItemTouchListener(new RecyclerTouchListener(getContext(), dialPad, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                //TextView note = dialPad.findViewById(R.id.num);
                num += numList.get(position);
                dialedNumber.setText(num);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        Button clearBtn = fragment.findViewById(R.id.clear);
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(num.length() > 0)
                num = num.substring(0,num.length()-1);
                dialedNumber.setText(num);
            }
        });

        Button call = fragment.findViewById(R.id.call);
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent call_intent = new Intent(Intent.ACTION_CALL);
                Uri numberUri = Uri.parse("tel:"+num);
                call_intent.setData(numberUri);
                startActivity(call_intent);
            }
        });

        return fragment;
    }


    private class DialAdapter extends RecyclerView.Adapter<DialViewHolder> {

        ArrayList<String> numList;

        public DialAdapter(ArrayList<String> list) {
            this.numList = list;
        }

        @NonNull
        @Override
        public DialViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dial_note,viewGroup,false);
            return new DialViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull DialViewHolder holder, int i) {
          holder.number.setText(numList.get(i));
        }

        @Override
        public int getItemCount() {
            return numList.size();
        }
    }

    private class DialViewHolder extends RecyclerView.ViewHolder {

        TextView number;
        public DialViewHolder(@NonNull View itemView) {
            super(itemView);
            number = itemView.findViewById(R.id.num);
        }
    }

    private void getPermissions() {
        int perm = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CALL_PHONE);
        if(perm == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(getActivity(),new String[]{
                    Manifest.permission.CALL_PHONE
            },121);

        }
    }
}
