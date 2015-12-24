package com.example.evan.bluetoothmanager;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class FileOptions extends AppCompatActivity {
    private String name;
    private Thread runningThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_options);
        TextView textView = (TextView) findViewById(R.id.fileTitle);
        name = getIntent().getStringExtra("matchName");
        if (name != null) {
            textView.setText(name);
        } else {
            Toast.makeText(this, "Failed To Open File", Toast.LENGTH_LONG).show();
        }
    }
    public void backToViewer(View view) {
        //This is done to be sure that the communications with the super are not ended early
        if (runningThread != null) {
            if (runningThread.isAlive()) {
                Toast.makeText(this, "Communications With Super Still In Progress...", Toast.LENGTH_LONG).show();
                return;
            }
        }
        startActivity(new Intent(this, FileViewer.class));
    }
    public void deleteFile(View view) {
        File file = new File(this.getFilesDir(), name);
        if (!file.delete()) {
            Toast.makeText(this, "Failed To Delete File", Toast.LENGTH_LONG).show();
        }
    }
    public void resendFile(View view) {
        BufferedReader file;
        try {
            file = new BufferedReader(new InputStreamReader(this.openFileInput(name)));
        } catch (IOException ioe) {
            Toast.makeText(this, "Failed To Open File", Toast.LENGTH_LONG).show();
            return;
        }
        String text = "";
        String buf;
        try {
            while ((buf = file.readLine()) != null) {
                text = text.concat(buf);
            }
        } catch (IOException ioe) {
            Toast.makeText(this, "Failed To Read From File", Toast.LENGTH_LONG).show();
        }
        runningThread = new ConnectThread(this, name, text);
        runningThread.start();
    }
}
