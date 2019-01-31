package com.example.android.bluetoothchat;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.BreakIterator;

public class InitialConfig extends AppCompatActivity {

    private static final String TAG = "init";
    BluetoothAdapter mBluetoothAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initial_config);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        enableDisableBT();


        EditText etName = findViewById(R.id.etName);
        String userName = etName.getText().toString();
        char nameI;
        if(userName == null || userName.length() > 0)
            nameI = userName.charAt(0);
        else
            nameI = '?';

        Drawable imageIcon = new TextDrawable(""+nameI);
        ImageView userIcon = findViewById(R.id.userIcon);
        userIcon.setImageDrawable(imageIcon);

//        TextView name_initial = findViewById(R.id.nameInitials);
//        name_initial.setText(userName.toUpperCase());
        TextView name = findViewById(R.id.username_bluetooth);
        name.setText(userName);
        TextView deviceName = findViewById(R.id.tvDeviceName);
        if(mBluetoothAdapter != null)
        deviceName.setText(": "+mBluetoothAdapter.getName());
    }



    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          String action = intent.getAction();

            if(action.equals(mBluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, mBluetoothAdapter.ERROR);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };

    public void enableDisableBT(){
        if(mBluetoothAdapter == null){
            Log.d(TAG, "enableDisableBT: Does not have BT capabilities.");
        }
        if(mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()){
            Log.d(TAG, "enableDisableBT: enabling BT.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);
            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mReceiver, BTIntent);
        }
    }

}
