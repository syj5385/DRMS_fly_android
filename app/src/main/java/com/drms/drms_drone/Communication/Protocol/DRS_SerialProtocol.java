package com.drms.drms_drone.Communication.Protocol;

import android.os.Handler;
import android.util.Log;

import com.drms.drms_drone.Communication.ClassicBluetooth.BluetoothService;
import com.drms.drms_drone.Communication.USBSerial.UsbService;
import com.drms.drms_drone.Drone_Controller.ContElement;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yeongjunsong on 2017. 6. 15..
 */

public class DRS_SerialProtocol {

    // DRS Serial Protocol
    //
    // #DRS< [_____] [_____] [_____]  [_____]   [_____]
    //
    // header LevR    size   command parameter checksum
    //

    private static final String DRS_header = "#DRS<";

    private int LEVEL_ROUND;
    private Handler mHandler;
    private BluetoothService bluetoothService;
    private UsbService mUSBService;
    private ContElement contElement;

    private static final int CURRENT_VBAT = 100;

    ////////////////////////////////////////////////////////////////////////////////////////////////

    ArrayList<Character> payload;

    public DRS_SerialProtocol(int LEVEL_ROUND, Handler mHandler, BluetoothService bluetoothService) {

        this.LEVEL_ROUND = LEVEL_ROUND;
        this.mHandler = mHandler;
        this.bluetoothService = bluetoothService;
    }
    public DRS_SerialProtocol(int LEVEL_ROUND, Handler mHandler, UsbService mUSBService, ContElement contElement) {
        this.LEVEL_ROUND = LEVEL_ROUND;
        this.mHandler = mHandler;
        this.mUSBService = mUSBService;
        this.contElement = contElement;
    }

    public void make_send_DRS(int command, int[] parameter){

        payload = new ArrayList<Character>();

        for(int i=0 ; i<parameter.length ; i++){
            payload.add((char)(parameter[i] & 0xff));
        }

        send_DRS(make_DRS(command, payload.toArray(new Character[payload.size()])));

    }

    public void make_send_DRS(int command){
        send_DRS(make_DRS(command));
    }

    private List<Byte> make_DRS(int command, Character[] payload){

        byte checksum = 0;
        byte sizeOfData = 0;

        List<Byte> DRS_data = new LinkedList<Byte>();

        for(byte c : DRS_header.getBytes()){
            DRS_data.add(c);
        }

        DRS_data.add((byte)(LEVEL_ROUND & 0xff));
        

        if(payload != null){
            sizeOfData = (byte)(payload.length & 0xff);
        }

        DRS_data.add(sizeOfData);
        checksum ^= (sizeOfData & 0xff);

        DRS_data.add((byte)(command & 0xff));
        checksum ^= (command & 0xff);

        if(payload != null){
            for(char c  : payload){
                DRS_data.add((byte)(c & 0xff));
                checksum ^= ((byte)c & 0xff);
            }
        }

        DRS_data.add(checksum);

        return DRS_data;

    }

    private List<Byte> make_DRS(int command){
        byte checksum = 0;
        byte sizeOfdata = 0;

        List<Byte> DRS_data = new LinkedList<Byte>();

        for(byte c : DRS_header.getBytes()){
            DRS_data.add(c);
        }

        DRS_data.add((byte)(LEVEL_ROUND & 0xff));

        DRS_data.add(sizeOfdata);
        checksum ^= (sizeOfdata & 0xff);

        DRS_data.add((byte)(command & 0xff));
        checksum ^= (command  & 0xff);

        DRS_data.add(checksum);

        return DRS_data;


    }

    private void send_DRS(List<Byte> DRS_data){
        byte[] r = new byte[DRS_data.size()];
        int index = 0;

        for(byte c : DRS_data){
            r[index++] = c;
        }
        if(bluetoothService != null && mUSBService == null) {
            bluetoothService.write(r, bluetoothService.MODE_REQUEST);
        }
        else if(bluetoothService == null && mUSBService != null){
            mUSBService.write(r);
        }

    }

    public boolean read_DRS(byte[] msg){
        if(msg.length > 0) {
            byte[] data = msg;
            String header = null;
            int level_round = read8(data[5]);
            int sizeOfdata = read8(data[6]);
            int command = read8(data[7]);
            int payload[] = new int[13];
            byte checksum = 0;
            byte checksum_temp = 0;

            for (int i = 0; i < 5; i++) {
                if (header == null)
                    header = String.valueOf((char) data[i]);

                else
                    header += (char) data[i];
            }

            switch (command) {
                case DRS_Constants.DRSCONTROLLER_DATA:

                    if (header.equals("#DRS>")) {
                        for (int i = 0; i < 24; i++) {
                            checksum ^= (read8(data[i + 6]) & 0xff);

                        }
                        checksum_temp ^= (read8(data[30]) & 0xff);

                        if (checksum == checksum_temp) {
                            int index = 8;
                            payload[0] = read16(data[index++],data[index++]);   //roll
                            payload[1] = read16(data[index++],data[index++]);   //pitch
                            payload[2] = read16(data[index++],data[index++]);   //yaw
                            payload[3] = read16(data[index++],data[index++]);   //throttle
                            payload[4] = read8(data[index++]);
                            payload[5] = read16(data[index++],data[index++]);
                            payload[6] = read16(data[index++],data[index++]);
                            payload[7] = read16(data[index++],data[index++]);
                            payload[8] = read16(data[index++],data[index++]);
                            payload[9] = read16(data[index++],data[index++]);
                            payload[10] = read8(data[index++]);
                            payload[11] = read8(data[index++]);
                            payload[12] = read8(data[index++]);


                            int[] rpy = {payload[0],payload[1],payload[2],payload[3]};
                            int[] digit = {payload[4],payload[5],payload[6],payload[7],payload[8],payload[9]};
                            int[] tream = {payload[10],payload[11],payload[12]};

                            contElement.setRPY(rpy);
                            contElement.setDigitPin(digit);
                            contElement.setTream(tream);
                        }
                    }
                    break;

            }

            return true;
        }
        else{
            return false;
        }

    }
    public int read8(byte int_8_1){
        return int_8_1 & 0xff;
    }

    public int read16(byte int_16_1, byte int_16_2){
        return ((int_16_1 & 0xff) + (int_16_2 << 8));
    }


}
