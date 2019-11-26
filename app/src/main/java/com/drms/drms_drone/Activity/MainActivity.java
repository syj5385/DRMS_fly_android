package com.drms.drms_drone.Activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.drms.drms_drone.BuildConfig;
import com.drms.drms_drone.R;


public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 0;
    private static final int PERMISSION_RESULT_OK = 1;


    private TextView version ;

    private TextView logo;
    private boolean isSplash = false;
    private int alpha = 0;
    private int count = 0;

    private Handler mHandler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );

        version = (TextView)findViewById(R.id.version);
        logo = (TextView)findViewById(R.id.logo);

        mHandler = new Handler();

        version.setText("v " + BuildConfig.VERSION_NAME);


        isSplash = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(isSplash){
                    alpha +=2;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            logo.setAlpha((float)alpha/255);
                        }
                    });

                    try{
                        Thread.sleep(15);
                    }catch (InterruptedException e){};
                    if(alpha >= 255) {
                        isSplash = false;
                        mHandler.post(nextActivityRunnable);
                    }
                }
            }
        }).start();
    }

    private Runnable nextActivityRunnable = new Runnable() {
        @Override
        public void run() {

            Intent splash_intent;
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                int permissionResult = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                int permissionResult2 = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
                int permissionResult3 = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if(permissionResult == PackageManager.PERMISSION_DENIED
                        || permissionResult2 == PackageManager.PERMISSION_DENIED
                        || permissionResult3 == PackageManager.PERMISSION_DENIED){
                    splash_intent = new Intent(MainActivity.this,CheckPermissionActivity.class);
                    startActivityForResult(splash_intent,REQUEST_PERMISSION);
                    overridePendingTransition(R.anim.fade, R.anim.hold);
                    finish();
                }
                else{
                    Log.d("HANDLER","OK");
                    splash_intent = new Intent(MainActivity.this,DroneMainActivity.class);
                    startActivity(splash_intent);
                    overridePendingTransition(R.anim.fade, R.anim.hold);
                    finish();
                }

            }
            else{
                splash_intent = new Intent(MainActivity.this,DroneMainActivity.class);
                startActivity(splash_intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
                finish();
            }

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PERMISSION){
            if(resultCode == PERMISSION_RESULT_OK){
                Intent startIntent = new Intent(MainActivity.this,DroneMainActivity.class);
                startActivity(startIntent);
                overridePendingTransition(R.anim.fade, R.anim.hold);
                finish();
            }

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isSplash = false;
        finish();
    }
}
