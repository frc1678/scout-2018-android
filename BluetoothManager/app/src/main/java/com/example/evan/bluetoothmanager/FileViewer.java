package com.example.evan.bluetoothmanager;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

public class FileViewer extends AppCompatActivity {
    //TODO provide a resend all button?

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_viewer);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        File dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData/");
        if (!dir.mkdir()) {
            Log.i("File Info", "Failed to make Directory. Unimportant");
        }
        File[] files = dir.listFiles();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        for (File tmpFile : files) {
            adapter.add(tmpFile.getName());
        }
        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
        final Context context = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(context, FileOptions.class)
                        .putExtra("matchName", parent.getItemAtPosition(position).toString()));
            }
        });
    }



    //'back' button on ui
    public void backToMain(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }



    //'delete all' button on ui
    public void deleteAllFiles(View view) {
        final Context context = this;
        new AlertDialog.Builder(this)
                .setTitle("Delete All Files")
                .setMessage("Are you sure you want to delete all the files on this device?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData");
                        if (!dir.mkdir()) {
                            Log.i("File Info", "Failed to make Directory. Unimportant");
                        }
                        File[] files = dir.listFiles();
                        for (File tmpFile : files) {
                            if (!tmpFile.delete()) {
                                Log.e("File Error", "Failed To Delete File");
                                Toast.makeText(context, "Failed To Delete File", Toast.LENGTH_LONG).show();
                            }
                        }
                        recreate();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}
