package com.melecio.wifidirectp2p;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * BroadcastReceiver que notifica de eventos importantes de Wi-Fi P2P
 */
public class WifiDirectBR extends BroadcastReceiver {

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
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            //Cambió el estado del WiFi
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Toast.makeText(mActivity, "¡WiFi encendido!", Toast.LENGTH_SHORT).show();
                mActivity.setIsWifiP2pEnabled(true);
            } else {
                Toast.makeText(mActivity, "¡WiFi apagado!", Toast.LENGTH_SHORT).show();
                mActivity.setIsWifiP2pEnabled(false);
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            //Cambió la lista de usuarios
            if (mManager != null) {
                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mManager.requestPeers(mChannel, mActivity.peerListListener);
                }
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Cambió el estado de la conexión
            if(mManager != null){
                NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
                if(networkInfo.isConnected()){
                    mManager.requestConnectionInfo(mChannel, mActivity.connectionInfoListener);
                }else{
                    mActivity.lblEstado.setText("Dispositivo desconectado");
                }
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //Cambió el estado del WiFi del dispositivo
        }
    }
}
