package com.example.evan.bluetoothconnectiontest;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ConnectThread thread = new ConnectThread(this);
        thread.start();
    }
    private class ConnectThread extends Thread {
        private Context context;
        public ConnectThread(Context context) {
            this.context = context;
        }
        @Override
        public void run() {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                System.out.println("Device not configured with bluetooth");
                return;
            }
            if (!adapter.isEnabled()) {
                System.out.println("Bluetooth not enabled");
                return;
            }
            Set<BluetoothDevice> devices = adapter.getBondedDevices();
            if (devices.size() < 1) {
                System.out.println("No paired devices");
                return;
            }
            adapter.cancelDiscovery();
            for (BluetoothDevice device : devices) {
                if (device.getName().equals("red super")) {
                    BluetoothSocket socket;
                    try {
                        socket = device.createRfcommSocketToServiceRecord(UUID.fromString("f8212682-9a34-11e5-8994-feff819cdc9f"));
                        System.out.println("Attempting to start connection...");
                        socket.connect();
                        System.out.println("Connection successful!  Getting ready to send data...");
                        PrintWriter file = new PrintWriter(context.openFileOutput("Sent-Data.txt", Context.MODE_PRIVATE));
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        try {
                            sendMassData(out, file, 1000);
                        } catch (IllegalArgumentException iae) {
                            System.out.println("Illegal argument at call: \"sendMassData\". Check for typos");
                            return;
                        } catch (IOException ioe) {
                            System.out.println("Data Send Failure. Exiting...");
                            return;
                        }
                        System.out.println("Done");
                        out.println("\0");
                        out.flush();
                        file.close();
                        out.close();
                        socket.close();
                        return;
                    } catch (IOException ioe) {
                        System.out.println("Failed to open socket");
                        return;
                    }
                }
            }
            System.out.println("No paired device with name: \"red super\"");
        }
        private void sendMassData(PrintWriter out, PrintWriter file, int times) throws IllegalArgumentException, IOException {
            if (times < 1) {
                throw new IllegalArgumentException();
            }
            for (int i = 0; i < times; i++) {
                //UUID used for random string generation purposes only, not used for connection in any way:
                String randomString = UUID.randomUUID().toString();
                file.println(randomString);
                //System.out.println(randomString);
                out.println(randomString);
                out.flush();
                if (out.checkError()) {
                    throw new IOException();
                }
            }
        }
    }
}