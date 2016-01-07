package com.example.evan.scout;

import android.app.Activity;
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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private static final String uuid = "f8212682-9a34-11e5-8994-feff819cdc9f";
    //private static final String superName = "red super";
    private static final String superName = "G Pad 7.0 LTE";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        updateListView();
        ListView listView = (ListView) findViewById(R.id.infoList);
        final Context context = this;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(context, FileOptions.class)
                        .putExtra("matchName", parent.getItemAtPosition(position).toString()));
            }
        });
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
                        updateListView();
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }



    //'send' button on ui
    public void sendData (View view) {
        String data;
        String tmp;
        EditText editText = (EditText) findViewById(R.id.nameEdit);
        tmp = editText.getText().toString();
        if (tmp.equals("")) {
            Toast.makeText(this, "Please Enter All Data", Toast.LENGTH_LONG).show();
            return;
        }
        data = tmp + "\n";
        editText = (EditText) findViewById(R.id.teamEdit);
        tmp = editText.getText().toString();
        if (tmp.equals("")) {
            Toast.makeText(this, "Please Enter All Data", Toast.LENGTH_LONG).show();
            return;
        }
        data = data.concat(tmp + "\n");
        editText = (EditText) findViewById(R.id.scoreEdit);
        tmp = editText.getText().toString();
        if (tmp.equals("")) {
            Toast.makeText(this, "Please Enter All Data", Toast.LENGTH_LONG).show();
            return;
        }
        data = data.concat(tmp + "\n");
        new ConnectThread(this, superName, uuid, "Test-Data_" + new SimpleDateFormat("MM-dd-yyyy-H:mm:ss").format(new Date()) + ".txt", data, new Runnable() {
            @Override
            public void run() {
                updateListView();
            }
        }).start();
    }



    private void updateListView() {
        File dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData/");
        if (!dir.mkdir()) {
            Log.i("File Info", "Failed to make Directory. Unimportant");
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        for (File tmpFile : files) {
            adapter.add(tmpFile.getName());
        }
        ListView listView = (ListView) findViewById(R.id.infoList);
        listView.setAdapter(adapter);
    }
}
