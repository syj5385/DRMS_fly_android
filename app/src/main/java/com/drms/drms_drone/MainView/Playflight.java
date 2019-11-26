package com.drms.drms_drone.MainView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.drms.drms_drone.Activity.DroneMainActivity;
import com.drms.drms_drone.Communication.ClassicBluetooth.BluetoothService;
import com.drms.drms_drone.Controller.DroneController.JoystickActivity;
import com.drms.drms_drone.MultiData;
import com.drms.drms_drone.R;
import com.drms.drms_drone.Service.BTService;
import com.drms.drms_drone.Sound.SoundManager;


/**
 * Created by yeongjunsong on 2017. 11. 7..
 */

public class Playflight extends LinearLayout {

    private SoundManager mSoundManager;

    private BTService mBTService;

    private double Pi = 3.141592;

    // Variable
    float canvas_width = 0;
    float canvas_height = 0;

    private String TAG = Playflight.class.getSimpleName();

    private Context mContext;
    private Activity mActivity;

    private Paint[] mPaint = new Paint[10];

    private NextBtn flight_play;
    private CurrentDrone myDrone;
    private MultiData mspdata;


    public Playflight(Context context, Activity mActivity) {
        super(context);

        this.mContext = context;
        this.mActivity = mActivity;
        mspdata = (MultiData)mActivity.getApplication();
        mSoundManager = new SoundManager(context);
    }

    public void setmBTService(BTService mBTService){
        this.mBTService = mBTService;
        Log.d(TAG,"set BTService in PlayFlight");
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // initialize canvas Size
        if(canvas_width == 0 && canvas_height == 0){
            canvas_width = canvas.getWidth();
            canvas_height = canvas.getHeight();
            Log.d(TAG,"Canvas Width : " + canvas_width +  "\nCanvas Height : " + canvas_height);
        }

        drawTopicAndNext(canvas);
        drawCurrentDrone(canvas);
    }


    private void drawTopicAndNext(Canvas canvas) {

        mPaint[0] = new Paint();
        mPaint[0].setTextAlign(Paint.Align.LEFT);
        mPaint[0].setTextSize(70);
        mPaint[0].setStrokeWidth(5);
        mPaint[0].setColor(mContext.getResources().getColor(R.color.mainTopic_2));

        Bitmap temp = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.nextbtn);
        canvas.drawText(" 비 행 ", mPaint[0].getTextSize()/2,mPaint[0].getTextSize()*3/2,mPaint[0] );

    }

    private void drawCurrentDrone(Canvas canvas){
        mPaint[1] = new Paint();
        mPaint[1].setStrokeWidth(5);
        mPaint[1].setColor(mContext.getResources().getColor(R.color.mainTopic_1));
        mPaint[1].setStyle(Paint.Style.STROKE);

        mPaint[2] = new Paint();
        mPaint[2].setColor(Color.BLACK);
        mPaint[2].setStrokeWidth(7);
        mPaint[2].setStyle(Paint.Style.STROKE);

        float radius = canvas.getHeight()/6;
        float[] center = {canvas.getWidth()/5,canvas.getHeight()*2/5};


        float[] positionOfRect ={
                canvas.getWidth()*26/30, canvas.getHeight()*3/30, canvas.getWidth()*29/30,
                canvas.getHeight()*9/30
        };

        float width = positionOfRect[2] - positionOfRect[0];
        canvas.drawRect(positionOfRect[0], positionOfRect[1], positionOfRect[2],
                positionOfRect[3],mPaint[2]);
        canvas.drawRect(positionOfRect[0]+width/3,canvas.getHeight()*2/30,positionOfRect[0]+width*2/3,
                positionOfRect[1],mPaint[2]);

        mPaint[3] = new Paint();
        mPaint[3].setTextSize(50);
        mPaint[3].setStrokeWidth(4);
        mPaint[3].setColor(mContext.getResources().getColor(R.color.mainBasicColor));
        mPaint[3].setTextAlign(Paint.Align.CENTER);


        int[] positionOfText = {(int)(canvas.getWidth()/2), (int)(center[1]-radius + mPaint[3].getTextSize()/2 )};
        canvas.drawText("R : ",positionOfText[0],positionOfText[1],mPaint[3]);
        canvas.drawText("P : ",positionOfText[0],positionOfText[1]+radius,mPaint[3]);
        canvas.drawText("Y : ",positionOfText[0],positionOfText[1]+radius*2,mPaint[3]);

        float longestWidth = mPaint[3].measureText("Pitch");

        int roll_temp = 0 ;
        int pitch_temp = 0;
        int yaw_temp = 0;

        if(mBTService != null){
            roll_temp = (int)mspdata.getAttitudeData()[0];
            pitch_temp = (int)mspdata.getAttitudeData()[1];
            yaw_temp = (int)mspdata.getAttitudeData()[2];
        }

        canvas.drawText(String.valueOf(roll_temp), positionOfText[0]+longestWidth,positionOfText[1],mPaint[3]);
        canvas.drawText(String.valueOf(pitch_temp), positionOfText[0]+longestWidth,positionOfText[1]+radius,mPaint[3]);
        canvas.drawText(String.valueOf(yaw_temp), positionOfText[0]+longestWidth,positionOfText[1]+radius*2,mPaint[3]);
        if(roll_temp > 180)
            roll_temp = 180;
        if(roll_temp < -180)
            roll_temp = -180;
        if(pitch_temp > 60)
            pitch_temp = 60;
        if(pitch_temp < -60)
            pitch_temp = -60;

        mPaint[4] = new Paint();
        mPaint[4].setStrokeWidth(10);
        mPaint[4].setColor(mContext.getResources().getColor(R.color.rollColor));

        mPaint[5] = new Paint();
        mPaint[5].setStrokeWidth(10);
        mPaint[5].setColor(mContext.getResources().getColor(R.color.pitchColor));


        int[] roll_line ={
                (int)(center[0] + radius*(Math.cos(roll_temp * Pi / 180))),
                (int)(center[1] + radius*(Math.sin(roll_temp * Pi / 180))),
                (int)(center[0] + radius*(Math.cos((roll_temp + 180)*Pi / 180))),
                (int)(center[1] + radius*(Math.sin((roll_temp + 180)*Pi / 180)))
        };

        int[] pitch_line ={
                (int)(center[0] + radius*(Math.cos(-pitch_temp * Pi / 180))),
                (int)(center[1] + radius*(Math.sin(-pitch_temp * Pi / 180))),
                (int)(center[0] + radius*(Math.cos((180+pitch_temp) * Pi / 180))),
                (int)(center[1] + radius*(Math.sin(-pitch_temp * Pi / 180))),
        };

        int[] degree = {0,45,90,135,180,225,270,315,360};
        canvas.drawLine(roll_line[0], roll_line[1], roll_line[2], roll_line[3],mPaint[4]);
        canvas.drawLine(pitch_line[0], pitch_line[1], pitch_line[2], pitch_line[3],mPaint[5]);

        canvas.drawCircle(center[0],center[1],radius,mPaint[1]);
        mPaint[6] = new Paint();
        mPaint[6].setStrokeWidth(8);
        mPaint[6].setColor(mContext.getResources().getColor(R.color.mainBasicColor));
        for(int i=0; i<degree.length ;i++){
            canvas.drawPoint((float)(center[0] + radius*(Math.cos(degree[i] *Pi / 180))),
                    (float)(center[1]+radius*(Math.sin(degree[i] * Pi / 180))),mPaint[6]);
        }

        mPaint[7] = new Paint();
        mPaint[7].setStyle(Paint.Style.FILL);
        mPaint[7].setColor(mContext.getResources().getColor(R.color.vbatColor));

        float height = canvas.getHeight()/5 - mPaint[2].getStrokeWidth();

        float current_vbat = mspdata.getAnalogData()[0];
        if(current_vbat == (float)0.1){
            current_vbat = (float)2.5;
        }
        float currentVbat = (float)((positionOfRect[3]-mPaint[2].getStrokeWidth()/2)-((current_vbat - 2.5)/(1.7) * height));

        canvas.drawRect(positionOfRect[0]+mPaint[2].getStrokeWidth()/2,currentVbat ,
                positionOfRect[2]-mPaint[2].getStrokeWidth()/2,positionOfRect[3]-mPaint[2].getStrokeWidth()/2,mPaint[7]);


        mPaint[8] = new Paint();
        mPaint[8].setColor(mContext.getResources().getColor(R.color.flightColor));
        mPaint[8].setTextSize(80);
        mPaint[8].setStrokeWidth(4);
        mPaint[8].setTextAlign(Paint.Align.CENTER);


        if(mBTService != null) {
            if(mBTService.getmBluetoothService() != null) {
                if (mBTService.getmBluetoothService().getState() == BluetoothService.STATE_CONNECTED) {
                    if (roll_temp <= 4 && roll_temp >= -4 &&
                            pitch_temp <= 4 && pitch_temp >= -4) {
                        canvas.drawText("비 행  가 능", canvas.getWidth() / 2, canvas.getHeight() * 7 / 8, mPaint[8]);
                    } else {
                        mPaint[8].setColor(Color.RED);
                        canvas.drawText("비 행  불 가", canvas.getWidth() / 2, canvas.getHeight() * 7 / 8, mPaint[8]);
                    }
                } else {
                    mPaint[8].setColor(Color.RED);
                    canvas.drawText("Disconnected", canvas.getWidth() / 2, canvas.getHeight() * 7 / 8, mPaint[8]);
                }
            }
            else{
                mPaint[8].setColor(Color.RED);
                canvas.drawText("Disconnected", canvas.getWidth() / 2, canvas.getHeight() * 7 / 8, mPaint[8]);
            }

        }
        else{
            mPaint[8].setColor(Color.RED);
            canvas.drawText("Disconnected", canvas.getWidth() / 2, canvas.getHeight() * 7 / 8, mPaint[8]);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){

//            if((event.getX() >= flight_play.getX() && event.getX() <= flight_play.getX()+flight_play.getIcon().getWidth())
//                    &&  (event.getY() >= flight_play.getY() && event.getY() <= flight_play.getY() + flight_play.getIcon().getHeight())){
//                flight_play.setDisplayed_icon(true);
//                invalidate();
//                Log.d(TAG,"onTouch");
//            }

        }
        else if(event.getAction() == MotionEvent.ACTION_UP){
            try{
                Thread.sleep(30);
            }
            catch (InterruptedException e){};
//            if((event.getX() >= flight_play.getX() && event.getX() <= flight_play.getX()+flight_play.getIcon().getWidth())
//                    &&  (event.getY() >= flight_play.getY() && event.getY() <= flight_play.getY() + flight_play.getIcon().getHeight())){
//                Intent playControlIntent = new Intent();
//                playControlIntent.setAction(BTService.REQUEST_DISPLAY_CONTROLLER);
//                mActivity.sendBroadcast(playControlIntent);
//            }
//            flight_play.setDisplayed_icon(false);
//            LinearLayout temp_layout = (LinearLayout)mActivity.findViewById(R.id.playflight);
//            temp_layout.setBackgroundColor(mContext.getResources().getColor(R.color.dronemain_Color));
//            invalidate();

            Log.d(TAG,"onTouch PlayFlight");
            // check
            if(mBTService.getmBluetoothService().getState() == BluetoothService.STATE_CONNECTED){
                if(mspdata.getAttitudeData()[0] > -5 && mspdata.getAttitudeData()[0] < 5){
                    if(mspdata.getAttitudeData()[1] >= -5 && mspdata.getAttitudeData()[1] <= 5 ){
                        Intent JoystickIntent = new Intent(mContext, JoystickActivity.class);

                        mActivity.sendBroadcast(new Intent().setAction(BTService.REQUEST_DISPLAY_CONTROLLER));
                        mActivity.startActivityForResult(JoystickIntent, DroneMainActivity.REQUEST_CONTROLLER);
                        mActivity.overridePendingTransition(R.anim.fade,R.anim.hold);

                        mSoundManager.play(0);
                    }
                }
            }
            else if(mBTService.getmBluetoothService().getState() == BluetoothService.STATE_CONNECTING){
                Toast.makeText(mActivity,"드론 연결 중...",Toast.LENGTH_SHORT).show();

            }
            else{
                Toast.makeText(mActivity,"드론을 연결해 주세요.",Toast.LENGTH_SHORT).show();
            }

        }
        return true;
    }

    private class NextBtn{

        private Bitmap icon,clickedicon;
        private Bitmap displayed_icon ;
        private float x,y;

        public NextBtn(Bitmap icon, Bitmap clickedicon, float x, float y) {
            this.icon = icon;
            this.clickedicon = clickedicon;
            this.x = x;
            this.y = y;
            this.displayed_icon = icon;
        }

        public Bitmap getIcon(){
            return icon;
        }

        public Bitmap getClickedicon(){
            return clickedicon;
        }

        public float getX(){
            return x;
        }

        public float getY(){
            return y;
        }

        public Bitmap getDisplayed_icon(){
            return displayed_icon;
        }

        public void setDisplayed_icon(boolean clicked){
            if(clicked){
                displayed_icon = clickedicon;
            }
            else{
                displayed_icon = icon;
            }
        }
    }

    private class CurrentDrone{

        private Bitmap drone;
        private float x, y;

        private float roll, pitch, yaw;
        private float current_vbat;

        public CurrentDrone(Bitmap drone, float x, float y) {
            this.drone = drone;
            this.x = x;
            this.y = y;
        }

        public Bitmap getDrone(){
            return drone;
        }

        public float getX(){
            return x;
        }

        public float getY(){
            return y;
        }

        public void setRPY(float[] rpy){
            this.roll = rpy[0];
            this.pitch = rpy[1] ;
            this.yaw = rpy[2];
        }

        public void setVbat(float vbat){
            this.current_vbat = vbat;
        }
    }



}
