package com.drms.drms_drone.MainView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import com.drms.drms_drone.Download.DownloadManager;
import com.drms.drms_drone.R;

/**
 * Created by yeongjunsong on 2017. 11. 7..
 */

public class EtcView extends LinearLayout {

    // Variable
    float canvas_width = 0;
    float canvas_height = 0;

    private String TAG = EtcView.class.getSimpleName();


    private Activity mActivity;
    private Context mContext;

    private Paint[] mPaint = new Paint[5];
    public EtcView(Context context,Activity mActivity) {
        super(context);
        this.mContext = context;
        this.mActivity = mActivity;


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

        canvas.drawText("Information", mPaint[0].getTextSize()/2,mPaint[0].getTextSize()*3/2,mPaint[0] );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){

        }
        else if(event.getAction() == MotionEvent.ACTION_UP){
            new DownloadManager(mActivity,mContext).download();
        }
        return true;
    }
}
