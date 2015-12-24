package com.example.evan.bluetoothmanager;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    //TODO provide automatic resend of data on startup
    private Thread runningThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //DO THIS OR PROGRAM WILL CRASH WHEN YOU ROTATE THE SCREEN:
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ConnectThread.initBluetooth(this);
    }



    public void sendData(View view) {
        String data = "";
        for (int i = 0; i < 100; i++) {
            data = data.concat(UUID.randomUUID().toString());
        }
        runningThread = new ConnectThread(this, data, "Test-Data.txt");
        runningThread.start();
    }



    public void openFileViewer(View view) {
        //This is done to be sure that the communications with the super are not ended early
        if (runningThread != null) {
            if (runningThread.isAlive()) {
                Toast.makeText(this, "Communications With Super Still In Progress...", Toast.LENGTH_LONG).show();
                return;
            }
        }
        startActivity(new Intent(this, FileViewer.class));
    }
}
