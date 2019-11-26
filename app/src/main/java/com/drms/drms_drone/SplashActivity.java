package com.drms.drms_drone;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.drms.drms_drone.Drone_Controller.ACCJoystick;
import com.drms.drms_drone.Drone_Controller.DualJoystick;
import com.drms.drms_drone.Drone_Controller.Setting_Activity;
import com.drms.drms_drone.Drone_Controller.SingleJoystick;
import com.drms.drms_drone.Drone_Controller.UploadActivity;

public class SplashActivity extends AppCompatActivity {

    private Intent Upload_Intent;
    private boolean running = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Thread mThread = new SplashThread();
        running = true;
        mThread.start();


        getSupportActionBar().hide();
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        setContentView(R.layout.activity_splash);

    }

    private class SplashThread extends Thread{
        public SplashThread() {
            super();
        }

        @Override
        public void run() {

                try{
                    Thread.sleep(1500);
                }catch (InterruptedException e){

                }

                mHandler.obtainMessage(0).sendToTarget();

        }
    }

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 0){
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);

                startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
                finish();
            }
        }
    };




}
