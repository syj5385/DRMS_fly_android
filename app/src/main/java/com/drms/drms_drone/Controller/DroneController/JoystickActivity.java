package com.drms.drms_drone.Controller.DroneController;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.drms.drms_drone.MultiData;
import com.drms.drms_drone.R;
import com.drms.drms_drone.Service.BTService;
import com.drms.drms_drone.Sound.SoundManager;

import java.util.Set;



/**
 * Created by jjunj on 2017-11-10.
 */

public class JoystickActivity extends AppCompatActivity {

    private static final String TAG = "JoystickActivity";

    private BTService mBTService;
    private Joystick_view mDualJoystickView;
    private SingleJoystickView mSingleJoystickView;
    private DrawerLayout Joystick1_drawer_layout;
    private MultiData mspdata;
    private SoundManager mSoundManager;

    private LinearLayout joystick_layout;

    public static final int REQUEST_WAIT = 0;


    int battery_level = 0;

    public  static final int REQUEST_JOYSTICK_MENU = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );



        joystick_layout = (LinearLayout)findViewById(R.id.joystick_layout);
        mspdata = (MultiData)this.getApplication();

//        mJoystick = new Joystick1_view(this,this,JoystickHandler);
        if(mspdata.getMYJOYSTICK() == MultiData.DUAL1) {
            mDualJoystickView = new Dual1JoystickView(this, this, JoystickHandler);
            mDualJoystickView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            joystick_layout.addView(mDualJoystickView);
        }
        else if(mspdata.getMYJOYSTICK() == MultiData.DUAL2){
            mDualJoystickView = new Dual2JoystickView(this,this,JoystickHandler);
            mDualJoystickView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            joystick_layout.addView(mDualJoystickView);
        }
        else if(mspdata.getMYJOYSTICK() == MultiData.SINGLE){
            mSingleJoystickView = new SingleJoystickView(this);
            joystick_layout.addView(mSingleJoystickView);
        }


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mSoundManager = new SoundManager(this);

//        Intent waitIntent = new Intent(this, WaitActivity.class);
//        startActivityForResult(waitIntent,REQUEST_WAIT);
    }

    private final ServiceConnection BTConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            mBTService = ((BTService.BtBinder) arg1).getService();
            mBTService.setHandler(JoystickHandler);
            Log.d(TAG,"Service : " + String.valueOf(mBTService));
            if(mDualJoystickView != null) {
                mDualJoystickView.setmBluetoothService(mBTService.getmBluetoothService());
            }
            if(mSingleJoystickView != null){

            }
//                       startDiscoveringBtDevice();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBTService = null;
            Log.e(TAG,"Service Disconnected");
        }
    };

    private Handler JoystickHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case BTService.UPDATE_UI :
                    if(mDualJoystickView != null)
                        mDualJoystickView.invalidate();
                    if(mSingleJoystickView != null){
                        mSingleJoystickView.invalidate();
                    }
                    break;

                case REQUEST_JOYSTICK_MENU :
                    if(mspdata.getReceivedRcdata()[3] <= 1050){
                        implementationJoystickSettingMenu();
                    }
                    else{
                        Toast.makeText(getApplicationContext(),"드론이 비행 중 입니다.", Toast.LENGTH_SHORT).show();
                    }

                    break;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
//        Toast.makeText(this,"onResume",Toast.LENGTH_SHORT).show();
        setFilter();
        startService(BTService.class, BTConnection, null);
//        Intent serviceIntent = new Intent(this,BTService.class);
//        bindService(serviceIntent,BTConnection,Context.BIND_AUTO_CREATE);
        if(mBTService != null){
//            mspdata.setRawRCDataAux(4,2000);
        }

        Log.d(TAG,"start on");
    }

    @Override
    protected void onPause() {
        super.onPause();

//        Toast.makeText(this,"onPause",Toast.LENGTH_SHORT).show();

        unbindService(BTConnection);
        for(int i=1; i<5; i++)
        mspdata.setRawRCDataAux(i,1000);

        mspdata.initializeMultiData();
//        JoystickHandler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//            }
//        },1000)
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(JoystickReceiver);
        if(mDualJoystickView != null) {
            if (mDualJoystickView.getmSensorManager() != null && mDualJoystickView.getmSensorEventListener() != null) {
                Log.d(TAG, "unregister Listener");
                mDualJoystickView.getmSensorManager().unregisterListener(mDualJoystickView.getmSensorEventListener());
            }
        }
    }

    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {

        Intent startService = new Intent(this, service);
        if (extras != null && !extras.isEmpty()) {
            Set<String> keys = extras.keySet();
            for (String key : keys) {
                String extra = extras.getString(key);
                startService.putExtra(key, extra);
            }
        }

        Intent bindingIntent = new Intent(this,service);
        bindService(bindingIntent,serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private BroadcastReceiver JoystickReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(Intent.ACTION_BATTERY_CHANGED)){
                battery_level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL,0);
                Log.d(TAG,"vbat : " + battery_level);
                MultiData msp = (MultiData)JoystickActivity.this.getApplication();
                msp.setMobile_vbat(battery_level);

            }
            if(action.equals(BTService.DISCONNECTED_BLUETOOTH)){
                mBTService.setCurrentDisplay(BTService.MAINACTIVITY);
                JoystickActivity.this.setResult(-1);
                finish();
            }

            if(action.equals(Setting1View.REQUEST_DUAL1_JOYSTICK) || action.equals(Setting1View.REQUEST_DUAL2_JOYSTICK) /*|| action.equals(Setting1View.REQUEST_SINGLE_JOYSTICK)*/){
                if(mDualJoystickView.getmSensorManager() != null && mDualJoystickView.getmSensorEventListener() != null) {
                    Log.d(TAG,"unregister Listener");
                    mDualJoystickView.getmSensorManager().unregisterListener(mDualJoystickView.getmSensorEventListener());
                }
                Log.d(TAG,"received Message : " + action);
                updateJoystick(action);
            }

            if(action.equals(Setting1View.REQUEST_SINGLE_JOYSTICK)){
                if(mDualJoystickView.getmSensorManager() != null && mDualJoystickView.getmSensorEventListener() != null) {
                    Log.d(TAG,"unregister Listener");
                    mDualJoystickView.getmSensorManager().unregisterListener(mDualJoystickView.getmSensorEventListener());
                }
                Log.d(TAG,"received Message : " + action);
                updateJoystick(action);
            }
            if(action.equals(BTService.ArduinoReset)){
                finish();
            }
        }
    };

    private void setFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(BTService.DISCONNECTED_BLUETOOTH);
        filter.addAction(Setting1View.REQUEST_DUAL1_JOYSTICK);
        filter.addAction(Setting1View.REQUEST_DUAL2_JOYSTICK);
        filter.addAction(Setting1View.REQUEST_SINGLE_JOYSTICK);
        filter.addAction(BTService.ArduinoReset);

        registerReceiver(JoystickReceiver,filter);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){

            finish();
        }
        return true;
    }


    private void implementationJoystickSettingMenu(){
        Intent settingIntent = new Intent(this,JoystickSettingActivity.class);
        startActivity(settingIntent);
        overridePendingTransition(R.anim.move,R.anim.hold);
    }

    private void updateJoystick(String request){
        if(mDualJoystickView != null) {
            joystick_layout.removeAllViews();
            mDualJoystickView = null;
            mSingleJoystickView = null;
        }
        if(request.equals(Setting1View.REQUEST_DUAL1_JOYSTICK)){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mDualJoystickView = new Dual1JoystickView(this, this, JoystickHandler);
            joystick_layout.addView(mDualJoystickView);
            mDualJoystickView.setmBluetoothService(mBTService.getmBluetoothService());
        }
        else if(request.equals(Setting1View.REQUEST_DUAL2_JOYSTICK)){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            mDualJoystickView = new Dual2JoystickView(this, this, JoystickHandler);
            joystick_layout.addView(mDualJoystickView);
            mDualJoystickView.setmBluetoothService(mBTService.getmBluetoothService());
        }

        else if(request.equals(Setting1View.REQUEST_SINGLE_JOYSTICK)){
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            mSingleJoystickView = new SingleJoystickView(this);
            joystick_layout.addView(mSingleJoystickView);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_WAIT){

        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold,R.anim.appear);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unbindService(BTConnection);
    }
}
