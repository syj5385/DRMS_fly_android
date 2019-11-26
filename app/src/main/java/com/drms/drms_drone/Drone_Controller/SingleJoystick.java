package com.drms.drms_drone.Drone_Controller;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.drms.drms_drone.Communication.ClassicBluetooth.BluetoothService;
import com.drms.drms_drone.Communication.Protocol.MSP;
import com.drms.drms_drone.R;

import java.text.DecimalFormat;
import java.util.Locale;


public class SingleJoystick extends AppCompatActivity {


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
    private static final int THRO_LAYOUT_SIZE = 11;
    private static final int SENSOR_VALUE = 12;

    private static final int TIMER = 20;

    private static final int Multiwii_PROTOCOL = 102;

    private BluetoothService bluetoothService_obj = null;
    public StringBuffer mOutStringBuffer;

    private long lastTimeBackPressed;
    private BluetoothDevice tmpdevice;

    private SensorManager mSensorManager;
    private SensorEventListener mSensorEventListener;

    private Vibrator mVibe;

    private MediaPlayer mMedaiPlayer;

    private TextToSpeech tts;

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private LinearLayout throttle_layout;

    private ImageView throttle;

    private ImageView bluetooth;

    private ImageView AUX, acc_cali ;

    private ImageView drone, arrowLeft, arrowRight, arrowTop, arrowBottom, plane;

    private TextView current_bat, timer;

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private MSP mMSP;
    private float thro_layout_width, thro_layout_height;

    private int[] RCsignal = {1500, 1500, 1500, 1000, 1000,1000,1000,1000};

    private boolean power_state = false;

    private boolean running = false;

    private Thread mthread;

    private boolean istimer = false;
    private long start_time =0;
    private long cycle_time = 0;

    int[] MSP_RC_DATA = new int[8];


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
//                            Toast.makeText(getApplicationContext(), "블루투스 연결에 성공하였습니다.\nDevice Address : "+bluetoothService_obj.address1, Toast.LENGTH_SHORT).show();
                            mMSP.SendRequestMSP_ACC_CALIBRATION();
//                            Toast.makeText(getApplicationContext(), "블루투스 연결에 성공하였습니다.\nDevice Address : " + bluetoothService_obj.address1, Toast.LENGTH_SHORT).show();
                            String bt_speaking = "블루투스가 연결되었습니다. 센서를 교정합니다.";
                            tts.speak(bt_speaking,TextToSpeech.QUEUE_FLUSH,null);

                            bluetooth.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth_cnt));
                            throttle.setY(thro_layout_height - throttle.getHeight());
                            RCsignal[3] = 1000;

                            running = true;
                            mthread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    while(running) {

                                        if (istimer == true) {
                                            cycle_time = System.currentTimeMillis();
                                            long time = cycle_time - start_time;

                                            int second = 0;
                                            int minute = 0;
                                            if (time / 1000 > 0) {
                                                second = (int) time / 1000;
                                                if (second / 60 > 0) {
                                                    minute = second / 60;
                                                    second = second % 60;
                                                }
                                            }

                                            mHandler.obtainMessage(TIMER, minute, second).sendToTarget();
                                        }


                                        mMSP.sendRequestMSP_SET_RAW_RC(RCsignal);
                                        try {
                                            Thread.sleep(15);
                                        } catch (InterruptedException e) {
                                        }

                                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_RC));
                                        try {
                                            Thread.sleep(15);
                                        } catch (InterruptedException e) {
                                        }

                                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_ANALOG));
                                        try {
                                            Thread.sleep(15);
                                        } catch (InterruptedException e) {
                                        }

                                        mMSP.sendRequestMSP_SET_RAW_RC(RCsignal);
                                        try {
                                            Thread.sleep(15);
                                        } catch (InterruptedException e) {
                                        }

                                        mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_ATTITUDE));
                                        try {
                                            Thread.sleep(15);
                                        } catch (InterruptedException e) {
                                        }
                                    }
                                }
                            });

                            mthread.start();
                            break;

                        case BluetoothService.STATE_CONNECTING:
                            Toast.makeText(getApplicationContext(), "연결중....", Toast.LENGTH_LONG).show();
                            break;

                        case BluetoothService.STATE_FAIL:
                            Toast.makeText(getApplicationContext(), "블루투스 연결에 실패하였습니다.", Toast.LENGTH_LONG).show();
                            bluetoothService_obj.stop();
                            break;

                        case BluetoothService.STATE_DISCONNECTED :
                            bluetooth.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth));
                            plane.setImageDrawable(getResources().getDrawable(R.drawable.plane));
                            timer.setText("00:00");
                            running = false;
                            istimer= false;
                            power_state = false;
                            AUX.setImageDrawable(getResources().getDrawable(R.drawable.power_off));
                            bluetoothService_obj.stop();

                            break;
                    }
                    break;

                case THRO_LAYOUT_SIZE :
                    thro_layout_width = msg.arg1;
                    thro_layout_height = msg.arg2;

                    break;

                case SENSOR_VALUE :
                    float[] orientation;
                    orientation = (float[])msg.obj;
                    int roll_value = -(int)(orientation[0]*10) + 1500;
                    int pitch_value = (int)(orientation[1]*10) + 1500;


                    if(roll_value > 1755) roll_value = 1755;
                    if(roll_value < 1245) roll_value = 1245;
                    if(pitch_value > 1755) pitch_value = 1755;
                    if(pitch_value < 1245) pitch_value = 1245;

                    if(arrowTop != null && arrowBottom != null && arrowLeft != null && arrowRight != null) {
                        if (roll_value > 1550)
                            arrowRight.setImageDrawable(getResources().getDrawable(R.drawable.arrow_right_on));
                        else if (roll_value <= 1550)
                            arrowRight.setImageDrawable(getResources().getDrawable(R.drawable.arrowright));

                        if (roll_value < 1450)
                            arrowLeft.setImageDrawable(getResources().getDrawable(R.drawable.arrow_left_on));
                        else if (roll_value >= 1450)
                            arrowLeft.setImageDrawable(getResources().getDrawable(R.drawable.arrowleft));

                        if (pitch_value > 1550)
                            arrowTop.setImageDrawable(getResources().getDrawable(R.drawable.arrow_up_on));
                        else if (pitch_value <= 1550)
                            arrowTop.setImageDrawable(getResources().getDrawable(R.drawable.arrowup));

                        if (pitch_value < 1450)
                            arrowBottom.setImageDrawable(getResources().getDrawable(R.drawable.arrow_botton_on));
                        else if(pitch_value >= 1450)
                            arrowBottom.setImageDrawable(getResources().getDrawable(R.drawable.arrowbottom));
                    }

                    RCsignal[0] = roll_value;
                    RCsignal[1] = pitch_value;
                    break;

                case Multiwii_PROTOCOL :
                    byte[] data = (byte[])msg.obj;
                    mMSP.readMSP(data);

                    break;

                case TIMER :
                    int minute = msg.arg1; String minute_string = null;
                    int seconds = msg.arg2; String seconds_string = null;
                    if(minute < 10)
                        minute_string = "0" + String.valueOf(minute);
                    else
                        minute_string = String.valueOf(minute);
                    if(seconds < 10)
                        seconds_string = "0" + String.valueOf(seconds);
                    else
                        seconds_string = String.valueOf(seconds);
                    timer.setText(minute_string + ":" + seconds_string);
                    break;



            }
        }
    };

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

    private Handler MSP_handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what){
                case MSP_MISC :
                    if(!MSP_MISC_state) {
                        MSP_MISC_DATA = (float[]) msg.obj;

                        MSP_MISC_state = true;
                    }
                    break;

                case MSP_RC_TUNING :
                    if(MSP_RC_TUNING_state == false) {
                        MSP_RC_TUNING_DATA = (float[]) msg.obj;


                        MSP_RC_TUNING_state = true;
                    }
                    break;



                case MSP_ANALOG :
//                    if(!MSP_MISC_state){
                    MSP_ANALOG_DATA = (float[])msg.obj;

                    DecimalFormat form = new DecimalFormat("#.##");
                    float vbat = MSP_ANALOG_DATA[0];
                    current_bat.setText(String.valueOf(form.format(vbat)) + " [V]");
//                    }
                    break;

                case MSP_RC :
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

                case MSP_ATTITUDE :
                    MSP_ATTITUDE_DATA = (float[])msg.obj;

                    drone.setRotationX(MSP_ATTITUDE_DATA[1]);
                    drone.setRotationY(MSP_ATTITUDE_DATA[0]);

                    if(drone.getRotationX()> 70)
                        drone.setRotationX(70);
                    if(drone.getRotationX() < -70)
                        drone.setRotationX(-70);
                    if(drone.getRotationY() > 70)
                        drone.setRotationY(70);
                    if(drone.getRotationY() < -70)
                        drone.setRotationY(-70);


                    break;

                case MSP_ACC_CALIBRATION :
                    acc_cali.setImageDrawable(getResources().getDrawable(R.drawable.cali));
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_singlejoystick);

        getSupportActionBar().hide();
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AUX = (ImageView)findViewById(R.id.AUX4);
        AUX.setOnTouchListener(mTouchListener);
        acc_cali = (ImageView)findViewById(R.id.acc_calibration);
        acc_cali.setOnTouchListener(mTouchListener);


        arrowTop = (ImageView)findViewById(R.id.arrow_top);
        arrowBottom = (ImageView)findViewById(R.id.arrow_bottom);
        arrowLeft = (ImageView)findViewById(R.id.arrow_left);
        arrowRight= (ImageView)findViewById(R.id.arrow_right);

        drone = (ImageView)findViewById(R.id.drone);
        plane = (ImageView)findViewById(R.id.plane);

        current_bat = (TextView)findViewById(R.id.current_battery);
        timer = (TextView)findViewById(R.id.timer);

        throttle_layout = (LinearLayout)findViewById(R.id.throttle_layout);
        throttle_layout.getViewTreeObserver().addOnGlobalFocusChangeListener(new ViewTreeObserver.OnGlobalFocusChangeListener(){
            @Override
            public void onGlobalFocusChanged(View oldFocus, View newFocus) {
                throttle_layout.getViewTreeObserver().removeOnGlobalFocusChangeListener(this);
            }
        });
        throttle_layout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                float layout_width = throttle_layout.getWidth();
                float layout_height = throttle_layout.getHeight();

                mHandler.obtainMessage(THRO_LAYOUT_SIZE,(int)layout_width,(int)layout_height).sendToTarget();
            }
        });

        throttle_layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_MOVE) {
                    if(event.getX() > (thro_layout_width/2 - throttle.getWidth()/2)
                            && event.getX() < (thro_layout_width/2 + throttle.getWidth()/2)) {
                        throttle.setX(thro_layout_width/2 - throttle.getWidth()/2);
                        throttle.setY(event.getY() - throttle.getHeight()/2);

                        if (throttle.getY() < 0) throttle.setY(0);
                        if (throttle.getY() > thro_layout_height - throttle.getHeight())
                            throttle.setY(thro_layout_height - throttle.getHeight());


                        RCsignal[2] = 1500;

                        RCsignal[3] = (int)(1000 - throttle.getY() * 1000 / (thro_layout_height - throttle.getHeight())) + 1000;



                    }

                    else{
                        throttle.setX(event.getX() - throttle.getWidth() / 2);
                        throttle.setY(event.getY() - throttle.getHeight() / 2);

                        if (throttle.getX() < 0) throttle.setX(0);
                        if (throttle.getX() > thro_layout_width - throttle.getWidth())
                            throttle.setX(thro_layout_width - throttle.getWidth());

                        if (throttle.getY() < 0) throttle.setY(0);
                        if (throttle.getY() > thro_layout_height - throttle.getHeight())
                            throttle.setY(thro_layout_height - throttle.getHeight());

                        RCsignal[2] = (int)((throttle.getX() - (thro_layout_width/2 - throttle.getWidth()/2)) * 500 / (thro_layout_width/2 - throttle.getWidth()/2) + 1500);
//                        RCsignal[2] = 1500;
                        RCsignal[3] = (int)(1000 - throttle.getY() * 1000 / (thro_layout_height - throttle.getHeight())) + 1000;


                    }
                }

                else if(event.getAction() == MotionEvent.ACTION_UP){
                    throttle.setX(thro_layout_width/2 - throttle.getWidth()/2);
                    RCsignal[2] = 1500;
                }


                return true;
            }
        });

        if(bluetoothService_obj == null){
            bluetoothService_obj = new BluetoothService(SingleJoystick.this, mHandler,"DRONEVERT","MSP");
            mOutStringBuffer = new StringBuffer("");
        }


        throttle = new ImageView(this);
        throttle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        throttle.setImageDrawable(getResources().getDrawable(R.drawable.throttle));
        throttle_layout.addView(throttle);


        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        bluetooth = (ImageView)findViewById(R.id.bluetooth);
        bluetooth.setOnTouchListener(mTouchListener);



        if(mMSP == null) {
            mMSP = new MSP(bluetoothService_obj,MSP_handler);
        }

        mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        mSensorEventListener = new SensorListener();

        mVibe = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        mMedaiPlayer = MediaPlayer.create(this, R.raw.start);

        tts = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                tts.setLanguage(Locale.KOREAN);
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
                        current_bat.setText("0.0 [V]");
                        timer.setText("00:00");

                        running = false;
                        AUX.setImageDrawable(getResources().getDrawable(R.drawable.power_off));
                        power_state = false;
                        istimer = false;
                        RCsignal[7] = 1000;


                        for (int i=0 ; i<30 ; i++) {
                            int[] RC_temp = {1500, 1500, 1500, 1000, 1000, 1000, 1000, 1000};

                            mMSP.sendRequestMSP_SET_RAW_RC(RC_temp);

                            try {
                                Thread.sleep(15);
                            } catch (InterruptedException e) {

                            }

                            mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_RC));
                            try {
                                Thread.sleep(15);
                            } catch (InterruptedException e) {
                            }


                        }
                        bluetoothService_obj.stop();
                    }


                    break;

                case R.id.acc_calibration :
                    if(bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTED) {
                        if (event.getAction() == MotionEvent.ACTION_DOWN) {
                            mMSP.SendRequestMSP_ACC_CALIBRATION();
                            acc_cali.setImageDrawable(getResources().getDrawable(R.drawable.cali_on));
                        } else if (event.getAction() == MotionEvent.ACTION_UP) {
                            String cali_speak = "가속도 센서 교정을 시작합니다.";
                            tts.speak(cali_speak, TextToSpeech.QUEUE_FLUSH, null);
                            acc_cali.setFocusable(false);
                        }
                    }
                    break;


                case R.id.AUX4 :
                    if(event.getAction() == MotionEvent.ACTION_DOWN && bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTED) {
                        if (power_state == false) {
                            start_time = System.currentTimeMillis();
                            if(MSP_ATTITUDE_DATA[0] > -5 && MSP_ATTITUDE_DATA[0] < 5
                                    && MSP_ATTITUDE_DATA[1] > -5 && MSP_ATTITUDE_DATA[1] < 5) {
                                AUX.setImageDrawable(getResources().getDrawable(R.drawable.power_on));
                                istimer = true;
                                RCsignal[7] = 2000;
                                power_state = true;
                                mMedaiPlayer.seekTo(0);
                                mMedaiPlayer.start();
                                long[] pattern = {200, 500, 200, 500, 200, 500};
                                mVibe.vibrate(pattern, -1);  // - : once , 3 : infinite repeat,
                                String power_on_speaking = "시동이 걸렸습니다.";
                                tts.speak(power_on_speaking, TextToSpeech.QUEUE_FLUSH, null);
                            }
                            else{
                                tts.speak("드론을 수평한 곳에 두고 아두이노 리셋 버튼을 눌러주세요.",TextToSpeech.QUEUE_FLUSH,null);

                            }


                        } else {
                            AUX.setImageDrawable(getResources().getDrawable(R.drawable.power_off));
                            RCsignal[7] = 1000;
                            istimer = false;
                            power_state = false;
                            String power_off_speaking = "시동이 꺼졌습니다.";
                            tts.speak(power_off_speaking,TextToSpeech.QUEUE_FLUSH,null);
                        }
                    }
                    break;
            }
            return true;
        }
    };

    public class SensorListener implements SensorEventListener {
        float roll;
        float pitch;
        float yaw;

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] v = event.values;
            switch(event.sensor.getType()){
                case Sensor.TYPE_ORIENTATION :
                    if(yaw != v[0] || pitch != v[1] || roll != v[2]){
                        yaw = v[0];

                        pitch = v[1];

                        roll = v[2];


                        Message sensor_msg = Message.obtain();
                        float[] roll_pitch = {roll, pitch};
                        sensor_msg.what = SENSOR_VALUE;
                        sensor_msg.obj = roll_pitch;
                        mHandler.sendMessage(sensor_msg);

                    }
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Nothiing
        }
    }



    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - lastTimeBackPressed <1500) {

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
        running = false;
        AUX.setImageDrawable(getResources().getDrawable(R.drawable.power_off));
        istimer = false;
        RCsignal[7] = 1000;


        for (int i=0 ; i<30 ; i++) {
            int[] RC_temp = {1500, 1500, 1500, 1000, 1000, 1000, 1000, 1000};

            mMSP.sendRequestMSP_SET_RAW_RC(RC_temp);

            try {
                Thread.sleep(15);
            } catch (InterruptedException e) {

            }

//                mMSP.sendRequestMSP(mMSP.requestMSP(MSP.MSP_RC));
//                try {
//                    Thread.sleep(15);
//                } catch (InterruptedException e) {
//                }
        }

        if (bluetoothService_obj.device != null) {
            tmpdevice = bluetoothService_obj.device;
            bluetoothService_obj.stop();
        }

        mSensorManager.unregisterListener(mSensorEventListener);


    }

    @Override
    protected void onStop() {
        super.onStop();


    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(tmpdevice != null){
            bluetoothService_obj.connect(tmpdevice);
            tmpdevice = null;
            bluetoothService_obj.setReadRunning(true);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mSensorManager.registerListener(mSensorEventListener,mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);

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
}
