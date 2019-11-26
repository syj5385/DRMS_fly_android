package com.drms.drms_drone.FileManagament;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by jjunj on 2017-09-15.
 */

public class FileManagement {

    private static final int FILEMANAGEMENT = 4;
    public static final int FINISHED_WRITE_DATA = 41;
    public static final int FINISHED_READ_DATA = 42;

    private Context context;
    private ArrayList<File> myFile_list = new ArrayList<File>();

    private String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath();
    private String filePath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private String BTdirPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    private File myFile;

    private File[] filelist ;

    private FileOutputStream outputStream;
    private FileInputStream inputStream;

    private Handler mHandler ;


    public FileManagement(Context context, Handler mHandler ){
        this.context = context;
        this.mHandler = mHandler;

        dirPath += "/DRMS(fly)";
//        BTdirPath += "/DrsProgramming/bluetooth";

        myFile = new File(dirPath);
        myFile.mkdirs(); // if the directory does not exist, Create new Directory in ExternalStorage in Android
        createFiles("tream");

//        filelist = myFile.listFiles();
//
//        myFile = new File(BTdirPath);
//        myFile.mkdirs();
//        createBTFile();



    }



    public boolean isFileExist(String fileName){
        myFile = new File(dirPath + "/" + fileName);
        if(myFile.exists()){
            return true;
        }
        else{
            return false;
        }
    }

    public boolean createFiles(String fileName){
        boolean success = false;
        filePath = dirPath +"/" + fileName + ".txt";
        myFile = new File(filePath);

        if(!myFile.exists()) {
            try {
                myFile.createNewFile();
                int[] tream = {0,0,0};
                char[] tream_temp = new char[6];
                int index= 0;
                for(int i=0; i<3 ;i++){
                    tream_temp[index++] = (char)((tream[i] + 500) & 0xff);
                    tream_temp[index++] = (char)(((tream[i] + 500) >> 8) & 0xff);
                }
                writeTreamOnFile(tream_temp);

            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), "IOException");
            }
            success= true;
        }
        else{

            success = false;
        }
        return success;

    }



    public boolean writeTreamOnFile(char[] tream){
        boolean success = false;

        myFile = new File(dirPath + "/tream.txt");
        if(!myFile.exists()) {
            createFiles("tream");
            Log.d("File","not exist");
        }
        Log.d("File","write");
        try {
            outputStream = new FileOutputStream(myFile);

            byte[] arr = new byte[6];
            int i=0;
            for(char c : tream){
                arr[i++] = (byte)(c & 0xff);
            }

            Log.d("File",new String(arr));
            outputStream.write(arr);

            outputStream.close();
            success = true;
        } catch (IOException e) {
            success = false;
            Log.d("File","IOException");
        }
        ;

        return success;
    }

    public byte[] readTream(){
        byte[] tream = null;
        myFile = new File(dirPath + "/tream.txt");
        byte[] addr = new byte[(int)myFile.length()];
        if(myFile.exists()){
            if(myFile.length() > 0) {
                Log.d("FILE", BTdirPath + "bluetooth.txt exist");
                try {
                    inputStream = new FileInputStream(myFile);
                    BufferedInputStream bufferReader = new BufferedInputStream(inputStream);

                    for (int i = 0; i < myFile.length(); i++) {
                        addr[i] = (byte) bufferReader.read();
                        Log.w("READ DATA", String.valueOf(addr[i]));
                    }
                } catch (IOException e) {
                }
                ;
                ;
            }
            else{
                return null;
            }
        }
        else{
            Log.d("FILE",BTdirPath + "bluetooth.txt not exist");
        }


        return addr;
    }


    public void deleteFile(String fileName){

        myFile = new File(dirPath+"/"+ fileName);

        if(myFile.exists()) {
            if(myFile.delete()){
                Toast.makeText(context,"파일을 삭제했습니다.", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(context,"파일을 삭제하지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        }

        updateFileList();
    }




    public File[] getFilelist(){
        return filelist;
    }

    private void updateFileList(){
        myFile = new File(dirPath);

        filelist = myFile.listFiles();
    }

    public int read8(byte int_8_1){
        return int_8_1 & 0xff;
    }


}
