package com.example.evan.testschedulesender;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothServerSocket serverSocket;
    private JSONObject schedule;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // NOTE: comment the reason for this line here (like you did in the other file)

        schedule = new JSONObject();
        try {
            Random random = new Random();
            for (int i = 0; i < 30; i++) {
                JSONObject qx = new JSONObject();
                JSONArray red = new JSONArray();
                JSONArray blue = new JSONArray();
                for (int j = 0; j < 3; j++) {
                    red.put(random.nextInt()%1000);
                    blue.put(random.nextInt()%1000);
                }
                qx.put("red", red);
                qx.put("blue", blue);
                schedule.put("Q" + Integer.toString(i+1), qx);
            }
        } catch (JSONException jsone) {
            Log.e("JSON error", "Error in JSON format");
        }


        Switch s = (Switch) findViewById(R.id.Accept);
        final Activity context = this;
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    new Acceptor(context).start();
                } else {
                    try {
                        serverSocket.close();
                    } catch (IOException ioe) {
                        Log.e("Bluetooth Error", "Failed to close serversocket");
                    }
                }
            }
        });
    }
    private class Acceptor extends Thread {
        Activity Context;
        public Acceptor (Activity Context) {
            this.Context = Context;
        }
        public void run() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            try {
                serverSocket = adapter.listenUsingRfcommWithServiceRecord("Test Connection", UUID.fromString("f8212682-9a34-11e5-8994-feff819cdc9f"));
            } catch (IOException ioe) {
                Log.e("Bluetooth Error", "Failed to Open ServerSocket");
                return;
            }
            BluetoothSocket socket;
            Switch accept = (Switch) findViewById(R.id.Accept);
            while (true) {
                if (accept.isChecked()) {
                    try {
                        socket = serverSocket.accept();
                        if (socket != null) {
                            new Communicator(Context, socket).start();
                        }
                    } catch (IOException ioe) {
                        Log.e("Bluetooth Error", "Failed to Open Socket");
                    }
                } else {
                    break;
                }
            }
        }
    }
    private class Communicator extends Thread {
        BluetoothSocket socket;
        Activity Context;
        public Communicator(Activity Context, BluetoothSocket socket) {
            this.socket = socket;
            this.Context = Context;
        }
        public void run() {
            BufferedReader in;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException ioe) {
                Log.e("Bluetooth Error", "Failed to Open BufferedReader");
                return;
            }
            String inLine;
            PrintWriter file;
            try {
                File dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/MatchData");
                if (!dir.mkdir()) {
                    Log.e("Blah", "No one cares"); //NOTE: what is this?
                }
                file = new PrintWriter(new File(dir, "Test-Data-Two.txt"));
            } catch (IOException ioe) {
                try {
                    socket.close();
                } catch (IOException ioe2) {
                    Log.e("Bluetooth Error", "Failed to Close socket");
                    return;
                }
                Log.e("File Error", "Failed to open");
                return;
            }
            try {
                int ackCode = Integer.parseInt(in.readLine());
                if (ackCode == -1) {
                    Log.i("Scout info", "requested schedule");
                    PrintWriter out;
                    try {
                        out = new PrintWriter(socket.getOutputStream(), true);
                    } catch (IOException ioe) {
                        Log.e("Bluetooth Error", "Failed to Open PrintWriter for schedule");
                        return;
                    }
                    try {
                        out.println(schedule.toString().length());
                        out.println(schedule.toString());
                        out.println("\0");
                        out.flush();
                        if (out.checkError()) {
                            throw new IOException();
                        }
                    } catch (IOException ioe) {
                        Log.e("Bluetooth Error", "Failed to send schedule");
                    }
                }
            } catch (IOException ioe) {
                Log.e("Bluetooth Error", "Failed to receive ackCode");
                return;
            }
            while (true) {
                try {
                    inLine = in.readLine();
                    if (inLine.equals("\0")) {
                        break;
                    }
                    file.println(inLine);
                } catch (IOException ioe) {
                    Log.e("Bluetooth Error", "Failed to Read From Socket");
                    return;
                }
            }
            file.close();
            PrintWriter out;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException ioe) {
                Log.e("Bluetooth Error", "Failed to Open PrintWriter");
                return;
            }
            Switch accept = (Switch) Context.findViewById(R.id.Response);
            if (accept.isChecked()) {
                out.println("0"); // NOTE: comment on the significance of 0 and 1
            } else {
                out.println("1");
            }
            try {
                socket.close();
            } catch (IOException ioe) {
                Log.e("Bluetooth Error", "Failed To End Socket");
            }
        }
    }
}
