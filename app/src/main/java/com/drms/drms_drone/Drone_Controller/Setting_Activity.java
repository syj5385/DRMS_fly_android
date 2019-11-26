package com.drms.drms_drone.Drone_Controller;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Toast;
import com.drms.drms_drone.Communication.ClassicBluetooth.BluetoothService;
import com.drms.drms_drone.Communication.Protocol.MSP;
import com.drms.drms_drone.R;

import java.text.DecimalFormat;


public class Setting_Activity extends AppCompatActivity {

    public static final int MSP_IDENT = 100;
    public static final int MSP_STATUS = 101;
    public static final int MSP_RAW_IMU = 102;
    public static final int MSP_SERVO = 103;
    public static final int MSP_MOTOR = 104;
    public static final int MSP_RC = 105;
    public static final int MSP_RAW_GPS = 106;
    public static final int MSP_COMP_GPS = 107;
    public static final int MSP_ATTITUDE = 108;
    public static final int MSP_ALTITUDE = 109;
    public static final int MSP_ANALOG = 110;
    public static final int MSP_RC_TUNING = 111;
    public static final int MSP_PID = 112;
    public static final int MSP_BOX = 113;
    public static final int MSP_MISC = 114;
    public static final int MSP_MOTOR_PINS = 115;
    public static final int MSP_BOXNAMES = 116;
    public static final int MSP_PIDNAMES = 117;
    public static final int MSP_WP = 118;
    public static final int MSP_BOXIDS = 119;
    public static final int MSP_SERVO_CONF = 120; // out message Servo settings

    public static final int MSP_NAV_STATUS = 121; // out message Returns
    // navigation status
    public static final int MSP_NAV_CONFIG = 122; // out message Returns
    // navigation parameters

    public static final int MSP_SET_RAW_RC = 200;
    public static final int MSP_SET_RAW_GPS = 201;
    public static final int MSP_SET_PID = 202;
    public static final int MSP_SET_BOX = 203;
    public static final int MSP_SET_RC_TUNING = 204;
    public static final int MSP_ACC_CALIBRATION = 205;
    public static final int MSP_MAG_CALIBRATION = 206;
    public static final int MSP_SET_MISC = 207;
    public static final int MSP_RESET_CONF = 208;
    public static final int MSP_SET_WP = 209;
    public static final int MSP_SELECT_SETTING = 210;
    public static final int MSP_SET_HEAD = 211;
    public static final int MSP_SET_SERVO_CONF = 212;
    public static final int MSP_SET_MOTOR = 214;

    public static final int MSP_BIND = 240;

    public static final int MSP_EEPROM_WRITE = 250;

    public static final int MSP_DEBUGMSG = 253;
    public static final int MSP_DEBUG = 254;

    public static final int MODE_REQUEST = 1;
    private int mSelectedBtn;
    private int mSendingState;

    private static final int STATE_SENDING = 1;
    private static final int STATE_NO_SENDING = 2;

    private static final String TAG = "MAIN";
    public static final boolean D = true;

    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;

    private static final int MESSAGE_STATE_CHANGE = 10;
    private static final int MESSAGE_WRITE = 2;
    private static final int MESSAGE_READ = 3;

    private static final int FILEDOWNLOAD_COMP = 10;
    private static final int THRO1_LAYOUT_SIZE = 11;
    private static final int THRO2_LAYOUT_SIZE = 12;
    private static final int SENSOR_VALUE = 13;

    private static final int Multiwii_PROTOCOL = 102;

    private BluetoothService bluetoothService_obj = null;
    public StringBuffer mOutStringBuffer;

    private long lastTimeBackPressed;
    private BluetoothDevice tmpdevice;



    private MSP mMSP ;



    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private ImageView bluetooth;
    private ImageView basic_setting_btn;

    private EditText rc_rate, rc_expo, Thr_mid, Thr_expo, thro_min, thro_max, failsafe, battery_scale, current_bat
            ,warning_1, warning_2, warning_3;

    private CheckBox[][] aux_box = new CheckBox[8][12];

    private Button save_btn;

    private boolean running = false;

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if (D) Log.i(TAG, "MESSAGE_STATE_CHANGE:" + msg.arg1);

                    switch (msg.arg1) {
                        case BluetoothService.STATE_CONNECTED:
                            Toast.makeText(getApplicationContext(), "블루투스 연결에 성공하였습니다.\nDevice Address : "+bluetoothService_obj.address1, Toast.LENGTH_SHORT).show();

                            running = true;
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while(running) {
                                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_MISC));
                                        try {
                                            Thread.sleep(10);
                                        } catch (InterruptedException e) {

                                        }

                                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_RC_TUNING));
                                        try {
                                            Thread.sleep(10);
                                        } catch (InterruptedException e) {

                                        }

                                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_BOX));
                                        try {
                                            Thread.sleep(10);
                                        } catch (InterruptedException e) {

                                        }

                                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_ANALOG));
                                        try {
                                            Thread.sleep(10);
                                        } catch (InterruptedException e) {

                                        }
                                    }
                                }
                            }).start();


                            bluetooth.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth_cnt));

                            break;

                        case BluetoothService.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(), "연결중....", Toast.LENGTH_LONG).show();
                            break;

                        case BluetoothService.STATE_FAIL:
                            Toast.makeText(getApplicationContext(), "블루투스 연결에 실패하였습니다.", Toast.LENGTH_LONG).show();
                            bluetoothService_obj.stop();
                            break;
                    }
                    break;

                case Multiwii_PROTOCOL :
                    byte[] data = (byte[])msg.obj;
                    mMSP.readMSP(data);

                    break;

            }
        }
    };

    boolean MSP_MISC_state = false;
    boolean MSP_RC_TUNING_state = false;
    boolean MSP_BOX_state = false;
    boolean MSP_ANALOG_state = false;

    private float[] MSP_ANALOG_DATA;
    private float[] MSP_RC_TUNING_DATA;
    private float[] MSP_MISC_DATA;
    private boolean[][] MSP_BOX_DATA;

    private Handler MSP_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what){
                case MSP_MISC :
                    if(!MSP_MISC_state) {
                        MSP_MISC_DATA = (float[]) msg.obj;
                        thro_min.setText(String.valueOf((int) MSP_MISC_DATA[1]));
                        thro_max.setText(String.valueOf((int) MSP_MISC_DATA[2]));
                        failsafe.setText(String.valueOf((int) MSP_MISC_DATA[4]));

                        battery_scale.setText(String.valueOf((int) MSP_MISC_DATA[8]));
                        warning_1.setText(String.valueOf(MSP_MISC_DATA[9]));
                        warning_2.setText(String.valueOf(MSP_MISC_DATA[10]));
                        warning_3.setText(String.valueOf(MSP_MISC_DATA[11]));
                        MSP_MISC_state = true;
                    }
                    break;

                case MSP_RC_TUNING :
                    if(MSP_RC_TUNING_state == false) {
                        MSP_RC_TUNING_DATA = (float[]) msg.obj;

                        rc_rate.setText(String.valueOf(MSP_RC_TUNING_DATA[0]));
                        rc_expo.setText(String.valueOf(MSP_RC_TUNING_DATA[1]));
                        Thr_mid.setText(String.valueOf(MSP_RC_TUNING_DATA[5]));
                        Thr_expo.setText(String.valueOf(MSP_RC_TUNING_DATA[6]));
                        MSP_RC_TUNING_state = true;
                    }
                    break;

                case MSP_BOX :
                    if(!MSP_BOX_state){
                        MSP_BOX_DATA = (boolean[][])msg.obj;

                        for(int i=0; i<8 ; i++){
                            for(int j=0 ; j<12; j++){
                                if(MSP_BOX_DATA[i][j] == true){
                                    aux_box[i][j].setChecked(true);
                                }
                                else{
                                    aux_box[i][j].setChecked(false);
                                }
                            }
                        }

                        HorizontalScrollView scrollView = (HorizontalScrollView)findViewById(R.id.scroll) ;
                        scrollView.setVisibility(View.VISIBLE);
                        MSP_BOX_state = true;
                    }
                    break;

                case MSP_ANALOG :
                        MSP_ANALOG_DATA = (float[])msg.obj;

                        DecimalFormat form = new DecimalFormat("#.##");
                        float vbat = MSP_ANALOG_DATA[0];
                        current_bat.setText(String.valueOf(form.format(vbat)));

                    break;

            }
        }
    };





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        init_view();


        if(bluetoothService_obj == null){
            bluetoothService_obj = new BluetoothService(this, mHandler,"SETTING","MSP");
            mOutStringBuffer = new StringBuffer("");
        }

        mMSP = new MSP(bluetoothService_obj,MSP_handler);

        save_btn = (Button)findViewById(R.id.save);
        save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothService_obj.getState() != BluetoothService.STATE_CONNECTED){
                    Toast.makeText(getApplicationContext(),"드론을 연결해주세요.",Toast.LENGTH_SHORT).show();
                }

                else{
                    // BOX CHECK
                    boolean check_box_temp[][] = new boolean[8][12];
                    for (int i = 0; i < 8; i++) {
                        for (int j = 0; j < 12; j++) {
                            if (aux_box[i][j].isChecked())
                                check_box_temp[i][j] = true;
                            else
                                check_box_temp[i][j] = false;
                        }
                    }
                    for(int i=0 ; i<3 ; i++) {
                        mMSP.sendRequestMSP_SET_BOX(check_box_temp);
                        try {
                            Thread.sleep(2);
                        } catch (InterruptedException e) {
                        }
                    }

                    // MSP_SET_MISC
                    int PowerTrigger = (int) MSP_MISC_DATA[0];
                    int minthrottle = Integer.parseInt(thro_min.getText().toString());
//                battery_scale.setText(String.valueOf(minthrottle));
                    int maxthrottle = (int) MSP_MISC_DATA[2];
                    int mincommand = (int) MSP_MISC_DATA[3];
                    int failsafe_throttle = Integer.parseInt(failsafe.getText().toString());
                    float mag_decliniation = (float) MSP_MISC_DATA[7];
                    int vbatscale = (int) MSP_MISC_DATA[8];
                    float vbatlevel_warn1 = Float.parseFloat(warning_1.getText().toString());
                    float vbatlevel_warn2 = Float.parseFloat(warning_2.getText().toString());
                    float vbatlevel_crit = Float.parseFloat(warning_3.getText().toString());

                    for(int i=0; i<3; i++) {
                        mMSP.SendRequestMSP_SET_MISC(PowerTrigger, minthrottle,
                                maxthrottle, mincommand, failsafe_throttle,
                                mag_decliniation, vbatscale, vbatlevel_warn1,
                                vbatlevel_warn2, vbatlevel_crit);

                        try {
                            Thread.sleep(2);
                        } catch (InterruptedException e) {
                        }
                    }


                    float[] rc_tuning_data = new float[7];

                    rc_tuning_data[0] = Float.parseFloat(rc_rate.getText().toString());
                    rc_tuning_data[1] = Float.parseFloat(rc_expo.getText().toString());
                    rc_tuning_data[2] = MSP_RC_TUNING_DATA[2];
                    rc_tuning_data[3] = MSP_RC_TUNING_DATA[3];
                    rc_tuning_data[4] = MSP_RC_TUNING_DATA[4];
                    rc_tuning_data[5] = Float.parseFloat(Thr_mid.getText().toString());
                    rc_tuning_data[6] = Float.parseFloat(Thr_expo.getText().toString());

                    for(int i=0 ; i<3; i++) {
                        mMSP.SendRequestMSP_SET_RC_TUNING(rc_tuning_data);

                        try {
                            Thread.sleep(2);
                        } catch (InterruptedException e) {
                        }
                    }

                    running = false;
                    bluetoothService_obj.stop();
                    Toast.makeText(getApplicationContext(), "설정이 저장되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        basic_setting_btn = (ImageView) findViewById(R.id.basic_setting_btn);
        basic_setting_btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(bluetoothService_obj.getState() != BluetoothService.STATE_CONNECTED){
                    Toast.makeText(getApplicationContext(),"드론을 연결해주세요.",Toast.LENGTH_SHORT).show();
                }
                if(event.getAction() == MotionEvent.ACTION_DOWN && bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTED){
                    // BOX CHECK
                    boolean check_box_temp[][] = new boolean[8][12];
                    for(int i=0 ; i<8 ; i++){
                        for(int j=0 ; j<12 ; j++){
                            if (i == 1 & (j == 0 || j == 1 || j == 2)) {
                                check_box_temp[i][j] = true;
                            }
                            else if(i == 0 & j==11){
                                check_box_temp[i][j] = true;
                            }
                            else{
                                check_box_temp[i][j] = false;
                            }
                        }
                    }

                    for(int i=0; i<2; i++) {

                        mMSP.sendRequestMSP_SET_BOX(check_box_temp);
                        try{
                            Thread.sleep(3);
                        }catch (InterruptedException e){}
                    }

                    // MSP_SET_MISC
                    int PowerTrigger = (int)MSP_MISC_DATA[0];
                    int minthrottle = 1050;
//                battery_scale.setText(String.valueOf(minthrottle));
                    int maxthrottle = 2000;
                    int mincommand = (int)MSP_MISC_DATA[3];
                    int failsafe_throttle = 1250;
                    float mag_decliniation = (float)MSP_MISC_DATA[7];
                    int vbatscale = (int)MSP_MISC_DATA[8];
                    float vbatlevel_warn1 = (float)2.8;
                    float vbatlevel_warn2 = (float)2.5;
                    float vbatlevel_crit = (float)2.3;

                    for(int i=0; i<3; i++) {

                        mMSP.SendRequestMSP_SET_MISC(PowerTrigger, minthrottle,
                                maxthrottle, mincommand, failsafe_throttle,
                                mag_decliniation, vbatscale, vbatlevel_warn1,
                                vbatlevel_warn2, vbatlevel_crit);

                        try {
                            Thread.sleep(3);
                        } catch (InterruptedException e) {
                        }
                    }

                    float[] rc_tuning_data = new float[7];

                    rc_tuning_data[0] =(float)87.0;
                    rc_tuning_data[1] = (float)62.0;
                    rc_tuning_data[2] = MSP_RC_TUNING_DATA[2];
                    rc_tuning_data[3] = MSP_RC_TUNING_DATA[3];
                    rc_tuning_data[4] = MSP_RC_TUNING_DATA[4];
                    rc_tuning_data[5] = (float)50.0;
                    rc_tuning_data[6] = (float)0.0;

                    for(int i=0; i<2; i++) {

                        mMSP.SendRequestMSP_SET_RC_TUNING(rc_tuning_data);
                        try {
                            Thread.sleep(3);
                        } catch (InterruptedException e) {
                        }
                    }

                    running = false;
                    bluetoothService_obj.stop();
                    Toast.makeText(getApplicationContext(),"기본 설정이 완료 되었습니다.",Toast.LENGTH_SHORT).show();
                    finish();
                }
                return true;
            }
        });



    }



    public View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (v.getId()) {
                case R.id.bluetooth:
                    if (event.getAction() == MotionEvent.ACTION_UP
                            &&( (bluetoothService_obj.getState() == BluetoothService.STATE_LISTEN)
                            || (bluetoothService_obj.getState() == BluetoothService.STATE_NONE))) {
                        if (bluetoothService_obj.getDeviceState()) {
                            bluetoothService_obj.setReadRunning(true);
                            bluetoothService_obj.enableBluetooth();
                        } else {
                            finish();
                        }
                        break;
                    }

                    else if(bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTING
                            && event.getAction() == MotionEvent.ACTION_UP){
                        Toast.makeText(getApplicationContext(),"블루투스를 연결중 입니다.",Toast.LENGTH_SHORT).show();
                        return false;
                    }
                    else if(bluetoothService_obj.getState() == BluetoothService.STATE_FAIL
                            && event.getAction() == MotionEvent.ACTION_UP){
                        return false;
                    }
                    else if(event.getAction() == MotionEvent.ACTION_UP
                            && bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTED){

                        bluetooth.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth));


                        running = false;

                        bluetoothService_obj.stop();
                    }

                    break;


            }
            return true;
        }
    };



    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - lastTimeBackPressed <1500) {
            if(bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTED) {
                bluetoothService_obj.stop();
            }

            finish();
            return;
        }
        if(bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTED) {
            Toast.makeText(getApplicationContext(), "'뒤로' 버튼을 한번 더 누르면 블루투스 연결 해제 후 화면이 종료됩니다.  ", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(getApplicationContext(),"'뒤로' 버튼을 한번 더 누르면 화면이 종료됩니다.  " , Toast.LENGTH_SHORT).show();

        lastTimeBackPressed = System.currentTimeMillis();
    }

    @Override
    protected void onPause() {
        super.onPause();
        running =false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(bluetoothService_obj.device != null) {
            tmpdevice = bluetoothService_obj.device;
            bluetoothService_obj.stop();
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(tmpdevice != null){
            bluetoothService_obj.connect(tmpdevice);
            tmpdevice = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult" + resultCode);

        switch (requestCode) {
            case REQUEST_ENABLE_BT :
                if (resultCode != Activity.RESULT_OK) {
                    bluetoothService_obj.scanDevice();
                } else {//cancel button
                    Log.d(TAG, "Bluetooth is not enable");
                }
                break;

            case REQUEST_CONNECT_DEVICE :
                if (resultCode == Activity.RESULT_OK) {
                    bluetoothService_obj.getDeviceInfo(data);
                }
                break;
        }
    }

    private synchronized void sendMessage(String message, int mode){
        if (mSendingState == STATE_SENDING){
            try{
                wait();
            }catch(InterruptedException e){
                e.printStackTrace();
            }
        }

        mSendingState = STATE_SENDING;

        if(bluetoothService_obj.getState() != BluetoothService.STATE_CONNECTED){
            mSendingState = STATE_NO_SENDING;
            return;
        }

        if(message.length()>0){
            byte[] send = message.getBytes();
            bluetoothService_obj.write(send,mode);
            mOutStringBuffer.setLength(0);
        }

        mSendingState = STATE_NO_SENDING;
        notify();
    }

    private void init_view(){

        bluetooth = (ImageView)findViewById(R.id.bluetooth);
        bluetooth.setOnTouchListener(mTouchListener);

        basic_setting_btn = (ImageView)findViewById(R.id.basic_setting_btn);
        basic_setting_btn.setOnTouchListener(mTouchListener);

        rc_rate = (EditText)findViewById(R.id.rc_rate);
        rc_expo = (EditText)findViewById(R.id.rc_expo);
        Thr_mid = (EditText)findViewById(R.id.Thr_mid);
        Thr_expo = (EditText)findViewById(R.id.Thr_expo);

        thro_min = (EditText)findViewById(R.id.thro_min);
        thro_max = (EditText)findViewById(R.id.thro_max);
        failsafe = (EditText)findViewById(R.id.failsafe);

        battery_scale = (EditText)findViewById(R.id.battery_scale);
        current_bat = (EditText)findViewById(R.id.current_bat);
        warning_1 = (EditText)findViewById(R.id.warning_1);
        warning_2 = (EditText)findViewById(R.id.warning_2);
        warning_3 = (EditText)findViewById(R.id.warning_3);

        aux_box[0][0] = (CheckBox)findViewById(R.id.chec0_0);
        aux_box[0][1] = (CheckBox)findViewById(R.id.chec0_1);
        aux_box[0][2] = (CheckBox)findViewById(R.id.chec0_2);
        aux_box[0][3] = (CheckBox)findViewById(R.id.chec0_3);
        aux_box[0][4] = (CheckBox)findViewById(R.id.chec0_4);
        aux_box[0][5] = (CheckBox)findViewById(R.id.chec0_5);
        aux_box[0][6] = (CheckBox)findViewById(R.id.chec0_6);
        aux_box[0][7] = (CheckBox)findViewById(R.id.chec0_7);
        aux_box[0][8] = (CheckBox)findViewById(R.id.chec0_8);
        aux_box[0][9] = (CheckBox)findViewById(R.id.chec0_9);
        aux_box[0][10] = (CheckBox)findViewById(R.id.chec0_10);
        aux_box[0][11] = (CheckBox)findViewById(R.id.chec0_11);

        aux_box[1][0] = (CheckBox)findViewById(R.id.chec1_0);
        aux_box[1][1] = (CheckBox)findViewById(R.id.chec1_1);
        aux_box[1][2] = (CheckBox)findViewById(R.id.chec1_2);
        aux_box[1][3] = (CheckBox)findViewById(R.id.chec1_3);
        aux_box[1][4] = (CheckBox)findViewById(R.id.chec1_4);
        aux_box[1][5] = (CheckBox)findViewById(R.id.chec1_5);
        aux_box[1][6] = (CheckBox)findViewById(R.id.chec1_6);
        aux_box[1][7] = (CheckBox)findViewById(R.id.chec1_7);
        aux_box[1][8] = (CheckBox)findViewById(R.id.chec1_8);
        aux_box[1][9] = (CheckBox)findViewById(R.id.chec1_9);
        aux_box[1][10] = (CheckBox)findViewById(R.id.chec1_10);
        aux_box[1][11] = (CheckBox)findViewById(R.id.chec1_11);

        aux_box[2][0] = (CheckBox)findViewById(R.id.chec2_0);
        aux_box[2][1] = (CheckBox)findViewById(R.id.chec2_1);
        aux_box[2][2] = (CheckBox)findViewById(R.id.chec2_2);
        aux_box[2][3] = (CheckBox)findViewById(R.id.chec2_3);
        aux_box[2][4] = (CheckBox)findViewById(R.id.chec2_4);
        aux_box[2][5] = (CheckBox)findViewById(R.id.chec2_5);
        aux_box[2][6] = (CheckBox)findViewById(R.id.chec2_6);
        aux_box[2][7] = (CheckBox)findViewById(R.id.chec2_7);
        aux_box[2][8] = (CheckBox)findViewById(R.id.chec2_8);
        aux_box[2][9] = (CheckBox)findViewById(R.id.chec2_9);
        aux_box[2][10] = (CheckBox)findViewById(R.id.chec2_10);
        aux_box[2][11] = (CheckBox)findViewById(R.id.chec2_11);

        aux_box[3][0] = (CheckBox)findViewById(R.id.chec3_0);
        aux_box[3][1] = (CheckBox)findViewById(R.id.chec3_1);
        aux_box[3][2] = (CheckBox)findViewById(R.id.chec3_2);
        aux_box[3][3] = (CheckBox)findViewById(R.id.chec3_3);
        aux_box[3][4] = (CheckBox)findViewById(R.id.chec3_4);
        aux_box[3][5] = (CheckBox)findViewById(R.id.chec3_5);
        aux_box[3][6] = (CheckBox)findViewById(R.id.chec3_6);
        aux_box[3][7] = (CheckBox)findViewById(R.id.chec3_7);
        aux_box[3][8] = (CheckBox)findViewById(R.id.chec3_8);
        aux_box[3][9] = (CheckBox)findViewById(R.id.chec3_9);
        aux_box[3][10] = (CheckBox)findViewById(R.id.chec3_10);
        aux_box[3][11] = (CheckBox)findViewById(R.id.chec3_11);

        aux_box[4][0] = (CheckBox)findViewById(R.id.chec4_0);
        aux_box[4][1] = (CheckBox)findViewById(R.id.chec4_1);
        aux_box[4][2] = (CheckBox)findViewById(R.id.chec4_2);
        aux_box[4][3] = (CheckBox)findViewById(R.id.chec4_3);
        aux_box[4][4] = (CheckBox)findViewById(R.id.chec4_4);
        aux_box[4][5] = (CheckBox)findViewById(R.id.chec4_5);
        aux_box[4][6] = (CheckBox)findViewById(R.id.chec4_6);
        aux_box[4][7] = (CheckBox)findViewById(R.id.chec4_7);
        aux_box[4][8] = (CheckBox)findViewById(R.id.chec4_8);
        aux_box[4][9] = (CheckBox)findViewById(R.id.chec4_9);
        aux_box[4][10] = (CheckBox)findViewById(R.id.chec4_10);
        aux_box[4][11] = (CheckBox)findViewById(R.id.chec4_11);

        aux_box[5][0] = (CheckBox)findViewById(R.id.chec5_0);
        aux_box[5][1] = (CheckBox)findViewById(R.id.chec5_1);
        aux_box[5][2] = (CheckBox)findViewById(R.id.chec5_2);
        aux_box[5][3] = (CheckBox)findViewById(R.id.chec5_3);
        aux_box[5][4] = (CheckBox)findViewById(R.id.chec5_4);
        aux_box[5][5] = (CheckBox)findViewById(R.id.chec5_5);
        aux_box[5][6] = (CheckBox)findViewById(R.id.chec5_6);
        aux_box[5][7] = (CheckBox)findViewById(R.id.chec5_7);
        aux_box[5][8] = (CheckBox)findViewById(R.id.chec5_8);
        aux_box[5][9] = (CheckBox)findViewById(R.id.chec5_9);
        aux_box[5][10] = (CheckBox)findViewById(R.id.chec5_10);
        aux_box[5][11] = (CheckBox)findViewById(R.id.chec5_11);

        aux_box[6][0] = (CheckBox)findViewById(R.id.chec6_0);
        aux_box[6][1] = (CheckBox)findViewById(R.id.chec6_1);
        aux_box[6][2] = (CheckBox)findViewById(R.id.chec6_2);
        aux_box[6][3] = (CheckBox)findViewById(R.id.chec6_3);
        aux_box[6][4] = (CheckBox)findViewById(R.id.chec6_4);
        aux_box[6][5] = (CheckBox)findViewById(R.id.chec6_5);
        aux_box[6][6] = (CheckBox)findViewById(R.id.chec6_6);
        aux_box[6][7] = (CheckBox)findViewById(R.id.chec6_7);
        aux_box[6][8] = (CheckBox)findViewById(R.id.chec6_8);
        aux_box[6][9] = (CheckBox)findViewById(R.id.chec6_9);
        aux_box[6][10] = (CheckBox)findViewById(R.id.chec6_10);
        aux_box[6][11] = (CheckBox)findViewById(R.id.chec6_11);

        aux_box[7][0] = (CheckBox)findViewById(R.id.chec7_0);
        aux_box[7][1] = (CheckBox)findViewById(R.id.chec7_1);
        aux_box[7][2] = (CheckBox)findViewById(R.id.chec7_2);
        aux_box[7][3] = (CheckBox)findViewById(R.id.chec7_3);
        aux_box[7][4] = (CheckBox)findViewById(R.id.chec7_4);
        aux_box[7][5] = (CheckBox)findViewById(R.id.chec7_5);
        aux_box[7][6] = (CheckBox)findViewById(R.id.chec7_6);
        aux_box[7][7] = (CheckBox)findViewById(R.id.chec7_7);
        aux_box[7][8] = (CheckBox)findViewById(R.id.chec7_8);
        aux_box[7][9] = (CheckBox)findViewById(R.id.chec7_9);
        aux_box[7][10] = (CheckBox)findViewById(R.id.chec7_10);
        aux_box[7][11] = (CheckBox)findViewById(R.id.chec7_11);
    }
}
