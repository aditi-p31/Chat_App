package edu.csulb.com.wifibluetoothchatapp.services;

import android.util.Log;

import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;

import java.util.ArrayList;
import java.util.List;


public class SocketServer implements SocketConnection {
    private AsyncHttpServer server;
    private List<WebSocket> _sockets;
    private OnSocketCallbackListener callBackListener;
    private int port;

    public SocketServer(int port) {
        _sockets = new ArrayList<>();
        this.port = port;
    }

    public void startServer() {
        if (server == null) {
            server = new AsyncHttpServer();
            server.websocket("/join", new AsyncHttpServer.WebSocketRequestCallback() {
                @Override
                public void onConnected(final WebSocket webSocket, AsyncHttpServerRequest request) {
                    _sockets.add(webSocket);

                    webSocket.setClosedCallback(new CompletedCallback() {
                        @Override
                        public void onCompleted(Exception ex) {
                            try {
                                if (ex != null)
                                    Log.e("WebSocket", "Error");
                            } finally {
                                _sockets.remove(webSocket);
                            }
                        }
                    });

                    webSocket.setStringCallback(new WebSocket.StringCallback() {
                        @Override
                        public void onStringAvailable(String s) {
                            if (callBackListener != null) {
                                callBackListener.onStringAvailable(s, webSocket);
                            }
                        }
                    });
                }
            });
            server.listen(port);
        }
    }

    public void send(String msg) {
        for (WebSocket socket : _sockets)
            socket.send(msg);
    }

    public void send(byte[] msg) {
        for (WebSocket socket : _sockets)
            socket.send(msg);
    }

    public void stopServer() {
        server.stop();
        _sockets.clear();
    }

    public void setCallBackListener(OnSocketCallbackListener callBackListener) {
        this.callBackListener = callBackListener;
    }
}
