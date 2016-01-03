package com.example.evan.bluetoothmanager;

//Main Activity is just for testing purposes and allows users to send random strings
//ConnectThread is a class that will send any string as the 'data' argument over bluetooth and saves it to a file with the 'matchName' argument
//FileViewer is an activity that displays files in the /sdcard/Android/MatchData directory, which is where ConnectThread will save it's data
//FileOptions is an activity called by FileViewer that gives users options to delete or resend files

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //DO THIS OR PROGRAM WILL CRASH WHEN YOU ROTATE THE SCREEN:
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }



    //'send' button on the ui
    //This is just for testing purposes
    public void sendData(View view) {
        String data = "";
        for (int i = 0; i < 100; i++) {
            data = data.concat(UUID.randomUUID().toString() + "\n");
        }
        new ConnectThread(this,"Test-Data_" + new SimpleDateFormat("MM-dd-yyyy-H:mm:ss").format(new Date()) + ".txt", data).start();
    }



    //'file viewer' button on the iu
    public void openFileViewer(View view) {
        startActivity(new Intent(this, FileViewer.class));
    }
}
