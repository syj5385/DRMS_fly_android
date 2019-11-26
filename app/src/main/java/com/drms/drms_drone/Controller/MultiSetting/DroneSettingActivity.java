package com.drms.drms_drone.Controller.MultiSetting;

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
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.drms.drms_drone.CustomAdapter.CustomAdapter1.Custom1_Item;
import com.drms.drms_drone.CustomAdapter.CustomAdapter1.CustomAdapter1;
import com.drms.drms_drone.FileManagement.FileManagement;
import com.drms.drms_drone.MultiData;
import com.drms.drms_drone.R;
import com.drms.drms_drone.Service.BTService;
import com.drms.drms_drone.Sound.SoundManager;

import java.text.DecimalFormat;
import java.util.Set;



/**
 * Created by jjunj on 2017-11-20.
 */

public class DroneSettingActivity extends AppCompatActivity {

    private static final String TAG = "DroneSettingActivity";

    private BTService mBTService;
    private SoundManager mSoundManager;
    private MultiData mspdata;

    //View
    private MyDroneSettingView drone;
    private BoxSettingView box;
    private ImageView openDrawer;
    private DrawerLayout drawer;

    private LinearLayout settinglayout;

    private static int DISPLAY_LOOP = 2;
    private int count_display = 0;

    private int SETTINGDISPLAY = 0;
    private static final int MYDRONE = 0 ;
    private static final int RC = 1;
    private static final int BOX =2;
    private static final int PID  = 3;
    private static final int GPS = 4;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dronesetting);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        sendBroadcast(new Intent(BTService.REQUEST_DISPLAY_SETTING));

        initializeView();
    }

    private void initializeView(){
        mspdata = (MultiData)this.getApplication();
        mSoundManager = new SoundManager(this);

        drawer = (DrawerLayout)findViewById(R.id.drawer);

        openDrawer = (ImageView)findViewById(R.id.open);
        openDrawer.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    switch(SETTINGDISPLAY){
                        case MYDRONE :

                            break;

                        case RC :
                            float[] rctuneData = {
                                    Float.parseFloat(rcrate.getText().toString()),
                                    Float.parseFloat(rcexpo.getText().toString()),
                                    Float.parseFloat(rpexpo.getText().toString()),
                                    Float.parseFloat(yawrate.getText().toString()),
                                    mspdata.getRCTUNEdata()[4],
                                    Float.parseFloat(thr_mid.getText().toString()),
                                    Float.parseFloat(thr_expo.getText().toString())
                            };

                            float[] miscData ={
                                    mspdata.getMISCdata()[0],
                                    Float.parseFloat(throttlemin.getText().toString()),
                                    mspdata.getMISCdata()[2],
                                    mspdata.getMISCdata()[3],
                                    Float.parseFloat(failsafe.getText().toString()),
                                    mspdata.getMISCdata()[5],
                                    mspdata.getMISCdata()[6],
                                    mspdata.getMISCdata()[7],
                                    Float.parseFloat(scale.getText().toString()),
                                    Float.parseFloat(warning1.getText().toString()),
                                    Float.parseFloat(warning2.getText().toString()),
                                    Float.parseFloat(warning3.getText().toString()),
                            };
                            mspdata.setRCTUNEdata(rctuneData);
                            mspdata.setMISCdata(miscData);

                            sendBroadcast(new Intent(BTService.REQUEST_RC_SETTING));
                            break;

                        case BOX :
                            Log.d(TAG,"send box data");
                            sendBroadcast(new Intent(BTService.REQUEST_BOX_SETTING));
                            break;

                        case PID :
                            float[] pid_p_temp = new float[10];
                            float[] pid_i_temp = new float[10];
                            float[] pid_d_temp = new float[10];

                            pid_p_temp[0] = Float.parseFloat(roll_p.getText().toString()) * 10;
                            pid_p_temp[1] = Float.parseFloat(pitch_p.getText().toString()) * 10;
                            pid_p_temp[2] = Float.parseFloat(yaw_p.getText().toString()) * 10;
                            pid_p_temp[3] = Float.parseFloat(alt_p.getText().toString()) * 10;
                            pid_p_temp[4] = Float.parseFloat(pos_p.getText().toString()) * 100;
                            pid_p_temp[5] = Float.parseFloat(posr_p.getText().toString()) * 10;
                            pid_p_temp[6] = Float.parseFloat(navr_p.getText().toString()) * 10;
                            pid_p_temp[7] = Float.parseFloat(lev_p.getText().toString()) * 10;
                            pid_p_temp[8] = Float.parseFloat(mag_p.getText().toString()) * 10;
                            pid_p_temp[9] = MSP_PID_P[9];

                            pid_i_temp[0] = Float.parseFloat(roll_i.getText().toString()) * 1000;
                            pid_i_temp[1] = Float.parseFloat(pitch_i.getText().toString()) * 1000;
                            pid_i_temp[2] = Float.parseFloat(yaw_i.getText().toString()) * 1000;
                            pid_i_temp[3] = Float.parseFloat(alt_i.getText().toString()) * 1000;
                            pid_i_temp[4] = Float.parseFloat(pos_i.getText().toString())  * 1000;
                            pid_i_temp[5] = Float.parseFloat(posr_i.getText().toString()) * 100;
                            pid_i_temp[6] = Float.parseFloat(navr_i.getText().toString()) * 100;
                            pid_i_temp[7] = Float.parseFloat(lev_i.getText().toString()) * 1000;
                            pid_i_temp[8] = MSP_PID_I[8];
                            pid_i_temp[9] = MSP_PID_I[9];

                            pid_d_temp[0] = Float.parseFloat(roll_d.getText().toString());
                            pid_d_temp[1] = Float.parseFloat(pitch_d.getText().toString());
                            pid_d_temp[2] = Float.parseFloat(yaw_d.getText().toString());
                            pid_d_temp[3] = Float.parseFloat(alt_d.getText().toString());
                            pid_d_temp[4] = MSP_PID_D[4];
                            pid_d_temp[5] = Float.parseFloat(posr_d.getText().toString()) * 1000;
                            pid_d_temp[6] = Float.parseFloat(navr_d.getText().toString()) * 1000;
                            pid_d_temp[7] = Float.parseFloat(lev_d.getText().toString());
                            pid_d_temp[8] = MSP_PID_D[8];
                            pid_d_temp[9] = MSP_PID_D[9];

                            Intent requestSetting = new Intent();

                            requestSetting.putExtra("P",pid_p_temp);
                            requestSetting.putExtra("I",pid_i_temp);
                            requestSetting.putExtra("D",pid_d_temp);
                            requestSetting.setAction(BTService.REQUEST_PID_SETTING);

                            sendBroadcast(requestSetting);
                            break;

                        case GPS :

                            break;
                    }
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                    try{
                        Thread.sleep(30);
                    }
                    catch (InterruptedException e){};
                    mSoundManager.play(0);
                    drawer.openDrawer(GravityCompat.START);

                }
                return true;
            }
        });

        settinglayout = (LinearLayout)findViewById(R.id.settinglayout);
        drone = new MyDroneSettingView(this,this);
        drone.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        settinglayout.addView(drone);

        implementationDrawer();
    }

    private FileManagement mFileManagemnet;
    private void implementationDrawer(){
        ListView drawerList = (ListView)findViewById(R.id.drawer_list);
        CustomAdapter1 adapter = new CustomAdapter1(this);
        adapter.addItem(new Custom1_Item(getResources().getDrawable(R.mipmap.mydrone),"My Drone"));
        adapter.addItem(new Custom1_Item(getResources().getDrawable(R.mipmap.rc_battery),"RC & Battery"));
        adapter.addItem(new Custom1_Item(getResources().getDrawable(R.mipmap.box),"Box Setting"));
        adapter.addItem(new Custom1_Item(getResources().getDrawable(R.mipmap.pid),"PID Setting"));
//        adapter.addItem(new Custom1_Item(getResources().getDrawable(R.mipmap.bt_setting),"GPS"));

        TextView mydevice = (TextView)findViewById(R.id.mybtAddress);
        mFileManagemnet = new FileManagement(DroneSettingActivity.this,DroneSettingHandler);
        mydevice.setText(mFileManagemnet.readBTAddress()[0]);

        drawerList.setAdapter(adapter);

        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                isRcSetting = false;
                SETTINGDISPLAY = i;
                switch(i){
                    case 0 :
                        settinglayout.removeAllViews();
                        drone = new MyDroneSettingView(DroneSettingActivity.this,DroneSettingActivity.this);
                        settinglayout.addView(drone);
                        drawer.closeDrawer(GravityCompat.START);
                        break;

                    case 1 :
                        implementationRcSetting();
                        drawer.closeDrawer(GravityCompat.START);
                        break;

                    case 2 :
                        settinglayout.removeAllViews();
                        box = new BoxSettingView(DroneSettingActivity.this,DroneSettingActivity.this);
                        settinglayout.addView(box);
                        drawer.closeDrawer(GravityCompat.START);
                        break;

                    case 3 :
                        implementationPIDSetting();
                        drawer.closeDrawer(GravityCompat.START);
                        break;

//                    case 4 :
//                        drawer.closeDrawer(GravityCompat.START);
//                        break;
                }
                mSoundManager.play(0);
            }
        });
    }

    private boolean isRcSetting = false;
    private EditText rcrate,rcexpo, rpexpo,thr_mid,thr_expo,yawrate,throttlemin,throttlemax,failsafe, scale, currentvbat, warning1,warning2, warning3;
    private void implementationRcSetting(){
        isRcSetting = true;
        settinglayout.removeAllViews();
        LinearLayout rcsettingLayout = (LinearLayout) View.inflate(this,R.layout.rcsettinglayout,null);
        rcsettingLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        settinglayout.addView(rcsettingLayout);
        final DecimalFormat form = new DecimalFormat("#.##");

        rcrate = (EditText)rcsettingLayout.findViewById(R.id.rc_rate);
        rcexpo = (EditText)rcsettingLayout.findViewById(R.id.rc_expo);
        rpexpo = (EditText)rcsettingLayout.findViewById(R.id.rp_rate);
        thr_mid = (EditText)rcsettingLayout.findViewById(R.id.Thr_mid);
        thr_expo = (EditText)rcsettingLayout.findViewById(R.id.Thr_expo);
        yawrate = (EditText)rcsettingLayout.findViewById(R.id.yaw_rate);

        throttlemin = (EditText)rcsettingLayout.findViewById(R.id.throttle_min);
        throttlemax = (EditText)rcsettingLayout.findViewById(R.id.throttle_max);
        failsafe = (EditText)rcsettingLayout.findViewById(R.id.failsafe);

        scale  = (EditText)rcsettingLayout.findViewById(R.id.vbat_scale);
        currentvbat = (EditText)rcsettingLayout.findViewById(R.id.current_vbat);
        warning1 =(EditText) rcsettingLayout.findViewById(R.id.vbat_warning1);
        warning2 = (EditText)rcsettingLayout.findViewById(R.id.vbat_warning2);
        warning3 = (EditText)rcsettingLayout.findViewById(R.id.vbat_warning3);

        throttlemin.setText(String.valueOf((int)mspdata.getMISCdata()[1]));
        throttlemax.setText(String.valueOf((int)mspdata.getMISCdata()[2]));
        failsafe.setText(String.valueOf((int)mspdata.getMISCdata()[4]));

        scale.setText(String.valueOf((int)mspdata.getMISCdata()[8]));
        warning1.setText(String.valueOf(form.format(mspdata.getMISCdata()[9])));
        warning2.setText(String.valueOf(form.format(mspdata.getMISCdata()[10])));
        warning3.setText(String.valueOf(form.format(mspdata.getMISCdata()[11])));

        rcrate.setText(String.valueOf(mspdata.getRCTUNEdata()[0]));
        rcexpo.setText(String.valueOf(mspdata.getRCTUNEdata()[1]));
        rpexpo.setText(String.valueOf(mspdata.getRCTUNEdata()[2]));
        yawrate.setText(String.valueOf(mspdata.getRCTUNEdata()[3]));
        thr_mid.setText(String.valueOf(mspdata.getRCTUNEdata()[5]));
        thr_expo.setText(String.valueOf(mspdata.getRCTUNEdata()[6]));
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isRcSetting) {
                   runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           currentvbat.setText(form.format(mspdata.getAnalogData()[0]));
                       }
                   });


                    try{
                        Thread.sleep(100);
                    }catch (InterruptedException e){};
                }

                Log.d(TAG,"end of vbat");
            }
        }).start();

    }

    private boolean isPIDSetting = false;
    private EditText roll_p, roll_i, roll_d, pitch_p, pitch_i,pitch_d, yaw_p, yaw_i,yaw_d, alt_p, alt_i, alt_d, pos_p,pos_i, posr_p,posr_i,posr_d, navr_p,navr_i,navr_d
            ,lev_p,lev_i,lev_d, mag_p;
    private float[] MSP_PID_P = new float[10];
    private float[] MSP_PID_I = new float[10];
    private float[] MSP_PID_D = new float[10];
    private void implementationPIDSetting(){
        isPIDSetting = true;
        settinglayout.removeAllViews();
        LinearLayout pidsetting = (LinearLayout) View.inflate(this,R.layout.pidsettinglayout,null);
        pidsetting.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        settinglayout.addView(pidsetting);

        roll_p = (EditText)pidsetting.findViewById(R.id.roll_p);
        roll_i = (EditText)pidsetting.findViewById(R.id.roll_i);
        roll_d = (EditText)pidsetting.findViewById(R.id.roll_d);

        pitch_p = (EditText)pidsetting.findViewById(R.id.pitch_p);
        pitch_i = (EditText)pidsetting.findViewById(R.id.pitch_i);
        pitch_d = (EditText)pidsetting.findViewById(R.id.pitch_d);

        yaw_p = (EditText)pidsetting.findViewById(R.id.yaw_p);
        yaw_i = (EditText)pidsetting.findViewById(R.id.yaw_i);
        yaw_d = (EditText)pidsetting.findViewById(R.id.yaw_d);

        alt_p = (EditText)pidsetting.findViewById(R.id.alt_p);
        alt_i = (EditText)pidsetting.findViewById(R.id.alt_i);
        alt_d = (EditText)pidsetting.findViewById(R.id.alt_d);

        pos_p = (EditText)pidsetting.findViewById(R.id.pos_p);
        pos_i = (EditText)pidsetting.findViewById(R.id.pos_i);

        posr_p = (EditText)pidsetting.findViewById(R.id.posr_p);
        posr_i = (EditText)pidsetting.findViewById(R.id.posr_i);
        posr_d = (EditText)pidsetting.findViewById(R.id.posr_d);

        navr_p = (EditText)pidsetting.findViewById(R.id.navr_p);
        navr_i = (EditText)pidsetting.findViewById(R.id.navr_i);
        navr_d = (EditText)pidsetting.findViewById(R.id.navr_d);

        lev_p = (EditText)pidsetting.findViewById(R.id.level_p);
        lev_i = (EditText)pidsetting.findViewById(R.id.level_i);
        lev_d = (EditText)pidsetting.findViewById(R.id.level_d);

        mag_p = (EditText)pidsetting.findViewById(R.id.mag_p);



        int index = 0;
        for(int i=0; i<10; i++){
            MSP_PID_P[i] = (float)mspdata.getPIDdata()[index++];
            MSP_PID_I[i] = (float)mspdata.getPIDdata()[index++];
            MSP_PID_D[i] = (float)mspdata.getPIDdata()[index++];
        }

        roll_p.setText(String.valueOf((MSP_PID_P[0])/10));
        pitch_p.setText(String.valueOf((MSP_PID_P[1])/10));
        yaw_p.setText(String.valueOf((MSP_PID_P[2])/10));
        alt_p.setText(String.valueOf((MSP_PID_P[3])/10));
        pos_p.setText(String.valueOf((MSP_PID_P[4])/100));
        posr_p.setText(String.valueOf((MSP_PID_P[5])/10));
        navr_p.setText(String.valueOf((MSP_PID_P)[6]/10));
        lev_p.setText(String.valueOf((int)(MSP_PID_P[7])/10));
        mag_p.setText(String.valueOf((int)(MSP_PID_P[8])/10));

        roll_i.setText(String.valueOf((MSP_PID_I[0])/1000));
        pitch_i.setText(String.valueOf((MSP_PID_I[1])/1000));
        yaw_i.setText(String.valueOf((MSP_PID_I[2])/1000));
        alt_i.setText(String.valueOf((MSP_PID_I[3])/1000));
        pos_i.setText(String.valueOf((MSP_PID_I[4])/1000));
        posr_i.setText(String.valueOf((MSP_PID_I[5])/100));
        navr_i.setText(String.valueOf((MSP_PID_I[6])/100));
        lev_i.setText(String.valueOf((MSP_PID_I[7])/1000));

        roll_d.setText(String.valueOf((int)(MSP_PID_D[0])));
        pitch_d.setText(String.valueOf((int)(MSP_PID_D[1])));
        yaw_d.setText(String.valueOf((int)(MSP_PID_D[2])));
        alt_d.setText(String.valueOf((int)(MSP_PID_D[3])));

        posr_d.setText(String.valueOf((MSP_PID_D[5])/1000));
        navr_d.setText(String.valueOf((MSP_PID_D)[6]/1000));
        lev_d.setText(String.valueOf((int)(MSP_PID_D[7])));


    }


    private final ServiceConnection BTConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            mBTService = ((BTService.BtBinder) arg1).getService();
            mBTService.setHandler(DroneSettingHandler);
            Log.d(TAG,"Service : " + String.valueOf(mBTService));
//            mDualJoystickView.setmBluetoothService(mBTService.getmBluetoothService());
//                       startDiscoveringBtDevice();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBTService = null;
            Log.e(TAG,"Service Disconnected");
        }
    };

    private Handler DroneSettingHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case BTService.UPDATE_UI :
                    if(count_display++ >= DISPLAY_LOOP){
                        drone.invalidate();
                        count_display = 0;
                    }

                    break;
            }
        }
    } ;

    private void setFilter(){
        IntentFilter filter = new IntentFilter();

        registerReceiver(DroneSettingReceiver,filter);

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

    private BroadcastReceiver DroneSettingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        setFilter();
        startService(BTService.class, BTConnection, null);
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(DroneSettingReceiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isRcSetting = false;
        unbindService(BTConnection);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(drawer.isDrawerOpen(GravityCompat.START)){
                drawer.closeDrawer(GravityCompat.START);
            }
            else {
                Intent requestSetting = new Intent();
                requestSetting.putExtra("REQUEST_BACK", -1);
                switch (SETTINGDISPLAY) {
                    case MYDRONE:

                        finish();
                        break;

                    case RC:
                        requestSetting.setAction(BTService.REQUEST_RC_SETTING);
                        float[] rctuneData = {
                                Float.parseFloat(rcrate.getText().toString()),
                                Float.parseFloat(rcexpo.getText().toString()),
                                Float.parseFloat(rpexpo.getText().toString()),
                                Float.parseFloat(yawrate.getText().toString()),
                                mspdata.getRCTUNEdata()[4],
                                Float.parseFloat(thr_mid.getText().toString()),
                                Float.parseFloat(thr_expo.getText().toString())
                        };
                        float[] miscData = {
                                mspdata.getMISCdata()[0],
                                Float.parseFloat(throttlemin.getText().toString()),
                                mspdata.getMISCdata()[2],
                                mspdata.getMISCdata()[3],
                                Float.parseFloat(failsafe.getText().toString()),
                                mspdata.getMISCdata()[5],
                                mspdata.getMISCdata()[6],
                                mspdata.getMISCdata()[7],
                                Float.parseFloat(scale.getText().toString()),
                                Float.parseFloat(warning1.getText().toString()),
                                Float.parseFloat(warning2.getText().toString()),
                                Float.parseFloat(warning3.getText().toString()),
                        };
                        mspdata.setRCTUNEdata(rctuneData);
                        mspdata.setMISCdata(miscData);

                        sendBroadcast(requestSetting);
                        finish();

                        break;

                    case BOX:
                        Log.d(TAG, "send box data");
                        requestSetting.setAction(BTService.REQUEST_BOX_SETTING);
                        sendBroadcast(requestSetting);
                        finish();
                        break;

                    case PID:
                        float[] pid_p_temp = new float[10];
                        float[] pid_i_temp = new float[10];
                        float[] pid_d_temp = new float[10];

                        pid_p_temp[0] = Float.parseFloat(roll_p.getText().toString()) * 10;
                        pid_p_temp[1] = Float.parseFloat(pitch_p.getText().toString()) * 10;
                        pid_p_temp[2] = Float.parseFloat(yaw_p.getText().toString()) * 10;
                        pid_p_temp[3] = Float.parseFloat(alt_p.getText().toString()) * 10;
                        pid_p_temp[4] = Float.parseFloat(pos_p.getText().toString()) * 100;
                        pid_p_temp[5] = Float.parseFloat(posr_p.getText().toString()) * 10;
                        pid_p_temp[6] = Float.parseFloat(navr_p.getText().toString()) * 10;
                        pid_p_temp[7] = Float.parseFloat(lev_p.getText().toString()) * 10;
                        pid_p_temp[8] = Float.parseFloat(mag_p.getText().toString()) * 10;
                        pid_p_temp[9] = MSP_PID_P[9];

                        pid_i_temp[0] = Float.parseFloat(roll_i.getText().toString()) * 1000;
                        pid_i_temp[1] = Float.parseFloat(pitch_i.getText().toString()) * 1000;
                        pid_i_temp[2] = Float.parseFloat(yaw_i.getText().toString()) * 1000;
                        pid_i_temp[3] = Float.parseFloat(alt_i.getText().toString()) * 1000;
                        pid_i_temp[4] = Float.parseFloat(pos_i.getText().toString()) * 1000;
                        pid_i_temp[5] = Float.parseFloat(posr_i.getText().toString()) * 100;
                        pid_i_temp[6] = Float.parseFloat(navr_i.getText().toString()) * 100;
                        pid_i_temp[7] = Float.parseFloat(lev_i.getText().toString()) * 1000;
                        pid_i_temp[8] = MSP_PID_I[8];
                        pid_i_temp[9] = MSP_PID_I[9];

                        pid_d_temp[0] = Float.parseFloat(roll_d.getText().toString());
                        pid_d_temp[1] = Float.parseFloat(pitch_d.getText().toString());
                        pid_d_temp[2] = Float.parseFloat(yaw_d.getText().toString());
                        pid_d_temp[3] = Float.parseFloat(alt_d.getText().toString());
                        pid_d_temp[4] = MSP_PID_D[4];
                        pid_d_temp[5] = Float.parseFloat(posr_d.getText().toString()) * 1000;
                        pid_d_temp[6] = Float.parseFloat(navr_d.getText().toString()) * 1000;
                        pid_d_temp[7] = Float.parseFloat(lev_d.getText().toString());
                        pid_d_temp[8] = MSP_PID_D[8];
                        pid_d_temp[9] = MSP_PID_D[9];

                        requestSetting.putExtra("P", pid_p_temp);
                        requestSetting.putExtra("I", pid_i_temp);
                        requestSetting.putExtra("D", pid_d_temp);
                        requestSetting.setAction(BTService.REQUEST_PID_SETTING);

                        sendBroadcast(requestSetting);

                        finish();
                        break;

                    case GPS:

                        break;
                }
            }
        }
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold,R.anim.appear);
    }
}

