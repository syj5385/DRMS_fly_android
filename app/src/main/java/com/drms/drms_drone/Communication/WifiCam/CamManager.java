
/*
 * This Implementation is just for set Resolution
 * Therefore, we have to modified this in order to various cam parameter
 */
package com.drms.drms_drone.Communication.WifiCam;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class CamManager extends Thread{
    private boolean available;

    private URL url;
    private HttpURLConnection conn;
    private OutputStream os;
    private StringBuffer sbParams;
    private String ip;


    public CamManager(String ip, int port) {
        this.ip = ip;

        start();
    }

    public boolean isAvailable(){
        return available;
    }

    private void connect(){
        try{
            url = new URL("http://" + ip +"/control?var=framesize&val=8");
            conn  = (HttpURLConnection)url.openConnection();

            conn.setDoInput(true);
            conn.setDoOutput(true);


        }catch (MalformedURLException e1){
            e1.printStackTrace();
        }
        catch (IOException e2){
            e2.printStackTrace();
            available = false;
            Log.e("CAM","IOException");
        }

    }

    private void disconnect(){
        conn.disconnect();
    }


    @Override
    public void run() {
        super.run();
        connect();
/*
        try{
            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os,"UTF-8"));

            writer.write(
                    "var=framesize&val=8"
            );

            writer.flush();
            writer.close();

        }catch (IOException e){
            e.printStackTrace();
        }
*/
        disconnect();
    }
}
