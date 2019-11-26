package com.drms.drms_drone.Download;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.renderscript.ScriptGroup;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.drms.drms_drone.BuildConfig;
import com.drms.drms_drone.R;
import com.drms.drms_drone.Service.BTService;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.Buffer;
import java.util.List;

/**
 * Created by jjunj on 2017-12-07.
 */

public class DownloadManager {
    private Context mContext;
    private Activity mActivity;
    private android.app.DownloadManager mDownloadManager;

    public static final int REQUEST_HOWTO_USE = 100;

    public DownloadManager(Activity mActivity,Context mContext) {
        this.mActivity = mActivity;
        this.mContext = mContext;


    }

    public void download(){
        final InputStream inputStream = mActivity.getResources().openRawResource(R.raw.flyhowto);
        File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/DRMS(fly)"+"/howto.pdf");
        OutputStream outputStream;

        if(!file.exists()){
            try {
                file.createNewFile();
            }
            catch (IOException e){};
        }


        try {
            byte[] temp = new byte[inputStream.available()];
            inputStream.read(temp);
            inputStream.close();

            outputStream = new FileOutputStream(file);
            outputStream.write(temp);
            Log.d("Down", "Size : " + file.length());
        }catch (IOException e){
            Log.d("Down","IOException");
        };
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        String extension = MimeTypeMap.getFileExtensionFromUrl("howto.pdf");
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        Log.d("down",extension);



        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
            intent.setDataAndType(Uri.fromFile(file),mimeType);
            try {
                mActivity.startActivityForResult(intent, REQUEST_HOWTO_USE);
                mActivity.sendBroadcast(new Intent(BTService.REQUEST_FINISH_SERVICE));
            }catch (ActivityNotFoundException e) {
                Toast.makeText(mActivity,"pdf파일을 열수 없습니다.",Toast.LENGTH_SHORT).show();
            }
        }
        else{
            Uri pdfuri = FileProvider.getUriForFile(mActivity.getApplicationContext(), BuildConfig.APPLICATION_ID+".provider",new File(Environment.getExternalStorageDirectory().getAbsoluteFile()+"/DRMS(fly)"+"/howto.pdf"));
            Uri uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID+".provider",file);
            intent.setDataAndType(uri,mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try{

            mActivity.startActivityForResult(intent,REQUEST_HOWTO_USE);
            mActivity.sendBroadcast(new Intent(BTService.REQUEST_FINISH_SERVICE));

            }catch (ActivityNotFoundException e) {
                Toast.makeText(mActivity,"pdf파일을 열수 없습니다.",Toast.LENGTH_SHORT).show();
            }
        }



    }

}
