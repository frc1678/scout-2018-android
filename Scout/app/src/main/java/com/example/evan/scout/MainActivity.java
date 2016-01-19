package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

//TODO pass team nums and add them along with match num to json
//TODO get device name and display teams, highlight edittexts accordingly
public class MainActivity extends AppCompatActivity {
    //uuid for bluetooth connection
    private static final String uuid = "f8212682-9a34-11e5-8994-feff819cdc9f";
    //paired device to connect to as super:
    //private static final String superName = "red super";
    private static final String superName = "G Pad 7.0 LTE";
    //used to update list of sent files when they are modified
    private FileObserver fileObserver;
    //current list of sent files
    private ArrayAdapter<String> fileListAdapter;
    //current match the scout is on
    private int matchNumber;
    //whether the automatic match progression is overridden or not
    private boolean overridden = false;
    //schedule of matches
    private JSONObject schedule = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //lock screen horizontal
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        //get any values received from other activities
        matchNumber = getIntent().getIntExtra("matchNumber", 1);
        overridden = getIntent().getBooleanExtra("overridden", false);




        //all of the following is just getting the schedule from the hard drive:
        //first open up file
        boolean scheduleAvailable = true;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/Schedule");
        if (!dir.mkdir()) {
            Log.i("File Info", "Failed to make Directory. Unimportant");
        }
        File scheduleFile = new File(dir, "Schedule.txt");
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(scheduleFile)));
        } catch (IOException ioe) {
            Log.e("File Error", "Failed to open schedule file");
            Toast.makeText(this, "Schedule not avaialble", Toast.LENGTH_LONG).show();
            scheduleAvailable = false;
        }
        //next read from it
        String scheduleString = "";
        if (scheduleAvailable) {
            try {
                String tmp;
                while ((tmp = in.readLine()) != null) {
                    scheduleString = scheduleString.concat(tmp);
                }
            } catch (IOException ioe) {
                Log.e("File Error", "Failed to read from schedule file");
                Toast.makeText(this, "Schedule not avaialble", Toast.LENGTH_LONG).show();
                scheduleAvailable = false;
            }
        }
        //finally parse it to json format
        if (scheduleAvailable) {
            try {
                schedule = new JSONObject(scheduleString);
            } catch (JSONException jsone) {
                Log.e("File Error", "Failed to parse JSON from schedule file");
                Toast.makeText(this, "Schedule not avaialble", Toast.LENGTH_LONG).show();
                scheduleAvailable = false;
                schedule = null;
            }
        }





        final EditText matchNumberTextView = (EditText) findViewById(R.id.matchNumberText);
        matchNumberTextView.setText("Q" + Integer.toString(matchNumber));
        final Activity context = this;
        matchNumberTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (overridden) {
                    final EditText editText = new EditText(context);
                    editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                    editText.setHint("Match Number");
                    new AlertDialog.Builder(context)
                            .setTitle("Set Match")
                            .setView(editText)
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    try {
                                        matchNumber = Integer.parseInt(editText.getText().toString());
                                    } catch (NumberFormatException nfe) {
                                        matchNumber = 1;
                                    }
                                    matchNumberTextView.setText("Q" + Integer.toString(matchNumber));
                                    updateTeamNumbers();
                                }
                            })
                            .show();
                }
            }
        });
        updateTeamNumbers();






        //TODO change this according to scout number
        TextView scoutTeamText = (TextView) findViewById(R.id.teamNumber1Edit);
        scoutTeamText.setBackgroundColor(Color.parseColor("#64FF64"));





        //if we don't have the schedule, they must enter the team numbers and it must be overridden.  If not, give them the choice
        if (!scheduleAvailable) {
            override();
        } else if (overridden) {
            override();
        } else {
            automate();
        }










        //set up list of sent files
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
        int teamNumber;
        try {
            //TODO change this according to scout number
            EditText teamNumber1Edit = (EditText) findViewById(R.id.teamNumber1Edit);
            teamNumber = Integer.parseInt(teamNumber1Edit.getText().toString());
        } catch (NumberFormatException nfe) {
            Toast.makeText(this, "Please enter valid team numbers", Toast.LENGTH_LONG);
            return;
        }
        fileObserver.stopWatching();
        startActivity(new Intent(this, AutoActivity.class).putExtra("uuid", uuid).putExtra("superName", superName)
                .putExtra("matchNumber", matchNumber).putExtra("overridden", overridden)
                .putExtra("teamNumber", teamNumber));
    }





    //update the list of sent files
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



    //update the menu at top of screen, either giving them the option to override or automate
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!overridden) {
            getMenuInflater().inflate(R.menu.main_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.main_menu2, menu);
        }
        return true;
    }


    //buttons at menu at top of screen:
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //override button
        if (item.getItemId() == R.id.mainOverride) {
            override();



            //automate button
        } else if (item.getItemId() == R.id.mainAutomate) {
            automate();




            //get schedule button
        } else if (item.getItemId() == R.id.scheduleButton) {
            final Activity context = this;
            new ScheduleReceiver(this, superName, uuid) {
                @Override
                public void onReceive(JSONObject receivedSchedule) {
                    //handle the JSONObject received from super:
                    Log.i("Schedule", receivedSchedule.toString());
                    File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/Schedule");
                    if (!dir.mkdir()) {
                        Log.i("File Info", "Failed to make Directory. Unimportant");
                    }
                    File scheduleFile = new File(dir, "Schedule.txt");
                    PrintWriter out;
                    try {
                         out = new PrintWriter(scheduleFile);
                    } catch (IOException ioe) {
                        Log.e("File Error", "Failed to save schedule data to file");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Failed to save schedule to file", Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    }
                    try {
                        out.println(receivedSchedule.toString());
                        if (out.checkError()) {
                            throw new IOException();
                        }
                    } catch (IOException ioe) {
                        Log.e("File Error", "Failed to save schedule data to file");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(context, "Failed to save schedule to file", Toast.LENGTH_LONG).show();
                            }
                        });
                        return;
                    }
                    schedule = receivedSchedule;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateTeamNumbers();
                        }
                    });
                }
            }.start();
        }
        return true;
    }



    //override the schedule
    private void override () {
        overridden = true;
        invalidateOptionsMenu();
    }
    //automate the schedule
    private void automate() {
        if (schedule != null) {
            overridden = false;
            invalidateOptionsMenu();
        } else {
            Toast.makeText(this, "Schedule not available. Please get schedule", Toast.LENGTH_LONG).show();
        }
    }



    //make sure the fileobserver gets stopped if the user presses back
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        fileObserver.stopWatching();
    }




    public void editTeamNumber(final View view) {
        final Activity context = this;
        if (overridden) {
            final EditText editText = new EditText(context);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setHint("Team Number");
            new AlertDialog.Builder(context)
                    .setTitle("Set Team Number")
                    .setView(editText)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TextView textView = (TextView) view;
                            int teamNum;
                            try {
                                 teamNum = Integer.parseInt(editText.getText().toString());
                            } catch (NumberFormatException nfe) {
                                return;
                            }
                            textView.setText(Integer.toString(teamNum));
                        }
                    })
                    .show();
        }
    }




    public void updateTeamNumbers() {
        if (schedule != null) {
            EditText teamNumber1Edit = (EditText) findViewById(R.id.teamNumber1Edit);
            EditText teamNumber2Edit = (EditText) findViewById(R.id.teamNumber2Edit);
            EditText teamNumber3Edit = (EditText) findViewById(R.id.teamNumber3Edit);
            try {
                JSONArray red = schedule.getJSONObject("Q" + matchNumber).getJSONArray("red");
                //TODO change this according to scout color
                teamNumber1Edit.setText(red.getString(0));
                teamNumber2Edit.setText(red.getString(1));
                teamNumber3Edit.setText(red.getString(2));
            } catch (JSONException jsone) {
                Log.e("JSON error", "Failed to read JSON");
                teamNumber1Edit.setText("");
                teamNumber2Edit.setText("");
                teamNumber3Edit.setText("");
            }
        }
    }
}
