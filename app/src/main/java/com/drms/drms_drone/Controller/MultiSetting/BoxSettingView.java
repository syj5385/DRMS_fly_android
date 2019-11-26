package com.drms.drms_drone.Controller.MultiSetting;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.MotionEvent;

import com.drms.drms_drone.R;
import com.drms.drms_drone.Sound.SoundManager;


/**
 * Created by jjunj on 2017-11-21.
 */

public class BoxSettingView extends SettingView{

    private Bitmap onTemp,offTemp;
    private CheckBox[][] checkbox ;
    private SoundManager mSoundManager;

    private int BOXITEMNUM  = 0;

    public BoxSettingView(Context context, Activity mActivity) {
        super(context, mActivity);

        mSoundManager = new SoundManager(mContext);

        if(mspdata.getBoxITEM() != null) {
            BOXITEMNUM = mspdata.getBoxITEM().length;
        }
        else{
            BOXITEMNUM = 0;
        }
        checkbox = new CheckBox[BOXITEMNUM][12];

    }

    private void initializeOnOff(){
        onTemp = Bitmap.createBitmap((int)(3*y), (int)(3*y), Bitmap.Config.ARGB_8888);
        offTemp = Bitmap.createBitmap((int)(3*y),(int)(3*y), Bitmap.Config.ARGB_8888);

        Canvas onCanvas = new Canvas(onTemp);
        Paint onPaint = new Paint();
        onPaint.setStrokeWidth(8);
        onPaint.setColor(mContext.getResources().getColor(R.color.mainBasicColor));
        onPaint.setStyle(Paint.Style.STROKE);
        onCanvas.drawCircle(onCanvas.getWidth()/2,onCanvas.getHeight()/2,onCanvas.getHeight()/2-onPaint.getStrokeWidth()/2,onPaint);

        Canvas offCanvas = new Canvas(offTemp);
        Paint offPaint = new Paint();
        offPaint.setStrokeWidth(8);
        offPaint.setColor(mContext.getResources().getColor(R.color.mainTopic_1));
        offPaint.setStyle(Paint.Style.STROKE);
        offCanvas.drawCircle(onCanvas.getWidth()/2,onCanvas.getHeight()/2,onCanvas.getHeight()/2-onPaint.getStrokeWidth()/2,offPaint);
        offPaint.setStrokeWidth(15);
        offCanvas.drawPoint(offCanvas.getWidth()/2,offCanvas.getHeight()/2,offPaint);

        float Xtemp = 10*x;
        float Ytemp = 7*y ;

        for(int i=0; i<BOXITEMNUM; i++){
            for(int j=0; j<12 ; j++){
                checkbox[i][j] = new CheckBox(Xtemp,Ytemp,mspdata.getCheckboxData()[i][j]);
                if(j%3 != 2){
                    Xtemp += 4*x;
                }
                else{
                    Xtemp += 6*x;
                }
            }
            Xtemp = 10*x;
            Ytemp += 4*y;
        }

        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(canvasWidth != canvas.getWidth() || canvasHeight != canvas.getHeight()){
            canvasWidth = canvas.getWidth();
            canvasHeight = canvas.getHeight();
            x = canvasWidth / 65;
            y = canvasHeight / 40;
            initializeOnOff();
        }

        for(int i=0; i<BOXITEMNUM; i++){
            for(int j=0; j<12; j++){
                canvas.drawBitmap(checkbox[i][j].getDisplayed_check(),checkbox[i][j].getPositionX(),checkbox[i][j].getPositionY(),null);
            }
        }

        Paint[] boxPaint = new Paint[10];

        boxPaint[0] = new Paint();
        boxPaint[0].setColor(mContext.getResources().getColor(R.color.mainTopic_2));
        boxPaint[0].setStrokeWidth(7);
        boxPaint[0].setTextAlign(Paint.Align.CENTER);
        boxPaint[0].setTextSize(2*y);


        if(BOXITEMNUM != 0) {
            String[] contents = new String[BOXITEMNUM];
            for(int i=0; i<BOXITEMNUM ; i++){
                contents[i] = mspdata.getBoxITEM()[i];
            }
            for (int i = 0; i < BOXITEMNUM; i++) {
                canvas.drawText(contents[i], 5 * x, ((checkbox[i][0].getPositionY() + (checkbox[i][0].getPositionY() + checkbox[i][0].getDisplayed_check().getHeight())) / 2) + boxPaint[0].getTextSize() / 2, boxPaint[0]);
            }
            String[] LMH = {"L", "M", "H"};
            for (int i = 0; i < 12; i++) {
                canvas.drawText(LMH[i % 3], ((checkbox[0][i].getPositionX() + (checkbox[0][i].getPositionX() + checkbox[0][i].getDisplayed_check().getWidth())) / 2), 6 * y, boxPaint[0]);
            }

            boxPaint[0].setTextSize(3 * y);
            boxPaint[0].setColor(mContext.getResources().getColor(R.color.splashBack));
            String[] aux = {"AUX1", "AUX2", "AUX3", "AUX4"};
            for (int i = 0; i < 4; i++) {
                canvas.drawText(aux[i], ((checkbox[0][3 * i + 1].getPositionX() + (checkbox[0][3 * i + 1].getPositionX() + checkbox[0][3 * i + 1].getDisplayed_check().getWidth())) / 2), (float) 3.3 * y, boxPaint[0]);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN) {
            for (int i = 0; i < BOXITEMNUM; i++) {
                for (int j = 0; j < 12; j++) {
                    if (event.getX() >= checkbox[i][j].getPositionX() && event.getX() <= checkbox[i][j].getPositionX() + checkbox[i][j].getDisplayed_check().getWidth()) {
                        if (event.getY() >= checkbox[i][j].getPositionY() && event.getY() <= checkbox[i][j].getPositionY() + checkbox[i][j].getDisplayed_check().getHeight()) {
                            if (checkbox[i][j].isChecked())
                                checkbox[i][j].setChecked(false);
                            else {
                                checkbox[i][j].setChecked(true);
                            }
                            invalidate();
                            mSoundManager.play(0);

                            mspdata.setEachCheckboxData(i,j,checkbox[i][j].isChecked());
                            break;
                        }
                    }
                }
            }
        }
        return true;

    }

    private class CheckBox{

        private float positionX,positionY ;
        private boolean checked;
        private Bitmap on, off;
        private Bitmap displayed_check;

        public CheckBox(float x, float y, boolean checked) {
            this.positionX= x ;
            this.positionY = y;
            this.checked = checked;

            this.on = onTemp;
            this.off = offTemp;
            if(checked)
                displayed_check = this.off;
            else
                displayed_check = this.on;
        }

        public Bitmap getDisplayed_check(){
            return displayed_check;
        }

        public float getPositionX(){
            return positionX;
        }

        public float getPositionY(){
            return positionY;
        }

        public boolean isChecked(){
            return checked;
        }

        public void setChecked(boolean checked){
            this.checked = checked;
            if(checked){
                displayed_check = off;
            }
            else{
                displayed_check = on;
            }
        }
    }
}
