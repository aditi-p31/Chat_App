package edu.csulb.com.wifibluetoothchatapp.services;

import com.koushikdutta.async.http.WebSocket;



public interface SocketConnection {
    void send(String msg);
    void send(byte[] data);
    void setCallBackListener(OnSocketCallbackListener callBackListener);

    interface OnSocketCallbackListener {
        void onStringAvailable(String data, WebSocket webSocket);
    }
}
