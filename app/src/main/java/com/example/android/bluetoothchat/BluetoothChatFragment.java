package com.example.android.bluetoothchat;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bluetoothchat.database.Constants;
import com.example.android.bluetoothchat.database.MessageFeederContract.MessageFeedEntry;
import com.example.android.bluetoothchat.adapters.MessageAdapter;
import com.example.android.bluetoothchat.controllers.BluetoothChatService;
import com.example.android.bluetoothchat.database.MyMessageHelper;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.example.android.bluetoothchat.database.MessageFeederContract.MessageFeedEntry.TABLE_NAME;

public class BluetoothChatFragment extends Fragment {

    private static final String TAG = "BluetoothChatFragment";

    private static MyMessageHelper mDBHelper;
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    // Layout Views
    //private ListView mConversationView;

    private RecyclerView mConversationView1;
    private EditText mOutEditText;
    private Button mSendButton;
    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    private NotificationManager notificationManager;

    /**
     * Array adapter for the conversation thread
     */
    //private ArrayAdapter<String> mConversationArrayAdapter;

    private MessageAdapter mConversationAdapter;
//
    private ArrayList<com.example.android.bluetoothchat.models.Message> messages;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            FragmentActivity activity = getActivity();
            Toast.makeText(activity, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            activity.finish();
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        mDBHelper = new MyMessageHelper(getContext());
        notificationManager = (NotificationManager)getContext().getSystemService(NOTIFICATION_SERVICE);
        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }

         Intent i = getActivity().getIntent();
         connectDevice(i,false);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_chat, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        //mConversationView = (ListView) view.findViewById(R.id.in);
        mConversationView1 =  view.findViewById(R.id.inRecycler);
                mOutEditText = (EditText) view.findViewById(R.id.edit_text_out);
        mSendButton = (Button) view.findViewById(R.id.button_send);
    }

    /**
     * Set up the UI and background operations for chat.
     */

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the array adapter for the conversation thread
      //  mConversationArrayAdapter = new ArrayAdapter<String>(getActivity(), R.layout.message);

        messages = new ArrayList<>();
        mConversationAdapter = new MessageAdapter(getContext(),messages);
        mConversationView1.setLayoutManager(new LinearLayoutManager(getContext()));
        mConversationView1.setAdapter(mConversationAdapter);

        //mConversationView.setAdapter(mConversationArrayAdapter);

        // Initialize the compose field with a listener for the return key
        mOutEditText.setOnEditorActionListener(mWriteListener);

        // Initialize the send button with a listener that for click events
        mSendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Send a message using content of the edit text widget
                View view = getView();
                if (null != view) {
                    TextView textView = (TextView) view.findViewById(R.id.edit_text_out);
                    String message = textView.getText().toString();
                    sendMessage(message);
                }
            }
        });

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(getContext(), mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");
    }

    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    /**
     * Sends a message.
     *
     * @param message A string of text to send.
     */
    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Toast.makeText(getActivity(), R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            mOutEditText.setText(mOutStringBuffer);
        }
    }

    /**
     * The action listener for the EditText widget, to listen for the return key
     */
    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * Updates the status on the action bar.
     *
     * @param resId a string resource ID
     */
    private void setStatus(int resId) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(resId);
    }

    /**
     * Updates the status on the action bar.
     *
     * @param subTitle status
     */
    private void setStatus(CharSequence subTitle) {
        FragmentActivity activity = getActivity();
        if (null == activity) {
            return;
        }
        final ActionBar actionBar = activity.getActionBar();
        if (null == actionBar) {
            return;
        }
        actionBar.setSubtitle(subTitle);
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void handleMessage(Message msg) {
            FragmentActivity activity = getActivity();
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                            retrieveMessages();
                            mConversationAdapter.notifyDataSetChanged();
                            // mConversationArrayAdapter.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case Constants.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    //          mConversationArrayAdapter.add("Me:  " + writeMessage);
                    Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                    messages.add(new com.example.android.bluetoothchat.models.Message(writeMessage,"Me",mConnectedDeviceName,""+cal.getTime().getHours()+":"+cal.getTime().getMinutes()));
                    mConversationAdapter.notifyDataSetChanged();
                    insertIntoDatabase(writeMessage,"Me",mConnectedDeviceName,""+cal.getTime().getHours()+":"+cal.getTime().getMinutes());
                    break;
                case Constants.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    //mConversationArrayAdapter.add(mConnectedDeviceName + ":  " + readMessage);
//                    Calendar cal = Calendar.getInstance(TimeZone.getDefault());
//                    messages.add(mConnectedDeviceName+" : "+ readMessage);
                    cal = Calendar.getInstance(TimeZone.getDefault());
                    messages.add(new com.example.android.bluetoothchat.models.Message(readMessage,mConnectedDeviceName,"Me",""+cal.getTime().getHours()+":"+cal.getTime().getMinutes()));
                    mConversationAdapter.notifyDataSetChanged();
                    insertIntoDatabase(readMessage,mConnectedDeviceName,"Me",""+cal.getTime().getHours()+":"+cal.getTime().getMinutes());
                    Notification notify = new Notification.Builder(getActivity().getApplicationContext()).setContentTitle("Admin Message").setSmallIcon(R.drawable.ic_bluetooth_blue_24dp)
                            .setContentText(readMessage).build();
                    notify.flags |= Notification.FLAG_AUTO_CANCEL;
                    if(isForeground(getActivity().getPackageName()))
                        notificationManager.notify(0,notify);
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                    if (null != activity) {
                        Toast.makeText(activity, "Connected to "
                                + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    }
                    break;
                case Constants.MESSAGE_TOAST:
                    if (null != activity) {
                        Toast.makeText(activity, msg.getData().getString(Constants.TOAST),
                                Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public boolean isForeground(String myPackage) {
        ActivityManager manager = (ActivityManager) getActivity()
                .getSystemService(Activity.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> runningTaskInfo = manager
                .getRunningTasks(1);

        ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
        if (componentInfo.getPackageName().equals(myPackage))
            return true;
        return false;
    }

    private void retrieveMessages() {

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        String []projection = {
                MessageFeedEntry.COLUMN_NAME_SENDER,
                MessageFeedEntry.COLUMN_NAME_RECEIVER,
                MessageFeedEntry.COLUMN_NAME_TEXT,
                MessageFeedEntry.COLUMN_NAME_TIME
        };

        String selection1 = MessageFeedEntry.COLUMN_NAME_SENDER + " = ? "+"AND "+
                MessageFeedEntry.COLUMN_NAME_RECEIVER + " = ?";
        String selection2 = MessageFeedEntry.COLUMN_NAME_SENDER + " = ? "+"AND "+
                MessageFeedEntry.COLUMN_NAME_RECEIVER + " = ?";
        String[] selectionArgs = {mConnectedDeviceName,"Me","Me",mConnectedDeviceName};
        String selection = selection1 + " OR "+selection2;
        Cursor cursor = db.query(
                TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );

        try {
            while(cursor.moveToNext()){
                String time = cursor.getString(cursor.getColumnIndexOrThrow(MessageFeedEntry.COLUMN_NAME_TIME));
                String receiver = cursor.getString(cursor.getColumnIndexOrThrow(MessageFeedEntry.COLUMN_NAME_RECEIVER));
                String sender = cursor.getString(cursor.getColumnIndexOrThrow(MessageFeedEntry.COLUMN_NAME_SENDER));
                String text = cursor.getString(cursor.getColumnIndexOrThrow(MessageFeedEntry.COLUMN_NAME_TEXT));
                messages.add(new com.example.android.bluetoothchat.models.Message(text,sender,receiver,time));
            }
            cursor.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    private void insertIntoDatabase(String writeMessage, String me_, String mConnectedDeviceName,String time) {
        SQLiteDatabase db = mDBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MessageFeedEntry.COLUMN_NAME_TEXT,writeMessage);
        values.put(MessageFeedEntry.COLUMN_NAME_SENDER,me_);
        values.put(MessageFeedEntry.COLUMN_NAME_RECEIVER,mConnectedDeviceName);
        values.put(MessageFeedEntry.COLUMN_NAME_TIME,time);

        db.insert(TABLE_NAME,null,values);
        Log.d("DATABASE", " "+writeMessage);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                //retrieveMessages();
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                //retrieveMessages();
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving,
                            Toast.LENGTH_SHORT).show();
                    getActivity().finish();
                }
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.bluetooth_chat, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.secure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(),DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
                return true;
            }
            case R.id.insecure_connect_scan: {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getActivity(),DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
                return true;
            }
            case R.id.discoverable: {
                // Ensure this device is discoverable by others
                ensureDiscoverable();
                return true;
            }
        }
        return false;
    }
}
