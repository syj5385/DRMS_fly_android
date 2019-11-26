package com.drms.drms_drone.Drone_Controller;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drms.drms_drone.Communication.ClassicBluetooth.BluetoothService;
import com.drms.drms_drone.Communication.Protocol.DRS_Constants;
import com.drms.drms_drone.Communication.Protocol.DRS_SerialProtocol;
import com.drms.drms_drone.Communication.Protocol.MSP;
import com.drms.drms_drone.Communication.Protocol.STK500v1;
import com.drms.drms_drone.Communication.USBSerial.UsbService;
import com.drms.drms_drone.R;

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.Set;

/**
 * Created by jjunj on 2017-10-01.
 */

public class DrsControllerActivity extends AppCompatActivity {

    public static final int MODE_REQUEST = 1;

    private static final String TAG = "MAIN";
    public static final boolean D = true;

    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;

    private static final int MESSAGE_STATE_CHANGE = 10;
    private static final int Multiwii_PROTOCOL = 102;


    private LinearLayout controller_window;

    private ImageView bluetooth, aux4, plane, acccali,battery ;
    private TextView timer;
    private TextView vbat;

    private DrsControllerView mConView ;
    private boolean layout_checked = false;


    private String USB_current = UsbService.ACTION_NO_USB;

    private Handler mHandler = new MyHandler();
    private UsbService mUSBService = null;
    private BluetoothService mBluetoothService = null;

    private DRS_SerialProtocol mDRS = null;
    private STK500v1 mSTK = null;
    private MSP mMSP = null;
    private ContElement contElement;
    private TextToSpeech tts;


    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            mUSBService = ((UsbService.UsbBinder) arg1).getService();
            mUSBService.setHandler(mHandler);
            mDRS = new DRS_SerialProtocol(DRS_Constants.DRSCONTROLLER,mHandler,mUSBService,contElement);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mUSBService = null;
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);
        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        contElement = new ContElement();

        controller_window = (LinearLayout)findViewById(R.id.controller_window);
        mConView = new DrsControllerView(this,this,mHandler,contElement);
        mConView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        controller_window.addView(mConView);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.KOREA);

            }
        });

        bluetooth = (ImageView)findViewById(R.id.bluetooth);
        aux4 = (ImageView)findViewById(R.id.AUX4);
        plane = (ImageView)findViewById(R.id.plane);
        timer = (TextView)findViewById(R.id.timer);
        vbat = (TextView)findViewById(R.id.current_bat);
        battery = (ImageView)findViewById(R.id.battery);

        acccali = (ImageView)findViewById(R.id.acc_calibration);

    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            USB_current = intent.getAction();
            switch (intent.getAction()) {
                case UsbService.ACTION_USB_PERMISSION_GRANTED: // USB PERMISSION GRANTED
                    Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(USB_current.equals(UsbService.ACTION_USB_PERMISSION_GRANTED)){
                                mDRS.make_send_DRS(DRS_Constants.DRSCONTROLLER_DATA);
                                try{
                                    Thread.sleep(40);
                                }catch (InterruptedException e){};

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(contElement.getDigitPin()[2] == 2000){
                                            aux4.setImageDrawable(getResources().getDrawable(R.drawable.power_on));
                                        }
                                        else if(contElement.getDigitPin()[2] == 1000){
                                            aux4.setImageDrawable(getResources().getDrawable(R.drawable.power_off));
                                        }
                                    }
                                });

                                mConView.getViewHandler().obtainMessage(DrsControllerView.TREAM,contElement.getTream()).sendToTarget();

                            }
                        }
                    }).start();

                    if(mBluetoothService == null){
                        mBluetoothService = new BluetoothService(DrsControllerActivity.this,mHandler,"DRONELANDS","MSP");
                        if (mBluetoothService.getDeviceState()) {
                            mBluetoothService.setReadRunning(true);
                            mBluetoothService.enableBluetooth();
                        } else {
                            finish();
                        }
                    }
                    break;
                case UsbService.ACTION_USB_PERMISSION_NOT_GRANTED: // USB PERMISSION NOT GRANTED
                    Toast.makeText(context, "USB Permission not granted", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_NO_USB: // NO USB CONNECTED
                    Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_DISCONNECTED: // USB DISCONNECTED
                    Toast.makeText(context, "USB disconnected", Toast.LENGTH_SHORT).show();
                    break;
                case UsbService.ACTION_USB_NOT_SUPPORTED: // USB NOT SUPPORTED
                    Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private class MyHandler extends Handler {
        public MyHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UsbService.MESSAGE_FROM_SERIAL_PORT:

                    byte[] receiveed_data = (byte[])msg.obj;
                    if(mDRS.read_DRS(receiveed_data)){
                        mConView.invalidate();
                    }else{

                    }

                    break;
                case UsbService.CTS_CHANGE:
//                    Toast.makeText(DrsControllerActivity.this, "CTS_CHANGE", Toast.LENGTH_LONG).show();
                    break;
                case UsbService.DSR_CHANGE:
//                    Toast.makeText(DrsControllerActivity.this, "DSR_CHANGE", Toast.LENGTH_LONG).show();
                    break;

                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE:" + msg.arg1);

                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
//
                            String bt_speaking = "블루투스가 연결되었습니다. 센서를 교정합니다.";
                            mMSP = new MSP(mBluetoothService,MSP_handler);
                            mMSP.SendRequestMSP_ACC_CALIBRATION();

                            tts.speak(bt_speaking, TextToSpeech.QUEUE_FLUSH,null);
                            bluetooth.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth_cnt));

                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while(mBluetoothService.getState() == BluetoothService.STATE_CONNECTED){

                                        mMSP.sendRequestMSP_SET_RAW_RC(getRCdata());
                                        try {
                                            Thread.sleep(30);
                                        } catch (InterruptedException e) {}

                                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_ATTITUDE));
                                        try {
                                            Thread.sleep(30);
                                        } catch (InterruptedException e) {
                                        }

                                        mMSP.sendRequestMSP_SET_RAW_RC(getRCdata());
                                        try {
                                            Thread.sleep(30);
                                        } catch (InterruptedException e) {}

                                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_RC));
                                        try {
                                            Thread.sleep(30);
                                        } catch (InterruptedException e) {
                                        }

                                        mMSP.sendRequestMSP_SET_RAW_RC(getRCdata());
                                        try {
                                            Thread.sleep(30);
                                        } catch (InterruptedException e) {}

                                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_ANALOG));
                                        try {
                                            Thread.sleep(30);
                                        } catch (InterruptedException e) {
                                        }
                                    }
                                }
                            }).start();
                            break;

                        case BluetoothService.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(), "연결중....", Toast.LENGTH_LONG).show();
                            break;

                        case BluetoothService.STATE_FAIL:
                            Toast.makeText(getApplicationContext(), "블루투스 연결에 실패하였습니다.", Toast.LENGTH_LONG).show();
                            mBluetoothService.stop();
                            break;

                        case BluetoothService.STATE_DISCONNECTED :
                            bluetooth.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth));
                            plane.setImageDrawable(getResources().getDrawable(R.drawable.plane));
                            timer.setText("00:00");

                            break;
                    }
                    break;

                case Multiwii_PROTOCOL :
                    byte[] data = (byte[])msg.obj;
                    mMSP.readMSP(data);

                    break;


            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setFilters();  // Start listening notifications from UsbService
        startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(mUsbReceiver);
        unbindService(usbConnection);

    }

    private void setFilters() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        registerReceiver(mUsbReceiver, filter);
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(this, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            startService(startService);
        }
        Intent bindingIntent = new Intent(this, service);
        bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult" + resultCode);

        switch (requestCode) {
            case REQUEST_ENABLE_BT :
                if (resultCode != Activity.RESULT_OK) {
                    mBluetoothService.scanDevice();
                } else {//cancel button
                    Log.d(TAG, "Bluetooth is not enable");
                }
                break;

            case REQUEST_CONNECT_DEVICE :
                if (resultCode == Activity.RESULT_OK) {
                    mBluetoothService.getDeviceInfo(data);
                }
                break;
        }
    }

    boolean MSP_MISC_state = false;
    boolean MSP_RC_TUNING_state = false;
    boolean MSP_BOX_state = false;
    boolean MSP_ANALOG_state = false;
    boolean MSP_ATTITUDE_state = false;
    int MSP_RC_counter = 0;
    float[] MSP_MISC_DATA;
    float[] MSP_RC_TUNING_DATA;
    float[] MSP_ANALOG_DATA;
    float[] MSP_ATTITUDE_DATA;
    int[] MSP_RC_DATA;

    float warning_vbat = (float)2.8;


    private Handler MSP_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.d("RECEIVED","Received");

            switch(msg.what){
                case MSP.MSP_MISC :
                    if(!MSP_MISC_state) {
                        MSP_MISC_DATA = (float[]) msg.obj;

                        warning_vbat = MSP_MISC_DATA[10];

                        MSP_MISC_state = true;
                    }
                    break;

                case MSP.MSP_RC_TUNING :
                    if(MSP_RC_TUNING_state == false) {
                        MSP_RC_TUNING_DATA = (float[]) msg.obj;

                        MSP_RC_TUNING_state = true;
                    }
                    break;



                case MSP.MSP_ANALOG :
                    if(!MSP_MISC_state){
                        MSP_ANALOG_DATA = (float[])msg.obj;

                        DecimalFormat form = new DecimalFormat("#.##");
                        float vbat_value = MSP_ANALOG_DATA[0];
                        if(vbat_value < warning_vbat){
                            battery.setImageDrawable(getResources().getDrawable(R.drawable.battery_low));
//                            Toast.makeText(getApplicationContext(),"배터리가 부족합니다!!!",Toast.LENGTH_SHORT).show();
                        }
                        else{
                            battery.setImageDrawable(getResources().getDrawable(R.drawable.vbat));
                        }
                        vbat.setText(String.valueOf(form.format(vbat_value)) + " [V]");
                    }
                    break;

                case MSP.MSP_RC :
//                    if(MSP_RC_counter >= 10) {
                    MSP_RC_DATA = (int[]) msg.obj;
                    switch (MSP_RC_DATA[7]) {
                        case 1000:
                            plane.setImageDrawable(getResources().getDrawable(R.drawable.plane));
                            break;

                        case 2000:
                            plane.setImageDrawable(getResources().getDrawable(R.drawable.plane_on));
                            break;
                    }


//                    }
//                    MSP_RC_counter ++;
//                    if (MSP_RC_counter > 15){
//                        MSP_RC_counter = 10;
//                    }
                    break;

                case MSP.MSP_ATTITUDE :
                    MSP_ATTITUDE_DATA = (float[])msg.obj;
                    Log.d("MSP",String.valueOf(MSP_ATTITUDE_DATA[0]));
                    mConView.getViewHandler().obtainMessage(DrsControllerView.RPY,MSP_ATTITUDE_DATA).sendToTarget();
                    mConView.invalidate();

                    break;

                case MSP.MSP_ACC_CALIBRATION :

//                    acc_cali.setImageDrawable(getResources().getDrawable(R.drawable.cali));

                    break;

            }
        }
    };

    private int[] getRCdata(){
        int[] RCsignal = new int[8];

        RCsignal[0] = (int)((contElement.getRPY()[0]-1500)*mConView.getSpeedRate() + 1501);
        RCsignal[1] = (int)((contElement.getRPY()[1]-1500)*mConView.getSpeedRate() + 1501);
        RCsignal[2] = (int)((contElement.getRPY()[2]-1500)*mConView.getSpeedRate() + 1501);
        RCsignal[3] = contElement.getRPY()[3];
        RCsignal[4] = 1000;
        RCsignal[5] = 1000;
        RCsignal[6] = 1000;
        RCsignal[7] = contElement.getDigitPin()[2];

        return RCsignal;
    }
}
