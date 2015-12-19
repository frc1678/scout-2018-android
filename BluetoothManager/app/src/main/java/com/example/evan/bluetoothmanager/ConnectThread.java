package com.example.evan.bluetoothmanager;


import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

public class ConnectThread extends Thread {
    private static BluetoothDevice device = null;
    private static final Object lock = new Object();
    private MainActivity context;
    private String data;



    public static void initBluetooth(final MainActivity context) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.wtf("Bluetooth Error", "Device Not Configured With Bluetooth");
            toastText("Device Not Configured With Bluetooth", context);
            return;
        }
        if (!adapter.isEnabled()) {
            Log.e("Bluetooth Error", "Bluetooth Not Enabled");
            toastText("Bluetooth Not Enabled", context);
            return;
        }
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        if (devices.size() < 1) {
            Log.e("Bluetooth Error", "No Paired Devices");
            toastText("No Paired Devices", context);
            return;
        }
        adapter.cancelDiscovery();
        for (BluetoothDevice tmpDevice : devices) {
            if (tmpDevice.getName().equals("red super")) {
                synchronized (lock) {
                    device = tmpDevice;
                }
                return;
            }
        }
        Log.e("Bluetooth Error", "No Paired Device With Name: \"red super\"");
        toastText("No Paired Device With Name: \"red super\"", context);
    }



    private static void toastText(final String text, final MainActivity context) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }


    public ConnectThread(MainActivity context, String data) {
        this.context = context;
        this.data = data;
    }


    @Override
    public void run() {
        PrintWriter out = null;
        BufferedReader in = null;
        BluetoothSocket socket = null;
        boolean complete = false;
        int counter = 0;
        while (!complete) {
            try {
                synchronized (lock) {
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString("f8212682-9a34-11e5-8994-feff819cdc9f"));
                }
                Log.i("Socket Info", "Attempting To Start Connection...");
                socket.connect();
                Log.i("Socket info", "Connection Successful!  Getting Ready To Send Data...");
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                complete = true;
            } catch (IOException ioe) {
                Log.e("Socket Error", "Failed To Open Socket");
                toastText("Failed To Connect To Super", context);
                complete = false;
                counter++;
                if ((counter >= 2) && (counter <= 3)) {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException ie) {
                        Log.wtf("Sleeping Error", "Interrupted During Sleep");
                        return;
                    }
                } else if (counter > 3) {
                    Log.e("Socket Error", "Repeated Socket Open Failure");
                    toastText("Repeated Connection Error. Get A Programmer", context);
                    return;
                }
            }
        }


        Log.i("Communications Info", "Starting To Communicate");
        counter = 0;
        complete = false;
        while (!complete) {
            try {
                out.println(data.length());
                out.println(data);
                out.println("\0");
                out.flush();
                if (out.checkError()) {
                    throw new IOException();
                }
                int ackCode = Integer.parseInt(in.readLine());
                if (ackCode == 1) {
                    throw new IOException();
                }
                complete = true;
            } catch (IOException ioe) {
                Log.e("Communications Error", "Failed To Send Data");
                toastText("Failed To Send Match Data To Super", context);
                complete = false;
                counter++;
                if (counter == 3) {
                    Log.e("Communications Error", "Repeated Data Send Failure");
                    toastText("Repeated Data Send Failure. Get A Programmer", context);
                    return;
                }
            }
        }



        Log.i("Communications Info", "Done");
        toastText("Data Send Success", context);
        try {
            //we print this at end of connection to prevent IOException from being thrown server-side
            out.close();
            socket.close();
        } catch (IOException ioe) {
            Log.e("Socket Error", "Failed To End Socket");
            toastText("Failed To Close Connection To Super", context);
        }
    }
}
