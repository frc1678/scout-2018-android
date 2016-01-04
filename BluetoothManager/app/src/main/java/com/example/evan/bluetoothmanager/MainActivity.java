package com.example.evan.bluetoothmanager;

//Main Activity is just for testing purposes and allows users to send random strings
//ConnectThread is a class that will send any string as the 'data' argument over bluetooth and saves it to a file with the 'matchName' argument
//FileViewer is an activity that displays files in the /sdcard/Android/MatchData directory, which is where ConnectThread will save it's data
//FileOptions is an activity called by FileViewer that gives users options to delete or resend files

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final String uuid = "f8212682-9a34-11e5-8994-feff819cdc9f";
    private static final String superName = "red super";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //DO THIS OR PROGRAM WILL CRASH WHEN YOU ROTATE THE SCREEN:
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //new AutoClicker(this).start();
    }



    //'send' button on the ui
    //This is just for testing purposes
    public void sendData(View view) {
        String data = "";
        for (int i = 0; i < 27; i++) {
            data = data.concat(UUID.randomUUID().toString() + "\n");
        }
        new ConnectThread(this, superName, uuid, "Test-Data_" + new SimpleDateFormat("MM-dd-yyyy-H:mm:ss").format(new Date()) + ".txt", data).start();
    }



    //'file viewer' button on the iu
    public void openFileViewer(View view) {
        startActivity(new Intent(this, FileViewer.class));
    }


    //class that automatically clicks the send button every minute.  Used for testing only
    private class AutoClicker extends Thread {
        Activity context;
        public AutoClicker(Activity context) {
            this.context = context;
        }
        public void run() {
            while (true) {
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.findViewById(R.id.sendButton).performClick();
                    }
                });
                try {
                    Thread.sleep(60000);
                } catch (InterruptedException ie) {
                    Log.wtf("Thread Error", "Sleep interrupted");
                }
            }
        }
    }
}
