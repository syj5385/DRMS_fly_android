package com.drms.drms_drone.Drone_Controller;

/**
 * Created by jjunj on 2017-10-02.
 */

public class ContElement {
    //  R   /  P  /  Y / thro/  D2  / D4  / D5  / D6  / D7  / D8  / R   /  P  / Y

    private int[] RPY = new int[4] ; // roll, pitch, yaw, throttle
    private int[] digitPin = new int[6] ; // D2, D4, D5, D6, D7, D8
    private int[] tream = new int[3] ;

    public ContElement() {

        RPY[0] = 1500; // roll
        RPY[1] = 1500; // pitch
        RPY[2] = 1500; // yaw
        RPY[3] = 1000; // throttle

        digitPin[0] = 3; // controll speed`
        digitPin[1] = 1000;
        digitPin[2] = 1000;
        digitPin[3] = 1000;
        digitPin[4] = 1000;
        digitPin[5] = 1000;

        tream[0] = 127; // pitch
        tream[1] = 127; // roll
        tream[2] = 127; // yaw


    }

    public void setRPY(int[] rpy){
        this.RPY = rpy;
    }

    public int[] getRPY(){
        return RPY;
    }

    public void setDigitPin(int[] digit){
        this.digitPin  = digit;
    }

    public int[] getDigitPin(){
        return digitPin;
    }

    public void setTream(int[] tream) {
        this.tream = tream;
    }

    public int[] getTream(){
        return tream;
    }
}
