package com.example.evan.bluetoothmanager;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

// NOTE: I don't really care about viewing files right now, because we are working on Bluetooth. Just letting you know im not looking over this file.

public class FileOptions extends AppCompatActivity {
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_options);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        TextView textView = (TextView) findViewById(R.id.fileTitle);
        name = getIntent().getStringExtra("matchName");
        if (name != null) {
            textView.setText(name);
        } else {
            Log.e("File Error", "Failed To Open File");
            Toast.makeText(this, "Failed To Open File", Toast.LENGTH_LONG).show();
        }
    }



    //'back' button on ui
    public void backToViewer(View view) {
        startActivity(new Intent(this, FileViewer.class));
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
        new ConnectThread(this, name, text).start();
    }
}
