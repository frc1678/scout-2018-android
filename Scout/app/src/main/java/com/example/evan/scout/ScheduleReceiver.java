package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.UUID;

//modified ConnectThread class specifically for receiving schedule JSON from super
public class ScheduleReceiver extends ConnectThread {
    public ScheduleReceiver(Activity context, String superName, String uuid) {
        super(context, superName, uuid, null, null);
    }



    @Override
    public void run() {
        //if not initialized, init bluetooth
        synchronized (isInitLock) {
            if (!isInit) {
                if(!initBluetooth(context, superName)) {
                    return;
                } else {
                    isInit = true;
                }
            }
        }



        //try the whole process three times before quitting
        for (int i = 0; i < 3; i++) {
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
                    //if this is the third time, give up
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
                    //we print -1 to request schedule
                    out.println("-1");
                    out.flush();
                    if (out.checkError()) {
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


            int ackCode = -1;
            String data;
            try {
                //first line will be length of schedule
                ackCode = Integer.parseInt(in.readLine());
                data = "";
                String tmp;
                while (true) {
                    tmp = in.readLine();
                    //'\0' signifies end
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
                //validate ack code
                if (ackCode == data.length()) {
                    JSONObject schedule;
                    //validate json
                    try {
                         schedule = new JSONObject(data);
                    } catch (JSONException jsone) {
                        Log.e("JSON Error", "Super sent bad schedule");
                        toastText("Bad Schedule", Toast.LENGTH_LONG, context);
                        return;
                    }
                    Log.i("Bluetooth Info", "Schedule receive success");
                    toastText("Schedule Receive Success", Toast.LENGTH_LONG, context);
                    onReceive(schedule);
                    //close sockets and stuff
                    try {
                        in.close();
                        out.close();
                        socket.close();
                    } catch (IOException ioe) {
                        Log.e("Socket Error", "Failed To End Socket");
                        toastText("Failed To Close Connection To Super", Toast.LENGTH_LONG, context);
                        return;
                    }
                    return;
                }
            }


        }
        //after third time trying, we give up
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



    //override this method to handle JSONObject
    public void onReceive (JSONObject schedule) {

    }
}
