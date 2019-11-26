package com.drms.drms_drone.Controller.DroneController;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.view.View;

import com.drms.drms_drone.Communication.ClassicBluetooth.BluetoothService;
import com.drms.drms_drone.MultiData;
import com.drms.drms_drone.R;
import com.drms.drms_drone.Sound.SoundManager;


/**
 * Created by jjunj on 2017-11-10.
 */

public class DrsControllerView extends View {

    protected static final String TAG = "DrsControllerView";

    protected SoundManager mSoundManager;
    protected Context mContext;
    protected Activity mActivity;
    protected Handler mHandler;
    protected BluetoothService mBluetoothService;

    protected Bitmap backBitmap ;
    protected MultiData mspdata;

    protected float width = 0;
    protected final int unitWidth = 85;
    protected float height = 0;
    protected final int unitHeight = 60;
    protected  float x,y;
    protected Throttle droneThrottle;

    protected long timer  = 0;
    protected long init_time = 0 ;
    protected int minute = 0 ;
    protected int seconds = 1;

    protected boolean unlocked_left_throttle = false;
    protected boolean unlocked_right_throttle = false;
    protected boolean[][] unlock_throttle  = new boolean[2][2];

    // first   [ left  right ]
    // second [ left  right  ]

    public DrsControllerView(Context context, Activity mActivity, Handler mHandler) {
        super(context);
        this.mActivity = mActivity;
        this.mContext = context;
        mspdata = (MultiData)mActivity.getApplication();

        this.mHandler = mHandler;
        mSoundManager = new SoundManager(mContext);
        init_time = System.currentTimeMillis() / 1000;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Paint paint = new Paint();
        paint.setStrokeWidth(5);
        paint.setColor(Color.WHITE);
        if(backBitmap == null){
            width = canvas.getWidth();
            height = canvas.getHeight();
            x = width / unitWidth;
            y = height / unitHeight;

            Bitmap scaled = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.cont_back),(int)width,(int)height,true);
            this.setBackground(new BitmapDrawable(scaled));

            backBitmap = drawBackground(width, unitWidth,height,unitHeight);
            this.setBackground(new BitmapDrawable(backBitmap));
        }

        drawController(canvas);

    }

    protected Bitmap drawBackground(float width, int unitW, float height, int unitH){
//        Bitmap contBacktemp = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(this.getResources(),R.drawable.cont_back),
//                (int)width,(int)height,true);
//
//        Bitmap contBack = Bitmap.createBitmap(contBacktemp.getWidth(),contBacktemp.getHeight(), Bitmap.Config.ARGB_8888);
//        Paint tempPaint = new Paint();
//        tempPaint.setAlpha(50);
//        Canvas tempCanvas = new Canvas(contBack);


        Paint[] backPaint = new Paint[10];

        backPaint[0] = new Paint(); // Joystick Paint
        backPaint[0].setAlpha(20);
        backPaint[0].setStrokeWidth(3);
        backPaint[0].setColor(mContext.getResources().getColor(R.color.mainBasicColor));
        backPaint[0].setARGB(130,167,167,167);
        backPaint[0].setStyle(Paint.Style.FILL);

        Bitmap tempBitmap = Bitmap.createBitmap((int)width, (int)height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(tempBitmap);

//        canvas.drawBitmap(contBack,0,0,tempPaint);


        // Roll pitch
        backPaint[1] = new Paint(); // roll, pitch
        backPaint[1].setStrokeWidth(7);
        backPaint[1].setColor(Color.BLACK);;

        backPaint[2] = new Paint();
        backPaint[2].setStrokeWidth(5);
        backPaint[2].setColor(mContext.getResources().getColor(R.color.mainTextColor));

        canvas.drawLine(35*x,13*y,37*x,13*y,backPaint[1]);
        canvas.drawLine(48*x,13*y,50*x,13*y,backPaint[1]);
        canvas.drawLine(35*x,27*y,37*x,27*y,backPaint[1]);
        canvas.drawLine(48*x,27*y,50*x,27*y,backPaint[1]);
        canvas.drawLine(35*x+backPaint[1].getStrokeWidth()/2,13*y-backPaint[1].getStrokeWidth()/2,35*x+backPaint[1].getStrokeWidth()/2,27*y + backPaint[1].getStrokeWidth()/2,backPaint[1]);
        canvas.drawLine(50*x+backPaint[1].getStrokeWidth()/2,13*y-backPaint[1].getStrokeWidth()/2,50*x+backPaint[1].getStrokeWidth()/2,27*y + backPaint[1].getStrokeWidth()/2,backPaint[1]);
        canvas.drawLine(40*x,13*y,45*x,13*y,backPaint[2]);
        canvas.drawLine(40*x,20*y,45*x,20*y,backPaint[2]);
        canvas.drawLine(40*x,27*y,45*x,27*y,backPaint[2]);

        backPaint[2].setColor(Color.BLACK);
        for(int i=0 ; i<6 ; i++)
            canvas.drawLine(42*x,(14+i)*y, 43*x,(14+i)*y,backPaint[2]);
        for(int i=0 ; i<6 ; i++)
            canvas.drawLine(42*x,(21+i)*y, 43*x,(21+i)*y,backPaint[2]);

        backPaint[2].setColor(mContext.getResources().getColor(R.color.vbatColor));
        backPaint[2].setStrokeWidth(10);


        // drawTream icon
        BitmapFactory.Options option = new BitmapFactory.Options();
        option.inSampleSize = 2;
        Bitmap[] arrowIcon =  {
                BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.move_up,option),    // up
                BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.move_down,option),   // down
                BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.move_left,option),    // left
                BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.move_right,option),   // right
        };
        Bitmap[] arrowIcon_on =  {
                BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.up_on,option),    // up
                BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.down_on,option),   // down
                BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.left_on,option),    // left
                BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.right_on,option),   // right
        };

        Bitmap[] arrowIconTemp = new Bitmap[4];
        Bitmap[] arrowIconTemp_on = new Bitmap[4];

        for(int i=0 ; i<4; i++)
            arrowIconTemp[i] = Bitmap.createScaledBitmap(arrowIcon[i],(int)(5*x),(int)(5*y),true);

        for(int i=0 ; i<4; i++)
            arrowIconTemp_on[i] = Bitmap.createScaledBitmap(arrowIcon[i],(int)(5*x),(int)(5*y),true);

//        canvas.drawBitmap(arrowIconTemp[0],40*x,30*y,null);
//        canvas.drawBitmap(arrowIconTemp[1],40*x,40*y,null);
//        canvas.drawBitmap(arrowIconTemp[2],3*x,46*y,null);
//        canvas.drawBitmap(arrowIconTemp[3],28*x,46*y,null);
//        canvas.drawBitmap(arrowIconTemp[2],52*x,46*y,null);
//        canvas.drawBitmap(arrowIconTemp[3],77*x,46*y,null);

        if(mspdata.getTream_touched()[MultiData.ROLL_DOWN]){
            canvas.drawBitmap(arrowIconTemp_on[2],52*x,46*y,null);
        }
        else{
            canvas.drawBitmap(arrowIconTemp[2],52*x,46*y,null);
        }

        if(mspdata.getTream_touched()[MultiData.ROLL_UP]){
            canvas.drawBitmap(arrowIconTemp_on[3],77*x,46*y,null);
        }
        else{
            canvas.drawBitmap(arrowIconTemp[3],77*x,46*y,null);
        }

        if(mspdata.getTream_touched()[MultiData.PITCH_DOWN]){
            canvas.drawBitmap(arrowIconTemp_on[1],40*x,40*y,null);
        }
        else{
            canvas.drawBitmap(arrowIconTemp[1],40*x,40*y,null);
        }

        if(mspdata.getTream_touched()[MultiData.PITCH_UP]){
            canvas.drawBitmap(arrowIconTemp_on[0],40*x,30*y,null);
        }
        else{
            canvas.drawBitmap(arrowIconTemp[0],40*x,30*y,null);
        }

        if(mspdata.getTream_touched()[MultiData.YAW_DOWN]){
            canvas.drawBitmap(arrowIconTemp_on[2],3*x,46*y,null);
        }
        else{
            canvas.drawBitmap(arrowIconTemp[2],3*x,46*y,null);
        }

        if(mspdata.getTream_touched()[MultiData.YAW_UP]){
            canvas.drawBitmap(arrowIconTemp_on[3],28*x,46*y,null);
        }
        else{
            canvas.drawBitmap(arrowIconTemp[3],28*x,46*y,null);
        }


        // menu icon
        Bitmap menuIcon = Bitmap.createBitmap((int)(7*x),(int)(5*y), Bitmap.Config.ARGB_8888);
        Canvas temp = new Canvas(menuIcon);
        Paint menuIconPaint = new Paint();
        menuIconPaint.setStrokeWidth(5);
        menuIconPaint.setColor(Color.BLACK);
        for(int i=0; i<3; i++) {
            canvas.drawLine(temp.getWidth()/5, temp.getHeight()*(i+1)/4, temp.getWidth()*4/5, temp.getHeight() *(i+1)/ 4, menuIconPaint);
        }
        canvas.drawBitmap(menuIcon,2*x,2*y,null);

        // Bottom Line
        backPaint[3] = new Paint();
        backPaint[3].setARGB(255,250,224,212);
        backPaint[3].setStrokeWidth(5);
        backPaint[3].setStyle(Paint.Style.STROKE);

//        canvas.drawRect(72*x, 52*y, 84*x,59*y,backPaint[3]);    //drone battery case
//        canvas.drawRect(59*x, 52*y, 71*x, 59*y,backPaint[3]);   // mobile battery case
//        canvas.drawRect(x,52*y, 6*x,59*y,backPaint[3]); // luetooth case


        // Bottom information
        // 1) Drone Battery
        Bitmap vbatIcon = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.hex_drone);
        Bitmap icon = Bitmap.createScaledBitmap(vbatIcon,(int)(5*x),(int)(5*y),true);
        canvas.drawBitmap(icon,73*x,53*y,null);

        Bitmap mobileIcon = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.mobile);
        Bitmap mobile = Bitmap.createScaledBitmap(mobileIcon,(int)(5*x),(int)(5*y),true);
        canvas.drawBitmap(mobile,60*x, 53*y,null);

        // timer Background
        Bitmap timerback_temp = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.timer_back);
        Bitmap timer_back = Bitmap.createScaledBitmap(timerback_temp,(int)(20*x), (int)(9*y),true);
        canvas.drawBitmap(timer_back,(int)(57*x),(int)(4*y),null);

        return tempBitmap;
    }

    private boolean isArmed = false;
    protected void drawController(Canvas canvas){
//        if(!isArmed && mspdata.getRcdata()[7] == 2000){
//            isArmed = true;
//        }
//        if(isArmed) {
        timer = System.currentTimeMillis() / 1000 - init_time;
//        }

        minute = (int) (timer / 60);
        seconds = (int) (timer % 60);

        Paint[] contPaint = new Paint[10];

        // draw Roll Pitch Graph
        contPaint[0] = new Paint();
        contPaint[0].setStrokeWidth(10);
        contPaint[0].setColor(mContext.getResources().getColor(R.color.splashBack));

        float pitch = -mspdata.getAttitudeData()[1];
        float roll = mspdata.getAttitudeData()[0];

        if(pitch > 60)  pitch = 60;
        if(pitch < -60) pitch = -60;
        if(roll > 90)   roll = 90;
        if(roll < -90)  roll = -90;

        float[] center = {(35*x + 50*x)/2, (13*y + 27*y)/2 + (5*x)*(pitch/60)};
        float radius = 5*x;

        canvas.drawLine(37*x,center[1], 48*x,center[1] ,contPaint[0]);

        contPaint[0].setColor(mContext.getResources().getColor(R.color.mainTopic_2));
        center[1] = (13*y + 27*y)/2;

        canvas.drawLine((float)(center[0]+radius* (Math.cos((roll * 3.141592)/180))),
                (float)(center[1]+radius*(Math.sin((roll * 3.141592)/180))),
                (float)(center[0]+radius* (Math.cos(((roll+180) * 3.141592)/180))),
                (float)(center[1]+radius*(Math.sin(((roll+180) * 3.141592)/180))),
                contPaint[0]);


        // drawThrottle
        // 1) left
//        canvas.drawBitmap(droneThrottle.getThrottleImage(),droneThrottle.getLeft()[0],droneThrottle.getLeft()[1],null);
//        canvas.drawBitmap(droneThrottle.getThrottleImage(),droneThrottle.getRight()[0],droneThrottle.getRight()[1],null);

        // draw Current Battery
        contPaint[1] = new Paint();
        contPaint[1].setTextSize((float)(2.5*x));
        contPaint[1].setColor(mContext.getResources().getColor(R.color.mainBasicColor));
        contPaint[1].setStrokeWidth(3);
        contPaint[1].setTextAlign(Paint.Align.CENTER);

        int currentVbat = (int)(((mspdata.getAnalogData()[0] - 2.5)/1.7) * 100);
        float droneBatWidth = contPaint[1].measureText(String.valueOf(currentVbat) + " %");

        canvas.drawText(String.valueOf(currentVbat) + " %",(78+85)/2*x , (52+59)/2*y+ contPaint[1].getTextSize()/2,contPaint[1]);

        int mobileVbat = mspdata.getMobile_vbat();
        float mobileBatWidth = contPaint[1].measureText(String.valueOf(mobileVbat) + " %");
        canvas.drawText(String.valueOf(mobileVbat) + " %",(65+71)/2*x , (52+59)/2*y+ contPaint[1].getTextSize()/2,contPaint[1]);

        // draw Bluetooth Connection

        Bitmap bluetooth  = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.bluetooth);
        if(mBluetoothService != null) {
            if (mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
                bluetooth = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.bluetooth_cnt);
            } else {
                bluetooth = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.bluetooth);
            }
        }

        float scale = 5*x / bluetooth.getHeight();
        Bitmap bt_image = Bitmap.createScaledBitmap(bluetooth,(int)(bluetooth.getWidth()*scale),(int)(bluetooth.getHeight()*scale),true);
        canvas.drawBitmap(bt_image, x,53*y,null);

        // draw Aux Btn
        Bitmap caliIcon = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.cali);
        Bitmap caliIcon_on = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.aux_on);
        Bitmap Aux4 = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.aux4);
        Bitmap[] cali = new Bitmap[4];
        for(int i=0; i<3; i++){
            if(mspdata.getReceivedRcdata()[i+4] == 1000){
                cali[i] = Bitmap.createScaledBitmap(caliIcon,(int)(5*x),(int)(5*y),true);
            }
            else if(mspdata.getReceivedRcdata()[i+4] == 2000){
                cali[i] = Bitmap.createScaledBitmap(caliIcon_on,(int)(5*x),(int)(5*y),true);
            }
            else{
                cali[i] = Bitmap.createScaledBitmap(caliIcon,(int)(5*x),(int)(5*y),true);
            }
        }


        Bitmap aux = Bitmap.createScaledBitmap(Aux4,(int)(5*x),(int)(5*y),true);
        contPaint[2] = new Paint();
        contPaint[2].setColor(Color.BLACK);
        contPaint[2].setTextSize(2*x);
        contPaint[2].setTextAlign(Paint.Align.CENTER);

        canvas.drawBitmap(cali[0], (int)(14*x), (int)(3*y),null);  // AUX1
        canvas.drawBitmap(cali[1], (int)(22*x), (int)(3*y),null);  // AUX2
        canvas.drawBitmap(cali[2], (int)(29*x), (int)(3*y),null);  // AUX3

        canvas.drawText("Aux1",(int)(16.5*x),(int)(11*y),contPaint[2]);
        canvas.drawText("Aux2",(int)(24.5*x),(int)(11*y),contPaint[2]);
        canvas.drawText("Aux3",(int)(31.5 *x),(int)(11*y),contPaint[2]);



        // Aux Test
        Bitmap plane;
        if(mspdata.getReceivedRcdata()[7] == 1000){
            plane = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.plane);
        }
        else if(mspdata.getReceivedRcdata()[7] == 2000){
            plane = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.plane_on);
        }
        else{
            plane = BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.plane);
        }

        Bitmap plane_image = Bitmap.createScaledBitmap(plane,(int)(7*x),(int)(7*y),true);
        canvas.drawBitmap(plane_image,58*x,5*y,null);

        //Timer
        contPaint[3] = new Paint();
        contPaint[3].setColor(Color.BLACK);
        contPaint[3].setStrokeWidth(3);
        contPaint[3].setTextSize(3*x);
        contPaint[3].setTextAlign(Paint.Align.CENTER);
        String minute_text ;
        String seconds_text;
        if(minute < 10)
            minute_text = "0" + String.valueOf(minute);
        else
            minute_text = String.valueOf(minute);
        if(seconds < 10)
            seconds_text = "0" + String.valueOf(seconds);
        else
            seconds_text = String.valueOf(seconds);

        // Tream
        contPaint[4] = new Paint();
        contPaint[4].setTextSize(2*y);
        contPaint[4].setColor(Color.WHITE);
        contPaint[4].setStrokeWidth(8);
        contPaint[4].setTextAlign(Paint.Align.CENTER);

        canvas.drawText(String.valueOf(mspdata.getTream()[0]), (int)((float)(52+82)/2*x),(int)((float)(46+51)/2*y)+contPaint[4].getTextSize()/2,contPaint[4]); // roll Tream
        canvas.drawText(String.valueOf(mspdata.getTream()[1]), (int)((float)(40+45)/2*x),(int)((float)(35+40)/2*y)+contPaint[4].getTextSize()/2,contPaint[4]); // roll Tream
        canvas.drawText(String.valueOf(mspdata.getTream()[2]), (int)((float)(3+33)/2*x),(int)((float)(46+51)/2*y)+contPaint[4].getTextSize()/2,contPaint[4]); // roll Tream

        String timer_text = minute_text + ":" + seconds_text;
        canvas.drawText(timer_text, (int)((66+77)/2*x),(int)((4+13)/2*y)+contPaint[3].getTextSize()/2, contPaint[3]);

        // Calibration
        Bitmap accCali;
        if(mspdata.getCalibration()[0] == true){
            accCali = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.cali_on),(int)(5*x),(int)(5*y),true);
        }
        else{;
            accCali = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.cali),(int)(5*x),(int)(5*y),true);
        }

        Bitmap magCali;
        if(mspdata.getCalibration()[1] == true){
            magCali = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.cali_on),(int)(5*x),(int)(5*y),true);
        }
        else{;
            magCali = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(mContext.getResources(),R.mipmap.cali),(int)(5*x),(int)(5*y),true);
        }

        canvas.drawText("acc교정",((float)42.5)*x , 8*y+contPaint[4].getTextSize(),contPaint[4]);
        canvas.drawText(" mag교정",((float)49.5)*x , 8*y+contPaint[4].getTextSize(),contPaint[4]);

        canvas.drawBitmap(accCali,40*x, 3*y,  null);
        canvas.drawBitmap(magCali,47*x, 3*y,  null);

        contPaint[5] = new Paint();
        contPaint[5].setTextAlign(Paint.Align.CENTER);
        contPaint[5].setColor(mContext.getResources().getColor(R.color.mainBasicColor));
        contPaint[5].setStrokeWidth(4);

    }




    public class Throttle{

        private Bitmap throttleImage;
        private int[] left, right;
        private int throttle_width, throttle_height;
        private int[][] throttle_position = new int[2][2];
        private int[] RawRc = new int[4];   // roll , picth, yaw, Throttle

        public Throttle(Bitmap throttle, int[] left, int[] right) {
            this.throttleImage = throttle;
            this.left = left;
            this.right = right;
            for(int i=0; i<2; i++){
                for(int j=0 ; j<2; j++){
                    throttle_position[i][j] = 0;
                }
            }

            RawRc[0] = 1500;
            RawRc[1] = 1500;
            RawRc[2] = 1500;
            RawRc[3] = 1000;
        }

        public void setLeft(int[] left) {
            this.left = left;
//            RawRc[3] = (1000 - ((left[1] - throttle_position[0][1]) * 1000 / (throttle_height - throttleImage.getHeight()))) + 1000;
            RawRc[3] = (((throttle_position[0][1]+throttle_height-throttleImage.getHeight()/2)-(left[1]+throttleImage.getHeight()/2)) * 1000 / (throttle_height-throttleImage.getHeight())) + 1000;

            RawRc[2] = (((left[0]+throttleImage.getWidth()/2)-(throttle_position[0][0]+throttle_width/2-throttle_width/getWidth()/2)) * mspdata.getDRONE_SPEED()/(throttle_width/2-throttleImage.getWidth()/2)) + 1500 + mspdata.getTream()[2];

            mspdata.setRawRCDataYawThrottle(RawRc[2], RawRc[3]);
//            Log.w(TAG,"yaw : " + RawRc[2]);
//            Log.w(TAG,"throttle : " +RawRc[3]);
        }

        public void setRight(int[] right){
            this.right = right;
//            RawRc[1] = (1000- ( (right[1] - throttle_position[1][1]) * 1000 / (throttle_height-throttleImage.getHeight()))) + 1000;
//            RawRc[0] = ( ( (right[0] - throttle_position[1][0]) * 1000 / (throttle_width-throttleImage.getWidth()))) + 1000;
//            RawRc[1] = ((right[1]-(throttle_height/2-throttleImage.getHeight()/2)) * (300)/(throttle_height/2-throttleImage.getHeight()/2)) + 1500 + mspdata.getTream()[1];
//            RawRc[1] = (((throttle_position[1][1]+throttle_height-throttleImage.getHeight()/2)-(right[1]+throttleImage.getHeight()/2)) * 1000 / (throttle_height-throttleImage.getHeight())) + 1000;
//            RawRc[1] = ((throttle_position[1][1]+throttleImage.getHeight()/2)+throttle_height/2 - (right[1]+throttleImage.getHeight()/2)) * mspdata.getDRONE_SPEED() / (throttle_height/2-throttleImage.getHeight()/2) + 1500;
            RawRc[0] = (((right[0]+throttleImage.getWidth()/2)-(throttle_position[1][0]+throttle_width/2)) * mspdata.getDRONE_SPEED()/(throttle_width/2-throttleImage.getWidth()/2)) + 1500 + mspdata.getTream()[0];
            RawRc[1] = (((throttle_position[1][1]+throttle_height/2)-(right[1]+throttleImage.getHeight()/2)) * mspdata.getDRONE_SPEED()/(throttle_height/2-throttleImage.getHeight()/2)) + 1500 + mspdata.getTream()[1];

            mspdata.setRawRCDataRollPitch(RawRc[0], RawRc[1]);
//            Log.w(TAG,"roll : " + RawRc[0]);

        }

        public Bitmap getThrottleImage(){
            return throttleImage;
        }

        public int[] getLeft(){
            return left;
        }

        public int[] getRight(){
            return right;
        }

        public void setThrottle_width(int throttle_width){
            this.throttle_width = throttle_width;
        }

        public void setThrottle_height(int throttle_height){
            this.throttle_height = throttle_height;
        }

        public void setThrottle_position(int[][] position){
            throttle_position = position;
        }
    }

    public void setmBluetoothService(BluetoothService mBluetoothService){
        this.mBluetoothService = mBluetoothService;
    }

    protected SensorManager mSensorManager;
    protected SensorEventListener mSensorEventListener;

    protected SensorManager getmSensorManager(){
        return mSensorManager;
    }

    protected SensorEventListener getmSensorEventListener(){
        return mSensorEventListener;
    }

    protected class SensorListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    }


}
