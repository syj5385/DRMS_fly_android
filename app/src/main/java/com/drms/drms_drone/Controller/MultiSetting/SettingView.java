package com.drms.drms_drone.Controller.MultiSetting;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.drms.drms_drone.MultiData;


/**
 * Created by jjunj on 2017-11-21.
 */

public class SettingView extends View {

    protected MultiData mspdata;
    protected Context mContext;
    protected Activity mActivity;

    protected float canvasWidth, canvasHeight;
    protected float x,y;

    public SettingView(Context context, Activity mActivity) {
        super(context);
        this.mContext = context;
        this.mActivity = mActivity;

        mspdata = (MultiData)mActivity.getApplication();
    }


}
