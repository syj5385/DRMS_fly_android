package com.drms.drms_drone.Drone_Controller;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageItemInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.drms.drms_drone.R;

/**
 * Created by jjunj on 2017-10-02.
 */

public class DrsControllerView extends View {

    private Context context;
    private Activity mActivity;
    private Handler mHandler;
    private ContElement contElement;

    private LinearLayout controll_window;

    private boolean layout_checked = false;
    private float view_width, view_height;

    private float speed_rate = (float)0.5;
    private boolean isSpeedSetting = false;

    private Paint[] mPaint = new Paint[10];

    public DrsControllerView(Context context, Activity mActivity, Handler mHandler,ContElement contElement) {
        super(context);
        this.context = context;
        this.mActivity = mActivity;
        this.mHandler = mHandler;
        this.contElement = contElement;

        controll_window = (LinearLayout)mActivity.findViewById(R.id.controller_window);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        view_width = canvas.getWidth();
        view_height = canvas.getHeight();

        drawThrottle(canvas);
        drawSpeed(canvas);
        drawRPY(canvas);
        drawTream(canvas);
    }

    private void drawThrottle(Canvas canvas){
        Canvas thro_canvas = canvas;
        mPaint[0] = new Paint();
        mPaint[0].setStyle(Paint.Style.FILL);
        mPaint[0].setStrokeWidth(10.0f);
        mPaint[0].setARGB(58,143,143,143);

        mPaint[1] = new Paint();
        mPaint[1].setStrokeWidth(5.0f);
        mPaint[1].setStyle(Paint.Style.STROKE);


        mPaint[2] = new Paint();
        mPaint[2].setStrokeWidth(3.0f);
        mPaint[2].setColor(Color.RED);

        mPaint[3] = new Paint();
        mPaint[3].setStrokeWidth(20.0f);
        mPaint[3].setColor(Color.BLUE);

        thro_canvas.drawRect(1,1, view_height/3,view_height/3,mPaint[0]);
        thro_canvas.drawRect(view_width-(view_height/3), 0, view_width-1,view_height/3-1,mPaint[0]);

        thro_canvas.drawRect(0,0, view_height/3,view_height/3,mPaint[1]);
        thro_canvas.drawRect(view_width-(view_height/3), 0, view_width,view_height/3,mPaint[1]);

        thro_canvas.drawLine(0,view_height/6,view_height/3,view_height/6,mPaint[2]);
        thro_canvas.drawLine(view_height/6,0,view_height/6,view_height/3,mPaint[2]);

        thro_canvas.drawLine(view_width-(view_height/3),view_height/6,view_width, view_height/6,mPaint[2]) ;
        thro_canvas.drawLine(view_width-(view_height/6),0,view_width-(view_height/6),view_height/3,mPaint[2]);

        int[] rpy = contElement.getRPY();



        thro_canvas.drawPoint(view_width-view_height/6 + (rpy[0]-1500)*(view_height/6-10)/500,
                view_height/6 - (rpy[1] - 1500)*(view_height/6-10)/500,mPaint[3]);

        thro_canvas.drawPoint(view_height/6 + (rpy[2]-1500)*(view_height/6-10)/500,
                view_height/6 - (rpy[3] - 1500)*(view_height/6-10)/500,mPaint[3]);
    }

    private void drawTream(Canvas canvas){
        Canvas tream_canvas = canvas;

        mPaint[0] = new Paint();
        mPaint[0].setTextSize(45.0f);
        mPaint[0].setTextAlign(Paint.Align.CENTER);
        mPaint[0].setColor(Color.RED);

        mPaint[1] = new Paint();
        mPaint[1].setStrokeWidth(15.0f);
        mPaint[1].setColor(Color.GRAY);

        float tream_R = tream_array[0] - 127;
        float tream_P = tream_array[1] - 127;
        float tream_Y = tream_array[2] - 127;

        tream_canvas.drawText(String.valueOf((int)tream_R),view_width-view_height/6,view_height/3+mPaint[0].getTextSize(),mPaint[0]);
        tream_canvas.drawText(String.valueOf((int)tream_P),view_width-view_height/3-mPaint[0].getTextSize(),view_height/6+mPaint[0].getTextSize()/2,mPaint[0]);
        tream_canvas.drawText(String.valueOf((int)tream_Y),view_height/6, view_height/3 + mPaint[0].getTextSize(),mPaint[0]);





    }

    private void drawSpeed(Canvas canvas){
        Canvas speed_canvas = canvas;
        mPaint[0] = new Paint();
        mPaint[0].setStrokeWidth(5.0f);
        mPaint[0].setStyle(Paint.Style.STROKE);

        mPaint[1] = new Paint();
        mPaint[1].setStyle(Paint.Style.FILL);
        mPaint[1].setColor(Color.GRAY);

        mPaint[2] = new Paint();
        mPaint[2].setTextSize(40);
        mPaint[2].setColor(Color.BLACK);
        mPaint[2].setTextAlign(Paint.Align.CENTER);

        speed_canvas.drawRect(view_width - view_height/4, view_height/2, view_width, view_height,mPaint[0]);

        float startY = view_height/2 + (view_height/2) * (1-speed_rate);
        speed_canvas.drawRect(view_width - view_height/4, startY, view_width, view_height,mPaint[1]);

        speed_canvas.drawText(String.valueOf((int)(speed_rate * 100)),((view_width-view_height/4) + view_width)/2, view_height*3/4, mPaint[2]);
        speed_canvas.drawText("Speed",((view_width-view_height/4) + view_width)/2,view_height*3/4-50,mPaint[2]);
    }

    public void drawRPY(Canvas canvas){
        Canvas RPY_canvas = canvas;
//        Canvas canvas2 = new Canvas();

        Bitmap drone = BitmapFactory.decodeResource(context.getResources(),R.drawable.hex_drone);
        RPY_canvas.drawBitmap(drone,view_width/2 - drone.getWidth()/2, view_height*2/3 - drone.getHeight()/2,new Paint());
        mPaint[0] = new Paint();
        mPaint[0].setStyle(Paint.Style.STROKE);
        mPaint[0].setStrokeWidth(10.0f);

        mPaint[1] = new Paint();
        mPaint[1].setStrokeWidth(5.0f);
        mPaint[1].setColor(Color.RED);

        mPaint[2] = new Paint();
        mPaint[2].setStrokeWidth(10.0f);
        mPaint[2].setColor(Color.YELLOW);

        mPaint[3] = new Paint();
        mPaint[3].setTextSize(10);
        mPaint[3].setStrokeWidth(5);

        float cx1 = view_width/3;
        float cx2 = view_width*2/3;
        float cy = view_height/6+10;
        float radius = view_height/6;



        RPY_canvas.drawLine(cx1-radius/5,cy, cx1+radius/5,cy,mPaint[3]);
        RPY_canvas.drawLine(cx1-radius/5,cy+radius/3, cx1+radius/5,cy+radius/3,mPaint[3]);
        RPY_canvas.drawLine(cx1-radius/5,cy-radius/3, cx1+radius/5,cy-radius/3,mPaint[3]);
        RPY_canvas.drawLine(cx1-radius/5,cy+radius*2/3, cx1+radius/5,cy+radius*2/3,mPaint[3]);
        RPY_canvas.drawLine(cx1-radius/5,cy-radius*2/3, cx1+radius/5,cy-radius*2/3,mPaint[3]);

        float startX1 = (float)(cx1 + radius*Math.cos(RPY_array[0]*3.141592/180));
        float startY1 = (float)(cy + radius*Math.sin(RPY_array[0]*3.141592/180));
        float endX1 = (float)(cx1 + radius*Math.cos((RPY_array[0]+180)*3.141592/180));
        float endY1 = (float)(cy + radius*Math.sin((RPY_array[0]+180)*3.141592/180));

        RPY_canvas.drawLine(startX1,startY1,endX1,endY1,mPaint[1]);
        RPY_canvas.drawLine(cx2-radius,cy,cx2+radius,cy,mPaint[1]);

        float currentY = (-1)*RPY_array[1]*radius/90;
        float currentstartX = (float)(cx1 - Math.sqrt(Math.pow(radius,2) - Math.pow(currentY,2)));
        float currentendX = (float)(cx1 + Math.sqrt(Math.pow(radius,2) - Math.pow(currentY,2)));
        RPY_canvas.drawLine(currentstartX, cy+currentY, currentendX,cy+currentY,mPaint[2]);

        RPY_canvas.drawCircle(cx1,cy,radius,mPaint[0]);
        RPY_canvas.drawCircle(cx2,cy,radius,mPaint[0]);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getX() > (view_width - view_height/4) && event.getX() < view_width){
            if(event.getY() > view_height/2 && event.getY() < view_height){
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    isSpeedSetting = true;
                    float speedY = event.getY() - view_height/2 ;
                    speedY = 1- (speedY / (view_height/2));
                    speed_rate = speedY;
                }
//
            }

        }
        if(isSpeedSetting){
//            if(event.getX() > (view_width - view_height/4) && event.getX() < view_width){
                if(event.getAction() == MotionEvent.ACTION_MOVE){
                    if(event.getY() > view_height/2 && event.getY() < view_height){
                        float speedY = event.getY() - view_height/2 ;
                        speedY = 1- (speedY / (view_height/2));
                        speed_rate = speedY;
                    }
                    else if(event.getY() < view_height/2){
                        speed_rate = 1;
                    }
                }

//            }


            if(event.getAction() == MotionEvent.ACTION_UP){
                if(event.getX() > (view_width - view_height/4) && event.getX() < view_width){
                     if(event.getY() > view_height/2 && event.getY() < view_height){
                         float speedY = event.getY() - view_height/2 ;
                        speedY = 1- (speedY / (view_height/2));
                        speed_rate = speedY;
                        isSpeedSetting = false;
                    }
                }
                else if(event.getY() < view_height/2){
                    speed_rate = 1;
                    isSpeedSetting = false;

                }
            }

        }

        return true;
    }

    public float getSpeedRate(){
        return speed_rate;
    }

    public static final int RPY = 0;
    public static final int TREAM =1;

    private float[] RPY_array = new float[3];
    private int[] tream_array = new int[3];

    private Handler ViewHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch(msg.what){
                case RPY :
                    RPY_array = (float[])msg.obj;
                    break;

                case TREAM :
                    tream_array = (int[])msg.obj;
                    break;

            }
        }
    };

    public Handler getViewHandler(){
        return ViewHandler;
    }

}