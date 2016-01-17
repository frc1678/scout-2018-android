package com.example.evan.scout;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.FileObserver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


public class MainActivity extends AppCompatActivity {
    private static final String uuid = "f8212682-9a34-11e5-8994-feff819cdc9f";
    private static final String superName = "red super";
    //private static final String superName = "G Pad 7.0 LTE";
    private FileObserver fileObserver;
    private ArrayAdapter<String> fileListAdapter;
    private int matchNumber;
    private boolean overridden = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);


        matchNumber = getIntent().getIntExtra("matchNumber", 1);
        EditText matchNumberText = (EditText) findViewById(R.id.matchNumberText);
        matchNumberText.setText("Q" + Integer.toString(matchNumber));
        overridden = getIntent().getBooleanExtra("overridden", false);
        if (overridden) {
            matchNumberText.setFocusableInTouchMode(true);
            invalidateOptionsMenu();
        } else {
            matchNumberText.setFocusable(false);
        }


        //set up list of files
        final Activity context = this;
        fileListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ListView fileList = (ListView) findViewById(R.id.infoList);
        fileList.setAdapter(fileListAdapter);
        updateListView();
        //when you click on a file, it sends it
        fileList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    final String name = parent.getItemAtPosition(position).toString();
                    //read data from file
                    BufferedReader file;
                    try {
                        file = new BufferedReader(new InputStreamReader(new FileInputStream(
                                new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData/" + name))));
                    } catch (IOException ioe) {
                        Log.e("File Error", "Failed To Open File");
                        Toast.makeText(context, "Failed To Open File", Toast.LENGTH_LONG).show();
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
                        Toast.makeText(context, "Failed To Read From File", Toast.LENGTH_LONG).show();
                        return;
                    }
                    //send it to super
                    new ConnectThread(context, superName, uuid, name, text).start();
                }
        });
        //update list view when something is renamed
        fileObserver = new FileObserver(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData") {
            @Override
            public void onEvent(int event, String path) {
                if ((event == FileObserver.MOVED_TO)) {
                    Log.i("File Observer", "detected file close");
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateListView();
                        }
                    });
                }
            }
        };
        fileObserver.startWatching();
    }


    //'delete all' button on ui
    /*public void deleteAllFiles(View view) {
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
    }*/





    //scout button on ui
    public void startScout (View view) {
        fileObserver.stopWatching();
        if (overridden) {
            EditText matchNumberText = (EditText) findViewById(R.id.matchNumberText);
            String current = matchNumberText.getText().toString();
            current = current.replaceAll("Q", "");
            matchNumber = Integer.parseInt(current);
        }
        startActivity(new Intent(this, AutoActivity.class).putExtra("uuid", uuid).putExtra("superName", superName)
                .putExtra("matchNumber", matchNumber).putExtra("overridden", overridden));
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
        fileListAdapter.clear();
        for (File tmpFile : files) {
            fileListAdapter.add(tmpFile.getName());
        }
        fileListAdapter.notifyDataSetChanged();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!overridden) {
            getMenuInflater().inflate(R.menu.main_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.main_menu2, menu);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.mainOverride) {
            EditText matchNumberText = (EditText) findViewById(R.id.matchNumberText);
            matchNumberText.setFocusableInTouchMode(true);
            overridden = true;
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.mainAutomate) {
            EditText matchNumberText = (EditText) findViewById(R.id.matchNumberText);
            String current = matchNumberText.getText().toString();
            current = current.replaceAll("Q", "");
            matchNumber = Integer.parseInt(current);
            current = "Q" + current;
            matchNumberText.setText(current);
            matchNumberText.setFocusable(false);
            overridden = false;
            invalidateOptionsMenu();
        } else if (item.getItemId() == R.id.scheduleButton) {
            new ScheduleReceiver(this, superName, uuid) {
                @Override
                public void onReceive(JSONObject schedule) {
                    Log.i("Schedule", schedule.toString());
                }
            }.start();
        }
        return true;
    }
}
