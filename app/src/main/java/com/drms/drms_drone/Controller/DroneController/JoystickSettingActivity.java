package com.drms.drms_drone.Controller.DroneController;

import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;



import com.drms.drms_drone.R;
import com.drms.drms_drone.Sound.SoundManager;


/**
 * Created by jjunj on 2017-11-13.
 */

public class JoystickSettingActivity extends AppCompatActivity {

    private static final String TAG = "JoystickSettingActivity";


    private LinearLayout[] optionMenu = new LinearLayout[2];
    private TextView[] optionText = new TextView[2];
    private LinearLayout bottomSetting ;

    private SoundManager mSoundManager;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joysticksetting);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        initializeView();
    }

    private void initializeView(){
        optionMenu[0] = (LinearLayout)findViewById(R.id.joystick_setting);
        optionMenu[1] = (LinearLayout)findViewById(R.id.control_setting);

        optionText[0] = (TextView)findViewById(R.id.joystick);
        optionText[1] = (TextView)findViewById(R.id.control);

        bottomSetting = (LinearLayout)findViewById(R.id.bottom_setting);

        for(int i=0; i<optionText.length; i++)
            optionText[i].setOnTouchListener(optionMenuTouchListener);

        mSoundManager = new SoundManager(this);

        View setting1 = new Setting1View(this,this);
        setting1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        bottomSetting.addView(setting1);

    }


    private boolean[] isSettingmenu = {false, false,false};
    private View.OnTouchListener optionMenuTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                switch(view.getId()){
                    case R.id.joystick :
                        isSettingmenu[0] =true;
                        break;

                    case R.id.control :
                        isSettingmenu[1] = true;
                        break;

                }
            }
            else if(motionEvent.getAction() == MotionEvent.ACTION_UP){
                try{
                    Thread.sleep(30);
                }catch (InterruptedException e){};
                mSoundManager.play(0);
                switch(view.getId()){
                    case R.id.joystick :
                        if(isSettingmenu[0]){
                            isSettingmenu[0] = false;
                            for(int i=0; i<optionMenu.length ; i++){
                                optionMenu[i].setBackgroundColor(getResources().getColor(R.color.joystickSettingBack));
                            }
                            optionMenu[0].setBackgroundColor(getResources().getColor(R.color.joystickSettingSelected));

                            bottomSetting.removeAllViews();
                            View setting1 = new Setting1View(JoystickSettingActivity.this,JoystickSettingActivity.this);
                            setting1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            bottomSetting.addView(setting1);
                        }
                        break;

                    case R.id.control :
                        if(isSettingmenu[1]){
                            isSettingmenu[1] = false;
                            for(int i=0; i<optionMenu.length ; i++){
                                optionMenu[i].setBackgroundColor(getResources().getColor(R.color.joystickSettingBack));
                            }
                            optionMenu[1].setBackgroundColor(getResources().getColor(R.color.joystickSettingSelected));

                            bottomSetting.removeAllViews();
                            View setting2 = new Setting2View(JoystickSettingActivity.this,JoystickSettingActivity.this);
                            setting2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                            bottomSetting.addView(setting2);
                        };
                        break;


                }
            }
            return true;
        }
    };

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.hold,R.anim.move_disappear);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

}
