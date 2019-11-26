package com.drms.drms_drone.Controller.DroneController;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.drms.drms_drone.MultiData;
import com.drms.drms_drone.R;
import com.drms.drms_drone.Sound.SoundManager;


/**
 * Created by jjunj on 2017-11-21.
 */

public class Setting2View extends View {

    private static final String TAG = "Setting1View";

    public static final String REQUEST_DUAL1_JOYSTICK = "request dual1";
    public static final String REQUEST_DUAL2_JOYSTICK = "request dual2";
    public static final String REQUEST_SINGLE_JOYSTICK = "request single";

    private Context context;
    private Activity activity;

    private SoundManager mSoundManager;

    private float x,y;

    private MultiData mspdata;
    public Setting2View(Context context, Activity activity) {
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
        set1Paint[0].setTextSize(4*y);
        set1Paint[0].setTextAlign(Paint.Align.CENTER);


        canvas.drawRect(15*x, 8*y,25*x,13*y, set1Paint[0]);
        canvas.drawRect(25*x,8*y,35*x,13*y,set1Paint[0]);
        canvas.drawRect(35*x,8*y,45*x,13*y,set1Paint[0]);
        canvas.drawRect(45*x,8*y,55*x,13*y,set1Paint[0]);
        canvas.drawRect(55*x,8*y,65*x,13*y,set1Paint[0]);

        set1Paint[0].setStyle(Paint.Style.FILL);
        canvas.drawText("비행 속도",40*x, 6*y, set1Paint[0]);
        set1Paint[0].setTextSize(3*y);
        canvas.drawText("매우 느림",20*x, (float)10.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        canvas.drawText("느림",30*x, (float)10.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        canvas.drawText("중간",40*x, (float)10.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        canvas.drawText("빠름",50*x, (float)10.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        canvas.drawText("매우 빠름",60*x, (float)10.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);

        set1Paint[0].setStyle(Paint.Style.STROKE);
        set1Paint[0].setColor(context.getResources().getColor(R.color.setting1RectLine));

        set1Paint[1] = new Paint();
        set1Paint[1].setColor(context.getResources().getColor(R.color.setting1Rect));


        if(mspdata.getDRONE_SPEED() == MultiData.veryslow){
            canvas.drawRect(15*x, 8*y,25*x,13*y, set1Paint[0]);
            canvas.drawRect(15*x+set1Paint[0].getStrokeWidth()/2,8*y+set1Paint[0].getStrokeWidth()/2,25*x-set1Paint[0].getStrokeWidth()/2,13*y-set1Paint[0].getStrokeWidth(),set1Paint[1]);
            set1Paint[0].setStyle(Paint.Style.FILL);
            canvas.drawText("매우 느림",20*x, (float)10.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        }
        else if(mspdata.getDRONE_SPEED() == MultiData.slow){
            canvas.drawRect(25*x, 8*y,35*x,13*y, set1Paint[0]);
            canvas.drawRect(25*x+set1Paint[0].getStrokeWidth()/2,8*y+set1Paint[0].getStrokeWidth()/2,35*x-set1Paint[0].getStrokeWidth()/2,13*y-set1Paint[0].getStrokeWidth(),set1Paint[1]);
            set1Paint[0].setStyle(Paint.Style.FILL);
            canvas.drawText("느림",30*x, (float)10.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        }

        else if(mspdata.getDRONE_SPEED() == MultiData.middle){
            canvas.drawRect(35*x, 8*y,45*x,13*y, set1Paint[0]);
            canvas.drawRect(35*x+set1Paint[0].getStrokeWidth()/2,8*y+set1Paint[0].getStrokeWidth()/2,45*x-set1Paint[0].getStrokeWidth()/2,13*y-set1Paint[0].getStrokeWidth(),set1Paint[1]);
            set1Paint[0].setStyle(Paint.Style.FILL);
            canvas.drawText("중간",40*x, (float)10.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        }
        else if(mspdata.getDRONE_SPEED() == MultiData.fast){
            canvas.drawRect(45*x, 8*y,55*x,13*y, set1Paint[0]);
            canvas.drawRect(45*x+set1Paint[0].getStrokeWidth()/2,8*y+set1Paint[0].getStrokeWidth()/2,55*x-set1Paint[0].getStrokeWidth()/2,13*y-set1Paint[0].getStrokeWidth(),set1Paint[1]);
            set1Paint[0].setStyle(Paint.Style.FILL);
            canvas.drawText("빠름",50*x, (float)10.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        }
        else if(mspdata.getDRONE_SPEED() == MultiData.veryfast){
            canvas.drawRect(55*x, 8*y,65*x,13*y, set1Paint[0]);
            canvas.drawRect(55*x+set1Paint[0].getStrokeWidth()/2,8*y+set1Paint[0].getStrokeWidth()/2,65*x-set1Paint[0].getStrokeWidth()/2,13*y-set1Paint[0].getStrokeWidth(),set1Paint[1]);
            set1Paint[0].setStyle(Paint.Style.FILL);
            canvas.drawText("매우 빠름",60*x, (float)10.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        }

        set1Paint[0].setTextSize(4*y);
        set1Paint[0].setColor(Color.BLACK);
        canvas.drawText("트림 조정 간격 ",40*x,16*y+set1Paint[0].getTextSize(), set1Paint[0]);

        set1Paint[0].setColor(Color.BLACK);
        set1Paint[0].setStrokeWidth(5);
        set1Paint[0].setStyle(Paint.Style.STROKE);
        set1Paint[0].setTextSize(4*y);
        set1Paint[0].setTextAlign(Paint.Align.CENTER);


        canvas.drawRect(15*x, 23*y,25*x,28*y, set1Paint[0]);
        canvas.drawRect(25*x,23*y,35*x,28*y,set1Paint[0]);
        canvas.drawRect(35*x,23*y,45*x,28*y,set1Paint[0]);
        canvas.drawRect(45*x,23*y,55*x,28*y,set1Paint[0]);
        canvas.drawRect(55*x,23*y,65*x,28*y,set1Paint[0]);

        set1Paint[0].setStyle(Paint.Style.FILL);
        set1Paint[0].setTextSize(3*y);
        canvas.drawText("1",20*x, (float)25.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        canvas.drawText("2",30*x, (float)25.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        canvas.drawText("3",40*x, (float)25.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        canvas.drawText("4",50*x, (float)25.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        canvas.drawText("5",60*x, (float)25.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);

        set1Paint[0].setStyle(Paint.Style.STROKE);
        set1Paint[0].setColor(context.getResources().getColor(R.color.setting1RectLine));

        set1Paint[1] = new Paint();
        set1Paint[1].setColor(context.getResources().getColor(R.color.setting1Rect));


        if(mspdata.getTreamInterval() ==1){
            canvas.drawRect(15*x, 23*y,25*x,28*y, set1Paint[0]);
            canvas.drawRect(15*x+set1Paint[0].getStrokeWidth()/2,23*y+set1Paint[0].getStrokeWidth()/2,25*x-set1Paint[0].getStrokeWidth()/2,28*y-set1Paint[0].getStrokeWidth(),set1Paint[1]);
            set1Paint[0].setStyle(Paint.Style.FILL);
            canvas.drawText("1",20*x, (float)25.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        }
        else if(mspdata.getTreamInterval() ==2){
            canvas.drawRect(25*x, 23*y,35*x,28*y, set1Paint[0]);
            canvas.drawRect(25*x+set1Paint[0].getStrokeWidth()/2,23*y+set1Paint[0].getStrokeWidth()/2,35*x-set1Paint[0].getStrokeWidth()/2,28*y-set1Paint[0].getStrokeWidth(),set1Paint[1]);
            set1Paint[0].setStyle(Paint.Style.FILL);
            canvas.drawText("2",30*x, (float)25.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        }

        else if(mspdata.getTreamInterval() ==3){
            canvas.drawRect(35*x, 23*y,45*x,28*y, set1Paint[0]);
            canvas.drawRect(35*x+set1Paint[0].getStrokeWidth()/2,23*y+set1Paint[0].getStrokeWidth()/2,45*x-set1Paint[0].getStrokeWidth()/2,28*y-set1Paint[0].getStrokeWidth(),set1Paint[1]);
            set1Paint[0].setStyle(Paint.Style.FILL);
            canvas.drawText("3",40*x, (float)25.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        }
        else if(mspdata.getTreamInterval() ==4){
            canvas.drawRect(45*x, 23*y,55*x,28*y, set1Paint[0]);
            canvas.drawRect(45*x+set1Paint[0].getStrokeWidth()/2,23*y+set1Paint[0].getStrokeWidth()/2,55*x-set1Paint[0].getStrokeWidth()/2,28*y-set1Paint[0].getStrokeWidth(),set1Paint[1]);
            set1Paint[0].setStyle(Paint.Style.FILL);
            canvas.drawText("4",50*x, (float)25.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        }
        else if(mspdata.getTreamInterval() ==5){
            canvas.drawRect(55*x, 23*y,65*x,28*y, set1Paint[0]);
            canvas.drawRect(55*x+set1Paint[0].getStrokeWidth()/2,23*y+set1Paint[0].getStrokeWidth()/2,65*x-set1Paint[0].getStrokeWidth()/2,28*y-set1Paint[0].getStrokeWidth(),set1Paint[1]);
            set1Paint[0].setStyle(Paint.Style.FILL);
            canvas.drawText("5",60*x, (float)25.5*y+set1Paint[0].getTextSize()/2,set1Paint[0]);
        }

        if(mspdata.getMYJOYSTICK() == MultiData.DUAL2) {
            set1Paint[0].setTextSize(4 * y);
            set1Paint[0].setColor(Color.BLACK);
            canvas.drawText("스마트폰 최대 기울기", 40 * x, 31 * y + set1Paint[0].getTextSize(), set1Paint[0]);

            set1Paint[0].setColor(Color.BLACK);
            set1Paint[0].setStrokeWidth(5);
            set1Paint[0].setStyle(Paint.Style.STROKE);
            set1Paint[0].setTextSize(4 * y);
            set1Paint[0].setTextAlign(Paint.Align.CENTER);


            canvas.drawRect(15 * x, 38 * y, 25 * x, 43 * y, set1Paint[0]);
            canvas.drawRect(25 * x, 38 * y, 35 * x, 43 * y, set1Paint[0]);
            canvas.drawRect(35 * x, 38 * y, 45 * x, 43 * y, set1Paint[0]);
            canvas.drawRect(45 * x, 38 * y, 55 * x, 43 * y, set1Paint[0]);
            canvas.drawRect(55 * x, 38 * y, 65 * x, 43 * y, set1Paint[0]);

            set1Paint[0].setStyle(Paint.Style.FILL);
            set1Paint[0].setTextSize(3 * y);
            canvas.drawText("30˚", 20 * x, (float) 40.5 * y + set1Paint[0].getTextSize() / 2, set1Paint[0]);
            canvas.drawText("40˚", 30 * x, (float) 40.5 * y + set1Paint[0].getTextSize() / 2, set1Paint[0]);
            canvas.drawText("50˚", 40 * x, (float) 40.5 * y + set1Paint[0].getTextSize() / 2, set1Paint[0]);
            canvas.drawText("60˚", 50 * x, (float) 40.5 * y + set1Paint[0].getTextSize() / 2, set1Paint[0]);
            canvas.drawText("70˚", 60 * x, (float) 40.5 * y + set1Paint[0].getTextSize() / 2, set1Paint[0]);

            set1Paint[0].setStyle(Paint.Style.STROKE);
            set1Paint[0].setColor(context.getResources().getColor(R.color.setting1RectLine));

            set1Paint[1] = new Paint();
            set1Paint[1].setColor(context.getResources().getColor(R.color.setting1Rect));


            if (mspdata.getSmartphoneAngle() == 30) {
                canvas.drawRect(15 * x, 38 * y, 25 * x, 43 * y, set1Paint[0]);
                canvas.drawRect(15 * x + set1Paint[0].getStrokeWidth() / 2, 38 * y + set1Paint[0].getStrokeWidth() / 2, 25 * x - set1Paint[0].getStrokeWidth() / 2, 43 * y - set1Paint[0].getStrokeWidth(), set1Paint[1]);
                set1Paint[0].setStyle(Paint.Style.FILL);
                canvas.drawText("30˚", 20 * x, (float) 40.5 * y + set1Paint[0].getTextSize() / 2, set1Paint[0]);
            } else if (mspdata.getSmartphoneAngle() == 40) {
                canvas.drawRect(25 * x, 38 * y, 35 * x, 43 * y, set1Paint[0]);
                canvas.drawRect(25 * x + set1Paint[0].getStrokeWidth() / 2, 38 * y + set1Paint[0].getStrokeWidth() / 2, 35 * x - set1Paint[0].getStrokeWidth() / 2, 43 * y - set1Paint[0].getStrokeWidth(), set1Paint[1]);
                set1Paint[0].setStyle(Paint.Style.FILL);
                canvas.drawText("40˚", 30 * x, (float) 40.5 * y + set1Paint[0].getTextSize() / 2, set1Paint[0]);
            } else if (mspdata.getSmartphoneAngle() == 50) {
                canvas.drawRect(35 * x, 38 * y, 45 * x, 43 * y, set1Paint[0]);
                canvas.drawRect(35 * x + set1Paint[0].getStrokeWidth() / 2, 38 * y + set1Paint[0].getStrokeWidth() / 2, 45 * x - set1Paint[0].getStrokeWidth() / 2, 43 * y - set1Paint[0].getStrokeWidth(), set1Paint[1]);
                set1Paint[0].setStyle(Paint.Style.FILL);
                canvas.drawText("50˚", 40 * x, (float) 40.5 * y + set1Paint[0].getTextSize() / 2, set1Paint[0]);
            } else if (mspdata.getSmartphoneAngle() == 60) {
                canvas.drawRect(45 * x, 38 * y, 55 * x, 43 * y, set1Paint[0]);
                canvas.drawRect(45 * x + set1Paint[0].getStrokeWidth() / 2, 38 * y + set1Paint[0].getStrokeWidth() / 2, 55 * x - set1Paint[0].getStrokeWidth() / 2, 43 * y - set1Paint[0].getStrokeWidth(), set1Paint[1]);
                set1Paint[0].setStyle(Paint.Style.FILL);
                canvas.drawText("60˚", 50 * x, (float) 40.5 * y + set1Paint[0].getTextSize() / 2, set1Paint[0]);
            } else if (mspdata.getSmartphoneAngle() == 70) {
                canvas.drawRect(55 * x, 38 * y, 65 * x, 43 * y, set1Paint[0]);
                canvas.drawRect(55 * x + set1Paint[0].getStrokeWidth() / 2, 38 * y + set1Paint[0].getStrokeWidth() / 2, 65 * x - set1Paint[0].getStrokeWidth() / 2, 43 * y - set1Paint[0].getStrokeWidth(), set1Paint[1]);
                set1Paint[0].setStyle(Paint.Style.FILL);
                canvas.drawText("70˚", 60 * x, (float) 40.5 * y + set1Paint[0].getTextSize() / 2, set1Paint[0]);
            }
        }


    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            Intent joystickIntent = new Intent();
            if(event.getX() >= 15*x && event.getX() < 25*x && event.getY() >= 8*y && event.getY() <= 13*y){
                mspdata.setDRONE_SPEED(MultiData.veryslow);
                mSoundManager.play(0);
                invalidate();
            }

            if(event.getX() >= 25*x && event.getX() < 35*x && event.getY() >= 8*y && event.getY() <= 13*y){
                mspdata.setDRONE_SPEED(MultiData.slow);
                mSoundManager.play(0);
                invalidate();
            }

            if(event.getX() >= 35*x && event.getX() < 45*x && event.getY() >= 8*y && event.getY() <= 13*y){
                mspdata.setDRONE_SPEED(MultiData.middle);
                mSoundManager.play(0);
                invalidate();
            }

            if(event.getX() >= 45*x && event.getX() < 55*x && event.getY() >= 8*y && event.getY() <= 13*y){
                mspdata.setDRONE_SPEED(MultiData.fast);
                mSoundManager.play(0);
                invalidate();
            }

            if(event.getX() >= 55*x && event.getX() < 65*x && event.getY() >= 8*y && event.getY() <= 13*y){
                mspdata.setDRONE_SPEED(MultiData.veryfast);
                mSoundManager.play(0);
                invalidate();
            }

            if(event.getX() >= 15*x && event.getX() < 25*x && event.getY() >= 23*y && event.getY() <= 28*y){
                mspdata.setTreamInterval(1);
                mSoundManager.play(0);
                invalidate();
            }

            if(event.getX() >= 25*x && event.getX() < 35*x && event.getY() >= 23*y && event.getY() <= 28*y){
                mspdata.setTreamInterval(2);
                mSoundManager.play(0);
                invalidate();
            }

            if(event.getX() >= 35*x && event.getX() < 45*x && event.getY() >= 23*y && event.getY() <= 28*y){
                mspdata.setTreamInterval(3);
                mSoundManager.play(0);
                invalidate();
            }

            if(event.getX() >= 45*x && event.getX() < 55*x && event.getY() >= 23*y && event.getY() <= 28*y){
                mspdata.setTreamInterval(4);
                mSoundManager.play(0);
                invalidate();
            }

            if(event.getX() >= 55*x && event.getX() < 65*x && event.getY() >= 23*y && event.getY() <= 28*y){
                mspdata.setTreamInterval(5);
                mSoundManager.play(0);
                invalidate();
            }

            if(mspdata.getMYJOYSTICK() == MultiData.DUAL2) {
                if (event.getX() >= 15 * x && event.getX() < 25 * x && event.getY() >= 38 * y && event.getY() <= 43 * y) {
                    mspdata.setSmartphoneAngle(30);
                    mSoundManager.play(0);
                    invalidate();
                }

                if (event.getX() >= 25 * x && event.getX() < 35 * x && event.getY() >= 38 * y && event.getY() <= 43 * y) {
                    mspdata.setSmartphoneAngle(40);
                    mSoundManager.play(0);
                    invalidate();
                }

                if (event.getX() >= 35 * x && event.getX() < 45 * x && event.getY() >= 38 * y && event.getY() <= 43 * y) {
                    mspdata.setSmartphoneAngle(50);
                    mSoundManager.play(0);
                    invalidate();
                }

                if (event.getX() >= 45 * x && event.getX() < 55 * x && event.getY() >= 38 * y && event.getY() <= 43 * y) {
                    mspdata.setSmartphoneAngle(60);
                    mSoundManager.play(0);
                    invalidate();
                }

                if (event.getX() >= 55 * x && event.getX() < 65 * x && event.getY() >= 38 * y && event.getY() <= 43 * y) {
                    mspdata.setSmartphoneAngle(70);
                    mSoundManager.play(0);
                    invalidate();
                }
            }
            Log.d(TAG,"drone speed : " + mspdata.getDRONE_SPEED());
        }
        return true;
    }
}
