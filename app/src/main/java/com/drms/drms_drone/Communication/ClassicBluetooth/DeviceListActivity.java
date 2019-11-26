package com.drms.drms_drone.Communication.ClassicBluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.drms.drms_drone.CustomAdapter.CustomAdatper2.Custom2_Item;
import com.drms.drms_drone.CustomAdapter.CustomAdatper2.CustomAdapter2;
import com.drms.drms_drone.R;

import java.util.Set;


/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */
public class DeviceListActivity extends Activity {
    // Debugging
    private static final String TAG = "DeviceListActivity";
    private static final boolean D = true;

    private  ListView newDevicesListView;

    // Return Intent extra
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    public static String EXTRA_DEVICE_NAME = "device_name";

    // Member fields
    private BluetoothAdapter mBtAdapter;
//    private ArrayAdapter<String> mPairedDevicesArrayAdapter;
//    private ArrayAdapter<String> mNewDevicesArrayAdapter;

    private CustomAdapter2 mPairedAdapter;
    private CustomAdapter2 mDiscoveredAdapter;

    private int DiscoverCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        Intent intent = getIntent();


        // Setup the window
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_devicelist);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize the button to perform device discovery
        Button scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                doDiscovery();
                v.setVisibility(View.GONE);
            }
        });

        // Initialize array adapters. One for already paired devices and
        // one for newly discovered devices
//        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
//        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mPairedAdapter = new CustomAdapter2(DeviceListActivity.this);
        mDiscoveredAdapter = new CustomAdapter2(DeviceListActivity.this);


        // Find and set up the ListView for paired devices
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mPairedAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);

        // Find and set up the ListView for newly discovered devices
        newDevicesListView = (ListView) findViewById(R.id.new_devices);
//        newDevicesListView.setVisibility(View.INVISIBLE);
//        newDevicesLis tView.setAdapter(mDiscoveredAdapter);
//        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String address = device.getAddress();
//                if((address.charAt(0) == '9' && address.charAt(1) == '8') || ( address.charAt(0) == '2' &&  address.charAt(1) == '0'))
                    mPairedAdapter.addItem(new Custom2_Item(getResources().getDrawable(R.mipmap.device_image),device.getName(),device.getAddress()));

            }

        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedAdapter.addItem(new Custom2_Item(getResources().getDrawable(R.mipmap.cancel),"no device", ""));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);
    }

    /**
     * Start device discover with the BluetoothAdapter
     */
    private void doDiscovery() {
        if (D) Log.d(TAG, "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        Button scan_button = (Button)findViewById(R.id.button_scan);
        scan_button.setVisibility(View.GONE);
        LinearLayout discover_box = (LinearLayout)findViewById(R.id.discovered_box);
        discover_box.setVisibility(View.VISIBLE);

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter

        new Thread(new Runnable() {
            @Override
            public void run() {
                mBtAdapter.startDiscovery();
                DiscoverCount ++;
                if(DiscoverCount > 1){
                    mDiscoveredAdapter.removeItem();
                    DiscoverCount = 0;
                }
            }
        }).start();


    }

    // The on-click listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View

            String name = ((Custom2_Item)av.getAdapter().getItem(arg2)).getData()[0];
            String address = ((Custom2_Item)av.getAdapter().getItem(arg2)).getData()[1];
//            String info = ((TextView) v).getText().toString();
//            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
            intent.putExtra(EXTRA_DEVICE_NAME,name);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();

        }
    };

    // The BroadcastReceiver that listens for discovered devices and
    // changes the title when discovery is finished
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    String name = device.getName();
                    String address = device.getAddress();
//                    if((address.charAt(0) == '9' && address.charAt(1) == '8') || ( address.charAt(0) == '2' &&  address.charAt(1) == '0')) {

                    if(device.getName() != null) {
                        Log.d(TAG, "Discoverd Device : " + device.getName() + " \n address : " + device.getAddress());
                        boolean alreadyDiscovered = false;

                        if(mDiscoveredAdapter.getCount() != 0) {
                            for (int i = 0; i < mDiscoveredAdapter.getCount(); i++) {
                                Custom2_Item temp = (Custom2_Item) mDiscoveredAdapter.getItem(i);
                                String name_temp = temp.getData()[0];
                                String address_temp = temp.getData()[1];
                                Log.d(TAG, "nameTemp : " + name_temp + "\naddressTemp : " + address_temp);
                                if (name.equals(name_temp) && address.equals(address_temp)) {
                                    alreadyDiscovered = true;
                                }
                            }
                        }
                        if(alreadyDiscovered){
                            Log.d(TAG,"this device is already discovered");
                        }
                        else {
                            if(mDiscoveredAdapter.getCount() != 0) {
                                Custom2_Item temp = (Custom2_Item) mDiscoveredAdapter.getItem(0);
                                if (temp.getData()[0].equals("No device")) {
                                    mDiscoveredAdapter.removeItem();
                                }
                            }
                            mDiscoveredAdapter.addItem(new Custom2_Item(getResources().getDrawable(R.mipmap.device_image), device.getName(), device.getAddress()));
                        }
//
                    }

                    newDevicesListView.setAdapter(mDiscoveredAdapter);
                    newDevicesListView.setOnItemClickListener(mDeviceClickListener);
                    newDevicesListView.setOnItemClickListener(mDeviceClickListener);

                }
            // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG,"finished " + action);
                doDiscovery();
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mDiscoveredAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mDiscoveredAdapter.addItem(new Custom2_Item(getResources().getDrawable(R.mipmap.cancel),"No device",""));
                }
            }
        }
    };

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold,R.anim.appear);
    }
}