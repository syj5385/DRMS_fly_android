package com.drms.drms_drone.Activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.drms.drms_drone.Communication.ClassicBluetooth.BluetoothService;
import com.drms.drms_drone.Communication.ClassicBluetooth.DeviceListActivity;
import com.drms.drms_drone.Communication.UsbSerial.UsbService;
import com.drms.drms_drone.Communication.WifiCam.CamManager;
import com.drms.drms_drone.Controller.DroneController.WaitActivity;
import com.drms.drms_drone.CustomAdapter.CustomAdapter1.Custom1_Item;
import com.drms.drms_drone.CustomAdapter.CustomAdapter1.CustomAdapter1;
import com.drms.drms_drone.Download.DownloadManager;
import com.drms.drms_drone.FileManagement.FileManagement;
import com.drms.drms_drone.MainView.EtcView;
import com.drms.drms_drone.MainView.Playflight;
import com.drms.drms_drone.MainView.SettingView;
import com.drms.drms_drone.MainView.UploadingView;
import com.drms.drms_drone.MultiData;
import com.drms.drms_drone.R;
import com.drms.drms_drone.Service.BTService;
import com.drms.drms_drone.Sound.SoundManager;

import java.io.File;
import java.util.Set;


/**
 * Created by yeongjunsong on 2017. 11. 6..
 */

public class DroneMainActivity extends AppCompatActivity implements View.OnSystemUiVisibilityChangeListener {


    private String TAG = DroneMainActivity.class.getSimpleName();
    // Bluetooth Constants
    private int mSendingState;

    private static final int STATE_SENDING = 1;
    private static final int STATE_NO_SENDING = 2;

    public static final boolean D = true;

    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    public static final int REQUEST_CONTROLLER = 3;
    public static final int REQUEST_SETTING = 4;
    public static final int REQUEST_UPLAOD = 5;
    public static final int REQUEST_CAFE = 6;
    public static final int REQUEST_WEB = 7;
    public static final int REQUEST_PROGRAMDRONE = 8;

    private static final int MESSAGE_STATE_CHANGE = 10;
    private static final int MESSAGE_FROM_USBSERVERICE = 11;
    private static final int MESSAGE_WRITE = 2;
    private static final int MESSAGE_READ = 3;

    private String bt_address = "";
    private String bt_name = "";

    private boolean requestReconnect = false;

    private static int DISPLAY_LOOP = 2;
    private int count_display = 0;

    // LinearLayout each Menu
    private LinearLayout playflight, setting, uploading, etc;
    private ImageView menu;
    private Bitmap menuIcon;
    private ImageView bluetooth;
    private TextView device_name, device_address;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private TextView myDevice;
    private ProgressBar tryConnect;
    private ImageView controller;

    // each view
    private Playflight mPlayflight;
    private SettingView mSettingView;
    private UploadingView mUploadingView;
    private EtcView mEtcView;
    private MultiData mspdata;

    // Object
    private SoundManager mSoundManager;
    private FileManagement mFileManagement;
    private BluetoothService mBluetoothService;

    //Service
    private BTService mBTService;
    private UsbService usbService;
    private boolean BtDiscoverEnable = false;
    private CamManager cam;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fly);

//        ControllerManager cont = new ControllerManager(this);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mspdata = (MultiData)this.getApplication();
        mspdata.initializeMultiData();
        initializeView();
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        Log.d(TAG,"changed");
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
    }

    private void initializeView() {
        playflight = (LinearLayout) findViewById(R.id.playflight);
        setting = (LinearLayout) findViewById(R.id.setting);
        uploading = (LinearLayout) findViewById(R.id.uploading);
        etc = (LinearLayout) findViewById(R.id.etc);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mPlayflight = new Playflight(this,this);
        mPlayflight.setLayoutParams(new LinearLayout.LayoutParams(size.x/2, ViewGroup.LayoutParams.MATCH_PARENT));
        playflight.addView(mPlayflight);
        playflight.invalidate();

        mSettingView = new SettingView(this,this);
        mSettingView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setting.addView(mSettingView);
        mSettingView.invalidate();

        mUploadingView = new UploadingView(this,this);
        mUploadingView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        uploading.addView(mUploadingView);
        mUploadingView.invalidate();

        mEtcView = new EtcView(this,this);
        mEtcView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        etc.addView(mEtcView);
        mEtcView.invalidate();

        controller = (ImageView)findViewById(R.id.controller);
        Bitmap cont_temp = BitmapFactory.decodeResource(this.getResources(),R.mipmap.controller);
        Bitmap notcontroller = Bitmap.createBitmap(cont_temp.getWidth(),cont_temp.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas notContCanvas = new Canvas(notcontroller);
        Paint paint = new Paint();
        paint.setStrokeWidth(8);
        paint.setColor(getResources().getColor(R.color.mainTextColor));
//        paint.setA

        notContCanvas.drawBitmap(cont_temp,0,0,null);
        notContCanvas.drawLine(0,0,notContCanvas.getWidth(),notContCanvas.getHeight(),paint);
        notContCanvas.drawLine(notContCanvas.getWidth(),0,0,notContCanvas.getHeight(),paint);

        controller.setImageDrawable(new BitmapDrawable(notcontroller));


        menu = (ImageView)findViewById(R.id.menu);
        int[] imageSize = getBitmapSize(R.mipmap.ic_launcher);
        menuIcon = Bitmap.createBitmap(imageSize[0],imageSize[1], Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(menuIcon);
        Paint menuIconPaint = new Paint();
        menuIconPaint.setStrokeWidth(8);
        menuIconPaint.setColor(getResources().getColor(R.color.mainBasicColor));

        for(int i=0; i<3; i++) {
            canvas.drawLine(canvas.getWidth()/5, canvas.getHeight()*(i+1)/4, canvas.getWidth()*4/5, canvas.getHeight() *(i+1)/ 4, menuIconPaint);
        }

        menu.setImageBitmap(menuIcon);
        menu.setOnTouchListener(mainTouchListener);

        bluetooth = (ImageView)findViewById(R.id.bluetooth);
        bluetooth.setOnTouchListener(mainTouchListener);

        tryConnect = (ProgressBar)findViewById(R.id.tryconnect);

        device_name = (TextView)findViewById(R.id.device_name);
        device_address = (TextView)findViewById(R.id.device_address);

        mSoundManager = new SoundManager(this);
        mFileManagement = new FileManagement(this,mHandler);
//        mFileManagement.writeBtAddressOnFile("");

        Log.d(TAG,"mFileManager " +mFileManagement);
        if (mFileManagement.readBTAddress() != null) {
            if(mFileManagement.readBTAddress()[1] != null) {
                if (mFileManagement.readBTAddress()[1] != "") {
                    bt_name = mFileManagement.readBTAddress()[0];
                    bt_address = mFileManagement.readBTAddress()[1];
//            Toast.makeText(this,bt_name+"\n"+bt_address,Toast.LENGTH_SHORT).show();
                } else {
                    bt_name = "";
                    bt_address = "";
                }
            }

        }

        Log.d(TAG,"BT address : " + bt_address);

        mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer);

        myDevice = (TextView)findViewById(R.id.mybtAddress);
        if(bt_name != null) {
            if (bt_address.length() == 17) {
                myDevice.setText(bt_name);
            } else {
                myDevice.setText("No device");
            }

        }
        else{
            myDevice.setText("No device");
        }

        implementationDrawerList();

    }

    private void initLayout(){
        playflight.setBackgroundColor(getResources().getColor(R.color.dronemain_Color));
        setting.setBackgroundColor(getResources().getColor(R.color.dronemain_Color));
        uploading.setBackgroundColor(getResources().getColor(R.color.dronemain_Color));
        etc.setBackgroundColor(getResources().getColor(R.color.dronemain_Color));
    }

    private int[] getBitmapSize(int id) {
        try {
            BitmapFactory.Options option = new BitmapFactory.Options();
            option.inJustDecodeBounds = true;
            BitmapFactory.decodeResource(this.getResources(), id,option);
            int[] imageSize = {
                    option.outWidth,
                    option.outHeight
            };
            return imageSize;
        } catch (Exception e) {
            int[] imageSize ={0,0};
            return imageSize;
        }
    }

    private void implementationDrawerList(){
        CustomAdapter1 adapter = new CustomAdapter1(this);
        adapter.addItem(new Custom1_Item(getResources().getDrawable(R.mipmap.mydevice),"나의 디바이스"));
        adapter.addItem(new Custom1_Item(getResources().getDrawable(R.mipmap.init_device),"디바이스 초기화"));
//        adapter.addItem(new Custom1_Item(getResources().getDrawable(R.mipmap.mydevice),"programming"));

        ListView drawerlist = (ListView)DroneMainActivity.this.findViewById(R.id.drawer_list);
        drawerlist.setAdapter(adapter);
        drawerlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mSoundManager.play(0);
                switch(position){
                    case 0 :
                        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                        if(btAdapter == null){
                            Log.e(TAG,"bluetooth is not available");
                        }
                        else{
                            Log.d(TAG,"Bluetooth is available");
                            if(!btAdapter.isEnabled()){
                                Intent btEnabledIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(btEnabledIntent,REQUEST_ENABLE_BT);
                            }
                            else{
                                sendBroadcast(new Intent(BTService.REQUEST_FINISH_SERVICE));
                                bt_name = "";
                                bt_address = "";
                                mFileManagement.writeBtAddressOnFile("","");

                                Intent scanIntent = new Intent(DroneMainActivity.this, DeviceListActivity.class);
                                mBTService.setisDiscovered(false);
                                startActivityForResult(scanIntent,REQUEST_CONNECT_DEVICE);
                                overridePendingTransition(R.anim.fade,R.anim.hold);
                            }
                        }
                        break;

                    case 1:

                        bluetooth.setImageDrawable(getResources().getDrawable(R.mipmap.bluetooth));
                        device_name.setText(" Disconnected...");
                        device_address.setText("...");
                        mFileManagement.writeBtAddressOnFile("", "");
                        Log.d(TAG, "initialize Bluetooth Device");
                        myDevice.setText("No device");
                        bt_address = "";
                        bt_name = "";
                        sendBroadcast(new Intent(BTService.REQUEST_FINISH_SERVICE));
                        break;

                    case 2 :
                        Intent programDrone = new Intent(DroneMainActivity.this,ProgrammingDroneActivity.class);
                        sendBroadcast(new Intent().setAction(BTService.REQUEST_PROGRAMDRONE));
                        mDrawerLayout.closeDrawer(GravityCompat.START);
                        startActivityForResult(programDrone,REQUEST_PROGRAMDRONE);
                        overridePendingTransition(R.anim.fade,R.anim.hold);

                        break;
                }
            }
        });

        ImageView gotoCafe = (ImageView)findViewById(R.id.gotoCafe);
        gotoCafe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSoundManager.play(0);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://cafe.naver.com/drmakersystem"));
                startActivityForResult(intent,REQUEST_CAFE);
            }
        });

        ImageView gotoWeb = (ImageView)findViewById(R.id.gotoWeb);
        gotoWeb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mSoundManager.play(0);
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://drmakersystem.com"));
                startActivityForResult(intent,REQUEST_WEB);
            }
        });

    }

    private void requestSettingBtDialog(){
        AlertDialog.Builder requestBtAddress = new AlertDialog.Builder(this,R.style.dialogStyle);
        requestBtAddress.setTitle("블루투스 장치 선택 안됨");
        requestBtAddress.setCancelable(false);
        requestBtAddress.setMessage("블루투스 장치를 선택화면으로 이동하시겠습니까?")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                        if(btAdapter == null){
                            Log.e(TAG,"bluetooth is not available");
                        }
                        else{
                            Log.d(TAG,"Bluetooth is available");
                            if(!btAdapter.isEnabled()){
                                Intent btEnabledIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(btEnabledIntent,REQUEST_ENABLE_BT);
                            }
                            else{
                                Intent scanIntent = new Intent(DroneMainActivity.this, DeviceListActivity.class);
                                startActivityForResult(scanIntent,REQUEST_CONNECT_DEVICE);
                                overridePendingTransition(R.anim.fade,R.anim.hold);

                            }
                        }
                    }
                }).setNegativeButton("취소", null).create().show();
    }

    private boolean isReqeustBTConnect = false;
    private View.OnTouchListener mainTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){

            }
            else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                mSoundManager.play(0);
                switch (view.getId()){
//                    case R.id.bluetooth :
//                        isReqeustBTConnect = true;
//                        BluetoothAdapter btAdapter_temp = BluetoothAdapter.getDefaultAdapter();
//                        if(btAdapter_temp != null) {
//                            if(btAdapter_temp.isEnabled()) {
//                                if (bt_address.length() == 17) {
//                                    Intent action = new Intent();
//                                    action.setAction(BTService.REQUEST_CONNECT_BT);
//                                    action.putExtra("BT", bt_address);
//                                    sendBroadcast(action);
//                                    isReqeustBTConnect = false;
//                                } else {
//                                    requestSettingBtDialog();
//                                }
//                            }
//                            else{
//                                Intent btEnabledIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//
//                                startActivityForResult(btEnabledIntent,REQUEST_ENABLE_BT);
//                            }
//                        }
//                        else{
//                            Log.e(TAG,"Bluetooth is not available");
//                        }
//                        break;

                    case R.id.menu :
                        mDrawerLayout.openDrawer(GravityCompat.START);

                        break;
                }
            }

            return true;
        }
    };

    private int BT_Connecting_tries = 0;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case BTService.UPDATE_UI :
                    mPlayflight.invalidate();

                    break;

                case MESSAGE_FROM_USBSERVERICE :
                    switch (msg.arg1) {
                        case UsbService.MESSAGE_FROM_SERIAL_PORT:
                            String data = (String) msg.obj;
                            Log.d("USBActivity",data);


                            break;
                        case UsbService.CTS_CHANGE:
                            Toast.makeText(DroneMainActivity.this, "CTS_CHANGE", Toast.LENGTH_LONG).show();
                            break;
                        case UsbService.DSR_CHANGE:
                            Toast.makeText(DroneMainActivity.this, "DSR_CHANGE", Toast.LENGTH_LONG).show();
                            break;

                    }

                    break;
            }

        }
    };

    private final ServiceConnection BTConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            mBTService = ((BTService.BtBinder) arg1).getService();
            mBTService.setHandler(mHandler);
            Log.d(TAG,"Service : " + String.valueOf(mBTService));
            mPlayflight.setmBTService(mBTService);
            mUploadingView.setmBTService(mBTService);
            mSettingView.setmBTService(mBTService);
//                        startDiscoveringBtDevice();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBTService = null;
            Log.e(TAG,"Service Disconnected");
        }
    };

    private final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            usbService = ((UsbService.UsbBinder) arg1).getService();
            usbService.setHandler(mHandler);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            usbService = null;
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult" + resultCode);
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                Log.d(TAG,"Result Code : " + resultCode);
                if (resultCode == RESULT_OK) {
                    if(mFileManagement.readBTAddress() != null) {
                        if (mFileManagement.readBTAddress()[1] == "") {
                            Intent scanIntent = new Intent(DroneMainActivity.this, DeviceListActivity.class);
                            startActivityForResult(scanIntent, REQUEST_CONNECT_DEVICE);
                            overridePendingTransition(R.anim.fade, R.anim.hold);
                        } else {
                            if (bt_address.length() == 17) {
                                isReqeustBTConnect = false;
                            } else {
                                requestSettingBtDialog();
                            }
                        }
                    }
                } else {//cancel button
                    Log.d(TAG, "Bluetooth is not enable");
                    finish();
                }
                break;

            case REQUEST_CONNECT_DEVICE:
                if (resultCode == RESULT_OK) {
                    mFileManagement.writeBtAddressOnFile(data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_NAME),data.getStringExtra(DeviceListActivity.EXTRA_DEVICE_ADDRESS));

                    bt_address = mFileManagement.readBTAddress()[1];
                    myDevice.setText(mFileManagement.readBTAddress()[0]);
                    bt_name = mFileManagement.readBTAddress()[0];
//                    Toast.makeText(this,bt_name+"\n"+bt_address,Toast.LENGTH_SHORT).show();
//                    if(isReqeustBTConnect){
                    if(bt_address.length() == 17) {
                        isReqeustBTConnect = false;
                    }
                    else{
                        Log.e(TAG,"not selected ");
                    }
                    Intent btIntent = new Intent();
                    btIntent.setAction(BTService.BLUETOOTH_ADDRESS);
                    sendBroadcast(btIntent);

                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent requestBt = new Intent();
                            requestBt.setAction(BTService.REQUEST_CONNECT_BT);
                            requestBt.putExtra("BT",mFileManagement.readBTAddress()[1]);
                            sendBroadcast(requestBt);
                        }
                    },500);

                }
                break;

            case REQUEST_CONTROLLER :
                mBTService.setHandler(mHandler);
                sendBroadcast(new Intent(BTService.REQUEST_DISPLAY_MAIN));
                if(resultCode == 1){
                    device_name.setText("disconnected");
                    device_address.setText("...");
                }
            break;

            case REQUEST_SETTING:
                mBTService.setHandler(mHandler);
                Intent result_setting = new Intent(DroneMainActivity.this,WaitActivity.class);
                result_setting.putExtra("request",WaitActivity.REQUEST_MAIN_THREAD);
                startActivityForResult(result_setting,WaitActivity.REQUEST_MAIN_THREAD);
                overridePendingTransition(R.anim.fade,R.anim.hold);
                sendBroadcast(new Intent(BTService.REQUEST_DISPLAY_MAIN));
                break;

            case REQUEST_UPLAOD :
                mBTService.setHandler(mHandler);
                Intent result_upload = new Intent(DroneMainActivity.this,WaitActivity.class);
                result_upload.putExtra("request",WaitActivity.REQUEST_MAIN_THREAD);
                startActivityForResult(result_upload,WaitActivity.REQUEST_MAIN_THREAD);
                overridePendingTransition(R.anim.fade,R.anim.hold);
                sendBroadcast(new Intent(BTService.REQUEST_DISPLAY_MAIN));

                break;


            case REQUEST_PROGRAMDRONE :
                mBTService.setHandler(mHandler);
                Intent requst_program = new Intent(DroneMainActivity.this,WaitActivity.class);
                requst_program.putExtra("request",WaitActivity.REQUEST_MAIN_THREAD);
                startActivityForResult(requst_program,WaitActivity.REQUEST_MAIN_THREAD);
                overridePendingTransition(R.anim.fade,R.anim.hold);
                sendBroadcast(new Intent(BTService.REQUEST_DISPLAY_MAIN));

                break;



        }
    }

    private void startDiscoveringBtDevice(){
        BluetoothAdapter btAdapter_temp = BluetoothAdapter.getDefaultAdapter();
        if(btAdapter_temp != null) {
            if(btAdapter_temp.isEnabled()) {
                Intent action = new Intent();
                action.setAction(BTService.REQUEST_CONNECT_BT);
                action.putExtra("BT", bt_address);
                sendBroadcast(action);
                isReqeustBTConnect = false;
            }
            else{
                Intent btEnabledIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(btEnabledIntent,REQUEST_ENABLE_BT);
            }
        }
        else{
            Log.e(TAG,"Bluetooth is not available");
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        setFilter();
        BluetoothAdapter btAdapter=  BluetoothAdapter.getDefaultAdapter();
        if(btAdapter != null){
            if(btAdapter.isEnabled()){
                startService(BTService.class, BTConnection, null); // Start BTService(if it was not started before) and Bind it
                startService(UsbService.class, usbConnection, null); // Start UsbService(if it was not started before) and Bind it
            }
            else{
                Intent btEnabledIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(btEnabledIntent,REQUEST_ENABLE_BT);
            }
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        );

        if(mPlayflight != null) {
            mPlayflight.startStreamingVideo();

        }

        // Inintialize Wifi Camera
        cam = new CamManager("192.168.4.1", 80);

    }

    @Override
    protected void onPause() {
        super.onPause();
//        sendBroadcast(new Intent(BTService.REQUEST_DISPLAY_NOTHING));
//        sendBroadcast(new Intent(BTService.REQUEST_FINISH_SERVICE));
//        bluetooth.setImageDrawable(getResources().getDrawable(R.mipmap.bluetooth));
//        device_name.setText(" Disconnected...");
//        device_address.setText("...");
//        unregisterReceiver(MainReceiver);
//        unbindService(BTConnection);

        unregisterReceiver(MainReceiver);
//        unbindService(BTConnection);
//        unbindService(usbConnection);
        mPlayflight.stopstreaming();

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG,"onRestart()");
        Intent intent = new Intent(this,BTService.class);
        setFilter();

        bindService(intent,BTConnection, Context.BIND_AUTO_CREATE);
//        mPlayflight.setmBTService(mBTService);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"onStop");
//        unregisterReceiver(MainReceiver);
        if(mBTService != null) {
            if(mBTService.getmBluetoothService() != null) {
                if (mBTService.getmBluetoothService().getState() == BluetoothService.STATE_CONNECTED) {
//                    unbindService(BTConnection);
//                    mBTService.onUnbind()
                    requestReconnect = true;
                }
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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

    private BroadcastReceiver MainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BTService.CONNECTED_BLUETOOTH.equals(action)){
                tryConnect.setVisibility(View.GONE);
                bluetooth.setImageDrawable(getResources().getDrawable(R.mipmap.bluetooth_cnt));
                bluetooth.setVisibility(View.VISIBLE);

                device_name.setTextColor(getResources().getColor(R.color.btColor));
                device_address.setTextColor(getResources().getColor(R.color.btColor));
                device_name.setText(bt_name);
                device_address.setText(bt_address);
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        while (mBTService.getmBluetoothService().getState() == BluetoothService.STATE_CONNECTED) {
//                            Log.d(TAG, "R : " + mBTService.getMspData().getAttitudeData()[0] +
//                                    "\nP : " + mBTService.getMspData().getAttitudeData()[1] +
//                                    "\nY : " + mBTService.getMspData().getAttitudeData()[2]);
//                            try {
//                                Thread.sleep(100);
//                            } catch (InterruptedException e) {
//                            }
//                            ;
//                        }
//
//                    }
//                }).start();

            }
            else if(BTService.DISCONNECTED_BLUETOOTH.equals(action)){
                Log.d(TAG,"in main disconnected");
                bluetooth.setImageDrawable(getResources().getDrawable(R.mipmap.bluetooth));
                device_name.setTextColor(getResources().getColor(R.color.btDisconnected));
                device_address.setTextColor(getResources().getColor(R.color.btDisconnected));
                device_name.setText(" Disconnected...");
                mPlayflight.invalidate();
                device_address.setText("...");
                BtDiscoverEnable = true;
//                if(BtDiscoverEnable)
//                    startDiscoveringBtDevice();
//                isReqeustBTConnect = false;

            } else if (BTService.FAILED_BLUETOOTH.equals(action)) {
                bluetooth.setImageDrawable(getResources().getDrawable(R.mipmap.bluetooth));
                device_name.setText(" Disconnected...");
                device_address.setText("...");
//                try{
//                    Thread.sleep(1000);
//                }catch (InterruptedException e){};
//
//                if(BtDiscoverEnable)
//                    startDiscoveringBtDevice();
            }

            else if(Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)){
                String reason = intent.getStringExtra("reason");
                if(reason != null){
                    if(reason.equals("homekey")){
//                        Toast.makeText(DroneMainActivity.this,"홈", Toast.LENGTH_SHORT).show();
                        sendBroadcast(new Intent(BTService.REQUEST_DISPLAY_NOTHING));
                        sendBroadcast(new Intent(BTService.REQUEST_FINISH_SERVICE));
                        bluetooth.setImageDrawable(getResources().getDrawable(R.mipmap.bluetooth));
                        device_name.setText(" Disconnected...");
                        device_address.setText("...");
                        unregisterReceiver(MainReceiver);
                        unbindService(BTConnection);
                        unbindService(usbConnection);
//                        finish();
                    }
                    else{

                    }
                }
            }
            else if(action.equals(BTService.DISCOVER_BT)){
                tryConnect.setVisibility(View.VISIBLE);
                bluetooth.setVisibility(View.GONE);
            }
            else if(action.equals(BTService.DISCOVER_FAILED)){
                bluetooth.setImageDrawable(getResources().getDrawable(R.mipmap.bluetooth));
                bluetooth.setVisibility(View.VISIBLE);
                tryConnect.setVisibility(View.GONE);
            }

            else if(action.equals(UsbService.ACTION_USB_PERMISSION_GRANTED)){
                // USB PERMISSION GRANTED
//                Toast.makeText(context, "USB Ready", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "USB Device is connected -> check Controller\n");


            }

            else if(action.equals(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED) ){ // USB PERMISSION NOT GRANTED
                Toast.makeText(context, "조종기 권한이 거부되어 연결할 수 없습니다.", Toast.LENGTH_SHORT).show();
            }

            else if(action.equals(UsbService.ACTION_NO_USB) ){ // USB PERMISSION NOT GRANTED
//                Toast.makeText(context, "No USB connected", Toast.LENGTH_SHORT).show();
            }
            else if(action.equals(UsbService.ACTION_USB_DISCONNECTED) ){ // USB PERMISSION NOT GRANTED
                Toast.makeText(context, "장치가 분리되었습니다", Toast.LENGTH_SHORT).show();
                controller.setImageDrawable(getResources().getDrawable(R.mipmap.controller));
                TextView Cont_text = (TextView)findViewById(R.id.controller_text);
                Cont_text.setText("Controller\ndisconnected");
                Cont_text.setTextColor(getResources().getColor(R.color.contDis));
            }
            else if(action.equals(UsbService.ACTION_USB_NOT_SUPPORTED) ){ // USB PERMISSION NOT GRANTED
                Toast.makeText(context, "USB device not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            else if(action.equals(UsbService.CONNECTED_CONTROLLER)){
                controller.setImageDrawable(getResources().getDrawable(R.mipmap.controller));
                TextView Cont_text = (TextView)findViewById(R.id.controller_text);
                Cont_text.setText("Controller\nConnected");
                Cont_text.setTextColor(Color.BLUE);
            }

            else if(action.equals(BTService.NEXT_DISPLAY)){
                mPlayflight.executeController();
            }
            else if (action.equals(BTService.PREVIOUS_DISPLAY)) {
                sendBroadcast(new Intent(BTService.REQUEST_DISPLAY_NOTHING));
                sendBroadcast(new Intent(BTService.REQUEST_FINISH_SERVICE));
                bluetooth.setImageDrawable(getResources().getDrawable(R.mipmap.bluetooth));
                device_name.setText(" Disconnected...");
                device_address.setText("...");
//                unregisterReceiver(MainReceiver);
                unbindService(BTConnection);
                unbindService(usbConnection);

                String bye_msg = "#bye";
                mspdata.setControllerConnected(false);
                usbService.write(bye_msg.getBytes());
                finish();
            }

        }
    };

    private void setFilter(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BTService.CONNECTED_BLUETOOTH);
        filter.addAction(BTService.DISCONNECTED_BLUETOOTH);
        filter.addAction(BTService.FAILED_BLUETOOTH);
        filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        filter.addAction(BTService.DISCOVER_BT);
        filter.addAction(BTService.DISCOVER_FAILED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_GRANTED);
        filter.addAction(UsbService.ACTION_NO_USB);
        filter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        filter.addAction(UsbService.ACTION_USB_NOT_SUPPORTED);
        filter.addAction(UsbService.ACTION_USB_PERMISSION_NOT_GRANTED);
        filter.addAction(UsbService.CONNECTED_CONTROLLER);
        filter.addAction(BTService.NEXT_DISPLAY);
        filter.addAction(BTService.PREVIOUS_DISPLAY);
        registerReceiver(MainReceiver,filter);
    }



    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(mDrawerLayout.isDrawerOpen(GravityCompat.START)){
                mDrawerLayout.closeDrawer(GravityCompat.START);
            }
            else{
                sendBroadcast(new Intent(BTService.REQUEST_DISPLAY_NOTHING));
                sendBroadcast(new Intent(BTService.REQUEST_FINISH_SERVICE));
                bluetooth.setImageDrawable(getResources().getDrawable(R.mipmap.bluetooth));
                device_name.setText(" Disconnected...");
                device_address.setText("...");
//                unregisterReceiver(MainReceiver);
                String bye_msg = "#bye";
                mspdata.setControllerConnected(false);
                usbService.write(bye_msg.getBytes());
                unbindService(BTConnection);
                unbindService(usbConnection);
                finish();
            }
        }

        return true;
    }





}
