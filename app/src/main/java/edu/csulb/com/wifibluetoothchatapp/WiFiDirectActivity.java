package edu.csulb.com.wifibluetoothchatapp;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import edu.csulb.com.wifibluetoothchatapp.adapter.WifiListArrayAdapter;
import edu.csulb.com.wifibluetoothchatapp.fragment.ChatFragment;
import edu.csulb.com.wifibluetoothchatapp.fragment.SelectorFragment;
import edu.csulb.com.wifibluetoothchatapp.fragment.WifiListFragment;
import edu.csulb.com.wifibluetoothchatapp.services.SocketConnection;
import edu.csulb.com.wifibluetoothchatapp.services.SocketService;
import edu.csulb.com.wifibluetoothchatapp.services.WifiDirectBroadcastReceiver;

public class WiFiDirectActivity extends AppCompatActivity implements WifiP2pManager.PeerListListener,
        WifiP2pManager.ConnectionInfoListener,
        WifiListFragment.WifiListListener,
        ChatFragment.OnFragmentInteractionListener, SelectorFragment.OnFragmentInteractionListener {

    public static final String TAG = WiFiDirectActivity.class.getName();
    private boolean isWifiP2pEnabled;

    private WifiP2pManager mManager;
    private IntentFilter mIntentFilter;
    private BroadcastReceiver mReceiver;
    private WifiP2pManager.Channel mChannel;
    private List<WifiP2pDevice> mPeerList = new ArrayList<WifiP2pDevice>();
    private WifiListArrayAdapter mWifiListArrayAdapter;

    private boolean isOwner;
    private boolean isWifiConnected;
    private InetAddress mGroupOwnerAddress;
    private SelectorFragment selectorFragment;
    private WifiListFragment wifiListFragment;
    private SocketService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wi_fi_direct);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), null);
        mReceiver = new WifiDirectBroadcastReceiver(mManager, mChannel, this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mPeerList = new ArrayList<>();
        mWifiListArrayAdapter = new WifiListArrayAdapter(this, mPeerList);

        selectorFragment = new SelectorFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, selectorFragment).commit();
    }

    private void showDeviceListFragment() {
        wifiListFragment = new WifiListFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.frame, wifiListFragment).addToBackStack(null).commit();
    }

    @Override
    public WifiListArrayAdapter getListAdapter() {
        return mWifiListArrayAdapter;
    }

    public void discoverPeers() {
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Toast.makeText(WiFiDirectActivity.this, "Discovering Peers...", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(int reasonCode) {
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.wifi_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.discover_peers:
                showDeviceListFragment();
                return true;
        }
        return false;
    }

    public void setWifiP2pEnabled(boolean wifiP2pEnabled) {
        isWifiP2pEnabled = wifiP2pEnabled;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        List<WifiP2pDevice> refreshedPeers = new ArrayList<>(peers.getDeviceList());
        if (!refreshedPeers.equals(peers)) {
            mPeerList.clear();
            mPeerList.addAll(refreshedPeers);
            mWifiListArrayAdapter.notifyDataSetChanged();
        }
    }

    private void connect(WifiP2pConfig config) {
        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                // WiFiDirectBroadcastReceiver will notify us. Ignore for now.
            }

            @Override
            public void onFailure(int reason) {
                Toast.makeText(WiFiDirectActivity.this, "Connect failed. Retry.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void disconnect() {
        if (mManager != null && mChannel != null) {
            mManager.requestGroupInfo(mChannel, new WifiP2pManager.GroupInfoListener() {
                @Override
                public void onGroupInfoAvailable(WifiP2pGroup group) {
                    selectorFragment.setTvStatus("DISCONNECTED");
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, selectorFragment).commit();
                    isWifiConnected = false;

                    if (group != null && mManager != null && mChannel != null
                            && group.isGroupOwner()) {
                        mManager.removeGroup(mChannel, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                Log.d(TAG, "removeGroup onSuccess -");
                            }

                            @Override
                            public void onFailure(int reason) {
                                Log.d(TAG, "removeGroup onFailure -" + reason);
                            }
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        // InetAddress from WifiP2pInfo struct.
        mGroupOwnerAddress = info.groupOwnerAddress; //.getHostAddress();
        isWifiConnected = true;

        if (selectorFragment != null) {
            selectorFragment.setTvStatus("Connected @ " + mGroupOwnerAddress);
        }
        if (wifiListFragment != null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.frame, selectorFragment).commit();
        }

        isOwner = info.groupFormed && info.isGroupOwner;
        if (info.groupFormed) {
            Intent intent = new Intent(this, SocketService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        }
    }

    public void onItemClick(int position) {
        Toast.makeText(this, "Connecting to " + mPeerList.get(position).deviceName, Toast.LENGTH_SHORT).show();

        WifiP2pDevice device = mPeerList.get(position);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        connect(config);
    }

    @Override
    public void sendData(String data) {
        mService.sendMessage(data);
    }

    @Override
    public void setOnSocketCallbackListener(SocketConnection.OnSocketCallbackListener onSocketCallbackListener) {
        if (mService != null) {
            mService.setCallBackListener(onSocketCallbackListener);
        }
    }

    public boolean isWifiConnected() {
        return isWifiConnected;
    }

    public void setWifiConnected(boolean wifiConnected) {
        isWifiConnected = wifiConnected;
    }

    @Override
    public void onSelectorItemClick(View v) {
        if (isWifiConnected) {
            switch (v.getId()) {
                case R.id.send_text:
                    ChatFragment wifiListFragment = new ChatFragment();
                    getSupportFragmentManager().beginTransaction().replace(R.id.frame, wifiListFragment).addToBackStack(null).commit();
                    break;
                case R.id.bConnectWifi:
                    disconnect();
                    break;
            }
        } else {
            showDeviceListFragment();
        }
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            Log.d(TAG, "CONNECTED");

            Toast.makeText(WiFiDirectActivity.this, "Service Connected", Toast.LENGTH_SHORT).show();
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            mService = binder.getService();
            bindSockets();
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.d(TAG, "DISCONNECTED");
        }
    };

    private void bindSockets() {
        if (isOwner) {
            mService.startServer();
            Toast.makeText(this, "Starting Server", Toast.LENGTH_SHORT).show();
        } else {
            mService.connectClient(mGroupOwnerAddress.getHostAddress());
            Toast.makeText(this, "Connecting to Server", Toast.LENGTH_SHORT).show();
        }
    }
}
