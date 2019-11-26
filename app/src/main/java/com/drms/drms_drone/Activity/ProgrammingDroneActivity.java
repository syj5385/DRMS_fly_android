package com.drms.drms_drone.Activity;

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

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import com.drms.drms_drone.MultiData;
import com.drms.drms_drone.R;
import com.drms.drms_drone.Service.BTService;

import java.util.Set;

/**
 * Created by comm on 2018-04-21.
 */

public class ProgrammingDroneActivity extends AppCompatActivity {
    private static final String TAG = "ProgrammingDrone";
    private BTService mBTService;
    private MultiData mspdata;

    private boolean running = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_programdrone);

        mspdata = (MultiData)this.getApplication();

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(running){
                   Log.d(TAG,"altitude : " + mspdata.getALTITUDEdata()[0] + "\tvario : " + mspdata.getALTITUDEdata()[1]);
                   try{
                       Thread.sleep(10);
                   }catch (InterruptedException e){}
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFilter();
        startService(BTService.class,BTConnection,null);
        Log.d(TAG,"programDrone Service");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(BTConnection);
        running = false;
        unregisterReceiver(programDroneRecevier);
    }

    private static Handler ProgramHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    private final ServiceConnection BTConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            mBTService = ((BTService.BtBinder) arg1).getService();
            mBTService.setHandler(ProgramHandler);
            Log.d(TAG,"Service : " + String.valueOf(mBTService));

        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBTService = null;
            Log.e(TAG,"Service Disconnected");
        }
    };

    private void setFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(BTService.DISCONNECTED_BLUETOOTH);

        filter.addAction(BTService.ArduinoReset);

        registerReceiver(programDroneRecevier,filter);
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

    private BroadcastReceiver programDroneRecevier = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };
}
