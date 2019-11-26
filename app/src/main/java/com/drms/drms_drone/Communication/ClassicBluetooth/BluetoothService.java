package com.drms.drms_drone.Communication.ClassicBluetooth;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;


import com.drms.drms_drone.Protocol.STK500v1.ConstantsStk500v1;
import com.drms.drms_drone.Service.BTService;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;



/**
 * Created by jjunj on 2016-12-22.
 */

public class BluetoothService {

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public BluetoothDevice device;
    public String address1;

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private static final String TAG = "BluetoothService";

    // RFCOMM Protocol
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private BluetoothAdapter btAdapter;
    private Activity mActivity;
    private Service mService;
    private Handler mHandler;
    private String level_round;
    private String Protocol;

    public static final int STATE_NONE = 1; // nothing
    public static final int STATE_LISTEN = 2;
    public static final int STATE_CONNECTING = 3;
    public static final int STATE_CONNECTED = 4;
    public static final int STATE_FAIL = 7;
    public static final int STATE_DISCONNECTED = 8;
    public int mState = STATE_NONE;
    public int mMode;


    public static final int MESSAGE_STATE_CHANGE = 10;
    public static final int MESSAGE_WRITE = 2;
    public static final int MESSAGE_READ = 3;
    public static final int MODE_REQUEST = 1;


    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;

    private static final int DRS_PROTOCOL = 101;
    private static final int Multiwii_PROTOCOL = 102;
    private static final int STK_PROTOCOL = 103;

    private boolean read_running = true;

    private String MyAddress = "";

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public BluetoothService(Service mService, Handler handler, String Protocol) {
//        mActivity = activity;
        this.mService = mService;
        mHandler = handler;
        this.level_round = level_round;
        this.Protocol = Protocol;

        btAdapter = BluetoothAdapter.getDefaultAdapter().getDefaultAdapter();
    }

    public void setProtocol(String protocol){
        Protocol = protocol;
    }

    public boolean getDeviceState() {
        Log.d(TAG, "Check the Bluetooth support");

        if (btAdapter == null) {
            Log.d(TAG, "Bluetooth is not available");
            return false;
        } else {
            Log.d(TAG, "Bluetooth is available");
            return true;
        }
    }

    public void enableBluetooth() {
        Log.i(TAG, "Check the enable Bluetooth");
        if (btAdapter.isEnabled()) {
            Log.d(TAG, "Bluetooth Enable Now");
            scanDevice();
        } else {
            Log.d(TAG, "Bluetooth Enable Request");
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            mService.sendBroadcast(intent);


        }
    }

    public void scanDevice() {
        Log.d(TAG, "SCAN DEVICE");
        Intent serverIntent = new Intent(mActivity, DeviceListActivity.class);
        serverIntent.setAction(BTService.REQUEST_SCAN_DEVICE);
//        mService.sendBroadcast(serverIntent);
//        mActivity.startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
    }

    private synchronized void setState(int state) {
        Log.d(TAG, "setState()" + mState + "->" + state);
        mState = state;
        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, state, -1).sendToTarget();

    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        Log.d(TAG, "start");

        if (mConnectedThread == null) {

        }
        else {
            mConnectThread.cancel();
            mConnectThread = null;
        }
    }

    public void getDeviceInfo(Intent data) {
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        address1 = address;
        device = btAdapter.getRemoteDevice(address);
        Log.d(TAG, "Get Device Info\n" + "address : " + address);
        connect(device);
    }

    public synchronized void connect(BluetoothDevice device) {
        Log.d(TAG, "connect to: " + device);

        if (mState == STATE_CONNECTING) {
            if (mConnectThread == null) {

            } else {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectThread = new ConnectThread(device);

        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        Log.d(TAG, "connected");

        if (mConnectThread == null) {

        } else {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread == null) {

        } else {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        Log.d(TAG, "STOP");
        read_running = false;

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        device = null;
        setState(STATE_NONE);
    }

    //////////////////////////connect Thread/////////////////////////////////////////////

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            btAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
                Log.d(TAG, "Connect Success");
            } catch (IOException e) {
                connectFailed();
                Log.d(TAG, "Connect Fail");

                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }

                BluetoothService.this.start();
                return;
            }
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            connected(mmSocket,mmDevice);

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////

    /////////////////////////////////connected thread///////////////////////////////////////////////////

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp socket net created", e);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");

            while(read_running) {
                read_protocol(Protocol);
            }

        }

        private void read_protocol(String protocol){
            if(protocol.equals("STK")){
                try {
                    byte[] data = new byte[2];

                    int SYNC_CHECK = mmInStream.read();
                    data[0] = (byte)SYNC_CHECK;
                    if((byte)SYNC_CHECK == ConstantsStk500v1.STK_INSYNC){
                        int RESP_OK = mmInStream.read();
                        data[1] = (byte)RESP_OK;
                    }

                    mHandler.obtainMessage(MESSAGE_READ,-1,-1,data).sendToTarget();
                }catch (IOException e){
                    Log.d(TAG, "disconnected");
                    ConnectLost();
                    read_running = false;
                    try {
                        mHandler.obtainMessage(MESSAGE_STATE_CHANGE, STATE_DISCONNECTED, -1).sendToTarget();
                    }catch (Exception e2){};

                }
            }
            else if(protocol.equals("MSP")){
                char header = 0;
                int[] header_array = new int[2];
                int sizeofdata =0;
                int command =0;
                int checksum = 0;
                try{
                    if((header = (char)mmInStream.read()) == '$' ) {
                        for(int i=0; i<2; i++){
                            header_array[i] = mmInStream.read();
                        }
                        sizeofdata = mmInStream.read();
                        command = mmInStream.read();
                        int[] payload = new int[sizeofdata];

                        for(int i=0; i<sizeofdata; i++){
                            payload[i] = mmInStream.read();
                        }

                        checksum = mmInStream.read();

                        byte[] msp_payload = new byte[6+sizeofdata];
                        int index =0;
                        msp_payload[index++] = '$';
                        for(int i=0; i<2; i++)
                            msp_payload[index++] = (byte)header_array[i];
                        msp_payload[index++] = (byte)sizeofdata;
                        msp_payload[index++] = (byte)command;
                        for(int i=0; i<sizeofdata ; i++)
                            msp_payload[index++] = (byte)payload[i];
                        msp_payload[index] = (byte)checksum;

                        mHandler.obtainMessage(Multiwii_PROTOCOL,index,-1,msp_payload).sendToTarget();

                    }
                }catch (IOException e){
                    Log.e(TAG, "disconnected");
                    mHandler.obtainMessage(MESSAGE_STATE_CHANGE,STATE_DISCONNECTED,-1).sendToTarget();
                    ConnectLost();
                    read_running = false;
                }

            }
//            else{
//
//            }
        }

        public void write(byte[] buffer, int mode) {
            try {
                mmOutStream.write(buffer);
                mMode = mode;

                if(mode == MODE_REQUEST){
//                    mHandler.obtainMessage(MESSAGE_WRITE,-1,-1, buffer).sendToTarget();
                }
            } catch (IOException e) {
                Log.e(TAG, "Exception during write");
            }

        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////

    public void write(byte[] out,int mode) {
        ConnectedThread r;

        synchronized (this) {
            if (mState != STATE_CONNECTED)
                return;
            r = mConnectedThread;
        }
        r.write(out, mode);
    }

    public void readMSP(){
        mConnectedThread.read_protocol("MSP");
    }

    private void connectFailed() {
        setState(STATE_FAIL);
    }

    private void ConnectLost() {
        setState(STATE_LISTEN);
    }

    public void setReadRunning(boolean read_running){
        this.read_running = read_running;
    }

    public void setmHandler(Handler mHandler){
        this.mHandler = mHandler;
    }
}