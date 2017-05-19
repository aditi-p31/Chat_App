package edu.csulb.com.wifibluetoothchatapp.services;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;


public class SocketClient implements SocketConnection {
    private WebSocket socket;
    private int port;
    private String uri;
    private OnSocketCallbackListener onSocketCallbackListener;

    public SocketClient(int port) {
        this.port = port;
    }

    public void connect(String uri) {
        if (socket == null) {
            this.uri = uri;

            AsyncHttpClient.getDefaultInstance().websocket(uri, null, new AsyncHttpClient.WebSocketConnectCallback() {
                @Override
                public void onCompleted(Exception ex, final WebSocket webSocket) {
                    if (ex != null) {
                        ex.printStackTrace();
                        return;
                    }

                    socket = webSocket;

                    webSocket.setStringCallback(new WebSocket.StringCallback() {
                        @Override
                        public void onStringAvailable(String s) {
                            if (onSocketCallbackListener != null) {
                                onSocketCallbackListener.onStringAvailable(s, webSocket);
                            }
                        }
                    });
                }
            });
        }
    }

    public void send(String msg) {
        if (socket.isOpen()) {
            socket.send(msg);
            return;
        }

        socket = null;
        connect(uri);
    }

    public void send(byte[] msg) {
        socket.send(msg);
    }

    public void setCallBackListener(OnSocketCallbackListener callBackListener) {
        this.onSocketCallbackListener = callBackListener;
    }
}
