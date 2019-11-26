package com.drms.drms_drone.Upload;

import android.app.Activity;

import com.drms.drms_drone.Communication.ClassicBluetooth.BluetoothService;


/**
 * Created by jjunj on 2017-11-10.
 */

public class DroneUploadManager {

    private BluetoothService mBluetoothService;
    private Activity mActivity;

    public DroneUploadManager(Activity mActivity, BluetoothService mBluetoothService){
        this.mBluetoothService = mBluetoothService;
        this.mActivity = mActivity;

    }
}
