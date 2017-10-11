package com.example.vip.smarthome;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Protocol {
    public static int temperature, humidity;
    public static final int SET_LIGHT = 18, SET_FAN = 28,
            SET_CURTAIN = 38, SET_AIR = 48,
            SET_TV = 58, REQUIRE_STATE = 68, OUTSIDE_MODE = 78, 
            POWERSAVING_MODE = 88, SMART_MODE = 98;
    private static byte checksum = 0;
    private static final String MSP_HEADER = "$M<";
    private final static byte[] msp = MSP_HEADER.getBytes();

    public static byte[] lightList = new byte[9];
    public static byte[] fanList = new byte[3];
    public static byte[] curtainList = new byte[3];
    public static byte[] airList = new byte[2];
    public static byte[] tvList = new byte[2];

    public static byte[] getCommandData(int cmd) {

        byte[] cmdData = null;
        switch (cmd) {
            case SET_LIGHT:
                cmdData = lightList;
                break;
            case SET_FAN:
                cmdData = fanList;
                break;
            case SET_CURTAIN:
                cmdData = curtainList;
                break;
            case SET_AIR:
                cmdData = airList;
                break;
            case SET_TV:
                cmdData = tvList;
                break;
            case REQUIRE_STATE:
                return null;
            case OUTSIDE_MODE:
                return null;
            case SMART_MODE:
                return null;
            case POWERSAVING_MODE:
                return null;
        }

//        for (int i = 0; i < cmdData.length; i++) {
//            Log.w("data", "commandData:" + cmdData[i]);
//        }
//        Log.w("data", "datasize:" + cmdData.length);
        return cmdData;
    }

    public static byte[] getSendData(int cmd, byte[] data) {
        if (cmd < 0)
            return null;
        List<Byte> bf = new LinkedList<Byte>();
        for (byte c : MSP_HEADER.getBytes()) {
            bf.add(c);
        }
        byte checksum = 0;

        byte dataSize = (byte) ((data != null) ? (data.length) : 0);
        bf.add(dataSize);
        checksum ^= (dataSize & 0xFF);

        bf.add((byte) (cmd & 0xFF));
        checksum ^= (cmd & 0xFF);

        if (data != null) {
            for (byte c : data) {
                bf.add((byte) (c & 0xFF));
                checksum ^= (c & 0xFF);
            }
        }
        bf.add(checksum);

        byte[] sendData = new byte[bf.size()];
        int i = 0;
        for (byte b : bf) {
            sendData[i++] = b;
        }

        return (sendData);

    }

    public static void processDataIn(byte[] inData, int len) {

        for (int i = 0; i < len; i++) {
//			Log.w("check","inData:"+inData[i]);
//			Log.w("check","MSP_HEADER:"+msp[0]+"++"+msp[1]+"=="+msp[2]);
            if (i < 3) {
                if (inData[i] == msp[i]) ;
                else return;
            }
            if (i >= 3 && i < len - 1) {
                checksum ^= inData[i];
            }
        }
        try {
            if (checksum == inData[len - 1]) {
                temperature = (int) inData[5];
                humidity = (int) inData[6];
                Log.w("check", "测试成功");
            }

            temperature = (int) inData[5];
            humidity = (int) inData[6];
            //Log.w("check","temperature:"+temperature);
            //Log.w("check","humidity:"+humidity);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}