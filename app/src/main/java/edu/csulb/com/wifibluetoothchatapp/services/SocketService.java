package edu.csulb.com.wifibluetoothchatapp.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.Nullable;


public class SocketService extends Service {
    public static final int PORT = 8888;
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    private final IBinder mBinder = new LocalBinder();

    private SocketServer mSocketServer;
    private SocketClient mSocketClient;
    private SocketConnection mSocketConnection;
    private boolean isConnected;

    @Override
    public void onCreate() {
        super.onCreate();

        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        mServiceLooper = thread.getLooper();
        mServiceHandler = new ServiceHandler(mServiceLooper);
//        mSocketServer = new SocketServer(8888);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Message msg = mServiceHandler.obtainMessage();
        msg.arg1 = startId;
        msg.obj = intent;
        mServiceHandler.sendMessage(msg);
        return START_STICKY;
    }

    public void sendMessage(String s) {
        Message message = mServiceHandler.obtainMessage();
        message.obj = s;
        mServiceHandler.sendMessage(message);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mSocketServer.stopServer();
    }

    public void setCallBackListener(SocketConnection.OnSocketCallbackListener callBackListener) {
        mSocketConnection.setCallBackListener(callBackListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void startServer() {
        if (mSocketServer == null) {
            if (isConnected) {
                throw new IllegalStateException("Already connected as Client;");
            }

            mSocketServer = new SocketServer(PORT);
            mSocketServer.startServer();
            mSocketConnection = mSocketServer;
            isConnected = true;
        }
    }

    public void connectClient(String uri) {
        if (mSocketClient == null) {
            if (isConnected) {
                throw new IllegalStateException("Already connected as Server;");
            }

            mSocketClient = new SocketClient(PORT);
            mSocketClient.connect("ws://" + uri + ":8888/join");
            mSocketConnection = mSocketClient;
            isConnected = true;
        }
    }

    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            String strMsg = (String) msg.obj;
            mSocketConnection.send(strMsg);
        }
    }

    public class LocalBinder extends Binder {
        public SocketService getService() {
            return SocketService.this;
        }
    }

}
