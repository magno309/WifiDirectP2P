package com.melecio.wifidirectp2p;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class WifiDirectBR extends BroadcastReceiver{

    private WifiP2pManager mManager;
    private WifiP2pManager.Channel mChannel;
    private MainActivity mActivity;

    public WifiDirectBR(WifiP2pManager manager, Channel channel, MainActivity activity) {
        super();
        this.mManager = manager;
        this.mChannel = channel;
        this.mActivity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Toast.makeText(mActivity, "Entra al Broadcast", Toast.LENGTH_LONG).show();
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            //Toast.makeText(mActivity, "Entra al STATE_CHANGED_ACTION", Toast.LENGTH_SHORT).show();
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(mActivity, "WiFi P2P habilitado", Toast.LENGTH_SHORT).show();
            } else {
                // Wi-Fi P2P is not enabled
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            Toast.makeText(mActivity, "Entra al PEERS_CHANGED_ACTION", Toast.LENGTH_LONG).show();
            try{
                if(mManager!=null){
                    mManager.requestPeers(mChannel, mActivity.peerListListener);
                }
            }catch (Exception e){
                Log.d("BRTAG", e.getMessage());
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            Toast.makeText(mActivity, "Entra al CONNECTION_CHANGED_ACTION", Toast.LENGTH_LONG).show();
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Toast.makeText(mActivity, "Entra al THIS_DEVICE", Toast.LENGTH_LONG).show();
        }
    }
}
