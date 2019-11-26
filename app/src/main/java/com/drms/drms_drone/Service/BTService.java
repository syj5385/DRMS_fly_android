package com.drms.drms_drone.Service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;

import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;


import com.drms.drms_drone.Communication.ClassicBluetooth.BluetoothService;
import com.drms.drms_drone.Communication.ClassicBluetooth.DeviceListActivity;
import com.drms.drms_drone.Controller.DroneController.JoystickActivity;
import com.drms.drms_drone.FileManagement.FileManagement;
import com.drms.drms_drone.MultiData;
import com.drms.drms_drone.Protocol.Multiwii.MSP;

import org.w3c.dom.Text;

import java.util.Locale;


/**
 * Created by yeongjunsong on 2017. 11. 8..
 */

public class BTService extends Service {

    private static final String TAG = "BTService";
    // BroadCast Action
    public static final String REQUEST_CONNECT_BT = "request_connect_bt";
    public static final String REQUEST_FINISH_SERVICE = "request_finish_service";
    public static final String CONNECTED_BLUETOOTH = "connected bluetooth in Service";
    public static final String FAILED_BLUETOOTH = "failed bluetooth in Service";
    public static final String DISCONNECTED_BLUETOOTH = "disconnected bluetooth in Service";
    public static final String REQUEST_SCAN_DEVICE = "request scan device";
    public static final String REQUEST_DISPLAY_MAIN = "request main display";
    public static final String REQUEST_DISPLAY_CONTROLLER = "request controller display";
    public static final String REQUEST_DISPLAY_UPLOAD = "request upload display";
    public static final String REQUEST_DISPLAY_NOTHING = "request nothing";
    public static final String REQUEST_PROGRAMDRONE= "request_program";
    public static final String BLUETOOTH_ADDRESS = "bluetooth address";
    public static final String REQUEST_ACC_CALIBRATION = "request acc calibration";
    public static final String REQUEST_MAG_CALIBRATION = "request mag calibration";
    public static final String REQUEST_MSP_SET_HEAD = "reqeust set head";
    public static final String REQUEST_DISPLAY_SETTING = "request display setting";
    public static final String REQUEST_BOX_SETTING = "request box setting";
    public static final String REQUEST_RC_SETTING = "request rc setting";
    public static final String REQUEST_PID_SETTING = "request pid setting";
    public static final String ArduinoReset = "arduino reset";
    public static final String DISCOVER_BT ="discover bluetooth";
    public static final String DISCOVER_FAILED = "discovr failed";
    public static final String PREVIOUS_DISPLAY= "com.drms.drms_drone.PREVIOUS_DISPLAY";
    public static final String NEXT_DISPLAY = "con.drms.drms_drone.NEXT_DISPLAY";


    private static final int Multiwii_PROTOCOL = 102;

    public static long  DISCOVER_LOOP = 5000;

    // Current Display Mode
    private int currentDisplay = 0;

    public static final int NOTHIHG = -1;
    public static final int MAINACTIVITY   = 0;
    public static final int CONTROLLER     = 1;
    public static final int UPLOAD = 2;
    public static final int SETTING = 3;
    public static final int PROGRAMDRONE = 4;

    // Bluetooth Constants
    private int mSendingState;

    private static final int STATE_SENDING = 1;
    private static final int STATE_NO_SENDING = 2;

    public static final boolean D = true;

    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;

    private static final int MESSAGE_STATE_CHANGE = 10;
    private static final int MESSAGE_WRITE = 2;
    private static final int MESSAGE_READ = 3;

    public static final int UPDATE_UI = 100;

    private String bt_address = "";
    
    public IBinder binder = new BtBinder();
    private BluetoothService mBluetoothService;
    private Handler mainHandler;

    private boolean isServiceOn = false;
    private boolean isDoCalibration = false;
    private boolean isDiscovered = false;

    private FileManagement mFilemanagement;

    private MultiData mspData;

    private MSP mMSP;

    private Thread mDiscoverThread;

    private TextToSpeech tts;


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class BtBinder extends Binder {
        public BTService getService() {
            return BTService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate Service");
        Log.d(TAG,"start BTService");
        mBluetoothService = new BluetoothService(this,BtHandler,"MSP");

        setFilter();
        currentDisplay = MAINACTIVITY;
        mspData = ((MultiData)this.getApplication());

        Log.d(TAG,"start discovering");
        isServiceOn = true;


        mFilemanagement=  new FileManagement(this,mainHandler);
        if(mFilemanagement.readBTAddress() != null) {
            if (mFilemanagement.readBTAddress()[1] == "") {
                bt_address = "";
            } else {
                bt_address = mFilemanagement.readBTAddress()[1];
            }
        }

        if(tts == null) {
            tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {
                    tts.setLanguage(Locale.KOREAN);
                }
            });
        }
        Log.d(TAG,bt_address);
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mFilemanagement.readBTAddress() != null) {
            if (mFilemanagement.readBTAddress()[1] != "") {
                Log.d(TAG, "trying to connect BT ");
                mDiscoverThread = new discoverBtDevice();
                isDiscovered = true;
                mDiscoverThread.start();
            }
        }



    }

    private Handler BtHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case MESSAGE_STATE_CHANGE  :
                    Intent action = new Intent();
                    switch(msg.arg1){
                        case BluetoothService.STATE_CONNECTED :
                            mMSP = new MSP(mBluetoothService,MSP_handler);
                            Log.d(TAG,"Connected in Service");
                            action.setAction(CONNECTED_BLUETOOTH);
                            action.putExtra("service", String.valueOf(this));
                            if(currentDisplay == MAINACTIVITY)
                                implementationMainDisplayThread();
                            else if(currentDisplay == CONTROLLER){
                                implementationControlThread();
                            }
                            break;

                        case BluetoothService.STATE_FAIL:
                            Log.d(TAG,"Connection Failed");
                            action.setAction(FAILED_BLUETOOTH);
                            mBluetoothService.stop();
                            sendBroadcast(new Intent(DISCOVER_FAILED));

                            break;

                        case BluetoothService.STATE_DISCONNECTED:
                            Log.d(TAG,"disconnected");
                            Intent btdisconnectedIntent = new Intent();
                            action.setAction(DISCONNECTED_BLUETOOTH);
                            sendBroadcast(action);
                            if(mFilemanagement.readBTAddress()[1] != "") {
                                mDiscoverThread = new discoverBtDevice();
                                isDiscovered = true;
                                mDiscoverThread.start();
                            }
                            break;

                        case BluetoothService.STATE_NONE :
                            Log.d(TAG,"state_none");
                            action.setAction(DISCONNECTED_BLUETOOTH);
                            break;

                    }
                    sendBroadcast(action);
                    break;

                case Multiwii_PROTOCOL :
                    byte[] data = (byte[])msg.obj;
                    mMSP.readMSP(data);

                    break;

            }
        }
    };

    public boolean getisDiscovered(){
        return isDiscovered;
    }

    public void setisDiscovered(boolean isDiscovered){
        this.isDiscovered = isDiscovered;
    }

    private BroadcastReceiver BtReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            Log.d(TAG,action);
            if (action.equals(REQUEST_CONNECT_BT)) {
//                if(mBluetoothService == null){
//                    mBluetoothService = new BluetoothService(BTService.this,BtHandler,"MSP");
//                }
//                if(mBluetoothService.getState() == BluetoothService.STATE_NONE
//                        || mBluetoothService.getState() == BluetoothService.STATE_LISTEN) {
//                    mBluetoothService = null;
//                    mBluetoothService = new BluetoothService(BTService.this,BtHandler,"MSP");
//                    new Thread(new Runnable() {
//                        @Override
//                        public void run() {
//                            Intent btintent = new Intent();
//                            btintent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, intent.getStringExtra("BT"));
//                            mBluetoothService.getDeviceInfo(btintent);
//                        }
//                    }).start();
//
//                }
//                else if(mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
//                    mBluetoothService.stop();
////                    mBluetoothService = null;
//                }
//                else if(mBluetoothService.getState() == BluetoothService.STATE_CONNECTING){
//                    Log.d(TAG,"Connecting");
//                }

                if(mFilemanagement.readBTAddress()[1] != "") {
                    bt_address = intent.getStringExtra("BT");
                    mDiscoverThread = new discoverBtDevice();
                    isDiscovered = true;
                    mDiscoverThread.start();
                }

            }
            else if(action.equals(REQUEST_FINISH_SERVICE)){
                if(mBluetoothService != null){
                    mBluetoothService.stop();
//                    mBluetoothService = null;
                    if(mFilemanagement.readBTAddress() != null) {
                        bt_address = mFilemanagement.readBTAddress()[1];
                    }
                    Log.d(TAG,"bt_address in Service : " + bt_address);
                }
            }
            else if(action.equals(REQUEST_DISPLAY_CONTROLLER)){
                Log.d(TAG,"Bluetooth State : + " + mBluetoothService.getState());
//                if(mBluetoothService.getState() == BluetoothService.STATE_CONNECTED){
                    currentDisplay = CONTROLLER;

                    implementationControlThread();
                    Log.d(TAG,"Received Request Controller BroadCast ");
//                }
            } else if (action.equals(REQUEST_PROGRAMDRONE)) {
                Log.d(TAG,"program Drone Mode");
                currentDisplay = PROGRAMDRONE;
                implementationProgramThread();

            } else {
                if (action.equals(BLUETOOTH_ADDRESS)) {
                    bt_address = intent.getStringExtra("BT");
                    Log.d(TAG, "BT address in Service : " + bt_address);
                } else if (action.equals(REQUEST_DISPLAY_UPLOAD)) {
                    Log.d(TAG, "preparing upload");
                    currentDisplay = UPLOAD;
                    mBluetoothService.setProtocol("STK");
                } else if (action.equals(REQUEST_DISPLAY_MAIN)) {
                    Log.d(TAG, "request main display");
                    mBluetoothService.setProtocol("MSP");
                    BtHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            implementationMainDisplayThread();
                            currentDisplay = MAINACTIVITY;
                            mBluetoothService.setmHandler(BtHandler);
                        }
                    }, 1000);
                } else if (action.equals(REQUEST_ACC_CALIBRATION)) {
                    Log.d(TAG, "request acc Calibration");
                    isDoCalibration = true;
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                    }
                    ;
                    mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_ACC_CALIBRATION));
                    String accCali = "가속도 센서 교정을 시작합니다.";
                    tts.speak(accCali, TextToSpeech.QUEUE_FLUSH, null);
                } else if (action.equals(REQUEST_MAG_CALIBRATION)) {
                    Log.d(TAG, "request mag Calibration");
                    isDoCalibration = true;
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                    }
                    ;
                    mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_MAG_CALIBRATION));
                    String magCali = "지자계 센서 교정을 시작합니다.";
                    tts.speak(magCali, TextToSpeech.QUEUE_FLUSH, null);
                } else if (action.equals(REQUEST_MSP_SET_HEAD)) {
                    int heading = intent.getIntExtra("head", 0);
                    Log.d(TAG, "heading : " + heading);
//                mMSP.SendRequestMSP_SET_HEAD(heading);
                } else if (action.equals(REQUEST_DISPLAY_SETTING)) {
                    Log.d(TAG, action);
                    mBluetoothService.setProtocol("MSP");
                    BtHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            implementationSettingThread();
                            currentDisplay = SETTING;
                            mBluetoothService.setmHandler(BtHandler);
                        }
                    }, 1000);
                } else if (action.equals(REQUEST_BOX_SETTING)) {
                    currentDisplay = NOTHIHG;
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMSP.sendRequestMSP_SET_BOX(mspData.getCheckboxData());
                            int requestExit = intent.getIntExtra("REQUEST_BACK", 0);
                            if (requestExit == 0) {
                                mainHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        currentDisplay = SETTING;
                                        implementationSettingThread();
                                    }
                                }, 1000);
                            } else if (requestExit == -1) {
                                Log.d(TAG, "go to Main");

                            }
                        }
                    }, 500);
                } else if (action.equals(REQUEST_RC_SETTING)) {
                    currentDisplay = NOTHIHG;

                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMSP.SendRequestMSP_SET_RC_TUNING(mspData.getRCTUNEdata());
                            mMSP.SendRequestMSP_SET_MISC((int) mspData.getMISCdata()[0], (int) mspData.getMISCdata()[1], (int) mspData.getMISCdata()[2],
                                    (int) mspData.getMISCdata()[3], (int) mspData.getMISCdata()[4], mspData.getMISCdata()[7], (int) mspData.getMISCdata()[8]
                                    , mspData.getMISCdata()[9], mspData.getMISCdata()[10], mspData.getMISCdata()[11]);

                            int requestExit = intent.getIntExtra("REQUEST_BACK", 0);
                            if (requestExit == 0) {
                                mainHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        currentDisplay = SETTING;
                                        implementationSettingThread();
                                    }
                                }, 1000);
                            } else if (requestExit == -1) {
                                Log.d(TAG, "go to Main");

                            }
                        }
                    }, 500);

                } else if (action.equals(REQUEST_PID_SETTING)) {
                    final float[] p = intent.getFloatArrayExtra("P");
                    final float[] i = intent.getFloatArrayExtra("I");
                    final float[] d = intent.getFloatArrayExtra("D");
                    currentDisplay = NOTHIHG;
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mMSP.SendRequestMSP_SET_PID_TUNING(p, i, d);
                            int requestExit = intent.getIntExtra("REQUEST_BACK", 0);
                            if (requestExit == 0) {
                                mainHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        currentDisplay = SETTING;
                                        implementationSettingThread();
                                    }
                                }, 1000);
                            } else if (requestExit == -1) {
                                Log.d(TAG, "go to Main");

                            }
                        }
                    }, 500);

                } else if (action.equals(DISCONNECTED_BLUETOOTH)) {
                    if (mBluetoothService != null) {
                        mBluetoothService.stop();
                    }
                }
                else if(action.equals(NEXT_DISPLAY)){
                    if(currentDisplay == MAINACTIVITY){
                        tts.speak("조종기 화면으로 이동합니다.", TextToSpeech.QUEUE_FLUSH,null);
                    }
                    else if(currentDisplay == CONTROLLER){
                        tts.speak("설정",TextToSpeech.QUEUE_FLUSH,null);
                    }

                } else if (action.equals(PREVIOUS_DISPLAY)) {
                    if(currentDisplay == MAINACTIVITY){
                        tts.speak("어플리케이션을 종료합니다 ", TextToSpeech.QUEUE_FLUSH,null);
                    }
                    else if(currentDisplay == CONTROLLER){
                        tts.speak("조종기 화면을 종료합니다.",TextToSpeech.QUEUE_FLUSH,null);

                    }
                }
            }


        }
    };

    private void setFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(REQUEST_CONNECT_BT);
        filter.addAction(REQUEST_FINISH_SERVICE);
        filter.addAction(REQUEST_DISPLAY_MAIN);
        filter.addAction(REQUEST_PROGRAMDRONE);
        filter.addAction(REQUEST_DISPLAY_CONTROLLER);
        filter.addAction(REQUEST_DISPLAY_NOTHING);
        filter.addAction(REQUEST_DISPLAY_UPLOAD);
        filter.addAction(REQUEST_ACC_CALIBRATION);
        filter.addAction(REQUEST_MAG_CALIBRATION);
        filter.addAction(REQUEST_MSP_SET_HEAD);
        filter.addAction(REQUEST_DISPLAY_SETTING);
        filter.addAction(REQUEST_BOX_SETTING);
        filter.addAction(REQUEST_RC_SETTING);
        filter.addAction(REQUEST_PID_SETTING);
        filter.addAction(NEXT_DISPLAY);
        filter.addAction(PREVIOUS_DISPLAY);
//        filter.addAction(BLUETOOTH_ADDRESS);

        registerReceiver(BtReceiver,filter);
    }

    public void setHandler(Handler mainHandler){
        this.mainHandler = mainHandler;
    }


    public BluetoothService getmBluetoothService(){
        return mBluetoothService;
    }

    private int count = 0;
    private long prev_t = 0;

    private void implementationMainDisplayThread(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(currentDisplay == MAINACTIVITY){
                    if(mBluetoothService != null ) {
                        if(mMSP == null){
                            mMSP = new MSP(mBluetoothService,MSP_handler);
                        }

                        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                            break;
                        }
                        prev_t = System.currentTimeMillis();
                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_ATTITUDE));
                        try {
                            if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                            }
                        } catch (InterruptedException e) {
                        }

                        prev_t = System.currentTimeMillis();
                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_ANALOG));
                        try{
                            if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                            }
                        }catch (InterruptedException e){};
                    }
                }
            }
        }).start();
    }

    private void implementationProgramThread(){
        Log.d(TAG,"implementation Program Thread");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(currentDisplay == PROGRAMDRONE){
                    if(mBluetoothService != null&& mMSP != null){

                        prev_t = System.currentTimeMillis();
                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_ALTITUDE));
                        try {
                            if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                            }
                        } catch (InterruptedException e) {
                        }
                    }
                }
            }
        }).start();
    }

    private boolean isProcessed = false;
    private void implementationControlThread(){
        Log.d(TAG,"implementationControlThread");
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(currentDisplay == CONTROLLER &&  !isDoCalibration ){
                    if(mBluetoothService != null ) {
                        if(mMSP == null){
                            mMSP = new MSP(mBluetoothService,MSP_handler);
                        }

                        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                            break;
                        }


                        prev_t = System.currentTimeMillis();
                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_ATTITUDE));
                        try {
                            if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                            }
                        } catch (InterruptedException e) {
                        }

                        prev_t = System.currentTimeMillis();
                        mMSP.sendRequestMSP_SET_RAW_RC(mspData.getRcdata());
                        try{
                            if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                            }
                        }catch(InterruptedException e){}



                        prev_t = System.currentTimeMillis();
                        mMSP.sendRequestMSP_SET_RAW_RC(mspData.getRcdata());
                        try{
                            if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                            }
                        }catch(InterruptedException e){}

                        prev_t = System.currentTimeMillis();
                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_ANALOG));
                        try{
                            if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                            }
                        }catch (InterruptedException e){};

                        prev_t = System.currentTimeMillis();
                        mMSP.sendRequestMSP_SET_RAW_RC(mspData.getRcdata());
                        try{
                            if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                            }
                        }catch(InterruptedException e){}

                        prev_t = System.currentTimeMillis();
                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_RC));
                        try{
                            if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                            }
                        }catch(InterruptedException e){}


                    }

                }
            }
        }).start();
    }

    boolean checkData = false;
    private void implementationSettingThread(){
        Log.d(TAG,"implementationSettingThread");
        checkData = false;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(currentDisplay == SETTING &&  !isDoCalibration ){
                    if(mBluetoothService != null ) {
                        if(mMSP == null){
                            mMSP = new MSP(mBluetoothService,MSP_handler);
                        }

                        if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
                            break;
                        }

                        prev_t = System.currentTimeMillis();
                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_ATTITUDE));
                        try {
                            if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                            }
                        } catch (InterruptedException e) {
                        }

                        prev_t = System.currentTimeMillis();
                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_ANALOG));
                        try{
                            if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                            }
                        }catch (InterruptedException e){};

                        prev_t = System.currentTimeMillis();
                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_RAW_IMU));
                        try{
                            if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                            }
                        }catch (InterruptedException e){};


                        if(checkData == false){

                            prev_t = System.currentTimeMillis();
                            mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_BOXNAMES));
                            try{
                                if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                    Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                                }
                            }catch (InterruptedException e){};

                            prev_t = System.currentTimeMillis();
                            mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_MISC));
                            try{
                                if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                    Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                                }
                            }catch (InterruptedException e){};

                            prev_t = System.currentTimeMillis();
                            mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_RC_TUNING));
                            try{
                                if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                    Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                                }
                            }catch (InterruptedException e){};

                            prev_t = System.currentTimeMillis();
                            mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_BOX));
                            try{
                                if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                    Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                                }
                            }catch (InterruptedException e){};

                            prev_t = System.currentTimeMillis();
                            mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_PID));
                            try{
                                if(mspData.getMSP_TIME() - (System.currentTimeMillis()-prev_t) > 0) {
                                    Thread.sleep(mspData.getMSP_TIME() - (System.currentTimeMillis() - prev_t));
                                }
                            }catch (InterruptedException e){};

                            checkData = true;
                        }
////
                    }

                }
            }
        }).start();
    }

    private long BTprev_t = 0;
    private long BTcurrent_t = 0;
    private Handler MSP_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            BTprev_t = BTcurrent_t;
            BTcurrent_t = System.currentTimeMillis();


            switch(msg.what){
                case MSP.MSP_MISC :
                    mspData.setMISCdata((float[])msg.obj);
                    break;

                case MSP.MSP_RC_TUNING :
                    mspData.setRCTUNEdata((float[])msg.obj);
                    break;

                case MSP.MSP_ANALOG :
                    mspData.setAnalogData((float[])msg.obj);

                    break;

                case MSP.MSP_RC :
                    mspData.setReceivedRcdata((int[])msg.obj);
                    break;

                case MSP.MSP_ATTITUDE :
                    mspData.setAttitudeData((float[])msg.obj);
                    mainHandler.obtainMessage(UPDATE_UI).sendToTarget();
//                    Log.d(TAG,"update UI");
                    break;

                case MSP.MSP_ACC_CALIBRATION :
                    mspData.setACCCalibration(false);
                    Log.d(TAG,"received acc cali");
                    String finishAcc = "가속도 센서 교정이 완료되었습니다.";
                    tts.speak(finishAcc, TextToSpeech.QUEUE_ADD,null);
                    isDoCalibration = false;
                    for(int i=1; i<4; i++)
                        mspData.setRawRCDataAux(i,1000);
//                    mspData.setRawRCDataAux(4,2000);
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(currentDisplay == CONTROLLER)
                                implementationControlThread();
                        }
                    },500);

                    break;

                case MSP.MSP_MAG_CALIBRATION :
                    mainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mspData.setMAGCalibration(false);
                            Log.d(TAG,"received mag cali");
                            String finishMag = "지자계 센서 교정이 완료되었습니다.";
                            tts.speak(finishMag, TextToSpeech.QUEUE_ADD,null);
                            isDoCalibration = false;
                            for(int i=1; i<4; i++)
                                mspData.setRawRCDataAux(i,1000);
//                            mspData.setRawRCDataAux(4,2000);
                            mainHandler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(currentDisplay == CONTROLLER)
                                        implementationControlThread();
                                }
                            },500);
                        }
                    },30000);

                    break;

                case MSP.MSP_SET_HEAD :
                    Log.w(TAG,"set Header");
                    break;

                case MSP.MSP_RAW_IMU :
                    mspData.setIMUdata((int[])msg.obj);
                    int[] temp = (int[])msg.obj;
                    mainHandler.obtainMessage(UPDATE_UI).sendToTarget();

                    break;

                case MSP.MSP_BOX :
                    mspData.setCheckboxData((boolean[][])msg.obj);
                    Log.d(TAG,"received checkbox");
                    break;

                case MSP.MSP_PID :
                    mspData.setPIDdata((int[])msg.obj);
                    Log.d(TAG,"received pid data");
                    break;

                case MSP.MSP_BOXNAMES :
                    mspData.initBoxITEM((String[])msg.obj);
                    Log.d(TAG,"set Box Name");
                    break;

                case MSP.MSP_ALTITUDE :
                    mspData.setALTITUDEdata((float[])msg.obj);

                    break;
            }
        }
    };

    public MultiData getMspData(){
        return mspData;
    }

    @Override
    public void onDestroy() {

        isServiceOn = false;
        super.onDestroy();
        if(tts != null){
            tts.shutdown();
        }
        unregisterReceiver(BtReceiver);
        Log.d(TAG,"onDestroy() on BTService");
    }

    public int getCurrentDisplay(){
        return currentDisplay;
    }

    public void setCurrentDisplay(int currentDisplay){
        this.currentDisplay = currentDisplay;
    }

    public class discoverBtDevice extends Thread {
        @Override
        public void run() {
            super.run();
            while(isServiceOn && isDiscovered){
                if(mBluetoothService != null){
                    if(mBluetoothService.getState() == BluetoothService.STATE_LISTEN
                            || mBluetoothService.getState() == BluetoothService.STATE_NONE){
                        if(bt_address.length() == 17) {
                            sendBroadcast(new Intent(DISCOVER_BT));
                            Log.d(TAG,"trying to connect BT");
                            mBluetoothService = null;
                            mBluetoothService = new BluetoothService(BTService.this, BtHandler, "MSP");
                            Intent btintent = new Intent();
                            btintent.putExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS, bt_address);
                            mBluetoothService.getDeviceInfo(btintent);
                        }
                        else{
                            Log.w(TAG,"Bluetooth Address is null");

                        }
                    }
                    else if(mBluetoothService.getState() == BluetoothService.STATE_CONNECTED){
                        Log.w(TAG,"Bluetooth is already connected");
                        break;
                    }
                    else if(mBluetoothService.getState() == BluetoothService.STATE_CONNECTING){
                        Log.w(TAG,"Bluetooth is connecting");
                    }
                }
                else{
                    Log.e(TAG,"mBluetoothService is null");
                }
                try{
                    this.sleep(DISCOVER_LOOP);
                }catch (InterruptedException e){
                    Log.e(TAG,"InterruptedException during discovering BT Device",e);
                    break;
                }
            }
        }
    }


}
