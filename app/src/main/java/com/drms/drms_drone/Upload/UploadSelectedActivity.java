package com.drms.drms_drone.Upload;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.drms.drms_drone.Communication.ClassicBluetooth.BluetoothService;
import com.drms.drms_drone.CustomAdapter.CustomAdapter3.Custom3_Item;
import com.drms.drms_drone.CustomAdapter.CustomAdapter3.CustomAdapter3;
import com.drms.drms_drone.FileManagement.FileManagement;
import com.drms.drms_drone.R;
import com.drms.drms_drone.Service.BTService;
import com.drms.drms_drone.Sound.SoundManager;

import java.util.Set;


/**
 * Created by jjunj on 2017-11-24.
 */

public class UploadSelectedActivity extends AppCompatActivity {

    public static final int REQUEST_QUAD_GY521 = 0;
    public static final int REQUEST_QUAD_GY85 = 1;
    public static final int REQUEST_QUAD_GY86 = 2;
    public static final int REQUEST_HEX_GY521 = 3;
    public static final int REQUEST_HEX_GY85 = 4;
    public static final int REQUEST_HEX_GY86 = 5;

    private static final String TAG = "UploadSelectActivity";
    private ListView quad_contents, hex_contents;
    private CustomAdapter3 quadAdapter, hexAdapter;

    private BTService mBTService;
    private FileManagement mFileManagement;
    private BluetoothService mBluetoothService;
    private SoundManager mSoundManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploadselect);


        Intent requestUpload = new Intent();
        requestUpload.setAction(BTService.REQUEST_DISPLAY_UPLOAD);
        sendBroadcast(requestUpload);

        quad_contents = (ListView)findViewById(R.id.quad_contents);
        hex_contents = (ListView)findViewById(R.id.hex_contents);

        quadAdapter = new CustomAdapter3(this);
        hexAdapter = new CustomAdapter3(this);

        quadAdapter.addItem(new Custom3_Item(getResources().getDrawable(R.mipmap.quad_drone),"Quad + GY-521","ACC + GYRO"));
        quadAdapter.addItem(new Custom3_Item(getResources().getDrawable(R.mipmap.quad_drone),"Quad + GY-85","ACC + GYRO + MAG"));
        quadAdapter.addItem(new Custom3_Item(getResources().getDrawable(R.mipmap.quad_drone),"Quad + GY-86","ACC + GYRO + MAG + BARO"));

        hexAdapter.addItem(new Custom3_Item(getResources().getDrawable(R.mipmap.hex_drone),"HEX + GY-521","ACC + GYRO"));
        hexAdapter.addItem(new Custom3_Item(getResources().getDrawable(R.mipmap.hex_drone),"HEX + GY-85","ACC + GYRO"));
        hexAdapter.addItem(new Custom3_Item(getResources().getDrawable(R.mipmap.hex_drone),"HEX + GY-86","ACC + GYRO"));

        quad_contents.setAdapter(quadAdapter);
        quad_contents.setOnItemClickListener(QuadItemClickListener);
        hex_contents.setAdapter(hexAdapter);
        hex_contents.setOnItemClickListener(HexItemClickListener);

    }

    private AdapterView.OnItemClickListener QuadItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            mSoundManager.play(0);
            int request = -1;
            switch(i){
                case 0 : // Gy521
                    request = REQUEST_QUAD_GY521;
                    break;

                case 1 :   // GY85
                    request = REQUEST_QUAD_GY85;
                    break;

                case 2 : //GY 86
                    request = REQUEST_QUAD_GY86;
                    break;
            }

            if(request != -1) {
                UploadManager uploadManager = new UploadManager(UploadSelectedActivity.this, mBluetoothService, mFileManagement.readBTAddress()[1], request);
            }
            else{
                Log.e(TAG,"error request Drone");
                finish();
            }
        }
    };

    private AdapterView.OnItemClickListener HexItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            int request = -1;
            mSoundManager.play(0);
            switch(i){
                case 0 : // Gy521
                    request = REQUEST_HEX_GY521;
                    break;

                case 1 :   // GY85
                    request = REQUEST_HEX_GY85 ;
                    break;

                case 2 : //GY 86
                    request = REQUEST_HEX_GY86;
                    break;
            }
            if(request != -1) {
                UploadManager uploadManager = new UploadManager(UploadSelectedActivity.this, mBluetoothService, mFileManagement.readBTAddress()[1], request);
            }
            else{
                Log.e(TAG,"error request Drone");
                finish();
            }
        }
    };

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    private final ServiceConnection BTConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            mBTService = ((BTService.BtBinder) arg1).getService();
            mBTService.setHandler(mHandler);
            Log.d(TAG,"Service : " + String.valueOf(mBTService));
            mBluetoothService = mBTService.getmBluetoothService();
            mFileManagement = new FileManagement(UploadSelectedActivity.this,mHandler);
            mSoundManager = new SoundManager(UploadSelectedActivity.this);

//                        startDiscoveringBtDevice();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBTService = null;
            Log.e(TAG,"Service Disconnected");
        }
    };



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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold,R.anim.appear);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFilter();
        startService(BTService.class,BTConnection,null);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(MainReceiver);
        unbindService(BTConnection);
    }

    private BroadcastReceiver MainReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

        }
    };

    private void setFilter(){
        IntentFilter filter = new IntentFilter();

        registerReceiver(MainReceiver,filter);
    }


}
