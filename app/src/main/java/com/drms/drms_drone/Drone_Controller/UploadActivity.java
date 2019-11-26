package com.drms.drms_drone.Drone_Controller;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.drms.drms_drone.Communication.ClassicBluetooth.BluetoothService;
import com.drms.drms_drone.Communication.Protocol.MSP;
import com.drms.drms_drone.R;
import com.drms.drms_drone.Communication.Protocol.STK500v1;
import java.io.IOException;
import java.io.InputStream;


public class UploadActivity extends AppCompatActivity {

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

    private static final int ORIENT_COMMAND = 20;
    private static final int RECEIVED_MESSAGE = 21;
    private static final int UPLOAD_PROGRESS = 22;
    private static final int UPLOAD_STATE = 23;

    private static final int Multiwii_Protocol = 102;
    private BluetoothService bluetoothService_obj = null;
    public StringBuffer mOutStringBuffer;

    private long lastTimeBackPressed;
    private BluetoothDevice tmpdevice;

    private STK500v1 stk500;

    public byte orient_command;

    String hexData = null;

    private String upload_file;

    private MSP mMSP;

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private ImageView execute, bluetooth, help, uploading;

    private TextView progress_value;

    private LinearLayout progress_layout;

    private ProgressBar current_state;

    private boolean[] setCheck = {false, false, false};

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
                            progress_value.setText("블루투스를 성공적으로 연결하였습니다.\n업로드 하시려면 위 업로드 버튼을 클릭해주세요");
                            bluetooth.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth_cnt));
                            execute.setImageDrawable(getResources().getDrawable(R.drawable.upload_icon));

                            break;

                        case BluetoothService.STATE_CONNECTING:
//                            Toast.makeText(getApplicationContext(), "연결중....", Toast.LENGTH_LONG).show();
                            break;

                        case BluetoothService.STATE_FAIL:
                            Toast.makeText(getApplicationContext(), "블루투스 연결에 실패하였습니다.", Toast.LENGTH_LONG).show();
                            bluetoothService_obj.stop();
                            break;

                    }
                    break;

                case MESSAGE_READ :
                    byte[] data = (byte[])msg.obj;
                    stk500.stk500_handler.obtainMessage(RECEIVED_MESSAGE,(int)orient_command,-1,data).sendToTarget();
                    int index = 0;

                    break;

                case ORIENT_COMMAND :
                    orient_command = (byte)msg.arg1;
                    break;


                case UPLOAD_STATE :
                    switch(msg.arg1){
                        case 1:
                            current_state.setVisibility(View.GONE);


                            if(upload_file.equals("QUAD") || upload_file.equals("HEX")) {
                                progress_value.setText("업로드 완료!\n비행을 위한 설정 수행");

                                bluetoothService_obj.setProtocol("MSP");
                                try {
                                    Thread.sleep(500);
                                } catch (InterruptedException e) {
                                }
                                ;

                                MSP_basicSet();

                                try {
                                    Thread.sleep(2000);
                                } catch (InterruptedException e) {
                                }
                                ;
                            }
                            Toast.makeText(getApplicationContext(),"완료되었습니다.",Toast.LENGTH_SHORT).show();
                            bluetoothService_obj.stop();
                            finish();
                            break;

                        case -1:
                            Toast.makeText(getApplicationContext(),"업로드를 실패하였습니다.",Toast.LENGTH_SHORT).show();
                            progress_value.setText("업로드를 실패하였습니다.\n다시 한번 시도해 주세요^^");
                            execute.setVisibility(View.VISIBLE);
                            uploading.setVisibility(View.GONE);
                            current_state.setVisibility(View.INVISIBLE);
                            break;

                        case 2 :
                            progress_value.setText("아두이노 리셋 버튼을 더 빨리 눌러주세요. \n 다시 업로드를 수행해주세요.");
                            execute.setVisibility(View.VISIBLE);
                            uploading.setVisibility(View.GONE);
                            break;

                        default :

                            int progress = msg.arg1 * 100 / msg.arg2;
                            float current_byte = (float)msg.arg1 / 1000;
                            float finished_byte = (float)msg.arg2 / 1000;
                            progress_value.setText("( "+ String.valueOf(current_byte) + " KB / " + String.valueOf(finished_byte)+" KB )" +
                                    "\n" + String.valueOf(progress) + " % 완료" + "\n완료 시 화면이 자동으로 종료되니 절대 화면을 이동하지 마세요.");
                            current_state.setProgress(progress);

                            break;

                    }
                    break;

                case Multiwii_Protocol :
                    byte[] payload = (byte[])msg.obj;
//                    Toast.makeText(UploadActivity.this, "OK1", Toast.LENGTH_SHORT).show();
                    switch((payload[4] & 0xff)){
                        case MSP.MSP_SET_BOX :
                            setCheck[0] = true;
                            break;

                        case MSP.MSP_SET_MISC :
                            setCheck[1] = true;
                            break;

                        case MSP.MSP_SET_RC_TUNING :
                            setCheck[2] = true;
                            break;
                    }




                    break;




            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        getSupportActionBar().hide();

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent upload_data_intent = getIntent();
        upload_file = upload_data_intent.getStringExtra("Firmware");

        hexData = makeHexStringFromHexFile(upload_file);

        progress_value = (TextView)findViewById(R.id.progress);

        execute = (ImageView)findViewById(R.id.execute);

        uploading = (ImageView)findViewById(R.id.uploading);

        progress_layout = (LinearLayout)findViewById(R.id.progress_layout);
//        current_state = new ProgressBar(UploadActivity.this);
//        current_state.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        current_state = (ProgressBar)findViewById(R.id.upload_state);
        current_state.setVisibility(View.INVISIBLE);
//        progress_layout.addView(current_state);

        if (bluetoothService_obj == null) {
            bluetoothService_obj = new BluetoothService(this, mHandler,"UPLOAD","STK");
            mOutStringBuffer = new StringBuffer("");
        }

        stk500 = new STK500v1(UploadActivity.this, bluetoothService_obj, mHandler);

        mMSP = new MSP(bluetoothService_obj,mHandler);

        bluetooth = (ImageView)findViewById(R.id.bluetooth);
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (( (bluetoothService_obj.getState() == BluetoothService.STATE_LISTEN)
                        || (bluetoothService_obj.getState() == BluetoothService.STATE_NONE))) {
                    if (bluetoothService_obj.getDeviceState()) {
                        bluetoothService_obj.setReadRunning(true);
                        bluetoothService_obj.enableBluetooth();
                    } else {
                        finish();
                    }

                }

                else if(bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTING){
                    Toast.makeText(getApplicationContext(),"블루투스를 연결중 입니다.",Toast.LENGTH_SHORT).show();

                }
                else if(bluetoothService_obj.getState() == BluetoothService.STATE_FAIL
                        ){
                }
                else if(bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTED){

                    bluetooth.setImageDrawable(getResources().getDrawable(R.drawable.bluetooth));
                    execute.setImageDrawable(getResources().getDrawable(R.drawable.upload_icon_non));
                    progress_value.setText("블루투스 연결이 해제 되었습니다");

                    bluetoothService_obj.stop();
                }

            }
        });

        help = (ImageView)findViewById(R.id.help);
        help.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://blog.naver.com/tiger2161/221048921142")));
                }
                return true;
            }
        });

        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(bluetoothService_obj.getState() == BluetoothService.STATE_CONNECTED) {
                    current_state.setVisibility(View.VISIBLE);
                    execute.setVisibility(View.GONE);
                    uploading.setVisibility(View.VISIBLE);

                    // create STK500 Object


                    new Thread(new Runnable() {
                        @Override
                        public void run() {

                            // make a binaryfile using Hexdata
                            byte[] binaryFile = new byte[hexData.length() / 2];
                            for (int i = 0; i < hexData.length(); i += 2) {
                                binaryFile[i / 2] = Integer.decode("0x" + hexData.substring(i, i + 2)).byteValue();
                            }

                            int sizeOfByte = 256;

                            // programHexFile
//                stk500.programHexFile(byte[] binaryFile, int sizeOfByte, boolean checkWrittenData);
//                                                data           sizeofByte             true : check / false : noncheck

                            if (stk500.programHexFile(binaryFile, sizeOfByte, true)) { // program Successful

                                mHandler.obtainMessage(UPLOAD_STATE, 1, -1).sendToTarget();


                            } else { // program failed
                                mHandler.obtainMessage(UPLOAD_STATE, -1, -1).sendToTarget();
                            }

                        }
                    }).start();
                }
                else{
                    Toast.makeText(UploadActivity.this, "블루투스가 연결되지 않았습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });




        if (bluetoothService_obj.getDeviceState()) {
            bluetoothService_obj.enableBluetooth();
        } else {
            finish();
        }

    }

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

    }

    @Override
    protected void onStop() {
        super.onStop();
        if(bluetoothService_obj.device != null) {
            tmpdevice = bluetoothService_obj.device;
            bluetoothService_obj.stop();
        }
        stk500.running = false;

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

    @Override
    protected void onResume() {
        super.onResume();


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


    private String makeHexStringFromHexFile(String upload_file){
        InputStream inputStream = null;

        if(upload_file.equals("QUAD")){
            inputStream = getResources().openRawResource(R.raw.drms_quadx);
            Toast.makeText(getApplicationContext(),upload_file,Toast.LENGTH_SHORT).show();
        }

        else if(upload_file.equals("HEX")){
            inputStream = getResources().openRawResource(R.raw.drms_hex);
            Toast.makeText(getApplicationContext(),upload_file,Toast.LENGTH_SHORT).show();
        }
        else if(upload_file.equals("DRONE_CONTROLLER")){
            inputStream = getResources().openRawResource(R.raw.drone_controller);
            Toast.makeText(getApplicationContext(),"Drone\nController",Toast.LENGTH_SHORT).show();
        }

        StringBuffer buffer = new StringBuffer();
        byte[] b = new byte[1024];

        try {
            for (int n; (n =inputStream.read(b)) != -1; ) {
                buffer.append(new String(b, 0, n));
            }
        }catch (IOException e){}

        String str = buffer.toString();

        String hexData_temp = str.replaceAll(":","3A");
        hexData_temp = hexData_temp.replaceAll(System.getProperty("line.separator"),"");
        hexData_temp = hexData_temp.replaceAll("\\p{Space}","");

        return hexData_temp;

    }

    private void MSP_basicSet(){


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
                Thread.sleep(20);
            }catch (InterruptedException e){}
        }

        // MSP_SET_MISC
        int PowerTrigger = (int)0;
        int minthrottle = 1050;
//                battery_scale.setText(String.valueOf(minthrottle));
        int maxthrottle = 2000;
        int mincommand = 1000;
        int failsafe_throttle = 1250;
        float mag_decliniation = 0;
        int vbatscale = 10;
        float vbatlevel_warn1 = (float)2.8;
        float vbatlevel_warn2 = (float)2.5;
        float vbatlevel_crit = (float)2.3;

        for(int i=0; i<1; i++) {

            mMSP.SendRequestMSP_SET_MISC(PowerTrigger, minthrottle,
                    maxthrottle, mincommand, failsafe_throttle,
                    mag_decliniation, vbatscale, vbatlevel_warn1,
                    vbatlevel_warn2, vbatlevel_crit);

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }

        float[] rc_tuning_data = new float[7];

        rc_tuning_data[0] =(float)87.0;
        rc_tuning_data[1] = (float)62.0;
        rc_tuning_data[2] = 0;
        rc_tuning_data[3] = 0;
        rc_tuning_data[4] = 0;
        rc_tuning_data[5] = (float)50.0;
        rc_tuning_data[6] = (float)0.0;

        for(int i=0; i<1; i++) {

            mMSP.SendRequestMSP_SET_RC_TUNING(rc_tuning_data);
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
            }
        }

    }


}


