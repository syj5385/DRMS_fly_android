package com.drms.drms_drone.Protocol.STK500v1;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.drms.drms_drone.Communication.ClassicBluetooth.BluetoothService;
import com.drms.drms_drone.MultiData;
import com.drms.drms_drone.Upload.UploadManager;
import com.drms.drms_drone.Upload.UploadSelectedActivity;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;




/**
 * Created by JJun on 2017-06-07.
 */


public class STK500v1 {

    private static final int ORIENT_COMMAND = 20;
    private static final int RECEIVED_MESSAGE = 21;
    private static final int UPLOADPROGRESS = 22;
    private static final int UPLOAD_STATE = 23;

    private static final String TAG = "STK500";
    public static final int MODE_REQUEST = 1;

    private OutputStream outstream;
    private InputStream inputstream;
    private Handler mHandler;
    private Activity mActivity;
    private BluetoothService bluetoothService;
    private Hex hexParser;

    long startTime, endTime;

    private ConstantsStk500v1 conststk500;

    private int bytesToLoad = 0;
    private int hexPosition = 0;
    private int complete_size =0 ;

    public boolean running = true;

    private byte[] command_set;

    //////////////////boolean check///////////////////////

    boolean programMode_entered = false;
    boolean loadAddress_success = false;
    boolean programMode_left = false;
    boolean programPage_success = false;

    boolean loadAddress_each_success = false;
    boolean programPage_each_success = false;

    int triesToleaveProgrammode = 0 ;

    private MultiData mspdata;
    private int request;


    public Handler stk500_handler = new Handler() {
        boolean toast_state = false;

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case RECEIVED_MESSAGE:
                    byte orient_command = (byte) msg.arg1;
                    byte[] received_message = (byte[]) msg.obj;

                    switch (orient_command) {
                        case 80: // 0x50  enterProgramMode
                            if(received_message[0] == ConstantsStk500v1.STK_INSYNC)
                                programMode_entered = true;
                            else if(received_message[0] == ConstantsStk500v1.STK_NOSYNC){
                                programMode_entered = false;
                            }
                            break;

                        case 81: // 0x51 leaveProgramMode
                            if(received_message[0] == ConstantsStk500v1.STK_INSYNC)
                                programMode_left = true;
                            else if(received_message[0] == ConstantsStk500v1.STK_NOSYNC){
                                programMode_left = false;
                            }
                            break;

                        case 85 : // 0x55 LoadAddress
                            if(received_message[0] == ConstantsStk500v1.STK_INSYNC){
                                loadAddress_success = true;
                                loadAddress_each_success = true;
                            }
                            else if(received_message[0] == ConstantsStk500v1.STK_NOSYNC){
                                loadAddress_success = false;
                                loadAddress_each_success = false;
                            }
                            else{
                                loadAddress_success = false;
                                loadAddress_each_success = false;
                            }

                            break;

                        case 100 : // 0x64 Program Page
                            if(received_message[0] == ConstantsStk500v1.STK_INSYNC){
                                programPage_success = true;
                                programPage_each_success = true;
                            }
                            else if(received_message[0] == ConstantsStk500v1.STK_NOSYNC){
                                programPage_success = false;
                                programPage_each_success = false;
                            }
                            else{
                                programPage_success = false;
                                programPage_each_success = false;
                            }
                            break;
                    }

                    break;
            }
        }
    };

    public Handler getStk500_handler(){
        return stk500_handler;
    }

    public STK500v1(Activity mActivity, BluetoothService bluetoothservice, Handler mHandler, int request) {
//        this.outstream = outstream;
//        this.inputstream = inputstream;
        this.bluetoothService = bluetoothservice;
        this.mHandler = mHandler;
        this.mActivity = mActivity;
        this.request = request;
        mspdata = (MultiData)mActivity.getApplication();
        if(request == UploadSelectedActivity.REQUEST_QUAD_GY521) {
            command_set = mspdata.getQUAD_GY521_EEPROM();
        }
        else if(request == UploadSelectedActivity.REQUEST_HEX_GY521){
            command_set = mspdata.getHEX_GY521_EEPROM();
        }

        conststk500 = new ConstantsStk500v1();
    }

    public boolean programHexFile(byte[] binaryFile, int numberOfBytes, boolean checkWrittenData) {
        hexParser = new Hex(binaryFile);

        complete_size = hexParser.getDataSize();
        bytesToLoad = numberOfBytes;

        boolean success = false;
        startTime = System.currentTimeMillis();
        endTime = System.currentTimeMillis();
        boolean entered = false;


        // sendMesasge to MainHandler what this Activity send
//        mHandler.obtainMessage(ORIENT_COMMAND,conststk500.STK_ENTER_PROGMODE,-1).sendToTarget();
        while (endTime - startTime <= 1500) {
            if(enterProgramMode()){
                break;
            }
            endTime = System.currentTimeMillis();
            if(endTime - startTime > 1500){
                mHandler.obtainMessage(UPLOAD_STATE,2,-1).sendToTarget();
                return false;
            }
        }


        if (programMode_entered) {
            Log.d(TAG,"Entering the Programm Mode" + "programMode_entered : "+ String.valueOf(programMode_entered));
            mHandler.obtainMessage(UPLOAD_STATE, UploadManager.UPLOAD_START,-1).sendToTarget();
            if(initializeEEPROM()) {
                if (writeHexFile()) {
                    mHandler.obtainMessage(UPLOAD_STATE, UploadManager.UPLOAD_END,-1).sendToTarget();
                    if(request == UploadSelectedActivity.REQUEST_QUAD_GY521 || request == UploadSelectedActivity.REQUEST_HEX_GY521)
                        writeDataOnEEPROM();
                    Log.d(TAG, "Write Command data on Arduino EEPROM");
                }
            }
        }
        else if(!programMode_entered && (endTime - startTime <= 1500)){
            Log.d(TAG,"Entering the Programm Mode" + "programMode_entered : "+ String.valueOf(programMode_entered));
            return false;
        }

//        Log.d(TAG,"Finished the writeHexFile" + " programPage_success : " + String.valueOf(programPage_success));


        if(programMode_entered) {
            while (!leaveProgramMode() && triesToleaveProgrammode < 5) {
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                }

                triesToleaveProgrammode++;
            }

            Log.d(TAG, "leaving the Programm Mode" + "programMode_left : " + String.valueOf(programMode_left));

            if (programMode_entered && loadAddress_success && programPage_success && programMode_left) {
                success = true;
            } else {
                success = false;

                mHandler.obtainMessage(UPLOAD_STATE,-1,-1).sendToTarget();
            }
        }

        else{
            success = false;
        }

        return success;
    }

    // 1st step  Enter Program mode
    private boolean enterProgramMode() {
        boolean entered = false;
        byte[] command = new byte[]{
                conststk500.STK_ENTER_PROGMODE, conststk500.CRC_EOP
        };

        mHandler.obtainMessage(ORIENT_COMMAND,conststk500.STK_ENTER_PROGMODE,-1).sendToTarget();

        bluetoothService.write(command,MODE_REQUEST);
        Log.d(TAG, "write enterProgramMode command");
        try{
            Thread.sleep(500);
        }catch (InterruptedException e){}

        if(programMode_entered){
            entered = true;
        }
        else{
            entered = false;
        }

        return entered;
    }

    // leave the program mode
    private boolean leaveProgramMode(){
        boolean success = false;

        byte[] command = new byte[]{
                conststk500.STK_LEAVE_PROGMODE, conststk500.CRC_EOP
        };

        mHandler.obtainMessage(ORIENT_COMMAND,conststk500.STK_LEAVE_PROGMODE,-1).sendToTarget();
        bluetoothService.write(command,MODE_REQUEST);
        Log.d(TAG,"write LeaveProgramMode command");

        try{
            Thread.sleep(300);
        }catch (InterruptedException e){}

        if(programMode_left)
            return true;
        else
            return false;
    }

    int uploadFileTries = 0;

    private boolean writeHexFile(){
        boolean success = false;
        hexPosition = 0;

        while(hexPosition < hexParser.getDataSize() && running){

            programPage_each_success = false;
            loadAddress_each_success = false;

            mHandler.obtainMessage(UPLOAD_STATE,hexPosition, hexParser.getDataSize()).sendToTarget();
            Log.d(TAG,"hexPosition : " + String.valueOf(hexPosition));
            if(uploadFileTries > 10) return false;

            byte[] tempArray = hexParser.getHexLine(hexPosition,bytesToLoad);
            Log.d(TAG,"tempArray size : " + String.valueOf(tempArray.length));

            if(tempArray.length == 0)
                return true;  // no more data to upload

            for(int i=0 ; i<5 ; i++){
                if(loadAddress(hexPosition)){
                    Log.d(TAG,"Upload files");
                    break;
                }
                else{
                    uploadFileTries++;
                }
            }

            if(programPage(true,tempArray)){ // true : write Flashmemory / false : write EEPROM
                Log.d(TAG,"temp_array size : " + tempArray.length);
                hexPosition += tempArray.length;
                success = true;
            }
            else{
                success = false;
                break;
            }

            Log.d(TAG,"Finished the writeHexFile" + " programPage_success : " + String.valueOf(programPage_success));

        }

//        byte[] data = {1,1,1,1};
//        for(int i=0; i<4; i++){
//            if(loadAddress(i)){
//                programPage(false,data);
//                Log.d(TAG, "programEEPROM");
//            }
//            else{
//                Log.d(TAG,"failed to Load Address");
//            }
//
//        }
        return success;
    }



    boolean initializeEEPROM(){
        boolean success = false;

        int[] init_EEPROM = new int[1024];
        for(int i=0; i<init_EEPROM.length; i++)
            init_EEPROM[i] = 255;

        byte[] init_EEPROM_BYTE = new byte[init_EEPROM.length];
        int EEPROM_index = 0;
        for(int i=0; i<init_EEPROM_BYTE.length ; i++){
            init_EEPROM_BYTE[i] = (byte)(char)(init_EEPROM[i] & 0xff);
        }

        int data_set_index = 0;

        int data_set_number = init_EEPROM_BYTE.length / 256 + 1;

        int index=0;

        ArrayList<byte[]> command = new ArrayList<byte[]>();

        for(int i=0; i<data_set_number; i++){
            if(i == (data_set_number -1)){
                byte[] temp_array = new byte[init_EEPROM_BYTE.length % 256];
                for(int j=0; j<init_EEPROM_BYTE.length % 256 ; j++){
                    temp_array[j] = init_EEPROM_BYTE[index++];
                }
                command.add(temp_array);
            }
            else{
                byte[] temp_array= new byte[256];
                for(int j=0; j<256; j++){
                    temp_array[j] = init_EEPROM_BYTE[index++];

                }
                command.add(temp_array);
            }
        }

        int address_allocation =0;
        int cycle_index = 0;

        while(cycle_index < data_set_number  && running){
            programPage_each_success = false;
            loadAddress_each_success = false;
            Log.d(TAG,"init -> cycle_index : " + cycle_index + "/ data_num : " + data_set_number);
            for(int i=0 ; i<5 ; i++){
                if(loadAddress(address_allocation)){
                    Log.d(TAG,"Upload files");
                    break;
                }
            }

            if(programPage(false,command.get(cycle_index))){ // true : write Flashmemory / false : write EEPROM
                address_allocation += 256;
                success = true;
            }

            cycle_index++;

            Log.w(TAG,"Finished Write EEPROM data");

        }
        return success;
    }
    private boolean loadAddress(int address) {
        //Split integer address into two bytes address
        byte[] tempAddr = packTwoBytes(address / 2);

        byte[] loadAddr = new byte[4];

        loadAddr[0] = ConstantsStk500v1.STK_LOAD_ADDRESS;
        loadAddr[1] = tempAddr[1];
        loadAddr[2] = tempAddr[0];
        loadAddr[3] = ConstantsStk500v1.CRC_EOP;


        mHandler.obtainMessage(ORIENT_COMMAND,conststk500.STK_LOAD_ADDRESS,-1).sendToTarget();
        bluetoothService.write(loadAddr,MODE_REQUEST);

//        try{
//            Thread.sleep(190);
//        }catch (InterruptedException e){
//
//        }
        int count = 0;
        while(!loadAddress_each_success){
            try{
                if(count++ > 1000){
                    break;
                }
                Thread.sleep(1);

            }catch (InterruptedException e){};
        };

        if(loadAddress_each_success){
            return true;
        }
        else {
            return false;
        }
    }



    private byte[] packTwoBytes(int integer) {
        byte[] bytes = new byte[2];
        //store the 8 least significant bits
        bytes[1] = (byte) (integer & 0xFF);
        //store the next 8 bits
        bytes[0] = (byte) ((integer >> 8) & 0xFF);
        return bytes;
    }

    private boolean programPage(boolean writeFlash, byte[] data) {
        byte[] programPage = new byte[5+data.length];
        byte memtype;

        programPage[0] = ConstantsStk500v1.STK_PROG_PAGE;

        programPage[1] = (byte) ((data.length >> 8) & 0xFF);
        programPage[2] = (byte) (data.length & 0xFF);

        // Write flash
        if (writeFlash) {
            memtype = (byte)'F';
        }
        // Write EEPROM
        else {
            // This is not implemented in optiboot
//            throw new IllegalArgumentException("Does not support writing to EEPROM.");

			memtype = (byte)'E';
        }
        programPage[3] = memtype;

        //Put all the data together with the rest of the command
        for (int i = 0; i < data.length; i++) {
            programPage[i+4] = data[i];
        }

        programPage[data.length+4] = ConstantsStk500v1.CRC_EOP;

        // Send bytes
        mHandler.obtainMessage(ORIENT_COMMAND,conststk500.STK_PROG_PAGE,-1).sendToTarget();
        bluetoothService.write(programPage,MODE_REQUEST);

        int count = 0;
        while(!programPage_each_success){
            try{
                Thread.sleep(1);
                if(count++ > 1000)
                    break;
            }catch (InterruptedException e){};
        }

        if(programPage_success){
            return true;
        }
        else{
            return false;
        }

    }

    public int getDataSize(){

        return complete_size;
    }

    public int getHexPosition(){
        return hexPosition;
    }

    private boolean writeDataOnEEPROM(){
        boolean success = false;
        int data_set_index = 0;
        int data_set_number = command_set.length / 256 + 1;

        int index=0;

        ArrayList<byte[]> command = new ArrayList<byte[]>();

        for(int i=0; i<data_set_number; i++){
            if(i == (data_set_number -1)){
                byte[] temp_array = new byte[command_set.length % 256];
                for(int j=0; j<command_set.length % 256 ; j++){
                    temp_array[j] = command_set[index++];
                }
                command.add(temp_array);
            }
            else{
                byte[] temp_array= new byte[256];
                for(int j=0; j<256; j++){
                    temp_array[j] = command_set[index++];

                }
                command.add(temp_array);
            }
        }

        int address_allocation =0;
        int cycle_index = 0;

        while(cycle_index < data_set_number  && running){
            programPage_each_success = false;
            loadAddress_each_success = false;
            Log.w(TAG,"cycle index : " + cycle_index + ", data_Set_number " + data_set_number);
            for(int i=0 ; i<5 ; i++){
                if(loadAddress(address_allocation)){
                    Log.d(TAG,"Upload files");
                    break;
                }
            }

            if(programPage(false,command.get(cycle_index))){ // true : write Flashmemory / false : write EEPROM
                Log.d(TAG,"Success Write data on EEPROM");
                address_allocation += 256;
                success = true;
            }
            cycle_index++;

            Log.w(TAG,"Finished Write EEPROM data");

        }

        return success;
    }



}
