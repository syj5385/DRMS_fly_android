package com.drms.drms_drone.Controller.MultiSetting;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.drms.drms_drone.R;

import java.text.DecimalFormat;


/**
 * Created by jjunj on 2017-11-20.
 */

public class MyDroneSettingView extends SettingView {

    private static final String TAG = "MyDroneSettingView";

    private Path[] settingPath = new Path[9];
    private float[] positionXGraph = new float[3];
    private float[] positionYGraph = new float[9];

    public MyDroneSettingView(Context context, Activity activity) {
        super(context,activity);


        for(int i=0; i<settingPath.length; i++){
            settingPath[i] = new Path();
        }


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(canvasWidth != canvas.getWidth() || canvasHeight !=canvas.getHeight()){
            canvasWidth = canvas.getWidth();
            canvasHeight = canvas.getHeight();

            x = canvasWidth/65;
            y = canvasHeight/40;

            settingPath[0].moveTo(2*x, 30*y);   // acc x
            settingPath[1].moveTo(2*x, 30*y);   // acc y
            settingPath[2].moveTo(2*x, 30*y);   // acc z

            settingPath[3].moveTo(23*x, 30*y);
            settingPath[4].moveTo(23*x, 30*y);
            settingPath[5].moveTo(23*x, 30*y);

            settingPath[6].moveTo(44*x, 30*y);
            settingPath[7].moveTo(44*x, 30*y);
            settingPath[8].moveTo(44*x, 30*y);

            positionXGraph[0] = 2*x;
            positionXGraph[1] = 23*x;
            positionXGraph[2] = 44*x;
            positionYGraph[0] = 30*y;
            positionYGraph[1] = 30*y;
            positionYGraph[2] = 30*y;
            positionYGraph[3] = 30*y;
            positionYGraph[4]  = 30*y;
            positionYGraph[5] = 30 * y;
            positionYGraph[6] = 30*y;
            positionYGraph[7]  = 30*y;
            positionYGraph[8] = 30 * y;

            Log.d(TAG,"x : " +  x + "/ y : " + y );
        }

        Paint[] myDronePaint = new Paint[10];

        // My drone
        Bitmap temp = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.quad_drone);
        float scale = 10*y / temp.getHeight();
        Bitmap myDrone = Bitmap.createScaledBitmap(temp, (int)(temp.getWidth()*scale), (int)(temp.getHeight()*scale),true);
        canvas.drawBitmap(myDrone,5*x,5*y, null);


        // Acc, Gyro, Orientation
        myDronePaint[0] = new Paint();
        myDronePaint[0].setStrokeWidth(7);
        myDronePaint[0].setColor(mContext.getResources().getColor(R.color.mainBasicColor));
        myDronePaint[0].setStyle(Paint.Style.STROKE);

        canvas.drawRect(20*x, 3*y, 30*x, 17*y, myDronePaint[0]);
        canvas.drawRect(35*x, 3*y, 45*x, 17*y, myDronePaint[0]);
        canvas.drawRect(50*x, 3*y, 60*x, 17*y, myDronePaint[0]);

        myDronePaint[1] = new Paint();
        myDronePaint[1].setStrokeWidth(8);
        myDronePaint[1].setColor(mContext.getResources().getColor(R.color.mainTopic_2));
        myDronePaint[1].setTextAlign(Paint.Align.CENTER);
        myDronePaint[1].setTextSize(3*y);

        canvas.drawText("Acc",25*x, (float)(6.5*y),myDronePaint[1]);
        canvas.drawText("Gyro",40*x, (float)(6.5*y),myDronePaint[1]);
        canvas.drawText("Orient",55*x, (float)(6.5*y),myDronePaint[1]);

        myDronePaint[1].setTextSize(2*y);
        myDronePaint[1].setColor(mContext.getResources().getColor(R.color.mainTopic_1));

        canvas.drawText("x", (float)(21.5*x), (float)(8.5*y+myDronePaint[1].getTextSize()/2),myDronePaint[1]);
        canvas.drawText("y", (float)(21.5*x), (float)(11.5*y+myDronePaint[1].getTextSize()/2),myDronePaint[1]);
        canvas.drawText("z", (float)(21.5*x), (float)(14.5*y+myDronePaint[1].getTextSize()/2),myDronePaint[1]);

        canvas.drawText("x", (float)(36.5*x), (float)(8.5*y+myDronePaint[1].getTextSize()/2),myDronePaint[1]);
        canvas.drawText("y", (float)(36.5*x), (float)(11.5*y+myDronePaint[1].getTextSize()/2),myDronePaint[1]);
        canvas.drawText("z", (float)(36.5*x), (float)(14.5*y+myDronePaint[1].getTextSize()/2),myDronePaint[1]);

        canvas.drawText("R", (float)(51.5*x), (float)(8.5*y+myDronePaint[1].getTextSize()/2),myDronePaint[1]);
        canvas.drawText("P", (float)(51.5*x), (float)(11.5*y+myDronePaint[1].getTextSize()/2),myDronePaint[1]);
        canvas.drawText("Y", (float)(51.5*x), (float)(14.5*y+myDronePaint[1].getTextSize()/2),myDronePaint[1]);

        myDronePaint[2] = new Paint();



        myDronePaint[0].setStyle(Paint.Style.FILL);
        myDronePaint[0].setColor(mContext.getResources().getColor(R.color.DrawerColor));

        canvas.drawRect(2*x, 22*y, 21*x,38*y, myDronePaint[0]);
        canvas.drawRect(23*x, 22*y, 42*x,38*y, myDronePaint[0]);
        canvas.drawRect(44*x, 22*y, 63*x,38*y, myDronePaint[0]);

        myDronePaint[0].setStrokeWidth(10);
        myDronePaint[0].setStyle(Paint.Style.STROKE);
        myDronePaint[0].setColor(mContext.getResources().getColor(R.color.mainBasicColor));
        canvas.drawRect(2*x, 22*y, 21*x,38*y, myDronePaint[0]);
        canvas.drawRect(23*x, 22*y, 42*x,38*y, myDronePaint[0]);
        canvas.drawRect(44*x, 22*y, 63*x,38*y, myDronePaint[0]);

        myDronePaint[2] = new Paint();
        myDronePaint[2].setStrokeWidth(6);
        myDronePaint[2].setTextSize((float)(1.5*y));
        myDronePaint[2].setColor(mContext.getResources().getColor(R.color.mainBasicColor));
        myDronePaint[2].setTextAlign(Paint.Align.CENTER);

        DecimalFormat form = new DecimalFormat("#.##");
        canvas.drawText(String.valueOf(form.format((float)mspdata.getIMUdata()[0]/50)),(float)(26.5*x),(float)(8.5*y)+myDronePaint[2].getTextSize()/2,myDronePaint[2] );
        canvas.drawText(String.valueOf(form.format((float)mspdata.getIMUdata()[1]/50)),(float)(26.5*x),(float)(11.5*y)+myDronePaint[2].getTextSize()/2,myDronePaint[2] );
        canvas.drawText(String.valueOf(form.format((float)mspdata.getIMUdata()[2]/50)),(float)(26.5*x),(float)(14.5*y)+myDronePaint[2].getTextSize()/2,myDronePaint[2] );

        canvas.drawText(String.valueOf(mspdata.getIMUdata()[3]),(float)(41.5*x),(float)(8.5*y)+myDronePaint[2].getTextSize()/2,myDronePaint[2] );
        canvas.drawText(String.valueOf(mspdata.getIMUdata()[4]),(float)(41.5*x),(float)(11.5*y)+myDronePaint[2].getTextSize()/2,myDronePaint[2] );
        canvas.drawText(String.valueOf(mspdata.getIMUdata()[5]),(float)(41.5*x),(float)(14.5*y)+myDronePaint[2].getTextSize()/2,myDronePaint[2] );

        canvas.drawText(String.valueOf((int)mspdata.getAttitudeData()[0]),(float)(56.5*x),(float)(8.5*y)+myDronePaint[2].getTextSize()/2,myDronePaint[2] );
        canvas.drawText(String.valueOf((int)mspdata.getAttitudeData()[1]),(float)(56.5*x),(float)(11.5*y)+myDronePaint[2].getTextSize()/2,myDronePaint[2] );
        canvas.drawText(String.valueOf((int)mspdata.getAttitudeData()[2]),(float)(56.5*x),(float)(14.5*y)+myDronePaint[2].getTextSize()/2,myDronePaint[2] );

        myDronePaint[2].setColor(mContext.getResources().getColor(R.color.mainTopic_2));
        canvas.drawText("Acc",12*x, 22*y-myDronePaint[2].getTextSize()/2,myDronePaint[2]);
        canvas.drawText("Gyro",33*x, 22*y-myDronePaint[2].getTextSize()/2,myDronePaint[2]);
        canvas.drawText("Orient",54*x, 22*y-myDronePaint[2].getTextSize()/2,myDronePaint[2]);

        myDronePaint[3] = new Paint();  // x;
        myDronePaint[4] = new Paint(); // y
        myDronePaint[5] = new Paint(); //z

        myDronePaint[3].setColor(mContext.getResources().getColor(R.color.mainTextColor));
        myDronePaint[4].setColor(mContext.getResources().getColor(R.color.rollColor));
        myDronePaint[5].setColor(mContext.getResources().getColor(R.color.pitchColor));

        myDronePaint[3].setStrokeWidth(5);
        myDronePaint[3].setStyle(Paint.Style.STROKE);

        myDronePaint[4].setStrokeWidth(5);
        myDronePaint[4].setStyle(Paint.Style.STROKE);

        myDronePaint[5].setStrokeWidth(5);
        myDronePaint[5].setStyle(Paint.Style.STROKE);

        if(x != 0 && y != 0 && (mspdata.getIMUdata()[0] != 0 || mspdata.getIMUdata()[1] != 0 || mspdata.getIMUdata()[2] != 0)) {

            float[] acc_temp = {
                    positionYGraph[3] - mspdata.getIMUdata()[0]/5,
                    positionYGraph[4] - mspdata.getIMUdata()[1]/5,
                    positionYGraph[5] - (mspdata.getIMUdata()[2]-490)/5,
            };

            for(int i=0; i<acc_temp.length; i++){
                if(acc_temp[i] < 22*y + myDronePaint[0].getStrokeWidth()/2) acc_temp[i] = 22*y - myDronePaint[0].getStrokeWidth()/2;
                if(acc_temp[i] > 38*y - myDronePaint[0].getStrokeWidth()/2) acc_temp[i] = 38*y - myDronePaint[0].getStrokeWidth()/2;
            }

            settingPath[0].lineTo(positionXGraph[0], acc_temp[0]);
            settingPath[1].lineTo(positionXGraph[0], acc_temp[1]);
            settingPath[2].lineTo(positionXGraph[0] += 3, acc_temp[2]);

            if (positionXGraph[0] >= 21 * x) {
                settingPath[0].reset();
                settingPath[0].moveTo(2 * x, 30 * y);

                settingPath[1].reset();
                settingPath[1].moveTo(2 * x, 30 * y);

                settingPath[2].reset();
                settingPath[2].moveTo(2 * x, 30 * y);

                positionXGraph[0] = 2 * x;
            }
            canvas.drawPath(settingPath[0], myDronePaint[3]);
            canvas.drawPath(settingPath[1], myDronePaint[4]);
            canvas.drawPath(settingPath[2], myDronePaint[5]);

            float[] gyro_temp = {
                    positionYGraph[3] - mspdata.getIMUdata()[3]/7,
                    positionYGraph[4] - mspdata.getIMUdata()[4]/7,
                    positionYGraph[5] - mspdata.getIMUdata()[5]/7,
            };

            for(int i=0; i<gyro_temp.length; i++){
                if(gyro_temp[i] < 22*y + myDronePaint[0].getStrokeWidth()/2) gyro_temp[i] = 22*y - myDronePaint[0].getStrokeWidth()/2;
                if(gyro_temp[i] > 38*y - myDronePaint[0].getStrokeWidth()/2) gyro_temp[i] = 38*y - myDronePaint[0].getStrokeWidth()/2;
            }

            settingPath[3].lineTo(positionXGraph[1],gyro_temp[0]);
            settingPath[4].lineTo(positionXGraph[1],gyro_temp[1]);
            settingPath[5].lineTo(positionXGraph[1]+=3,gyro_temp[2]);

            if (positionXGraph[1] >= 42* x) {
                settingPath[3].reset();
                settingPath[3].moveTo(23 * x, 30 * y);

                settingPath[4].reset();
                settingPath[4].moveTo(23 * x, 30 * y);

                settingPath[5].reset();
                settingPath[5].moveTo(23 * x, 30 * y);

                positionXGraph[1] = 23 * x;
            }

            canvas.drawPath(settingPath[3], myDronePaint[3]);
            canvas.drawPath(settingPath[4], myDronePaint[4]);
            canvas.drawPath(settingPath[5], myDronePaint[5]);

            settingPath[6].lineTo(positionXGraph[2],positionYGraph[3] - mspdata.getAttitudeData()[0]);
            settingPath[7].lineTo(positionXGraph[2],positionYGraph[4] - mspdata.getAttitudeData()[1]);
            settingPath[8].lineTo(positionXGraph[2]+=3,positionYGraph[5] - mspdata.getAttitudeData()[2]);
            if (positionXGraph[2] >= 63* x) {
                settingPath[6].reset();
                settingPath[6].moveTo(44 * x, 30 * y);

                settingPath[7].reset();
                settingPath[7].moveTo(44 * x, 30 * y);

                settingPath[8].reset();
                settingPath[8].moveTo(44 * x, 30 * y);

                positionXGraph[2] = 44 * x;
            }

            canvas.drawPath(settingPath[6], myDronePaint[3]);
            canvas.drawPath(settingPath[7], myDronePaint[4]);
            canvas.drawPath(settingPath[8], myDronePaint[5]);

        }




    }
}
