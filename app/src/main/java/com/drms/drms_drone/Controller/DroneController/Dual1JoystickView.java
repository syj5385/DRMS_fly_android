package com.drms.drms_drone.Controller.DroneController;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.drms.drms_drone.MultiData;
import com.drms.drms_drone.R;
import com.drms.drms_drone.Service.BTService;


/**
 * Created by jjunj on 2017-11-15.
 */

public class Dual1JoystickView extends Joystick_view {


    public Dual1JoystickView(Context context, Activity mActivity, Handler mHandler) {
        super(context, mActivity, mHandler);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Bitmap throttle = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.throttle);
        Bitmap throttle_image = Bitmap.createScaledBitmap(throttle,(int)(6*x),(int)(6*y),true);
    }

    @Override
    protected Bitmap drawBackground(float width, int unitW, float height, int unitH) {
        return super.drawBackground(width, unitW, height, unitH);
    }

    @Override
    protected void drawController(Canvas canvas) {
        super.drawController(canvas);

        canvas.drawBitmap(droneThrottle.getThrottleImage(),droneThrottle.getRight()[0],droneThrottle.getRight()[1],null);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int pointer_count = event.getPointerCount();
        if(pointer_count > 2) pointer_count = 2;

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN :  // always first
                Log.d(TAG,"onTouch");
                Log.w(TAG,"x: " + event.getX() + "\ny : "+ event.getY());
                if (event.getX() >= 3 * x + droneThrottle.getThrottleImage().getWidth()/2
                        && event.getX() <= 33 * x - droneThrottle.getThrottleImage().getWidth() / 2
                        &&event.getY() >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                        && event.getY()  <= 50 * y) {
                    Log.d(TAG,"unlock left throttle");

                    if(mspdata.getRcdata()[7] == 1000) {
                        float position = 45*y - droneThrottle.getThrottleImage().getHeight();
                        if(event.getY() <= 50*y && event.getY() >= position){
                            mspdata.setRawRCDataAux(4, 2000);
                        }
                    }

                    int[] down_left = {
                            (int)(event.getX()-droneThrottle.getThrottleImage().getWidth()/2),
                            (int)(event.getY()-droneThrottle.getThrottleImage().getHeight()/2),
                    };
                    unlock_throttle[0][0] = true;
                    unlock_throttle[0][1] = false;
                    if(event.getY() >= 45*y-droneThrottle.getThrottleImage().getHeight()/2 && event.getY() <= 50*y){
                        if(event.getX() >= 12*x && event.getX() <= 23*x){
                            down_left[0] = (int)(45*y - droneThrottle.getThrottleImage().getHeight());
                        }
                        else {
                            unlock_throttle[0][0] = false;
                            unlock_throttle[0][1] = false;
                        }

                    }
                    if(unlock_throttle[0][1]) {
                        droneThrottle.setLeft(down_left);
                    }
                }

                else if (event.getX() >= 52 * x + droneThrottle.getThrottleImage().getWidth()/2
                        && event.getX() <= 82 * x - droneThrottle.getThrottleImage().getWidth() / 2
                        &&event.getY() >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                        && event.getY()  <= 45 * y- droneThrottle.getThrottleImage().getHeight() / 2) {
                    Log.d(TAG,"unlock right throttle");
                    unlock_throttle[0][0] = false;
                    unlock_throttle[0][1] = true;
                    int[] down_right = {
                            (int)(event.getX()-droneThrottle.getThrottleImage().getWidth()/2),
                            (int)(event.getY()-droneThrottle.getThrottleImage().getHeight()/2),
                    };
                    droneThrottle.setRight(down_right);
                }
                else{
                    unlock_throttle[0][0] = false;
                    unlock_throttle[0][1] = false;
                }

                if(event.getX() >= 40*x && event.getX() <= 45*x && event.getY() >= 48*y && event.getY() <= 53*y){   // lock yaw
                    if(mspdata.getLocked()){
                        mspdata.setLocked(false);
                    }
                    else{
                        mspdata.setLocked(true);
                    }
                    mSoundManager.play(0);
                    Log.d(TAG,"locked : " + mspdata.getLocked());

                }

                if(event.getX() >= 2*x && event.getX() <= 9*x && event.getY() > 2*y && event.getY() < 12*y){ // click menu
                    mHandler.obtainMessage(JoystickActivity.REQUEST_JOYSTICK_MENU).sendToTarget();
                    mSoundManager.play(0);

                }

                if(event.getX() >= 3*x && event.getX() <= 8*x && event.getY() >= 46*y && event.getY() <= 51*y) { // Yaw Tream down
                    mSoundManager.play(0);
                    mspdata.setYawTream(mspdata.getTream()[2]-mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.YAW_DOWN,true);
                }

                if(event.getX() >= 28*x && event.getX() <= 33*x && event.getY() >= 46*y && event.getY() <= 51*y) { // Yaw Tream Up
                    mSoundManager.play(0);
                    mspdata.setYawTream(mspdata.getTream()[2]+mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.YAW_UP,true);
                }

                if(event.getX() >= 40*x && event.getX() <= 45*x && event.getY() >= 30*y && event.getY() <= 35*y) { // pitch Tream Up
                    mSoundManager.play(0);
                    mspdata.setPitchTream(mspdata.getTream()[1]+mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.PITCH_UP,true);
                }

                if(event.getX() >= 40*x && event.getX() <= 45*x && event.getY() >= 40*y && event.getY() <= 45*y) { // pitch Tream Down
                    mSoundManager.play(0);
                    mspdata.setPitchTream(mspdata.getTream()[1]-mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.PITCH_DOWN,true);
                }

                if(event.getX() >= 52*x && event.getX() <= 57*x && event.getY() >= 46*y && event.getY() <= 51*y) { // Roll Tream Down
                    mSoundManager.play(0);
                    mspdata.setRollTream(mspdata.getTream()[0]-mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.ROLL_DOWN,true);
                }

                if(event.getX() >= 77*x && event.getX() <= 82*x && event.getY() >= 46*y && event.getY() <= 51*y) { // Roll Tream Up
                    mSoundManager.play(0);
                    mspdata.setRollTream(mspdata.getTream()[0]+mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.ROLL_UP,true);
                }

                if(event.getX() >= 14*x && event.getX() <= 19*x && event.getY() >= 5*y && event.getY() <= 10*y) { // Aux  Up
                    mSoundManager.play(0);
                    if(mspdata.getReceivedRcdata()[4] == 2000){
                        mspdata.setRawRCDataAux(1,1000);
                    }
                    else if(mspdata.getReceivedRcdata()[4] == 1000){
                        mspdata.setRawRCDataAux(1,2000);
                    }
                }

                if(event.getX() >= 22*x && event.getX() <= 27*x && event.getY() >= 5*y && event.getY() <= 10*y) { // Aux2  Up
                    mSoundManager.play(0);

                    if(mspdata.getReceivedRcdata()[5] == 2000){
                        mspdata.setRawRCDataAux(2,1000);
                    }
                    else if(mspdata.getReceivedRcdata()[5] == 1000){
                        mspdata.setRawRCDataAux(2,2000);
//                        Intent intent = new Intent();
//                        intent.setAction(BTService.REQUEST_MSP_SET_HEAD);
//                        intent.putExtra("head", 70);
//                        mContext.sendBroadcast(intent);
                    }
                }

                if(event.getX() >= 29*x && event.getX() <= 34*x && event.getY() >= 5*y && event.getY() <= 10*y) { // Aux3  Up
                    mSoundManager.play(0);
                    if(mspdata.getReceivedRcdata()[6] == 2000){
                        mspdata.setRawRCDataAux(3,1000);
                    }
                    else if(mspdata.getReceivedRcdata()[6] == 1000){
                        mspdata.setRawRCDataAux(3,2000);
                    }
                }

                if(event.getX() >= 40*x && event.getX() <= 45*x && event.getY() >= 3*y && event.getY() <= 8*y) { // Acc Cali
                    if(!mspdata.getCalibration()[0]) {
                        mSoundManager.play(0);
                        mspdata.setACCCalibration(true);

                        mspdata.setRawRCDataRollPitch(1500,1500);
                        mspdata.setRawRCDataYawThrottle(1500,1000);
                        for(int i=1; i<5; i++)
                            mspdata.setRawRCDataAux(i,1000);

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mContext.sendBroadcast(new Intent().setAction(BTService.REQUEST_ACC_CALIBRATION));
                            }
                        },500);
                    }
                    else{
                        Toast.makeText(mContext,"교정 중...", Toast.LENGTH_SHORT).show();
                    }
                }

                if(event.getX() >= 47*x && event.getX() <= 52*x && event.getY() >= 3*y && event.getY() <= 8*y) { // Mag Cali
                    if(!mspdata.getCalibration()[1]) {
                        mSoundManager.play(0);
                        mspdata.setMAGCalibration(true);
//                        mContext.sendBroadcast(new Intent().setAction(BTService.REQUEST_MAG_CALIBRATION));
                        mspdata.setRawRCDataRollPitch(1500,1500);
                        mspdata.setRawRCDataYawThrottle(1500,1000);
                        for(int i=1; i<5; i++)
                            mspdata.setRawRCDataAux(i,1000);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mContext.sendBroadcast(new Intent().setAction(BTService.REQUEST_MAG_CALIBRATION));

                            }
                        },500);
                    }
                    else{
                        Toast.makeText(mContext,"교정 중...", Toast.LENGTH_SHORT).show();
                    }
                }


                break;

            case MotionEvent.ACTION_MOVE :
                int[] left = {droneThrottle.getLeft()[0], droneThrottle.getLeft()[1]};
                int[] right = {droneThrottle.getRight()[0], droneThrottle.getRight()[1]};

                if(pointer_count == 1) {
                    if (unlock_throttle[0][0] && !unlock_throttle[0][1]) {
                        if (event.getX() >= 3 * x + droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX() <= 33 * x - droneThrottle.getThrottleImage().getWidth() / 2) {

                            if(!mspdata.getLocked()) {
                                if (event.getX() >= 3 * x + 6 * x && event.getX() <= 33 * x - 6 * x) {
                                    left[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                                } else {
                                    left[0] = (int) event.getX(0) - droneThrottle.getThrottleImage().getWidth() / 2;
                                }
                            }else{
                                left[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                            }
                        }
                        else if (event.getX() > 33 * x - droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX() < width / 2) {
                            if(!mspdata.getLocked())
                                left[0] = (int) (33 * x - droneThrottle.getThrottleImage().getWidth());
                        }
                        else if (event.getX() < 3 * x + droneThrottle.getThrottleImage().getWidth() / 2) {
                            if(!mspdata.getLocked())
                                left[0] = (int) (3 * x);
                        }
                        else if(event.getX() > width /2){
                            if(!mspdata.getLocked())
                                left[0] = (int)((int)(18*x-droneThrottle.getThrottleImage().getWidth()/2));
                        }

                        if (event.getY() >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY() <= 45 * y - droneThrottle.getThrottleImage().getHeight() / 2) {
                                left[1] = (int) event.getY() - droneThrottle.getThrottleImage().getHeight() / 2;
                        }
                        else if (event.getY() < 15 * y + droneThrottle.getThrottleImage().getHeight() / 2) {
                                left[1] = (int) (15 * y);
                        }
                        else if (event.getY() > 45 * y - droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY() < height) {
                                left[1] = (int) (45 * y - droneThrottle.getThrottleImage().getHeight());
                        }
                    }

                    if (!unlock_throttle[0][0] && unlock_throttle[0][1]) {

                        if (event.getX() >= 52 * x + droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX() <= 82 * x - droneThrottle.getThrottleImage().getWidth() / 2) {
                            right[0] = (int) event.getX() - droneThrottle.getThrottleImage().getWidth() / 2;
                        }
                        else if (event.getX() > 82 * x - droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX() < width) {
                            right[0] = (int) (82 * x - droneThrottle.getThrottleImage().getWidth());
                        }
                        else if (event.getX() < 52 * x + droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX() > width/2) {
                            right[0] = (int) (52 * x);
                        }
                        else if(event.getX() < width/2){
                            right[0] = (int)(67*x-droneThrottle.getThrottleImage().getWidth()/2);
                        }

                        if (event.getY() >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY() <= 45 * y - droneThrottle.getThrottleImage().getHeight() / 2) {
                            right[1] = (int) event.getY() - droneThrottle.getThrottleImage().getHeight() / 2;
                        } else if (event.getY() < 15 * y + droneThrottle.getThrottleImage().getHeight() / 2) {
                            right[1] = (int) (15 * y);
                        } else if (event.getY() > 45 * y - droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY() < height) {
                            right[1] = (int) (45 * y - droneThrottle.getThrottleImage().getHeight());
                        }
                    }

                    if(unlock_throttle[1][0] && !unlock_throttle[1][1]){
                        //second Touch
                        if (event.getX(0) >= 3 * x + droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(0) <= 33 * x - droneThrottle.getThrottleImage().getWidth() / 2) {
                            if(!mspdata.getLocked()) {
                                if (event.getX(0) > 3 * x + 6 * x && event.getX() < 33 * x - 6 * x) {
                                    left[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                                } else {
                                    left[0] = (int) event.getX(0) - droneThrottle.getThrottleImage().getWidth() / 2;
                                }
                            }
                            else
                                left[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);

                        } else if (event.getX(0) > 33 * x - droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(0) < width / 2) {
                            left[0] = (int) (33 * x - droneThrottle.getThrottleImage().getWidth());
                        } else if (event.getX(0) < 3 * x + droneThrottle.getThrottleImage().getWidth() / 2) {
                            left[0] = (int) (3 * x);
                        } else if (event.getX(0) > width / 2) {
                            left[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                        }

                        if (event.getY(0) >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY(0) <= 45 * y - droneThrottle.getThrottleImage().getHeight() / 2) {
                            left[1] = (int) event.getY(0) - droneThrottle.getThrottleImage().getHeight() / 2;
                        } else if (event.getY(0) < 15 * y + droneThrottle.getThrottleImage().getHeight() / 2) {
                            left[1] = (int) (15 * y);
                        } else if (event.getY(0) > 45 * y - droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY(0) < height) {
                            left[1] = (int) (45 * y - droneThrottle.getThrottleImage().getHeight());
                        }

                    }

                    if (unlock_throttle[1][1] && !unlock_throttle[1][0]) {
                        if (event.getX(0) >= 52 * x + droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(0) <= 82 * x - droneThrottle.getThrottleImage().getWidth() / 2) {
                            right[0] = (int) event.getX(0) - droneThrottle.getThrottleImage().getWidth() / 2;
                        } else if (event.getX(0) > 82 * x - droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(0) < width) {
                            right[0] = (int) (82 * x - droneThrottle.getThrottleImage().getWidth());
                        } else if (event.getX(0) < 52 * x + droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(0) > width / 2) {
                            right[0] = (int) (52 * x);
                        } else if (event.getX(0) < width / 2) {
                            right[0] = (int) (67 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                        }

                        if (event.getY(0) >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY(0) <= 45 * y - droneThrottle.getThrottleImage().getHeight() / 2) {
                            right[1] = (int) event.getY(0) - droneThrottle.getThrottleImage().getHeight() / 2;
                        } else if (event.getY(0) < 15 * y + droneThrottle.getThrottleImage().getHeight() / 2) {
                            right[1] = (int) (15 * y);
                        } else if (event.getY(0) > 45 * y - droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY(0) < height) {
                            right[1] = (int) (45 * y - droneThrottle.getThrottleImage().getHeight());
                        }
                    }

                    droneThrottle.setLeft(left);
                    droneThrottle.setRight(right);
                } // end of Single Touch Event

                else if(pointer_count == 2) {
                    if (unlock_throttle[0][0]) {
                        if (event.getX(0) >= 3 * x + droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(0) <= 33 * x - droneThrottle.getThrottleImage().getWidth() / 2) {
                            if(!mspdata.getLocked()) {
                                if (event.getX(0) > 3 * x + 6 * x && event.getX() < 33 * x - 6 * x) {
                                    left[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                                } else {
                                    left[0] = (int) event.getX(0) - droneThrottle.getThrottleImage().getWidth() / 2;
                                }
                            }
                            else{
                                left[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                            }
                        } else if (event.getX(0) > 33 * x - droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(0) < width / 2) {
                            if(!mspdata.getLocked())
                                left[0] = (int) (33 * x - droneThrottle.getThrottleImage().getWidth());
                        } else if (event.getX(0) < 3 * x + droneThrottle.getThrottleImage().getWidth() / 2) {
                            if(!mspdata.getLocked())
                                left[0] = (int) (3 * x);
                        } else if (event.getX(0) > width / 2) {
                            if(!mspdata.getLocked())
                                left[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                        }

                        if (event.getY(0) >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY(0) <= 45 * y - droneThrottle.getThrottleImage().getHeight() / 2) {
                            left[1] = (int) event.getY(0) - droneThrottle.getThrottleImage().getHeight() / 2;
                        } else if (event.getY(0) < 15 * y + droneThrottle.getThrottleImage().getHeight() / 2) {
                            left[1] = (int) (15 * y);
                        } else if (event.getY(0) > 45 * y - droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY(0) < height) {
                            left[1] = (int) (45 * y - droneThrottle.getThrottleImage().getHeight());
                        }
                    }
                    if (unlock_throttle[1][0]) {
                        // second Touch
                        if (event.getX(1) >= 3 * x + droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(1) <= 33 * x - droneThrottle.getThrottleImage().getWidth() / 2) {
                            if(!mspdata.getLocked()) {
                                Log.d(TAG,"second Move left throttle");
                                if (event.getX(1) > 3 * x + 6 * x && event.getX(1) < 33 * x - 6 * x) {
                                    left[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);

                                } else {
                                    left[0] = (int) event.getX(1) - droneThrottle.getThrottleImage().getWidth() / 2;
                                }
                            }
                            else{
                                left[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                            }
                        } else if (event.getX(1) > 33 * x - droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(1) < width / 2) {
                            if(!mspdata.getLocked())
                                left[0] = (int) (33 * x - droneThrottle.getThrottleImage().getWidth());
                        } else if (event.getX(1) < 3 * x + droneThrottle.getThrottleImage().getWidth() / 2) {
                            if(!mspdata.getLocked())
                                left[0] = (int) (3 * x);
                        } else if (event.getX(1) > width / 2) {
                            if(!mspdata.getLocked())
                                left[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                        }

                        if (event.getY(1) >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY(1) <= 45 * y - droneThrottle.getThrottleImage().getHeight() / 2) {
                            left[1] = (int) event.getY(1) - droneThrottle.getThrottleImage().getHeight() / 2;
                        } else if (event.getY(1) < 15 * y + droneThrottle.getThrottleImage().getHeight() / 2) {
                            left[1] = (int) (15 * y);
                        } else if (event.getY(1) > 45 * y - droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY(1) < height) {
                            left[1] = (int) (45 * y - droneThrottle.getThrottleImage().getHeight());
                        }
                    }

                    droneThrottle.setLeft(left);

                    if (unlock_throttle[0][1]) {
                        if (event.getX(0) >= 52 * x + droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(0) <= 82 * x - droneThrottle.getThrottleImage().getWidth() / 2) {
                            right[0] = (int) event.getX(0) - droneThrottle.getThrottleImage().getWidth() / 2;
                        } else if (event.getX(0) > 82 * x - droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(0) < width) {
                            right[0] = (int) (82 * x - droneThrottle.getThrottleImage().getWidth());
                        } else if (event.getX(0) < 52 * x + droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(0) > width / 2) {
                            right[0] = (int) (52 * x);
                        } else if (event.getX(0) < width / 2) {
                            right[0] = (int) (67 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                        }

                        if (event.getY(0) >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY(0) <= 45 * y - droneThrottle.getThrottleImage().getHeight() / 2) {
                            right[1] = (int) event.getY(0) - droneThrottle.getThrottleImage().getHeight() / 2;
                        } else if (event.getY(0) < 15 * y + droneThrottle.getThrottleImage().getHeight() / 2) {
                            right[1] = (int) (15 * y);
                        } else if (event.getY(0) > 45 * y - droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY(0) < height) {
                            right[1] = (int) (45 * y - droneThrottle.getThrottleImage().getHeight());
                        }
                    }
                    if (unlock_throttle[1][1]) {
                        if (event.getX(1) >= 52 * x + droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(1) <= 82 * x - droneThrottle.getThrottleImage().getWidth() / 2) {
                            right[0] = (int) event.getX(1) - droneThrottle.getThrottleImage().getWidth() / 2;
                        } else if (event.getX(1) > 82 * x - droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(1) < width) {
                            right[0] = (int) (82 * x - droneThrottle.getThrottleImage().getWidth());
                        } else if (event.getX(1) < 52 * x + droneThrottle.getThrottleImage().getWidth() / 2
                                && event.getX(1) > width / 2) {
                            right[0] = (int) (52 * x);
                        } else if (event.getX(1) < width / 2) {
                            right[0] = (int) (67 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                        }

                        if (event.getY(1) >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY(1) <= 45 * y - droneThrottle.getThrottleImage().getHeight() / 2) {
                            right[1] = (int) event.getY(1) - droneThrottle.getThrottleImage().getHeight() / 2;
                        } else if (event.getY(1) < 15 * y + droneThrottle.getThrottleImage().getHeight() / 2) {
                            right[1] = (int) (15 * y);
                        } else if (event.getY(1) > 45 * y - droneThrottle.getThrottleImage().getHeight() / 2
                                && event.getY(1) < height) {
                            right[1] = (int) (45 * y - droneThrottle.getThrottleImage().getHeight());
                        }
                    }
                    droneThrottle.setRight(right);

                }
                invalidate();
                break;

            case MotionEvent.ACTION_POINTER_1_DOWN :
                Log.d(TAG,"pointer1 down");
                if (event.getX() >= 3 * x + droneThrottle.getThrottleImage().getWidth()/2
                        && event.getX() <= 33 * x - droneThrottle.getThrottleImage().getWidth() / 2
                        &&event.getY() >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                        && event.getY()  <= 50 * y) {
                    Log.d(TAG,"unlock left throttle");
                    if(mspdata.getRcdata()[7] == 1000) {
                        float position = 45*y - droneThrottle.getThrottleImage().getHeight();
                        if(event.getY() <= 50*y && event.getY() >= position){
                            mspdata.setRawRCDataAux(4, 2000);
                        }
                    }

                    int[] down_left = {
                            (int)(event.getX()-droneThrottle.getThrottleImage().getWidth()/2),
                            (int)(event.getY()-droneThrottle.getThrottleImage().getHeight()/2),
                    };
                    unlock_throttle[0][0] = true;
                    unlock_throttle[0][1] = false;
                    if(event.getY() >= 45*y-droneThrottle.getThrottleImage().getHeight()/2 && event.getY() <= 50*y){
                        if(event.getX() >= 12*x && event.getX() <= 23*x){
                            down_left[0] = (int)(45*y - droneThrottle.getThrottleImage().getHeight());
                        }
                        else {
                            unlock_throttle[0][0] = false;
                            unlock_throttle[0][1] = false;
                        }

                    }
                    if(unlock_throttle[0][1]) {
                        droneThrottle.setLeft(down_left);
                    }
                }

                else if (event.getX(0) >= 52 * x + droneThrottle.getThrottleImage().getWidth()/2
                        && event.getX(0) <= 82 * x - droneThrottle.getThrottleImage().getWidth() / 2
                        &&event.getY(0) >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                        && event.getY(0)  <= 45 * y- droneThrottle.getThrottleImage().getHeight() / 2) {
                    Log.d(TAG,"unlock right throttle");
                    unlock_throttle[0][0] = false;
                    unlock_throttle[0][1] = true;
                    int[] pointer1_down_right = {
                            (int)(event.getX(0)-droneThrottle.getThrottleImage().getWidth()/2),
                            (int)(event.getY(0)-droneThrottle.getThrottleImage().getHeight()/2),
                    };
                    droneThrottle.setRight(pointer1_down_right);
                }

                if(event.getX() >= 40*x && event.getX() <= 45*x && event.getY() >= 48*y && event.getY() <= 53*y){   // lock yaw
                    if(mspdata.getLocked()){
                        mspdata.setLocked(false);
                    }
                    else{
                        mspdata.setLocked(true);
                    }
                    mSoundManager.play(0);
                    Log.d(TAG,"locked : " + mspdata.getLocked());

                }

                if(event.getX() >= 3*x && event.getX() <= 8*x && event.getY() >= 46*y && event.getY() <= 51*y) { // Yaw Tream down
                    mSoundManager.play(0);
                    mspdata.setYawTream(mspdata.getTream()[2]-mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.YAW_DOWN,true);
                }

                if(event.getX() >= 28*x && event.getX() <= 33*x && event.getY() >= 46*y && event.getY() <= 51*y) { // Yaw Tream Up
                    mSoundManager.play(0);
                    mspdata.setYawTream(mspdata.getTream()[2]+mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.YAW_UP,true);
                }

                if(event.getX() >= 40*x && event.getX() <= 45*x && event.getY() >= 30*y && event.getY() <= 35*y) { // pitch Tream Up
                    mSoundManager.play(0);
                    mspdata.setPitchTream(mspdata.getTream()[1]+mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.PITCH_UP,true);
                }

                if(event.getX() >= 40*x && event.getX() <= 45*x && event.getY() >= 40*y && event.getY() <= 45*y) { // pitch Tream Down
                    mSoundManager.play(0);
                    mspdata.setPitchTream(mspdata.getTream()[1]-mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.PITCH_DOWN,true);
                }

                if(event.getX() >= 52*x && event.getX() <= 57*x && event.getY() >= 46*y && event.getY() <= 51*y) { // Roll Tream Down
                    mSoundManager.play(0);
                    mspdata.setRollTream(mspdata.getTream()[0]-mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.ROLL_DOWN,true);
                }

                if(event.getX() >= 77*x && event.getX() <= 82*x && event.getY() >= 46*y && event.getY() <= 51*y) { // Roll Tream Up
                    mSoundManager.play(0);
                    mspdata.setRollTream(mspdata.getTream()[0]+mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.ROLL_UP,true);
                }

                if(event.getX() >= 14*x && event.getX() <= 19*x && event.getY() >= 5*y && event.getY() <= 10*y) { // Aux  Up
                    mSoundManager.play(0);
                    if(mspdata.getReceivedRcdata()[4] == 2000){
                        mspdata.setRawRCDataAux(1,1000);
                    }
                    else if(mspdata.getReceivedRcdata()[4] == 1000){
                        mspdata.setRawRCDataAux(1,2000);
                    }
                }

                if(event.getX() >= 22*x && event.getX() <= 27*x && event.getY() >= 5*y && event.getY() <= 10*y) { // Aux2  Up
                    mSoundManager.play(0);

                    if(mspdata.getReceivedRcdata()[5] == 2000){
                        mspdata.setRawRCDataAux(2,1000);
                    }
                    else if(mspdata.getReceivedRcdata()[5] == 1000){
                        mspdata.setRawRCDataAux(2,2000);
//                        Intent intent = new Intent();
//                        intent.setAction(BTService.REQUEST_MSP_SET_HEAD);
//                        intent.putExtra("head", 70);
//                        mContext.sendBroadcast(intent);
                    }
                }

                if(event.getX() >= 29*x && event.getX() <= 34*x && event.getY() >= 5*y && event.getY() <= 10*y) { // Aux3  Up
                    mSoundManager.play(0);
                    if(mspdata.getReceivedRcdata()[6] == 2000){
                        mspdata.setRawRCDataAux(3,1000);
                    }
                    else if(mspdata.getReceivedRcdata()[6] == 1000){
                        mspdata.setRawRCDataAux(3,2000);
                    }
                }

                if(event.getX() >= 40*x && event.getX() <= 45*x && event.getY() >= 3*y && event.getY() <= 8*y) { // Acc Cali
                    if(!mspdata.getCalibration()[0]) {
                        mSoundManager.play(0);
                        mspdata.setACCCalibration(true);

                        mspdata.setRawRCDataRollPitch(1500,1500);
                        mspdata.setRawRCDataYawThrottle(1500,1000);
                        for(int i=1; i<5; i++)
                            mspdata.setRawRCDataAux(i,1000);

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mContext.sendBroadcast(new Intent().setAction(BTService.REQUEST_ACC_CALIBRATION));
                            }
                        },500);
                    }
                    else{
                        Toast.makeText(mContext,"교정 중...", Toast.LENGTH_SHORT).show();
                    }
                }

                if(event.getX() >= 47*x && event.getX() <= 52*x && event.getY() >= 3*y && event.getY() <= 8*y) { // Mag Cali
                    if(!mspdata.getCalibration()[1]) {
                        mSoundManager.play(0);
                        mspdata.setMAGCalibration(true);
//                        mContext.sendBroadcast(new Intent().setAction(BTService.REQUEST_MAG_CALIBRATION));
                        mspdata.setRawRCDataRollPitch(1500,1500);
                        mspdata.setRawRCDataYawThrottle(1500,1000);
                        for(int i=1; i<5; i++)
                            mspdata.setRawRCDataAux(i,1000);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mContext.sendBroadcast(new Intent().setAction(BTService.REQUEST_MAG_CALIBRATION));

                            }
                        },500);
                    }
                    else{
                        Toast.makeText(mContext,"교정 중...", Toast.LENGTH_SHORT).show();
                    }
                }

                break;


            case MotionEvent.ACTION_POINTER_2_DOWN :
                Log.d(TAG,"pointer2 down");
                if (event.getX(1) >= 3 * x + droneThrottle.getThrottleImage().getWidth()/2
                        && event.getX(1) <= 33 * x - droneThrottle.getThrottleImage().getWidth() / 2
                        &&event.getY(1) >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                        && event.getY(1)  <= 50 * y) {
                    Log.d(TAG,"unlock left throttle");
                    if(mspdata.getRcdata()[7] == 1000) {
                        float position = 45*y - droneThrottle.getThrottleImage().getHeight();
                        if(event.getY(1) <= 50*y && event.getY(1) >= position){
                            mspdata.setRawRCDataAux(4, 2000);
                        }
                    }
                    unlock_throttle[1][0] = true;
                    unlock_throttle[1][1] = false;
                    int[] pointer2_down_left = {
                            (int)(event.getX(1)-droneThrottle.getThrottleImage().getWidth()/2),
                            (int)(event.getY(1)-droneThrottle.getThrottleImage().getHeight()/2),
                    };
                    droneThrottle.setLeft(pointer2_down_left);
                }

                else if (event.getX(1) >= 52 * x + droneThrottle.getThrottleImage().getWidth()/2
                        && event.getX(1) <= 82 * x - droneThrottle.getThrottleImage().getWidth() / 2
                        &&event.getY(1) >= 15 * y + droneThrottle.getThrottleImage().getHeight() / 2
                        && event.getY(1)  <= 45 * y- droneThrottle.getThrottleImage().getHeight() / 2) {
                    Log.d(TAG,"unlock right throttle");
                    unlock_throttle[1][0] = false;
                    unlock_throttle[1][1] = true;
                    int[] pointer2_down_right = {
                            (int)(event.getX(1)-droneThrottle.getThrottleImage().getWidth()/2),
                            (int)(event.getY(1)-droneThrottle.getThrottleImage().getHeight()/2),
                    };
                    droneThrottle.setRight(pointer2_down_right);
                }

                if(event.getX(1) >= 40*x && event.getX(1) <= 45*x && event.getY(1) >= 48*y && event.getY(1) <= 53*y){   // lock yaw
                    if(mspdata.getLocked()){
                        mspdata.setLocked(false);
                    }
                    else{
                        mspdata.setLocked(true);
                    }
                    mSoundManager.play(0);
                    Log.d(TAG,"locked : " + mspdata.getLocked());

                }

                if(event.getX(1) >= 3*x && event.getX(1) <= 8*x && event.getY(1) >= 46*y && event.getY(1) <= 51*y) { // Yaw Tream down
                    mSoundManager.play(0);
                    mspdata.setYawTream(mspdata.getTream()[2]-mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.YAW_DOWN,true);
                }

                if(event.getX(1) >= 28*x && event.getX(1) <= 33*x && event.getY(1) >= 46*y && event.getY(1) <= 51*y) { // Yaw Tream Up
                    mSoundManager.play(0);
                    mspdata.setYawTream(mspdata.getTream()[2]+mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.YAW_UP,true);
                }

                if(event.getX(1) >= 40*x && event.getX(1) <= 45*x && event.getY(1) >= 30*y && event.getY(1) <= 35*y) { // pitch Tream Up
                    mSoundManager.play(0);
                    mspdata.setPitchTream(mspdata.getTream()[1]+mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.PITCH_UP,true);
                }

                if(event.getX(1) >= 40*x && event.getX(1) <= 45*x && event.getY(1) >= 40*y && event.getY(1) <= 45*y) { // pitch Tream Down
                    mSoundManager.play(0);
                    mspdata.setPitchTream(mspdata.getTream()[1]-mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.PITCH_DOWN,true);
                }

                if(event.getX(1) >= 52*x && event.getX(1) <= 57*x && event.getY(1) >= 46*y && event.getY(1) <= 51*y) { // Roll Tream Down
                    mSoundManager.play(0);
                    mspdata.setRollTream(mspdata.getTream()[0]-mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.ROLL_DOWN,true);
                }

                if(event.getX(1) >= 77*x && event.getX(1) <= 82*x && event.getY(1) >= 46*y && event.getY(1) <= 51*y) { // Roll Tream Up
                    mSoundManager.play(0);
                    mspdata.setRollTream(mspdata.getTream()[0]+mspdata.getTreamInterval());
                    mspdata.setTream_touched(MultiData.ROLL_UP,true);
                }

                if(event.getX(1) >= 14*x && event.getX(1) <= 19*x && event.getY(1) >= 5*y && event.getY(1) <= 10*y) { // Aux  Up
                    mSoundManager.play(0);
                    if(mspdata.getReceivedRcdata()[4] == 2000){
                        mspdata.setRawRCDataAux(1,1000);
                    }
                    else if(mspdata.getReceivedRcdata()[4] == 1000){
                        mspdata.setRawRCDataAux(1,2000);
                    }
                }

                if(event.getX(1) >= 22*x && event.getX(1) <= 27*x && event.getY(1) >= 5*y && event.getY(1) <= 10*y) { // Aux2  Up
                    mSoundManager.play(0);

                    if(mspdata.getReceivedRcdata()[5] == 2000){
                        mspdata.setRawRCDataAux(2,1000);
                    }
                    else if(mspdata.getReceivedRcdata()[5] == 1000){
                        mspdata.setRawRCDataAux(2,2000);
//                        Intent intent = new Intent();
//                        intent.setAction(BTService.REQUEST_MSP_SET_HEAD);
//                        intent.putExtra("head", 70);
//                        mContext.sendBroadcast(intent);
                    }
                }

                if(event.getX(1) >= 29*x && event.getX(1) <= 34*x && event.getY(1) >= 5*y && event.getY(1) <= 10*y) { // Aux3  Up
                    mSoundManager.play(0);
                    if(mspdata.getReceivedRcdata()[6] == 2000){
                        mspdata.setRawRCDataAux(3,1000);
                    }
                    else if(mspdata.getReceivedRcdata()[6] == 1000){
                        mspdata.setRawRCDataAux(3,2000);
                    }
                }

                if(event.getX(1) >= 40*x && event.getX(1) <= 45*x && event.getY(1) >= 3*y && event.getY(1) <= 8*y) { // Acc Cali
                    if(!mspdata.getCalibration()[0]) {
                        mSoundManager.play(0);
                        mspdata.setACCCalibration(true);

                        mspdata.setRawRCDataRollPitch(1500,1500);
                        mspdata.setRawRCDataYawThrottle(1500,1000);
                        for(int i=1; i<5; i++)
                            mspdata.setRawRCDataAux(i,1000);

                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mContext.sendBroadcast(new Intent().setAction(BTService.REQUEST_ACC_CALIBRATION));
                            }
                        },500);
                    }
                    else{
                        Toast.makeText(mContext,"교정 중...", Toast.LENGTH_SHORT).show();
                    }
                }

                if(event.getX(1) >= 47*x && event.getX(1) <= 52*x && event.getY(1) >= 3*y && event.getY(1) <= 8*y) { // Mag Cali
                    if(!mspdata.getCalibration()[1]) {
                        mSoundManager.play(0);
                        mspdata.setMAGCalibration(true);
//                        mContext.sendBroadcast(new Intent().setAction(BTService.REQUEST_MAG_CALIBRATION));
                        mspdata.setRawRCDataRollPitch(1500,1500);
                        mspdata.setRawRCDataYawThrottle(1500,1000);
                        for(int i=1; i<5; i++)
                            mspdata.setRawRCDataAux(i,1000);
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mContext.sendBroadcast(new Intent().setAction(BTService.REQUEST_MAG_CALIBRATION));

                            }
                        },500);
                    }
                    else{
                        Toast.makeText(mContext,"교정 중...", Toast.LENGTH_SHORT).show();
                    }
                }

                break;

            case MotionEvent.ACTION_POINTER_1_UP :
                if(unlock_throttle[0][0]) {
                    if (event.getX(0) < width / 2) { // left
                        int[] left_temp = new int[2];
                        if(mspdata.getRcdata()[6] == 2000) {
                            left_temp[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                            left_temp[1] = (int) (30 * y - droneThrottle.getThrottleImage().getHeight() / 2);
                        }
                        else if(mspdata.getRcdata()[6] == 1000){
                            left_temp[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                            left_temp[1] =  droneThrottle.getLeft()[1];
                        }
                        droneThrottle.setLeft(left_temp);
                        unlock_throttle[0][0] = false;
                        if(mspdata.getRcdata()[3] <= 1050){
                            mspdata.setRawRCDataAux(4,1000);
                        }
                    }
                }
                else if(unlock_throttle[0][1]) {
                    if(event.getX() > width/ 2){
                        int[] left_temp = {(int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2), (int) (30 * y - droneThrottle.getThrottleImage().getHeight() / 2)};
                        int[] right_temp = {(int) (67 * x - droneThrottle.getThrottleImage().getWidth() / 2), (int) (30 * y - droneThrottle.getThrottleImage().getHeight() / 2)};
                        droneThrottle.setRight(right_temp);
                        if (unlock_throttle[0][1]) {
                            unlock_throttle[0][1] = false;
                        }
                    }
                }
                Log.d(TAG,"pointer1 count : " + pointer_count );
                break;

            case MotionEvent.ACTION_POINTER_2_UP :
//                if(event.getX(1) < width/2){ // left
//                    int[] left_temp = {(int)(18*x-droneThrottle.getThrottleImage().getWidth()/2), droneThrottle.getLeft()[1]};
//                    int[] right_temp = {(int)(67*x-droneThrottle.getThrottleImage().getWidth()/2), (int)(30*y-droneThrottle.getThrottleImage().getHeight()/2)};
//                    droneThrottle.setLeft(left_temp);
//                    if(unlock_throttle[1][0]){
//                        unlock_throttle[1][0] = false;
//                    }
//                }

                if (event.getX(1) < width / 2) { // left
                    int[] left_temp = new int[2];
                    if(mspdata.getRcdata()[6] == 2000) {
                        left_temp[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                        left_temp[1] = (int) (30 * y - droneThrottle.getThrottleImage().getHeight() / 2);
                    }
                    else if(mspdata.getRcdata()[6] == 1000){
                        left_temp[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                        left_temp[1] =  droneThrottle.getLeft()[1];
                    }
                    droneThrottle.setLeft(left_temp);
                    unlock_throttle[1][0] = false;
                    if(mspdata.getRcdata()[3] <= 1050){
                        mspdata.setRawRCDataAux(4,1000);
                    }
                }
                else{
                    int[] left_temp = {(int)(18*x-droneThrottle.getThrottleImage().getWidth()/2), (int)(30*y-droneThrottle.getThrottleImage().getHeight()/2)};
                    int[] right_temp = {(int)(67*x-droneThrottle.getThrottleImage().getWidth()/2), (int)(30*y-droneThrottle.getThrottleImage().getHeight()/2)};
                    droneThrottle.setRight(right_temp);
                    if(unlock_throttle[1][1]){
                        unlock_throttle[1][1] = false;
                    }
                }
                Log.d(TAG,"pointer2 count : " + pointer_count );
                break;

            case MotionEvent.ACTION_UP :
//                if(event.getX() < width/2){ // left
//                    int[] left_temp = {(int)(18*x-droneThrottle.getThrottleImage().getWidth()/2), droneThrottle.getLeft()[1]};
//                    int[] right_temp = {(int)(67*x-droneThrottle.getThrottleImage().getWidth()/2), (int)(30*y-droneThrottle.getThrottleImage().getHeight()/2)};
//                    droneThrottle.setLeft(left_temp);
//                    if(unlock_throttle[0][0]){
//                        unlock_throttle[0][0] = false;
//                    }
//                    if(unlock_throttle[1][0]){
//                        unlock_throttle[1][0] = false;
//                    }
//                }
                if (event.getX(0) < width / 2) { // left
                    int[] left_temp = new int[2];
                    if(mspdata.getRcdata()[6] == 2000) {
                        left_temp[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                        left_temp[1] = (int) (30 * y - droneThrottle.getThrottleImage().getHeight() / 2);
                    }
                    else if(mspdata.getRcdata()[6] == 1000){
                        left_temp[0] = (int) (18 * x - droneThrottle.getThrottleImage().getWidth() / 2);
                        left_temp[1] =  droneThrottle.getLeft()[1];
                    }
                    droneThrottle.setLeft(left_temp);
                    if(unlock_throttle[0][0]){
                        unlock_throttle[0][0] = false;
                    }
                    if(unlock_throttle[1][0]){
                        unlock_throttle[1][0] = false;
                    }

                    if(mspdata.getRcdata()[3] <= 1050){
                        mspdata.setRawRCDataAux(4,1000);
                    }

                }
                else{
                    int[] left_temp = {(int)(18*x-droneThrottle.getThrottleImage().getWidth()/2), (int)(30*y-droneThrottle.getThrottleImage().getHeight()/2)};
                    int[] right_temp = {(int)(67*x-droneThrottle.getThrottleImage().getWidth()/2), (int)(30*y-droneThrottle.getThrottleImage().getHeight()/2)};
                    droneThrottle.setRight(right_temp);
                    if(unlock_throttle[0][1]){
                        unlock_throttle[0][1] = false;
                    }
                    if(unlock_throttle[1][1]){
                        unlock_throttle[1][1] = false;
                    }
                }
                break;

        }
        return true;
    }
}
