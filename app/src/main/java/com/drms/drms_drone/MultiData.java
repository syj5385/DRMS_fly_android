package com.drms.drms_drone;

import android.app.Application;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import static android.content.ContentValues.TAG;

/**
 * Created by yeongjunsong on 2017. 11. 9..
 */

public final class MultiData extends Application {

    private TextToSpeech tts ;

    public static final int DUAL1 = 0;
    public static final int DUAL2 = 1;
    public static final int SINGLE = 2;

    private static String BtName = "";
    private static String BtAddress = "";

    private static long DISCOVER_LOOP = 7000;

    private int mobile_vbat = 0;

    public static final int veryslow = 100;
    public static final int slow = 200;
    public static final int middle = 300;
    public static final int fast = 350;
    public static final int veryfast = 400;

    private int DRONE_SPEED = middle;

    private int smartphoneAngle = 50;

    private int MYJOYSTICK = DUAL1;

    private int MSP_TIME = 23;

    private int treamInterval = 3;

    private String[] BOX_ITEM;

    private int[] tream ={
            0,0,0
    };

    public static final int ROLL_DOWN = 0 ;
    public static final int ROLL_UP = 1;
    public static final int PITCH_DOWN = 2;
    public static final int PITCH_UP = 3;
    public static final int YAW_DOWN = 4;
    public static final int YAW_UP = 5;

    private boolean[] tream_touched = {false, false, false, false, false, false};

    public void setTream_touched(int index, boolean state){
        tream_touched[index] = state;
    }

    public boolean[] getTream_touched(){
        return tream_touched;
    }

    public void setMYJOYSTICK(int MYJOYSTICK){
        this.MYJOYSTICK = MYJOYSTICK;
    }

    public int getMYJOYSTICK(){
        return MYJOYSTICK;
    }


    private boolean locked = false;

    private int[] identData ={
            0,0,0,0
    };

    private int[] rcdata = {
            1500,   // Roll
            1500,   // Pitch
            1500,   // Yaw
            1000,   // Throttle
            1000,   // Aux1
            1000,   // Aux2
            1000,   // Aux3
            1000    // Aux4
    };

    private int[] receivedRcdata = {
            1500,
            1500,
            1500,
            1000,
            1000,
            1000,
            1000,
            1000
    };
    private float[] attitudeData ={
            (float)0.0, // roll
            (float)0.0, // pith
            (float)0.0, // yaw
    };
    private float[] analogData ={
            (float)4.2,
            (float)0.0,
            (float)0.0,
            (float)0.0
    };

    private int[] rawRCData = {
            1500,1500,1500,1000,1000,1000,1000,1000
    };

    private int[] IMUdata = {
            0,0,0,0,0,0,0,0,0
    } ;

    private float[] MISCdata={
            0,0,0,0,0,0,0,0,0,0,0,0
    };

    private float[] RCTUNEdata ={
            0,0,0,0,0,0,0
    };

    private int[] PIDdata = {
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0,
            0,0,0,0,0,0,0,0,0,0
    } ;

    private float[] ALTITUDEdata = {0,0};

    private boolean[] calibration = {false, false}; // acc / Mag

    private boolean[][] checkboxData;
    private boolean[][] sendcheckboxData = new boolean[8][12];

    public void setALTITUDEdata(float[] altitude){
        this.ALTITUDEdata = altitude;
    }

    public float[] getALTITUDEdata(){
        return ALTITUDEdata;
    }

    public boolean[][] getCheckboxData(){
        return checkboxData;
    }

    public void initBoxITEM(String[] boxitem){
        BOX_ITEM = boxitem;
        checkboxData = new boolean[BOX_ITEM.length][12];
        Log.d(TAG,"box item length : " + boxitem.length);
    }

    public String[] getBoxITEM(){
        return BOX_ITEM;
    }


    public void setTreamInterval(int treamInterval){
        this.treamInterval = treamInterval;
    }

    public int getTreamInterval(){
        return treamInterval;
    }

    public void setEachCheckboxData(int x, int y, boolean checked){
        this.checkboxData[x][y] = checked;
    }

    public void setCheckboxData(boolean[][] checkdata){
        this.checkboxData = checkdata;

    }


    public void setPIDdata(int[] piddata){
        this.PIDdata = piddata;
    }

    public int[] getPIDdata(){
        return PIDdata;
    }


    public void setRCTUNEdata(float[] RCTUNEdata){
        this.RCTUNEdata = RCTUNEdata;
    }

    public float[] getRCTUNEdata(){
        return RCTUNEdata;
    }


    public void setMISCdata(float[] MISCdata){
        this.MISCdata = MISCdata;
    }

    public float[] getMISCdata(){
        return MISCdata;
    }

    public void setIMUdata(int[] IMUdata){
        this.IMUdata = IMUdata;
    }

    public int[] getIMUdata(){
        return IMUdata;
    }

    public int getMSP_TIME(){
        return MSP_TIME;
    }

    public int getDRONE_SPEED(){
        return DRONE_SPEED;
    }

    public int getSmartphoneAngle(){
        return smartphoneAngle;
    }

    public void setSmartphoneAngle(int smartphoneAngle){
        this.smartphoneAngle = smartphoneAngle;
    }

    public void setDRONE_SPEED(int DRONE_SPEED){
        this.DRONE_SPEED = DRONE_SPEED;
    }

    public void setACCCalibration(boolean accCalibration){
        calibration[0] = accCalibration;
    }

    public void setMAGCalibration(boolean magCalibration){
        calibration[1] = magCalibration;
    }

    public boolean[] getCalibration(){
        return calibration;
    }

    public void setMobile_vbat(int vbat){
        mobile_vbat = vbat;
    }

    public int getMobile_vbat(){
        return mobile_vbat;
    }

    public int[] getTream(){
        return tream;
    }

    public boolean getLocked(){
        return locked;
    }

    public void setLocked(boolean locked){
        this.locked = locked;
    }
    public void setDiscoverLoop(long discoverLoop){
        DISCOVER_LOOP = discoverLoop;
    }

    public long getDiscoverLoop(){
        return DISCOVER_LOOP;
    }

    public void setRcdata(int[] rc){
        this.rcdata = rc;
    }

    public int[] getRcdata(){
        return rawRCData;
    }

    public void setAttitudeData(float[] attitudeData){
        this.attitudeData = attitudeData;
    }

    public float[] getAttitudeData(){
        return attitudeData;
    }

    public void setReceivedRcdata(int[] receivedRcdata){
        this.receivedRcdata = receivedRcdata;
    }

    public int[] getReceivedRcdata(){
        return receivedRcdata;
    }

    public void setAnalogData(float[] analogData){
        this.analogData = analogData;
        if(this.analogData[0] < 2.5) {
            if(this.analogData[0] >= 0 && this.analogData[0] < 0.2)
                this.analogData[0] = (float)0.1;

            else
                this.analogData[0] = (float) 2.5;
        }
    }

    public void setRawRCDataRollPitch(int roll, int pitch){
        rawRCData[0] = roll;
        rawRCData[1] = pitch;

    }
    public void setRawRCDataYawThrottle(int yaw, int throttle){

        rawRCData[2] = yaw;
        rawRCData[3] = throttle;
    }

    public void setRawRCDataAux(int position, int value){
        rawRCData[position +3] = value;
    }

    public float[] getAnalogData(){
        return analogData;
    }

    public void setRollTream(int rollTream){
        if(rollTream >= 127){
            rollTream = 127;
        }
        if(rollTream <= -127){
            rollTream = -127;
        }
        tream[0] = rollTream;
    }

    public void setPitchTream(int pitchTream){
        if(pitchTream >= 127){
            pitchTream = 127;
        }
        if(pitchTream <= -127){
            pitchTream = -127;
        }
        tream[1] = pitchTream;
    }

    public void setYawTream(int yawTream){
        if(yawTream >= 127){
            yawTream = 127;
        }
        if(yawTream <= -127){
            yawTream = -127;
        }
        tream[2] = yawTream;
    }

    public void initializeMultiData(){
        mobile_vbat = 0;

        rcdata[0] = 1500;
        rcdata[1] = 1500;
        rcdata[2] = 1500;
        rcdata[3] = 1000;
        rcdata[4] = 1000;
        rcdata[5] = 1000;
        rcdata[6] = 1000;
        rcdata[7] = 1000;

        receivedRcdata[0] = 1500;
        receivedRcdata[1] = 1500;
        receivedRcdata[2] = 1500;
        receivedRcdata[3] = 1000;
        receivedRcdata[4] = 1000;
        receivedRcdata[5] = 1000;
        receivedRcdata[6] = 1000;
        receivedRcdata[7] = 1000;

        attitudeData[0] = 0;
        attitudeData[1] = 0;
        attitudeData[2] = 0;

        analogData[0] = (float)4.2;
        analogData[1] = 0;
        analogData[2] = 0;
        analogData[3] = 0;

        rawRCData[0] = 1500;
        rawRCData[1] = 1500;
        rawRCData[2] = 1500;
        rawRCData[3] = 1000;
        rawRCData[4] = 1000;
        rawRCData[5] = 1000;
        rawRCData[6] = 1000;
        rawRCData[7] = 1000;

        calibration[0] = false;
        calibration[1] = false;

        for(int i=0; i<IMUdata.length ;i++){
            IMUdata[i] = 0;
        }

        for(int i=0; i<MISCdata.length ; i++){
            MISCdata[i] = 0;
        }

        for(int i=0; i<RCTUNEdata.length ;i++){
            RCTUNEdata[0] = 0;
        }

        for(int i=0; i<PIDdata.length; i++){
            PIDdata[i] = 0 ;
        }

        if(BOX_ITEM != null) {
            for (int i = 0; i < BOX_ITEM.length; i++) {
                for (int j = 0; j < 12; j++) {
                    checkboxData[i][j] = false;
                }
            }
        }

        BOX_ITEM = null;
    }

    private byte[] EEPROMData = {
                                //0             1            2            3            4            5             6             7            8            9
            /*  00 */           (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,
            /*  01 */           (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)33 ,   (byte)30 ,   (byte)23 ,   (byte)33 ,
            /*  02 */           (byte)30 ,   (byte)23 ,   (byte)68 ,   (byte)45 ,   (byte)0  ,   (byte)64 ,   (byte)25 ,   (byte)24 ,   (byte)15 ,   (byte)0  ,
            /*  03 */           (byte)0  ,   (byte)34 ,   (byte)14 ,   (byte)53 ,   (byte)25 ,   (byte)33 ,   (byte)83 ,   (byte)90 ,   (byte)10 ,   (byte)100,
            /*  04 */           (byte)40 ,   (byte)255,   (byte)255,   (byte)0  ,   (byte)0  ,   (byte)0  ,   (byte)87 ,   (byte)62 ,   (byte)0  ,   (byte)0  ,
            /*  05 */           (byte)0  ,   (byte)50 ,   (byte)0  ,   (byte)0  ,   (byte)0  ,   (byte)0  ,   (byte)0  ,   (byte)0  ,   (byte)8  ,   (byte)7  ,
            /*  06 */           (byte)0  ,   (byte)0  ,   (byte)0  ,   (byte)0  ,   (byte)0  ,   (byte)0  ,   (byte)0  ,   (byte)0  ,   (byte)0  ,   (byte)0  ,
            /*  07 */           (byte)0  ,   (byte)0  ,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,
            /*  08 */           (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,
            /*  09 */           (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,
            /*  10 */           (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,
            /*  11 */           (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,
            /*  12 */           (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)255,   (byte)226,   (byte)4  ,
            /*  13 */           (byte)10 ,   (byte)84 ,   (byte)75 ,   (byte)69 ,   (byte)26 ,   (byte)4  ,   (byte)10 ,   (byte)120,   (byte)0  ,   (byte)228
    };

    public byte[] getEEPROMData(){
        return EEPROMData;
    }
}
