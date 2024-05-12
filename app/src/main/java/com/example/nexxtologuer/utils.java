package com.example.nexxtologuer;

import java.nio.ByteBuffer;

public class utils {

    final static int ONE_HOUR = 60 * 60 * 1000;
    final static int ONE_DAY = 24 * ONE_HOUR;
    public static long byteArrayToLong(byte[] byteArray, int index, int size) {
        long value = 0;
        for (int i = index; i < (index + size); i++) {
            value = (value * 256) + Byte.toUnsignedInt(byteArray[i]);
        }

        return value;
    }
    public static byte[] longToByteArray(long x) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(x);
        return buffer.array();
    }
    public static byte[] intToByteArray(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(x);
        return buffer.array();
    }
    public static String decodeTime(long timeMillis){
        int days =  (int)Math.floor((float)timeMillis/(ONE_DAY));
        int hours = (int)Math.floor(((float)timeMillis%(ONE_DAY))/ONE_HOUR);
        int mins =  (int)Math.floor(((float)timeMillis%(ONE_HOUR))/60000);
        double secs = ((float)timeMillis%60000.0)/1000;
        if(days==0){
            if(hours==0){
                if(mins==0){
                    return secs + "s";
                }
                return mins + "m " + secs + "s";
            }
            return hours + "h " + mins + "m " + secs + "s";
        }
        return days + "d " + hours + "h " + mins + "m " + secs + "s";
    }
}
