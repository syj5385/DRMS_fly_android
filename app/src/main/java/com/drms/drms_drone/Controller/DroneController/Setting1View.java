package com.drms.drms_drone.Controller.DroneController;

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
import android.view.View;
import android.widget.Toast;

import com.drms.drms_drone.MultiData;
import com.drms.drms_drone.R;
import com.drms.drms_drone.Sound.SoundManager;


/**
 * Created by jjunj on 2017-11-14.
 */

public class Setting1View extends View {

    private static final String TAG = "Setting1View";

    public static final String REQUEST_DUAL1_JOYSTICK = "request dual1";
    public static final String REQUEST_DUAL2_JOYSTICK = "request dual2";
    public static final String REQUEST_SINGLE_JOYSTICK = "request single";

    private Context context;
    private Activity activity;

    private SoundManager mSoundManager;

    private float x,y;

    private MultiData mspdata;
    public Setting1View(Context context, Activity activity) {
        super(context);
        this.context = context;
        this.activity = activity;
        mSoundManager = new SoundManager(context);

        mspdata = (MultiData)activity.getApplication();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(x == 0 && y==0){
            x = canvas.getWidth()/80;
            y = canvas.getHeight()/45;
            Log.d(TAG, "x : " +x + "\ny : " + y  );
        }
        Paint[] set1Paint = new Paint[10];

        set1Paint[0] = new Paint();
        set1Paint[0].setColor(Color.BLACK);
        set1Paint[0].setStrokeWidth(5);
        set1Paint[0].setStyle(Paint.Style.STROKE);
        set1Paint[0].setTextSize(6*y);
        set1Paint[0].setTextAlign(Paint.Align.CENTER);

        canvas.drawRect(10*x, 5*y,70*x,15*y, set1Paint[0]);
        canvas.drawLine(30*x,5*y,30*x,15*y,set1Paint[0]);
        canvas.drawLine(50*x,5*y,50*x,15*y,set1Paint[0]);

        set1Paint[0].setStyle(Paint.Style.FILL);
        canvas.drawText("DUAL1",20*x, 10*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        canvas.drawText("DUAL2",40*x, 10*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        canvas.drawText("SINGLE",60*x, 10*y+set1Paint[0].getTextSize()/2,set1Paint[0]);

        set1Paint[0].setStyle(Paint.Style.STROKE);
        set1Paint[0].setColor(context.getResources().getColor(R.color.setting1RectLine));

        set1Paint[1] = new Paint();
        set1Paint[1].setColor(context.getResources().getColor(R.color.setting1Rect));


        if(mspdata.getMYJOYSTICK() == MultiData.DUAL1){
            canvas.drawRect(10*x, 5*y,30*x,15*y, set1Paint[0]);
            canvas.drawRect(10*x+set1Paint[0].getStrokeWidth()/2,5*y+set1Paint[0].getStrokeWidth()/2,30*x-set1Paint[0].getStrokeWidth()/2,15*y-set1Paint[0].getStrokeWidth(),set1Paint[1]);
            set1Paint[0].setStyle(Paint.Style.FILL);
            canvas.drawText("DUAL1",20*x, 10*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        }
        else if(mspdata.getMYJOYSTICK() == MultiData.DUAL2){
            canvas.drawRect(30*x, 5*y,50*x,15*y, set1Paint[0]);
            canvas.drawRect(30*x+set1Paint[0].getStrokeWidth()/2,5*y+set1Paint[0].getStrokeWidth()/2,50*x-set1Paint[0].getStrokeWidth()/2,15*y-set1Paint[0].getStrokeWidth(),set1Paint[1]);
            set1Paint[0].setStyle(Paint.Style.FILL);
            canvas.drawText("DUAL2",40*x, 10*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        }

        else if(mspdata.getMYJOYSTICK() == MultiData.SINGLE){
            canvas.drawRect(50*x, 5*y,70*x,15*y, set1Paint[0]);
            canvas.drawRect(50*x+set1Paint[0].getStrokeWidth()/2,5*y+set1Paint[0].getStrokeWidth()/2,70*x-set1Paint[0].getStrokeWidth()/2,15*y-set1Paint[0].getStrokeWidth(),set1Paint[1]);
            set1Paint[0].setStyle(Paint.Style.FILL);
            canvas.drawText("SINGLE",60*x, 10*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        }

        set1Paint[2] = new Paint();
        set1Paint[2].setColor(Color.BLACK);
        set1Paint[2].setStrokeWidth(5);
        set1Paint[2].setStyle(Paint.Style.STROKE);

        set1Paint[3] = new Paint();
        set1Paint[3].setColor(context.getResources().getColor(R.color.dronemain_Color));
        set1Paint[3].setStyle(Paint.Style.FILL);



        Bitmap throttle = BitmapFactory.decodeResource(context.getResources(),R.mipmap.throttle);
        Bitmap throttle_icon = Bitmap.createScaledBitmap(throttle,(int)(3*x),(int)(3*y),true);

        if(mspdata.getMYJOYSTICK() == MultiData.DUAL1 || mspdata.getMYJOYSTICK() == MultiData.DUAL2){
            canvas.drawRect(20*x, 20*y, 60*x,40*y,set1Paint[2]);
            canvas.drawRect(23*x,23*y,37*x, 37*y,set1Paint[3]);
            canvas.drawRect(43*x,23*y,57*x, 37*y,set1Paint[3]);
            if(mspdata.getMYJOYSTICK() == MultiData.DUAL1){
                canvas.drawBitmap(throttle_icon,30*x - throttle_icon.getWidth()/2,30*y-throttle_icon.getHeight()/2,null);
                canvas.drawBitmap(throttle_icon,50*x - throttle_icon.getWidth()/2,30*y-throttle_icon.getHeight()/2,null);
            }
            else if(mspdata.getMYJOYSTICK() == MultiData.DUAL2){
                canvas.drawBitmap(throttle_icon,30*x - throttle_icon.getWidth()/2,30*y-throttle_icon.getHeight()/2,null);
                set1Paint[0].setTextSize(2*y);
                set1Paint[0].setColor(context.getResources().getColor(R.color.mainBasicColor));
            }
        }
        else if(mspdata.getMYJOYSTICK() == MultiData.SINGLE){
            canvas.drawRect(30*x, 17*y, 50*x,43*y,set1Paint[2]);
            canvas.drawRect(32*x,25*y,48*x, 41*y,set1Paint[3]);
            canvas.drawBitmap(throttle_icon,40*x - throttle_icon.getWidth()/2,33*y-throttle_icon.getHeight()/2,null);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            Intent joystickIntent = new Intent();
            if(event.getX() >= 10*x && event.getX() < 30*x && event.getY() >= 5*y && event.getY() <= 15*y){
                mspdata.setMYJOYSTICK(MultiData.DUAL1);
                mSoundManager.play(0);
                joystickIntent.setAction(REQUEST_DUAL1_JOYSTICK);
                invalidate();
                context.sendBroadcast(joystickIntent);
                Log.d(TAG,"send BroadCast : " + joystickIntent.getAction());
            }

            if(event.getX() >= 30*x && event.getX() < 50*x && event.getY() >= 5*y && event.getY() <= 15*y){
                mspdata.setMYJOYSTICK(MultiData.DUAL2);
                mSoundManager.play(0);
                joystickIntent.setAction(REQUEST_DUAL2_JOYSTICK);
                invalidate();
                context.sendBroadcast(joystickIntent);
                Log.d(TAG,"send BroadCast : " + joystickIntent.getAction());
            }

            if(event.getX() >= 50*x && event.getX() < 70*x && event.getY() >= 5*y && event.getY() <= 15*y){
//                mspdata.setMYJOYSTICK(MultiData.SINGLE);
//                joystickIntent.setAction(REQUEST_SINGLE_JOYSTICK);
                mSoundManager.play(0);
                Toast.makeText(activity,"준비 중 입니다.", Toast.LENGTH_SHORT).show();
                invalidate();
            }


        }
        return true;
    }
}
