package com.melecio.wifidirectp2p;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerClass extends AsyncTask<Void, Void, String> {

    Socket socket;
    ServerSocket serverSocket;
    Context mContext;
    MainActivity mActivity;

    public ServerClass(Context context, MainActivity mainActivity) {
        this.mContext = context;
        this.mActivity = mainActivity;
    }

    @Override
    protected String doInBackground(Void... voids) {
        try{
            serverSocket = new ServerSocket(8888);
            socket = serverSocket.accept();

            final File f = new File(mContext.getExternalFilesDir("received"),
                    "wifip2pshared-" + System.currentTimeMillis()
                            + ".jpg");
            File dirs = new File(f.getParent());
            if (!dirs.exists()){
                dirs.mkdirs();
            }
            f.createNewFile();
            InputStream inputstream = socket.getInputStream();
            copyFile(inputstream, new FileOutputStream(f));
            serverSocket.close();
            return f.getAbsolutePath();
        }catch (IOException e){
            Log.e("SCLOG", e.getMessage());
            return null;
        }
    }

    @Override
    protected void onPostExecute(String result) {
        if(result != null){
            Toast.makeText(mContext, "Archivo copiado - " + result, Toast.LENGTH_LONG).show();
            File recvFile = new File(result);
            Uri fileUri =  Uri.fromFile(recvFile);
            mActivity.imgPrev.setImageURI(fileUri);
        }
    }

    public static boolean copyFile(InputStream inputStream, OutputStream out) {
        byte buf[] = new byte[1024];
        int len;
        try {
            while ((len = inputStream.read(buf)) != -1) {
                out.write(buf, 0, len);
            }
            out.close();
            inputStream.close();
        } catch (IOException e) {
            Log.d("SCLOG", e.toString());
            return false;
        }
        return true;
    }

}
