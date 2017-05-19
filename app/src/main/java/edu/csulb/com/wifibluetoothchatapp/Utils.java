package edu.csulb.com.wifibluetoothchatapp;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


public class Utils {
    public static String bytesToString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static byte[] toBytes(String data) {
        return data.getBytes(StandardCharsets.UTF_8);
    }

    public static void close(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
