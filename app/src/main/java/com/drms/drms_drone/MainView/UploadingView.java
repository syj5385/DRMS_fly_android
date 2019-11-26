package com.drms.drms_drone.MainView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.drms.drms_drone.Activity.DroneMainActivity;
import com.drms.drms_drone.Communication.ClassicBluetooth.BluetoothService;
import com.drms.drms_drone.FileManagement.FileManagement;
import com.drms.drms_drone.MultiData;
import com.drms.drms_drone.R;
import com.drms.drms_drone.Service.BTService;
import com.drms.drms_drone.Sound.SoundManager;
import com.drms.drms_drone.Upload.UploadSelectedActivity;


/**
 * Created by yeongjunsong on 2017. 11. 7..
 */

public class UploadingView extends LinearLayout {

    private SoundManager soundManager;

    // Variable
    float canvas_width = 0;
    float canvas_height = 0;

    private String TAG = UploadingView.class.getSimpleName();

    private Context mContext;
    private Activity mActivity;
    private BTService mBTService;

    private String bt_address;


    private Paint[] mPaint = new Paint[5];

    private MultiData mspdata;


    public UploadingView(Context context, Activity mActivity) {
        super(context);
        soundManager = new SoundManager(context);
        this.mContext = context;
        this.mActivity = mActivity;
        mspdata = (MultiData)mActivity.getApplication();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // initialize canvas Size
        if(canvas_width == 0 && canvas_height == 0){
            canvas_width = canvas.getWidth();
            canvas_height = canvas.getHeight();
            Log.d(TAG,"Canvas Width : " + canvas_width +  "\nCanvas Height : " + canvas_height);
        }

        drawTopic(canvas);

    }

    public void drawTopic(Canvas canvas) {
        mPaint[0] = new Paint();
        mPaint[0].setTextAlign(Paint.Align.LEFT);
        mPaint[0].setTextSize(70);
        mPaint[0].setStrokeWidth(5);
        mPaint[0].setColor(mContext.getResources().getColor(R.color.mainTopic_2));

        canvas.drawText(" 업로드 ", mPaint[0].getTextSize()/2,mPaint[0].getTextSize()*3/2,mPaint[0] );
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP){
            soundManager.play(0);


            FileManagement mFileManager = new FileManagement(mContext,null);

            if(mBTService.getmBluetoothService().getState() == BluetoothService.STATE_CONNECTED) {
                Intent uploadIntent = new Intent(mActivity, UploadSelectedActivity.class);
                mActivity.startActivityForResult(uploadIntent, DroneMainActivity.REQUEST_UPLAOD);
                mActivity.overridePendingTransition(R.anim.fade, R.anim.hold);
            }
            else if(mBTService.getmBluetoothService().getState() == BluetoothService.STATE_CONNECTING) {
                Toast.makeText(mContext, "드론 연결 중...", Toast.LENGTH_SHORT).show();
            }
            else{
                Toast.makeText(mContext,"드론을 연결해주세요.",Toast.LENGTH_SHORT).show();
            }


        }
        return true;
    }

    public void setmBTService(BTService mBTService){
        this.mBTService = mBTService;
        Log.d(TAG,"set BTService in UploadView");
    }
}
