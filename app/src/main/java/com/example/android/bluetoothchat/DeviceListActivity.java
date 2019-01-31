package com.example.android.bluetoothchat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bluetoothchat.adapters.ClickListener;
import com.example.android.bluetoothchat.adapters.DeviceListAdapter;
import com.example.android.bluetoothchat.adapters.RecyclerTouchListener;
import com.example.android.bluetoothchat.models.device;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class DeviceListActivity extends AppCompatActivity {

    private static final String TAG = "DeviceListActivity";
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private ArrayList<device> mNewDevicesList;
    private ArrayList<device> pairedDeviceList;
    private HashMap<String,device> pairedSet;
    private HashMap<String,device> newSet;
    private DeviceListAdapter newDevicesListAdapter;
    private DeviceListAdapter pairedDevicesListAdapter;
    public RecyclerView pairedRecyclerView;
    public RecyclerView mNewRecyclerView;
    private BluetoothAdapter mBluetoothAdapter = null;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_list);
        setResult(Activity.RESULT_CANCELED);
        getPermissions();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        enableDisableBT();
        doDiscovery();
        setProgressBarIndeterminateVisibility(true);
        ensureDiscoverable();
        setUpListeners();
        registerBroadcasters();
        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
           // findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                device d = new device(1,device.getName(),device.getAddress());
                 pairedSet.put(device.getAddress(),d);
                 Log.d("paired devices",d.address);
                 pairedDeviceList.add(d);
                 pairedDevicesListAdapter.notifyDataSetChanged();
            }
        }
        else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            pairedDeviceList.add(new device(0,noDevices,""));
            pairedDevicesListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_option,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.discoverable){
            ensureDiscoverable();
        }
        return super.onOptionsItemSelected(item);
    }

    private void registerBroadcasters() {
        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);
    }


    public void setUpListeners(){
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                doDiscovery();
            }
        });

        mNewDevicesList = new ArrayList<>();
        pairedDeviceList = new ArrayList<>();
        pairedSet = new HashMap<>();
        newSet = new HashMap<>();

        // Find and set up the ListView for paired devices
        pairedRecyclerView = findViewById(R.id.paired_devices);
        pairedDevicesListAdapter = new DeviceListAdapter(getBaseContext(),pairedDeviceList);
        pairedRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        pairedRecyclerView.setAdapter(pairedDevicesListAdapter);
        pairedRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(DeviceListActivity.this, pairedRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                mBluetoothAdapter.cancelDiscovery();

                if (pairedDeviceList.get(position).status == 1) {

                    Toast.makeText(DeviceListActivity.this, "Device Not Available", Toast.LENGTH_SHORT).show();
                } else {
                    //TextView name = view.findViewById(R.id.device_name);
                    TextView addr = view.findViewById(R.id.device_address);
                    // Get the device MAC address, which is the last 17 chars in the View
                    String address = addr.getText().toString();

                    // Create the result Intent and include the MAC address
                    Intent intent = new Intent(DeviceListActivity.this, MainActivity.class);
                    intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

        mNewRecyclerView = findViewById(R.id.new_devices);
        newDevicesListAdapter = new DeviceListAdapter(getBaseContext(),mNewDevicesList);
        mNewRecyclerView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        mNewRecyclerView.setAdapter(newDevicesListAdapter);
        mNewRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(DeviceListActivity.this, mNewRecyclerView, new ClickListener() {
            @Override
            public void onClick(View view, int position) {
                mBluetoothAdapter.cancelDiscovery();

                //TextView name = view.findViewById(R.id.device_name);
                TextView addr = view.findViewById(R.id.device_address);
                // Get the device MAC address, which is the last 17 chars in the View
                String address = addr.getText().toString();

                // Create the result Intent and include the MAC address
                Intent intent = new Intent(DeviceListActivity.this,MainActivity.class);
                intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
                startActivity(intent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));

    }

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

    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Toast.makeText(DeviceListActivity.this,"Discovery Mode On",Toast.LENGTH_SHORT);
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBluetoothAdapter != null) {
            mBluetoothAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        // Request discover from BluetoothAdapter
        mBluetoothAdapter.startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @SuppressLint("NewApi")
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
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if(!newSet.containsKey(device.getAddress())) {
                        device newDevice = new device(2, device.getName(), device.getAddress());
                        Log.d("new found devices ", device.getAddress());
                        newSet.put(device.getAddress(), newDevice);
                        mNewDevicesList.add(newDevice);
                        newDevicesListAdapter.notifyDataSetChanged();
                    }
                }
                else
                {
                     if(pairedSet.containsKey(device.getAddress())){
                         for(int i=0;i<pairedDeviceList.size();i++) {
                             device d = pairedDeviceList.get(i);
                             if (device.getAddress().toString().equals(d.address)) {
                                 Log.d("found devices ", device.getAddress());
                                 d.setStatus(2);
                                 pairedDeviceList.remove(i);
                                 pairedDeviceList.add(d);
                                 pairedSet.remove(d.address);
                                 pairedSet.put(d.address,d);
                             }
                         }
                     }
                     pairedDevicesListAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                swipeRefreshLayout.setRefreshing(false);
                if (mNewDevicesList.size() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewDevicesList.add(new device(0,noDevices,""));
                    newDevicesListAdapter.notifyDataSetChanged();
                }
                if(pairedDeviceList.size() == 0){
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    pairedDeviceList.add(new device(0,noDevices,""));
                    pairedDevicesListAdapter.notifyDataSetChanged();
                }
                setTitle(R.string.app_name);
            }
        }
    };

    private void getPermissions() {
        int perm1 = ContextCompat.checkSelfPermission(DeviceListActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
        int perm2 = ContextCompat.checkSelfPermission(DeviceListActivity.this,Manifest.permission.ACCESS_FINE_LOCATION);
        if(perm1 == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION
            },121);
        }
        if(perm2 == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this,new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            },121);
        }
    }
}