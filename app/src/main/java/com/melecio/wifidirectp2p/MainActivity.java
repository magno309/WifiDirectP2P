package com.melecio.wifidirectp2p;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.InetAddresses;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    Uri imageUri;

    Button btnSeleccionar, btnDescubrir, btnEnviar;
    ImageView imgPrev;
    ListView lvPeers;
    TextView lblEstado;

    WifiP2pManager manager;
    WifiP2pManager.Channel channel;
    BroadcastReceiver receiver;
    IntentFilter intentFilter;

    private List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    Boolean IsWifiP2pEnabled;

    public final static String TAG = "WIFIDIRECTP2PA";
    public static final String ACTION_SEND_FILE = "com.melecio.wifidirectp2p.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    public boolean esHost = false;
    public boolean esCliente = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSeleccionar = findViewById(R.id.btnSeleccionar);
        btnDescubrir = findViewById(R.id.btnDescubrir);
        btnEnviar = findViewById(R.id.btnEnviar);
        lvPeers = findViewById(R.id.lvPeers);
        imgPrev = findViewById(R.id.imgPrev);
        lblEstado = findViewById(R.id.lblEstado);

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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                manager.discoverPeers(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        lblEstado.setText("Buscando...");
                        Toast.makeText(MainActivity.this, "Buscando dispositivos...", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(int i) {
                        lblEstado.setText("Error");
                        Toast.makeText(MainActivity.this, "Error al buscar dispositivos...", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        btnEnviar.setOnClickListener(v -> {
            if(esCliente && imageUri!=null) {
                Intent enviarImagen = new Intent(MainActivity.this, FileTransferService.class);
                enviarImagen.putExtra(EXTRAS_FILE_PATH, imageUri.toString());
                enviarImagen.putExtra(EXTRAS_GROUP_OWNER_ADDRESS, hostAddress);
                enviarImagen.putExtra(EXTRAS_GROUP_OWNER_PORT, 8888);
                startService(enviarImagen);
            }
        });

        lvPeers.setOnItemClickListener((adapterView, view, i, l) -> {
            final WifiP2pDevice device = deviceArray[i];
            WifiP2pConfig config = new WifiP2pConfig();
            config.deviceAddress = device.deviceAddress;

            manager.connect(channel, config, new WifiP2pManager.ActionListener() {
                @Override
                public void onSuccess() {
                    Toast.makeText(getApplicationContext(), "Conectado a " + device.deviceName, Toast.LENGTH_LONG).show();

                }
                @Override
                public void onFailure(int i) {
                    Toast.makeText(getApplicationContext(), "Error al conectar!", Toast.LENGTH_SHORT).show();
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
            if(wifiP2pDeviceList.getDeviceList().size()!=peers.size()){
                peers.clear();
                peers.addAll(wifiP2pDeviceList.getDeviceList());
                deviceNameArray = new String[wifiP2pDeviceList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[wifiP2pDeviceList.getDeviceList().size()];
                for(int i=0;i<wifiP2pDeviceList.getDeviceList().size();i++){
                    deviceNameArray[i] = peers.get(i).deviceName;
                    deviceArray[i] = peers.get(i);
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, deviceNameArray);
                lvPeers.setAdapter(adapter);

                Toast.makeText(MainActivity.this, "Dispositivos encontrados -> " + peers.size(), Toast.LENGTH_SHORT).show();
            }
            if(peers.size() == 0){
                Toast.makeText(MainActivity.this, "No se encontraron dispositivos!", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    String hostAddress;

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            final InetAddress groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
            if(wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner){
                lblEstado.setText("Host");
                esHost = true;
                esCliente = false;
                ServerClass serverClass = new ServerClass(getApplicationContext(), MainActivity.this); /*Cómo recibo la clase main?*/
                serverClass.execute();
            }else if(wifiP2pInfo.groupFormed){
                lblEstado.setText("Cliente");
                hostAddress = groupOwnerAddress.getHostAddress();
                esCliente = true;
                esHost = false;
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