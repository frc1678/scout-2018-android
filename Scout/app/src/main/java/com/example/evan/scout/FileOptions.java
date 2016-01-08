package com.example.evan.scout;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class FileOptions extends AppCompatActivity {
    private String uuid;
    private String superName;
    private String name;
    private boolean canClick = true;
    private static final Object canClickLock = new Object();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_options);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        TextView textView = (TextView) findViewById(R.id.fileTitle);
        name = getIntent().getStringExtra("matchName");
        if (name != null) {
            textView.setText(name);
        } else {
            Log.e("File Error", "Failed To Open File");
            Toast.makeText(this, "Failed To Open File", Toast.LENGTH_LONG).show();
        }
        uuid = getIntent().getStringExtra("uuid");
        superName = getIntent().getStringExtra("superName");
    }



    //'back' button on ui
    public void backToViewer(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }



    //'delete' button on ui
    public void deleteFile(View view) {
        File file = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData/" + name);
        if (!file.delete()) {
            Log.e("File Error", "Failed To Delete File");
            Toast.makeText(this, "Failed To Delete File", Toast.LENGTH_LONG).show();
            return;
        }
        (findViewById(R.id.backButton2)).performClick();
    }



    //'resend' button on ui
    public void resendFile(View view) {
        if (canClick) {
            synchronized (canClickLock) {
                canClick = false;
            }
            ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
            timer.schedule(new Runnable() {
                @Override
                public void run() {
                    synchronized (canClickLock) {
                        canClick = true;
                    }
                }
            }, 5, TimeUnit.SECONDS);
            BufferedReader file;
            try {
                file = new BufferedReader(new InputStreamReader(new FileInputStream(
                        new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData/" + name))));
            } catch (IOException ioe) {
                Log.e("File Error", "Failed To Open File");
                Toast.makeText(this, "Failed To Open File", Toast.LENGTH_LONG).show();
                return;
            }
            String text = "";
            String buf;
            try {
                while ((buf = file.readLine()) != null) {
                    text = text.concat(buf + "\n");
                }
            } catch (IOException ioe) {
                Log.e("File Error", "Failed To Read From File");
                Toast.makeText(this, "Failed To Read From File", Toast.LENGTH_LONG).show();
                return;
            }
            final Activity context = this;
            new ConnectThread(this, superName, uuid, name, text) {
                @Override
                public void onFinish(boolean error) {
                    if (!error) {
                        context.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView textView = (TextView) context.findViewById(R.id.fileTitle);
                                if (name.contains("UNSENT_")) {
                                    textView.setText(name.replaceFirst("UNSENT_", ""));
                                }
                            }
                        });
                    }
                }
            }.start();
        }
    }
}
