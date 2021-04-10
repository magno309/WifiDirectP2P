package com.melecio.wifidirectp2p;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    Uri imageUri;

    Button btnSeleccionar, btnDescubrir, btnEnviar;
    ImageView imgPrev;
    ListView lvPeers;

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    Boolean IsWifiP2pEnabled;

    public final static String TAG = "WIFIDIRECTP2PA";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSeleccionar = findViewById(R.id.btnSeleccionar);
        btnDescubrir= findViewById(R.id.btnDescubrir);
        btnEnviar = findViewById(R.id.btnEnviar);
        imgPrev = findViewById(R.id.imgPrev);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        }
        channel = manager.initialize(this, getMainLooper(), null);
        receiver = new WifiDirectBR(manager, channel, this);
        intentFilter = new IntentFilter();
        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        btnSeleccionar.setOnClickListener(v -> {
            abrirGaleria();
        });

        btnDescubrir.setOnClickListener(v -> {
            manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(MainActivity.this, "Buscando dispositivos...", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(int i) {
                    Toast.makeText(MainActivity.this, "Error al buscar dispositivos...", Toast.LENGTH_LONG).show();
                }
            });
        });

    }

    private void abrirGaleria() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            imageUri = data.getData();
            imgPrev.setImageURI(imageUri);
        }
    }


    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            if(!wifiP2pDeviceList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());
                deviceNameArray = new String[wifiP2pDeviceList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];
                for(int i=0;i<peers.size();i++){
                    deviceNameArray[i] = peers.get(i).deviceName;
                    deviceArray[i] = peers.get(i);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                lvPeers.setAdapter(adapter);

                Toast.makeText(MainActivity.this, "Dispositivos encontrados -> " + peers.size(), Toast.LENGTH_LONG).show();
            }
            if(peers.size() == 0){
                Toast.makeText(MainActivity.this, "No se encontraron dispositivos!", Toast.LENGTH_LONG).show();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    public void setIsWifiP2pEnabled(boolean b) {
        IsWifiP2pEnabled = b;
    }
}