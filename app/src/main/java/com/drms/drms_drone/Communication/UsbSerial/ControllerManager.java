package com.drms.drms_drone.Communication.UsbSerial;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by comm on 2018-04-18.
 */

public class ControllerManager {

    private Context context;

    private UsbManager usbmanager;
    private UsbDevice device;


    public ControllerManager(Context context) {
        this.context = context;

    }

    private void findSerialPortDevice(){
        HashMap<String,UsbDevice> usbDevices = usbmanager.getDeviceList();
        if(!usbDevices.isEmpty()){
            for(Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()){
                device =entry.getValue();
                int deviceVID = device.getVendorId();
                int devicePID = device.getProductId();

                Toast.makeText(context,"ID : " + deviceVID + "\nvid : " + deviceVID,Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(context,"USB empty",Toast.LENGTH_SHORT).show();
        }
    }

}
