package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

public class ScheduleReceiver extends Thread {
    private Activity context;
    private String superName;
    private String uuid;
    private static BluetoothDevice device = null;
    private static final Object deviceLock = new Object();
    private static boolean isInit = false;
    private static final Object isInitLock = new Object();
    public ScheduleReceiver(Activity context, String superName, String uuid) {
        this.context = context;
        this.superName = superName;
        this.uuid = uuid;
    }


    private static boolean initBluetooth(final Activity context, String superName) {
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
        synchronized (isInitLock) {
            if (!isInit) {
                if(!initBluetooth(context, superName)) {
                    return;
                } else {
                    isInit = true;
                }
            }
        }
        for (int i = 0; i < 3; i++) {


            PrintWriter out = null;
            BufferedReader in = null;
            BluetoothSocket socket = null;
            boolean complete = false;
            int counter = 0;
            //we loop until a connection is made
            while (!complete) {
                try {
                    synchronized (deviceLock) {
                        socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuid)); // make this a constant somewhere nice
                    }
                    Log.i("Socket Info", "Attempting To Start Connection...");
                    socket.connect();
                    Log.i("Socket info", "Connection Successful!  Getting Ready To Send Data...");
                    out = new PrintWriter(socket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    complete = true;
                } catch (IOException ioe) {
                    Log.e("Socket Error", "Failed To Open Socket");
                    toastText("Failed To Connect To Super", Toast.LENGTH_SHORT, context);
                    complete = false;
                    counter++;
                    //first two times it fails, we immediately try again.  Next two we wait 30 seconds before trying again.
                    //On the 5th failure we terminate the thread and notify the user
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
            complete = false;
            //we loop until the data is sent without io error or error code from super
            while (!complete) {
                try {
                    //we print the length of the data before we print the data so the super can identify corrupted data
                    out.println("-1");
                    out.flush();
                    if (out.checkError()) {
                        throw new IOException();
                    }
                    complete = true;
                } catch (IOException ioe) {
                    Log.e("Communications Error", "Failed To Send Data");
                    toastText("Failed To Send Match Data To Super", Toast.LENGTH_SHORT, context);
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


            int ackCode = -1;
            String data = null;
            try {
                ackCode = Integer.parseInt(in.readLine());
                data = "";
                String tmp;
                while (true) {
                    tmp = in.readLine();
                    if (tmp.equals("\0")) {
                        break;
                    }
                    data = data.concat(tmp);
                }
            } catch (IOException ioe) {
                Log.e("Bluetooth Error", "Failed to receive schedule from super");
                toastText("Failed to receive schedule from super", Toast.LENGTH_SHORT, context);
                data = null;
            }

            if (data != null) {
                if (ackCode == data.length()) {
                    JSONObject schedule;
                    try {
                         schedule = new JSONObject(data);
                    } catch (JSONException jsone) {
                        Log.e("JSON Error", "Super sent bad schedule");
                        toastText("Bad Schedule", Toast.LENGTH_LONG, context);
                        return;
                    }
                    Log.e("Bluetooth Info", "Schedule receive success");
                    toastText("Schedule Receive Success", Toast.LENGTH_LONG, context);
                    onReceive(schedule);
                    return;
                }
            }


        }
        Log.e("Bluetooth Error", "Repeated Schedule Receive Error");
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(context)
                        .setTitle("Repeated Schedule Receive Failure")
                        .setMessage("Please resend this data when successful data transfer is made.")
                        .setNeutralButton("Dismiss", null)
                        .show();
            }
        });
    }



    public void onReceive (JSONObject schedule) {

    }



    private static void toastText(final String text, final int duration, final Activity context) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, duration).show();
            }
        });
    }
}
