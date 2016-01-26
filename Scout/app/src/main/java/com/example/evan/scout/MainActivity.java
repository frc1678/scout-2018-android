package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.FileObserver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity {
    //uuid for bluetooth connection
    private static final String uuid = "f8212682-9a34-11e5-8994-feff819cdc9f";

    //paired device to connect to as super:
    private String superName;
    //private static final String redSuperName = "red super";
    //private static final String blueSuperName = "blue super";
    private static final String redSuperName = "G Pad 7.0 LTE";
    private static final String blueSuperName = "G Pad 7.0 LTE";

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

    //shared preferences to receive previous matchNumber, scoutNumber
    private SharedPreferences preferences;
    private static final String PREFERENCES_FILE = "com.example.evan.scout";

    //the id of the scout.  1-3 is red, 4-6 is blue
    private int scoutNumber;

    //we highlight the edittext that has the team number that this scout needs to scout, but if they change their id we need to reset it
    //this is the original background that was with the edittext
    private Drawable originalEditTextDrawable;

    //initials of scout scouting
    private String scoutName;

    //number of team this scout needs to scout
    private int teamNumber;

    //save a reference to this activity for subclasses
    final Activity context = this;

    //when resending files, indicates whether the user pressed the 'cancel resend' button or not
    private boolean continueResend = true;

    //an onclicklistener for the 'resend all' button, declared globally to be reused
    private View.OnClickListener originalResendAllOnClick;

    //list of unsent file names, only updated when 'resend all' button is pressed
    private List<String> unsentFileNames;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("test", "at 1");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //lock screen horizontal
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //see comment on this variable above
        originalEditTextDrawable = findViewById(R.id.teamNumber1Edit).getBackground();

        unsentFileNames = new ArrayList<>();

        //get any values received from other activities
        preferences = getSharedPreferences(PREFERENCES_FILE, 0);
        //overridden
        overridden = getIntent().getBooleanExtra("overridden", false);
        //match number
        matchNumber = getIntent().getIntExtra("matchNumber", -1);
        //if matchNumber was not passed from a previous activity, load it from hard disk
        if (matchNumber == -1) {
            matchNumber = preferences.getInt("matchNumber", 1);
            //otherwise, save it to hard disk
        } else {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt("matchNumber", matchNumber);
            editor.commit();
        }
        //scout id
        scoutNumber = preferences.getInt("scoutNumber", -1);
        //if we don't have it, get it
        if (scoutNumber == -1) {
            setScoutNumber();
            //if we have it, change edittexts accordingly
        } else {
            highlightTeamNumberTexts();
        }
        //scout initials
        scoutName = getIntent().getStringExtra("scoutName");




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



        //set the match number edittext's onclick to open a dialog.  We do this so the screen does not shrink and the user can see what he/she types
        final EditText matchNumberTextView = (EditText) findViewById(R.id.matchNumberText);
        matchNumberTextView.setText("Q" + Integer.toString(matchNumber));
        matchNumberTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //display dialog if overridden
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
                                    //when they click done, we get the matchnumber from what they put
                                    try {
                                        matchNumber = Integer.parseInt(editText.getText().toString());
                                    } catch (NumberFormatException nfe) {
                                        matchNumber = 1;
                                    }
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt("matchNumber", matchNumber);
                                    editor.commit();
                                    matchNumberTextView.setText("Q" + Integer.toString(matchNumber));
                                    updateTeamNumbers();
                                }
                            })
                            .show();
                }
            }
        });
        updateTeamNumbers();


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
                    sendFile(name);
                }
        });



        //update list view when something is renamed
        fileObserver = new FileObserver(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData") {
            @Override
            public void onEvent(int event, String path) {
                if ((event == FileObserver.MOVED_TO) || (event == FileObserver.CREATE)) {
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


        //initialize 'resend all' button
        final Button resendAllButton = (Button) findViewById(R.id.resendAll);
        originalResendAllOnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //when clicked, update unsent file list
                for (int i = 0; i < fileListAdapter.getCount(); i++) {
                    String name = fileListAdapter.getItem(i);
                    if (name.contains("UNSENT_")) {
                        unsentFileNames.add(name);
                    }
                }
                //set the button to cancel the resend process
                resendAllButton.setText("cancel resend");
                resendAllButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        continueResend = false;
                        cancelResend();
                    }
                });
                //and then resend all the files
                resendAllFiles();
            }
        };
        resendAllButton.setOnClickListener(originalResendAllOnClick);

        Log.i("test", "at 2");



        //if there is data to send from teleop activity, send it.
        //we send it in this activity so all error dialogs will appear here
        //we send it at the end of oncreate because sometimes android will call oncreate twice, so if we put it at the end this will not happen
        String matchData = getIntent().getStringExtra("matchData");
        if (matchData != null) {
            new ConnectThread(this, superName, uuid,
                    "Test-Data_" + new SimpleDateFormat("MM-dd-yyyy-H:mm:ss", Locale.US).format(new Date()) + ".txt",
                    matchData + "\n").start();
        }
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



    //highlight the edittext with the team number of the team that this scout will be scouting
    private void highlightTeamNumberTexts() {
        TextView scoutTeamText1 = (TextView) this.findViewById(R.id.teamNumber1Edit);
        TextView scoutTeamText2 = (TextView) this.findViewById(R.id.teamNumber2Edit);
        TextView scoutTeamText3 = (TextView) this.findViewById(R.id.teamNumber3Edit);
        if (scoutNumber%3 == 1) {
            scoutTeamText1.setBackgroundColor(Color.parseColor("#64FF64"));
            scoutTeamText2.setBackground(originalEditTextDrawable);
            scoutTeamText3.setBackground(originalEditTextDrawable);
        } else if (scoutNumber%3 == 2) {
            scoutTeamText2.setBackgroundColor(Color.parseColor("#64FF64"));
            scoutTeamText1.setBackground(originalEditTextDrawable);
            scoutTeamText3.setBackground(originalEditTextDrawable);
        } else if (scoutNumber%3 == 0) {
            scoutTeamText3.setBackgroundColor(Color.parseColor("#64FF64"));
            scoutTeamText1.setBackground(originalEditTextDrawable);
            scoutTeamText2.setBackground(originalEditTextDrawable);
        }



        //change ui depending on color
        if (scoutNumber < 4) {
            //update paired device name
            superName = redSuperName;

            //change actionbar color
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                //red
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#C40000")));
            }
        } else {
            //update paired device name
            superName = blueSuperName;

            //change actionbar color
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                //blue
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4169e1")));
            }
        }
        updateTeamNumbers();
    }



    //fill in the edittexts with the team numbers found in the schedule
    public void updateTeamNumbers() {
        if (schedule != null) {
            EditText teamNumber1Edit = (EditText) findViewById(R.id.teamNumber1Edit);
            EditText teamNumber2Edit = (EditText) findViewById(R.id.teamNumber2Edit);
            EditText teamNumber3Edit = (EditText) findViewById(R.id.teamNumber3Edit);
            try {
                //TODO JSONArray red = schedule.getJSONObject(Integer.toString(matchNumber)).getJSONArray("redAllianceTeamNumbers");
                if (scoutNumber < 3) {
                    JSONArray red = schedule.getJSONObject("Q" + matchNumber).getJSONArray("red");
                    teamNumber1Edit.setText(red.getString(0));
                    teamNumber2Edit.setText(red.getString(1));
                    teamNumber3Edit.setText(red.getString(2));
                } else {
                    JSONArray blue = schedule.getJSONObject("Q" + matchNumber).getJSONArray("blue");
                    teamNumber1Edit.setText(blue.getString(0));
                    teamNumber2Edit.setText(blue.getString(1));
                    teamNumber3Edit.setText(blue.getString(2));
                }
            } catch (JSONException jsone) {
                Log.e("JSON error", "Failed to read JSON");
                teamNumber1Edit.setText("");
                teamNumber2Edit.setText("");
                teamNumber3Edit.setText("");
            }
        }
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



    //display dialog to set scout number
    private void setScoutNumber() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setHint("Scout ID");
        new AlertDialog.Builder(this)
                .setTitle("Set Scout ID")
                .setView(editText)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            scoutNumber = Integer.parseInt(editText.getText().toString());
                            if ((scoutNumber < 0) || (scoutNumber > 6)) {
                                throw new NumberFormatException();
                            }
                        } catch (NumberFormatException nfe) {
                            setScoutNumber();
                        }
                        highlightTeamNumberTexts();
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putInt("scoutNumber", scoutNumber);
                        editor.commit();
                    }
                })
                .show();
    }



    //update actionbar at top of screen, either giving them the option to override or automate
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!overridden) {
            //this is a menu with override as a button
            getMenuInflater().inflate(R.menu.main_menu, menu);
        } else {
            //this is a menu with automate as a button
            getMenuInflater().inflate(R.menu.main_menu2, menu);
        }
        return true;
    }



    //onclicks for buttons on actionbar
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //override button
        if (item.getItemId() == R.id.mainOverride) {
            override();



            //automate button
        } else if (item.getItemId() == R.id.mainAutomate) {
            automate();



            //set scout id button
        } else if (item.getItemId() == R.id.setScoutIDButton) {
            setScoutNumber();



            //get schedule button
        } else if (item.getItemId() == R.id.scheduleButton) {
            //start new thread to receive schedule
            new ScheduleReceiver(this, superName, uuid) {
                @Override
                public void onReceive(JSONObject receivedSchedule) {
                    //handle the JSONObject received from super:
                    Log.i("Schedule", receivedSchedule.toString());
                    //open file
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

                    //write to file
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

                    //save reference to schedule
                    schedule = receivedSchedule;

                    //update ui with schedule
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



    //onclick for edittexts containing team numbers
    //again, we display dialogs to prevent screen shrinking
    public void editTeamNumber(final View view) {
        if (overridden) {
            final EditText editText = new EditText(this);
            editText.setInputType(InputType.TYPE_CLASS_NUMBER);
            editText.setHint("Team Number");
            new AlertDialog.Builder(this)
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



    //resend all button on ui
    public void resendAllFiles() {
        //if the 'cancel resend' button has not been clicked
        if (continueResend) {
            Log.i("test", "here");
            ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();
            String name;
            //get the first unsent file in the list, and remove it
            try {
                name = unsentFileNames.remove(0);
            } catch (IndexOutOfBoundsException ioobe) {
                cancelResend();
                return;
            }
            //send it to super
            sendFile(name);
            if (unsentFileNames.size() != 0) {
                //finally if there is another file in the list, wait 5 seconds before sending it again
                timer.schedule(new Runnable() {
                    @Override
                    public void run() {
                        resendAllFiles();
                    }
                }, 5, TimeUnit.SECONDS);
                return;
            }
            //if there is not another file in the list, stop the resend process
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cancelResend();
                }
            });
        } else {
            //if the user did click the 'cancel resend' button, reset the flag
            continueResend = true;
        }
    }



    public void cancelResend() {
        //clear list of unsent files
        unsentFileNames.clear();
        //reset button
        Button resendAllButton = (Button) findViewById(R.id.resendAll);
        resendAllButton.setText("resend all");
        resendAllButton.setOnClickListener(originalResendAllOnClick);
    }



    //read file from hard disk and send it to super
    private void sendFile (String name) {
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



    //scout button on ui
    public void startScout (View view) {
        //collect the team number
        try {
            if (scoutNumber%3 == 1) {
                TextView scoutTeamText = (TextView) findViewById(R.id.teamNumber1Edit);
                teamNumber = Integer.parseInt(scoutTeamText.getText().toString());
            } else if (scoutNumber%3 == 2) {
                TextView scoutTeamText = (TextView) findViewById(R.id.teamNumber2Edit);
                teamNumber = Integer.parseInt(scoutTeamText.getText().toString());
            } else if (scoutNumber%3 == 0) {
                TextView scoutTeamText = (TextView) findViewById(R.id.teamNumber3Edit);
                teamNumber = Integer.parseInt(scoutTeamText.getText().toString());
            } else {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException nfe) {
            Toast.makeText(this, "Please enter valid team numbers", Toast.LENGTH_LONG).show();
            return;
        }
        fileObserver.stopWatching();
        startAutoActivity();
    }



    //in order to redisplay the dialog to ask for scout initials, we start a new method, and recursively call the method if the input is wrong
    public void startAutoActivity() {
        if (scoutName == null) {
            final EditText editText = new EditText(this);
            editText.setHint("Scout Initials");
            new AlertDialog.Builder(this)
                    .setTitle("Set Scout Initials")
                    .setView(editText)
                    .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            scoutName = editText.getText().toString();
                            if (scoutName.equals("")) {
                                scoutName = null;
                            }
                            startAutoActivity();
                        }
                    })
                    .show();
        } else {
            startActivity(new Intent(this, AutoActivity.class)
                    .putExtra("matchNumber", matchNumber).putExtra("overridden", overridden)
                    .putExtra("teamNumber", teamNumber).putExtra("scoutName", scoutName));
        }
    }
}
