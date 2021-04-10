package com.melecio.wifidirectp2p;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class FileTransferService extends IntentService {

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.melecio.wifidirectp2p.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";

    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Context context = getApplicationContext();
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            Log.d("FTSC", "URI - " + fileUri);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);
            try {
                Log.d("FTSC", "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);
                Log.d("FTSC", "Client socket - " + socket.isConnected());
                OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d("FTSC", e.toString());
                }
                ServerClass.copyFile(is, stream);
                Log.d("FTSC", "Client: Data written");
            } catch (IOException e) {
                Log.e("FTSC", e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

    }
}
