package com.example.evan.bluetoothmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

public class ConnectThread extends Thread {
    private static BluetoothDevice device = null;
    private static final Object lock = new Object();
    private Activity context;
    private String matchName;
    private String data;



    public ConnectThread(Activity context, String matchName, String data) {
        this.context = context;
        if (matchName.contains("UNSENT_")) {
            matchName = matchName.replaceFirst("UNSENT_", "");
        }
        this.matchName = matchName;
        this.data = data;
    }



    //called once before use to set up bluetooth
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
            //red super:
            //if (tmpDevice.getName().equals("red super")) {
            //sam's tablet:
            //if (tmpDevice.getName().equals("GT-P5113")) {
            //sam's phone:
            //if (tmpDevice.getName().equals("Samuel Chung's LG-D415")) {
            //evan's android tablet:
            if (tmpDevice.getName().equals("G Pad 7.0 LTE")) {
                synchronized (lock) {
                    device = tmpDevice;
                }
                return;
            }
        }
        Log.e("Bluetooth Error", "No Paired Device With Name: \"red super\"");
        toastText("No Paired Device With Name: \"red super\"", context);
    }



    @Override
    public void run() {
        //first we save to a file so if something goes wrong we have backups.  We use external storage so it is not deleted when app is reinstalled.
        //storage path: /sdcard/Documents/MatchData
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.e("File Error", "External Storage not Mounted");
            toastText("External Storage Not Mounted", context);
            return;
        }
        File dir;
        File file;
        PrintWriter fileWriter;
        try {
            dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Documents/MatchData");
            if (!dir.mkdir()) {
                Log.i("File Info", "Failed to make Directory.  Unimportant");
            }
            //we first name the file with the prefix "UNSENT_".  If all goes well, it is renamed without the prefix, but if something fails it will still have it.
            file = new File(dir, "UNSENT_" + matchName);
            fileWriter = new PrintWriter(file);
        } catch (IOException ioe) {
            Log.e("File Error", "Failed to open file");
            toastText("Failed To Open File", context);
            return;
        }



        fileWriter.println(data.length());
        fileWriter.print(data);
        fileWriter.close();
        if (fileWriter.checkError()) {
            Log.e("File Error", "Failed to Write to File");
            toastText("Failed To Save Match Data To File", context);
            return;
        }



        PrintWriter out = null;
        BufferedReader in = null;
        BluetoothSocket socket = null;
        boolean complete = false;
        int counter = 0;
        //we loop until a connection is made
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
                //first two times it fails, we immediately try again.  Next two we wait 30 seconds before trying again.
                //On the 5th failure we terminate the thread and notify the user
                if ((counter >= 2) && (counter <= 3)) {
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException ie) {
                        Log.wtf("Sleeping Error", "Interrupted During Sleep");
                        return;
                    }
                } else if (counter > 3) {
                    Log.e("Socket Error", "Repeated Socket Open Failure");
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(context)
                                    .setTitle("Repeated Connection Failure")
                                    .setMessage("Please resend this data when successful data transfer is made.")
                                    .setNeutralButton("Dismiss", null)
                                    .show();
                        }
                    });
                    return;
                }
            }
        }



        Log.i("Communications Info", "Starting To Communicate");
        counter = 0;
        complete = false;
        //we loop until the data is sent without io error or error code from super
        while (!complete) {
            try {
                //we print the length of the data before we print the data so the super can identify corrupted data
                out.println(data.length());
                out.print(data);
                //we print '\0' at end of data to signify the end
                out.println("\0");
                out.flush();
                if (out.checkError()) {
                    throw new IOException();
                }
                int ackCode = Integer.parseInt(in.readLine());
                //super will send 0 if the data sizes match up, 1 if they don't
                if (ackCode == 1) {
                    throw new IOException();
                }
                complete = true;
            } catch (IOException ioe) {
                Log.e("Communications Error", "Failed To Send Data");
                toastText("Failed To Send Match Data To Super", context);
                complete = false;
                counter++;
                //after the third failure, we terminate thread and notify user
                if (counter == 3) {
                    Log.e("Communications Error", "Repeated Data Send Failure");
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(context)
                                    .setTitle("Repeated Data Send Failure")
                                    .setMessage("Please resend this data when successful data transfer is made.")
                                    .setNeutralButton("Dismiss", null)
                                    .show();
                        }
                    });
                    return;
                }
            }
        }




        Log.i("Communications Info", "Done");
        toastText("Data Send Success", context);
        try {
            if (!file.renameTo(new File(dir, matchName))) {
                Log.e("File Error", "Failed to Rename File");
            }
            in.close();
            out.close();
            socket.close();
        } catch (IOException ioe) {
            Log.e("Socket Error", "Failed To End Socket");
            toastText("Failed To Close Connection To Super", context);
        }
    }



    private static void toastText(final String text, final Activity context) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }
}
