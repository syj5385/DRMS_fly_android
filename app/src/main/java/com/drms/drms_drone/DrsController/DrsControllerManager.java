package com.drms.drms_drone.DrsController;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.drms.drms_drone.Controller.DroneController.JoystickActivity;
import com.drms.drms_drone.MultiData;
import com.drms.drms_drone.Service.BTService;

/**
 * Created by jjun on 2018. 5. 19..
 */

public class DrsControllerManager {

    private int roll, pitch, yaw, throttle, left_resistor, right_resistor;
    private int power, d1, d2, d3, d4, d5, d6;
    private int roll_tream, pitch_tream, yaw_tream;

    private static final String TAG = "DrsControllerManager";

    private Service service;
    private MultiData multiData;

    public DrsControllerManager(Service service) {
        this.service  = service;
        multiData = (MultiData)service.getApplication();
    }

    public boolean processControllerData(byte[] data){
        if(data.length > 0) {
//            Log.d(TAG,"size : " + data.length);
            if ((char) data[0] == '#' && data.length == 28) {
                int[] recvdata = new int[14];
                String header = "";
                for (int i = 0; i < 5; i++) {
                    header += (char) data[i];
                }
//                Log.d(TAG, "header : " + header);
                int index = 5;
                byte checksum = 0;
                if(!header.equals("#cont")){
                    return false;
                }

                for(int i=0; i<22; i++){
                    checksum ^= data[index++];
                }

                if(checksum == data[data.length-1]) {
                    index = 5;

                    roll = read16(data[index++], data[index++]);
                    pitch = read16(data[index++], data[index++]);
                    yaw = read16(data[index++], data[index++]);
                    throttle = read16(data[index++], data[index++]);

                    power = read8(data[index++]);
                    d1 = read8(data[index++]);
                    d2 = read8(data[index++]);
                    d3 = read8(data[index++]);
                    d4 = read8(data[index++]);
                    d5 = read8(data[index++]);
                    d6 = read8(data[index++]);

                    left_resistor = read16(data[index++], data[index++]);
                    right_resistor = read16(data[index++], data[index++]);

                    roll_tream = read8(data[index++]);
                    pitch_tream = read8(data[index++]);
                    yaw_tream = read8(data[index++]);
//
//                Log.d(TAG,"Roll : " + roll + "\tpitch : " + pitch + "\tyaw : " + yaw + "\tthrottle : " + throttle);
//                Log.d(TAG,"power : " + power);
//                Log.d(TAG,"D1 : " + d1);
//                Log.d(TAG,"D2 : " + d2);
//                Log.d(TAG,"D3 : " + d3);
//                Log.d(TAG,"D4 : " + d4);
//                Log.d(TAG,"D5 : " + d5);
//                Log.d(TAG,"D6 : " + d6);
//                Log.d(TAG,"left : " + left_resistor + "\tright : " + right_resistor);
//                Log.d(TAG,"roll_t : " + roll_tream + "\tpitch_t : " + pitch_tream + "\tYaw_t : " + yaw_tream);

                    setRPYT(roll,pitch,yaw,throttle);
                    setPower(power);
                    setDroneSpeed(d1);
                    setAccCalibration(d6);
                    setTream(roll_tream,pitch_tream,yaw_tream);
                    setPreviousDisplay(d4);
                    setNextDisplay(d5);

                }
                else{
                    return false;
                }
                return true;
            }
        }
        else{
            return false;
        }
        return true;
    }

    private void setRPYT(int roll, int pitch, int yaw, int throttle){
        this.roll = ((roll - 1500) * multiData.getDRONE_SPEED() / 500) + 1500 + multiData.getTream()[0];
        this.pitch = ((pitch - 1500) * multiData.getDRONE_SPEED() / 500) + 1500 + multiData.getTream()[1];
        this.yaw = ((yaw - 1500) * multiData.getDRONE_SPEED() / 500) + 1500 + multiData.getTream()[2];
        multiData.setRawRCDataRollPitch(this.roll, this.pitch);
        multiData.setRawRCDataYawThrottle(this.yaw, this.throttle);
    }

    private void setPower(int aux4){
        if (power == 200) {
            if (multiData.getRcdata()[7] == 2000) {
                multiData.setRawRCDataAux(4, 1000);
            } else if (multiData.getRcdata()[7] == 1000) {
                multiData.setRawRCDataAux(4, 2000);
            }
        }
    }

    private void setPreviousDisplay(int d4){
        if(d4 == 200){
            service.sendBroadcast(new Intent(BTService.PREVIOUS_DISPLAY));
        }

    }

    private void setNextDisplay(int d5){
        if(d5 == 200){
            service.sendBroadcast(new Intent(BTService.NEXT_DISPLAY));
        }

    }


    private void setTream(int roll, int pitch, int yaw){
        int[] tream = multiData.getTream();
        int interval =  multiData.getTreamInterval();

        if(roll == 200){
            tream[0] += interval;
        }
        else if(roll == 100){
            tream[0] -= interval;
        }
        else if(roll == 150){

        }

        if(pitch == 200){
            tream[1] += interval;
        }
        else if(pitch == 100){
            tream[1] -= interval;
        }
        else if(pitch == 150){

        }

        if(yaw == 200){
            tream[2] += interval;
        }
        else if(yaw == 100){
            tream[2] -= interval;
        }
        else if(yaw == 150){

        }

        multiData.setRollTream(tream[0]);
        multiData.setPitchTream(tream[1]);
        multiData.setYawTream(tream[2]);


    }

    private void setDroneSpeed(int d1){
        if(d1 == 200){
            if(multiData.getDRONE_SPEED() == MultiData.veryslow){
                multiData.setDRONE_SPEED(MultiData.slow);
                return;
            }
            if(multiData.getDRONE_SPEED() == MultiData.slow){
                multiData.setDRONE_SPEED(MultiData.middle);
                return;
            }
            if(multiData.getDRONE_SPEED() == MultiData.middle){
                multiData.setDRONE_SPEED(MultiData.fast);
                return;
            }
            if(multiData.getDRONE_SPEED() == MultiData.fast){
                multiData.setDRONE_SPEED(MultiData.veryfast);
                return;
            }
            if(multiData.getDRONE_SPEED() == MultiData.veryfast){
                multiData.setDRONE_SPEED(MultiData.veryslow);
                return;
            }
        }
    }

    private void setAccCalibration(int d6){
        if(d6 == 200){
            service.sendBroadcast(new Intent(BTService.REQUEST_ACC_CALIBRATION));
        }
    }

    public int read8(byte int_8_1){
        return int_8_1 & 0xff;
    }

    public int read16(byte int_16_1, byte int_16_2){
        return ((int_16_1 & 0xff) + (int_16_2 << 8));
    }

}
