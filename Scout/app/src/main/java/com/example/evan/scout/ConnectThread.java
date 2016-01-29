package com.example.evan.scout;

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
    protected static BluetoothDevice device = null;
    protected static final Object deviceLock = new Object();
    protected static boolean isInit = false;
    protected static final Object isInitLock = new Object();
    protected Activity context;
    protected String superName;
    protected String uuid;
    private String matchName;
    private String data;



    public ConnectThread(Activity context, String superName, String uuid, String matchName, String data) {
        this.context = context;
        this.superName = superName;
        this.uuid = uuid;
        if (matchName != null) {
            if (matchName.contains("UNSENT_")) {
                matchName = matchName.replaceFirst("UNSENT_", "");
            }
        }
        this.matchName = matchName;
        this.data = data;
    }




    protected static boolean initBluetooth(final Activity context, String superName) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter == null) {
            Log.wtf("Bluetooth Error", "Device Not Configured With Bluetooth");
            toastText("Device Not Configured With Bluetooth", Toast.LENGTH_LONG, context);
            return false;
        }
        if (!adapter.isEnabled()) {
            Log.e("Bluetooth Error", "Bluetooth Not Enabled");
            toastText("Bluetooth Not Enabled", Toast.LENGTH_LONG, context);
            return false;
        }
        Set<BluetoothDevice> devices = adapter.getBondedDevices();
        if (devices.size() < 1) {
            Log.e("Bluetooth Error", "No Paired Devices");
            toastText("No Paired Devices", Toast.LENGTH_LONG, context);
            return false;
        }
        adapter.cancelDiscovery();
        for (BluetoothDevice tmpDevice : devices) {
            if (tmpDevice.getName().equals(superName)) {
                synchronized (deviceLock) {
                    device = tmpDevice;
                }
                return true;
            }
        }
        Log.e("Bluetooth Error", "No Paired Device With Name: \"red super\"");
        toastText("No Paired Device With Name: \"" + superName + "\"", Toast.LENGTH_LONG, context);
        return false;
    }



    @Override
    public void run() {
        //if bluetooth has not been initialized, initialize it
        synchronized (isInitLock) {
            if (!isInit) {
                if(!initBluetooth(context, superName)) {
                    return;
                } else {
                    isInit = true;
                }
            }
        }
        //first we save to a file so if something goes wrong we have backups.  We use external storage so it is not deleted when app is reinstalled.
        //storage path: /sdcard/Android/MatchData
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            Log.e("File Error", "External Storage not Mounted");
            toastText("External Storage Not Mounted", Toast.LENGTH_LONG, context);
            return;
        }
        File dir;
        File file;
        PrintWriter fileWriter;
        try {
            dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData");
            if (!dir.mkdir()) {
                Log.i("File Info", "Failed to make Directory.  Unimportant");
            }
            //we first name the file with the prefix "UNSENT_".  If all goes well, it is renamed without the prefix, but if something fails it will still have it.
            file = new File(dir, "UNSENT_" + matchName);
            fileWriter = new PrintWriter(file);
        } catch (IOException ioe) {
            Log.e("File Error", "Failed to open file");
            toastText("Failed To Open File", Toast.LENGTH_LONG, context);
            return;
        }



        fileWriter.print(data);
        fileWriter.close();
        if (fileWriter.checkError()) {
            Log.e("File Error", "Failed to Write to File");
            toastText("Failed To Save Match Data To File", Toast.LENGTH_LONG, context);
            return;
        }



        PrintWriter out;
        BufferedReader in;
        BluetoothSocket socket;
        int counter = 0;
        //we loop until a connection is made
        while (true) {
            try {
                synchronized (deviceLock) {
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid)); // make this a constant somewhere nice
                }
                Log.i("Socket Info", "Attempting To Start Connection...");
                socket.connect();
                Log.i("Socket info", "Connection Successful!  Getting Ready To Send Data...");
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                break;
            } catch (IOException ioe) {
                Log.e("Socket Error", "Failed To Open Socket");
                toastText("Failed To Connect To Super", Toast.LENGTH_SHORT, context);
                counter++;
                //we try three times before giving up
                if (counter == 3) {
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
        //we loop until the data is sent without io error or error code from super
        while (true) {
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
                break;
            } catch (IOException ioe) {
                Log.e("Communications Error", "Failed To Send Data");
                toastText("Failed To Send Match Data To Super", Toast.LENGTH_SHORT, context);
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
        toastText("Data Send Success", Toast.LENGTH_LONG, context);
        try {
            if (!file.renameTo(new File(dir, matchName))) {
                Log.e("File Error", "Failed to Rename File");
            }
            in.close();
            out.close();
            socket.close();
        } catch (IOException ioe) {
            Log.e("Socket Error", "Failed To End Socket");
            toastText("Failed To Close Connection To Super", Toast.LENGTH_LONG, context);
        }
    }



    protected static void toastText(final String text, final int duration, final Activity context) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, duration).show();
            }
        });
    }
}
