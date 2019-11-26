package com.drms.drms_drone.FileManagement;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.drms.drms_drone.CustomAdapter.CustomAdapter1.CustomAdapter1;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;



/**
 * Created by jjunj on 2017-09-15.
 */

public class FileManagement {

    private static final String TAG = "FileManagement.class";
    private static final int FILEMANAGEMENT = 4;
    public static final int FINISHED_WRITE_DATA = 41;
    public static final int FINISHED_READ_DATA = 42;
    public static final int FAILED_READ_DATA = 43;
    public static final int REQUEST_SAVE_TEMP = 44;
    public static final int REQUEST_OPEN_TEMP = 45;

    private Context context;
    private ArrayList<File> myFile_list = new ArrayList<File>();

    private String BTdirPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private File myFile;

    private File[] filelist ;

    private CustomAdapter1 Adapter;

    private FileOutputStream outputStream;
    private FileInputStream inputStream;

    private Handler mHandler ;


    public FileManagement(Context context, Handler mHandler ){
        this.context = context;
        this.mHandler = mHandler;

        BTdirPath += "/DRMS(fly)";

        myFile = new File(BTdirPath);
        myFile.mkdirs();

        createBTFile();
    }


    private boolean createBTFile(){
        boolean success = false;
        String Btfile_Path = BTdirPath+"/"+"bluetoothAddr.txt";
        myFile = new File(Btfile_Path);
        if(!myFile.exists()){
            try{
                myFile.createNewFile();
                success= true;
            }catch (IOException e){
                Log.e(getClass().getSimpleName(), "IOException");
                success= false;
            }

        }
        return success;
    }


    public boolean writeBtAddressOnFile(String name, String address){
        boolean success = false;

        myFile = new File(BTdirPath + "/bluetoothAddr.txt");
        if(!myFile.exists()) {
            createBTFile();
        }

        try {
            outputStream = new FileOutputStream(myFile);
            String Bt = name + "\n" + address;

            byte[] address_temp = Bt.getBytes();

            outputStream.write(address_temp);


            success = true;
        } catch (IOException e) {
            success = false;
        }
        ;

        return success;
    }

    public String[] readBTAddress(){

        myFile = new File(BTdirPath + "/bluetoothAddr.txt");
        char[] addr = new char[(int)myFile.length()];
        if(myFile.exists()){
            if(myFile.length() > 0) {
                Log.d("FILE", BTdirPath + "bluetooth.txt exist");
                try {
                    inputStream = new FileInputStream(myFile);
                    BufferedInputStream bufferReader = new BufferedInputStream(inputStream);

                    for (int i = 0; i < myFile.length(); i++) {
                        addr[i] = (char) bufferReader.read();
                        Log.w("READ DATA", String.valueOf(addr[i]));
                    }
                    inputStream.close();
                    bufferReader.close();
                } catch (IOException e) {
                }

            }
            else{
                return null;
            }
        }
        else{
            Log.d("FILE",BTdirPath + "bluetooth.txt not exist");
        }

        String[] BT = new String[2];
        BT[0] = "";
        BT[1] = "";

        if(addr.length > 0) {
            int index = 0;
            while (addr[index] != '\n') {
                BT[0] += addr[index];
                index++;
            }

            index++;
            for (; index < addr.length; index++) {
                BT[1] += addr[index];

            }

            Log.d(TAG, "name : " + BT[0] + "\naddress : " + BT[1]);
        }


        return BT;
    }


}
