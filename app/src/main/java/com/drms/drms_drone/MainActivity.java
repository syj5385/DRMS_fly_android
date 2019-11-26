package com.drms.drms_drone;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.drms.drms_drone.Drone_Controller.ACCJoystick;
import com.drms.drms_drone.Drone_Controller.DrsControllerActivity;
import com.drms.drms_drone.Drone_Controller.DualJoystick;
import com.drms.drms_drone.Drone_Controller.Setting_Activity;
import com.drms.drms_drone.Drone_Controller.SingleJoystick;
import com.drms.drms_drone.Drone_Controller.UploadActivity;
import com.drms.drms_drone.Sound.SoundManager;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ImageView quad_x, hex_x;

    private ImageView controller_upload;

    private LinearLayout multiwiiconf;

    private Intent Upload_Intent;

    private long lastTimeBackPressed;

    private SoundManager mSoundManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getSupportActionBar().hide();
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int permissionResult = checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
            int permissionResult2 = checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION);
            int permissionResult3 = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if (permissionResult == PackageManager.PERMISSION_DENIED || permissionResult2 == PackageManager.PERMISSION_DENIED
                    || permissionResult3 == PackageManager.PERMISSION_DENIED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                        || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                    dialog.setTitle("권한이 필요합니다.").setMessage("이 기능을 사용하기 위해서는 단말기의 권한이 필요합니다. 계속 하시겠습니까")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                                Manifest.permission.ACCESS_FINE_LOCATION}, 1000);

                                    }
                                }
                            }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(MainActivity.this, "기능을 취소했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }).create().show();

                } else {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},1000);

                }

            }
            else{
            }
        }
        else{

        }

        setContentView(R.layout.activity_main);
        quad_x = (ImageView)findViewById(R.id.quad_x);
        quad_x.setOnTouchListener(mTouchListener);

        hex_x = (ImageView)findViewById(R.id.hex_x);
        hex_x.setOnTouchListener(mTouchListener);

        controller_upload = (ImageView)findViewById(R.id.controller_upload);
        controller_upload.setOnTouchListener(mTouchListener);

        multiwiiconf = (LinearLayout)findViewById(R.id.multiwiiconf);
        multiwiiconf.setOnTouchListener(mTouchListener);

        Upload_Intent = new Intent(getApplicationContext(),UploadActivity.class);

        mSoundManager = new SoundManager(this);
        mSoundManager.addSound(0,R.raw.button2);

    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch(v.getId()){
                case R.id.quad_x :
                    if(event.getAction() == MotionEvent.ACTION_DOWN) {
                        quad_x.setImageDrawable(getResources().getDrawable(R.drawable.quad_press));


                    }
                    else if(event.getAction() == MotionEvent.ACTION_UP){
                        mSoundManager.play(0);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {}

                        quad_x.setImageDrawable(getResources().getDrawable(R.drawable.quad_select));
                        Upload_Intent.putExtra("Firmware", "QUAD");
                        AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                        LinearLayout title_layout = new LinearLayout(MainActivity.this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        title_layout.setLayoutParams(params);
                        title_layout.setPadding(100, 20, 100, 20);
                        title_layout.setBackgroundColor(getResources().getColor(R.color.dialogColor));

                        ImageView title = new ImageView(MainActivity.this);
                        title.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        title.setImageDrawable(getResources().getDrawable(R.drawable.quad_info));

                        title_layout.addView(title);


                        dialog.setCustomTitle(title_layout);
                        dialog.setView(R.layout.drone_dialog);

                        dialog.show();
                    }

                    break;

                case R.id.hex_x :
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        hex_x.setImageDrawable(getResources().getDrawable(R.drawable.hex_press));
                    }

                    else if(event.getAction() == MotionEvent.ACTION_UP) {
                        mSoundManager.play(0);
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {}

                        hex_x.setImageDrawable(getResources().getDrawable(R.drawable.hex_select));
                        Upload_Intent.putExtra("Firmware", "HEX");
                        AlertDialog.Builder dialog2 = new AlertDialog.Builder(MainActivity.this);
                        LinearLayout title_layout2 = new LinearLayout(MainActivity.this);
                        LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        title_layout2.setLayoutParams(params2);
                        title_layout2.setPadding(100, 20, 100, 20);
                        title_layout2.setBackgroundColor(getResources().getColor(R.color.dialogColor));

                        ImageView title2 = new ImageView(MainActivity.this);
                        title2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                        title2.setImageDrawable(getResources().getDrawable(R.drawable.hex_info));

                        title_layout2.addView(title2);


                        dialog2.setCustomTitle(title_layout2);
                        dialog2.setView(R.layout.drone_dialog);

                        dialog2.show();
                    }
                    break;

                case R.id.controller_upload :
                    if(event.getAction() == MotionEvent.ACTION_DOWN){
                        controller_upload.setImageDrawable(getResources().getDrawable(R.drawable.cont_upload_2));
                    }
                    else if(event.getAction() == MotionEvent.ACTION_UP){
                        mSoundManager.play(0);
                        try{
                            Thread.sleep(100);
                        }catch (InterruptedException e){}

                        controller_upload.setImageDrawable(getResources().getDrawable(R.drawable.cont_upload_1));
//                        Upload_Intent.putExtra("Firmware","DRONE_CONTROLLER");
//                        startActivity(Upload_Intent);
                        Intent controller_intent = new Intent(MainActivity.this, DrsControllerActivity.class);
                        startActivity(controller_intent);
//                        Toast.makeText(getApplicationContext(),"DRS 조종기를 준비 중입니다.^^",Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.multiwiiconf :
                    if(event.getAction() == MotionEvent.ACTION_DOWN){

                    }
                    else if(event.getAction() == MotionEvent.ACTION_UP){
                        mSoundManager.play(0);

                        if(getPackageList()){
                            Intent intent = getPackageManager().getLaunchIntentForPackage("jjun.msp.mspconfig");
                            startActivity(intent);

                        }
                        else{
                            String url = "market://details?id=" + "jjun.msp.mspconfig";
                            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                            startActivity(i);
                        }

                    }

                    break;
                }



            return true;
        }
    };

    public void onClick1(View v){
        Intent intent = new Intent(getApplicationContext(),DualJoystick.class);
        startActivity(intent);

    }

    public void onClick2(View v){
        Intent intent = new Intent(getApplicationContext(),SingleJoystick.class);
        startActivity(intent);

    }

    public void onClick3(View v){
        Intent intent = new Intent(getApplicationContext(),ACCJoystick.class);
        startActivity(intent);

    }

    public void onClick4(View v){
        Intent intent = new Intent(getApplicationContext(),Setting_Activity.class);
        startActivity(intent);

    }

    public void onClick5(View v){
        startActivity(Upload_Intent);
    }

    @Override
    public void onBackPressed() {
        if(System.currentTimeMillis() - lastTimeBackPressed <1500) {
            finish();
            return;
        }

        lastTimeBackPressed = System.currentTimeMillis();
        Toast.makeText(getApplicationContext(),"뒤로 버튼을 한번 더 누르면 종료됩니다.",Toast.LENGTH_SHORT).show();
    }

    public boolean getPackageList(){
        boolean isExist = false;

        PackageManager pm = getPackageManager();
        List<ResolveInfo> mApps;
        Intent mainIntent = new Intent(Intent.ACTION_MAIN,null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        mApps = pm.queryIntentActivities(mainIntent,0);

        try{
            for(int i=0 ; i <mApps.size() ; i++){
                if(mApps.get(i).activityInfo.packageName.startsWith("jjun.msp.mspconfig")){
                    isExist = true;
                    break;
                }
            }
        }catch(Exception e){
            isExist = false;
        }
        return isExist;
    }
}
