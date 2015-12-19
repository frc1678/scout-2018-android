package com.example.sam.blutoothsocketreceiver;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import junit.framework.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {
    BluetoothServerSocket tmp;
    BluetoothServerSocket mmServerSocket;
    BluetoothAdapter mBluetoothAdapter;
    BluetoothSocket socket;
    BluetoothDevice device;
    String Text;
    String uuid;
    Context context;
    String byteSize;
    String appendString;
    String data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        AcceptThread thread = new AcceptThread();
        thread.start();
    }

    public class AcceptThread extends Thread {

        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            tmp = null;
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            uuid = "f8212682-9a34-11e5-8994-feff819cdc9f";
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("Test_Connection", UUID.fromString(uuid));
            } catch (IOException e) {

            }
            mmServerSocket = tmp;
        }

        public void run() {
            socket = null;
            // Keep listening until exception occurs or a socket is returned
            while (true) {
                try {
                    if (mmServerSocket.equals(null)) {

                        Log.e("asdf", "is null");
                    }
                    System.out.println("accepting...");
                    socket = mmServerSocket.accept();

                } catch (IOException e) {
                }
                // If a connection was accepted
                if (socket != null) {
                    // Do work to manage the connection (in a separate thread)
                    try {
                        PrintWriter out;
                        out = new PrintWriter(socket.getOutputStream(), true);
                        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        System.out.println("BeforeReadLine");
                        //PrintWriter file = new PrintWriter(context.openFileOutput("Send-Data.txt", Context.MODE_PRIVATE));
                        PrintWriter file = null;
                        try {
                            File dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/MassStringText");
//                            if (!dir.mkdir()) {
//                                Log.e("File Error", "Failed to make directory");
//                                throw new IOException();
//                            }
                            file = new PrintWriter(new FileOutputStream(new File(dir, "Send-Data.txt")));
                        } catch (IOException IOE){
                            Log.e("File error", "Failed to open File");
                            return;
                        }
                        String text = "";
                        byteSize = reader.readLine();
                        int size = Integer.parseInt(byteSize);
                        while (true) {
                           text = reader.readLine();
                            data = data.concat(text);
                            if (text.equals("\0")) {
                                break;
                            }
                        }
                        if (size != data.length()) {
                            //send error message to scout.
                            //0 = no error, 1 = ERROR!
                            out.println("1");
                            out.flush();
                        } else if (size == data.length()){
                            file.println(text);
                            System.out.println(text);
                            out.println("0");
                            out.flush();
                            Log.e("success","right byte size");
                        }
                        System.out.println("AfterReadLine");
                        file.close();
                        socket.close();
                    } catch (IOException e) {
                        System.out.println("Failed to receive Data..");
                        Log.getStackTraceString(e);
                    }

                    break;
                }
            }
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}


