package com.example.evan.bluetoothmanager;

//Main Activity is just for testing purposes and allows users to send random strings
//ConnectThread is a class that will send any string as the 'data' argument over bluetooth and saves it to a file with the 'matchName' argument
//FileViewer is an activity that displays files in the /sdcard/Documents/MatchData directory, which is where ConnectThread will save it's data
//FileOptions is an activity called by FileViewer that gives users options to delete or resend files

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.Random;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private Thread runningThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //DO THIS OR PROGRAM WILL CRASH WHEN YOU ROTATE THE SCREEN:
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ConnectThread.initBluetooth(this);
    }



    //'send' button on the ui
    //This is just for testing purposes
    public void sendData(View view) {
        String data = "";
        for (int i = 0; i < 100; i++) {
            data = data.concat(UUID.randomUUID().toString() + "\n");
        }
        //Asigns random variable for match number
        Random random = new Random();
        runningThread = new ConnectThread(this,"Test-Data-" + Integer.toString(random.nextInt(50)) + ".txt", data);
        runningThread.start();
    }



    //'file viewer' button on the iu
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
