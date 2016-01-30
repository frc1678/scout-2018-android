package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class TeleopActivity extends AppCompatActivity {
    //data, in JSON format in a string, from auto activity
    private String autoJSON;
    //list of successful cross times for each defense
    private List<List<Long>> successCrossTimes;
    //list of failed cross times for each defense
    private List<List<Long>> failCrossTimes;
    //class to get and save counters
    private UIComponentCreator counterCreator;
    //class to get toggle buttons and save values to access later
    private UIComponentCreator toggleCreator;
    private int matchNumber;
    private boolean overridden;
    private int teamNumber;
    private String scoutName;
    private String uuid;
    private String superName;
    private int scoutNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teleop);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //get fields from previous activity
        uuid = getIntent().getStringExtra("uuid");
        superName = getIntent().getStringExtra("superName");
        autoJSON = getIntent().getStringExtra("autoJSON");
        matchNumber = getIntent().getIntExtra("matchNumber", 1);
        overridden = getIntent().getBooleanExtra("overridden", false);
        teamNumber = getIntent().getIntExtra("teamNumber", -1);
        scoutName = getIntent().getStringExtra("scoutName");
        scoutNumber = getIntent().getIntExtra("scoutNumber", 1);




        //init lists
        successCrossTimes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            successCrossTimes.add(i, new ArrayList<Long>());
        }
        failCrossTimes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            failCrossTimes.add(i, new ArrayList<Long>());
        }


        //populate toggles
        toggleCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList("Challenge",
                "Scale", "Disabled", "Incap.")));
        LinearLayout toggleLayout = (LinearLayout) findViewById(R.id.teleToggleButtonLinearLayout);
        //empty relative layout to space out buttons
        RelativeLayout fillerSpace = new RelativeLayout(this);
        fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.75f));
        toggleLayout.addView(fillerSpace);
        for (int i = 0; i < 4; i++) {
            toggleLayout.addView(toggleCreator.getNextToggleButton(ViewGroup.LayoutParams.MATCH_PARENT));
            fillerSpace = new RelativeLayout(this);
            fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.75f));
            toggleLayout.addView(fillerSpace);
        }



        //fill row of defense buttons
        final Activity context = this;
        LinearLayout defenseLayout = (LinearLayout) findViewById(R.id.teleDefenseButtonLinearLayout);
        UIComponentCreator buttonCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList("Defense 1", "Defense 2", "Defense 3", "Defense 4",
                "Defense 5")));
        fillerSpace = new RelativeLayout(this);
        fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
        defenseLayout.addView(fillerSpace);
        for (int i = 0; i < 5; i++) {
            Button button = buttonCreator.getNextDefenseButton();
            //on click for buttons
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //first find out what button was clicked, based on the name of the button
                    final int buttonNum = Integer.parseInt(((Button) v).getText().toString().replaceAll("Defense ", "")) - 1;
                    //next get the time in milliseconds
                    final Long startTime = Calendar.getInstance().getTimeInMillis();
                    new AlertDialog.Builder(context)
                            .setTitle("Attempt Defense Cross")
                            .setPositiveButton("success", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //if they click success, add the time since they clicked the defense button to the list
                                    successCrossTimes.get(buttonNum).add(Calendar.getInstance().getTimeInMillis() - startTime);
                                }
                            })
                            .setNeutralButton("cancel", null)
                            .setNegativeButton("failure", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //if they clicked failure, add it to the fail list
                                    failCrossTimes.get(buttonNum).add(Calendar.getInstance().getTimeInMillis() - startTime);
                                }
                            })
                            .show();
                }
            });
            defenseLayout.addView(button);
            fillerSpace = new RelativeLayout(this);
            fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
            defenseLayout.addView(fillerSpace);
        }




        //populate counters
        LinearLayout rowLayout = (LinearLayout) findViewById(R.id.teleCounterLinearLayout);
        counterCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList( "Ground Intakes",
                 "High Shots Made",  "High Shots Missed",  "Low Shots Made",
                 "Low Shots Missed", "Shots Blocked")));
        for (int i = 0; i < 6; i++) {
            rowLayout.addView(counterCreator.getNextTitleRow(1));
            rowLayout.addView(counterCreator.getNextCounterRow(1));
        }
    }


    //add action bar button
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.teleop_menu, menu);
        return true;
    }


    //send data when action bar button clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.teleopSendButton) {
            //json object to store everything
            JSONObject data = new JSONObject();



            try {
                data.put("scoutName", scoutName);
            } catch (JSONException jsone) {
                Log.e("JSON error", "Failed to add scout name to JSON");
                Toast.makeText(this, "Invalid data in scout name", Toast.LENGTH_LONG).show();
                return false;
            }


            //add data in toggles
            List<String> toggleVariableNames = new ArrayList<>(Arrays.asList("didChallengeTele", "didScaleTele",
                    "didGetDisabled", "didGetIncapacitated"));
            List<View> toggleList = toggleCreator.getComponentViews();
            for (int i = 0; i < toggleList.size(); i++) {
                ToggleButton toggleButton = (ToggleButton) toggleList.get(i);
                try {
                    data.put(toggleVariableNames.get(i), toggleButton.isChecked());
                } catch (JSONException jsone) {
                    Log.e("JSON error", "Failed to add toggle " + Integer.toString(i) + " to JSON");
                    Toast.makeText(this, "Invalid data in counter" + Integer.toString(i), Toast.LENGTH_LONG).show();
                    return false;
                }
            }



            //add defenses crossed counters
            JSONArray successDefenseTimes = new JSONArray();
            for (int i = 0; i < successCrossTimes.size(); i++) {
                JSONArray tmp = new JSONArray();
                for (int j = 0; j < successCrossTimes.get(i).size(); j++) {
                    tmp.put(successCrossTimes.get(i).get(j));
                }
                successDefenseTimes.put(tmp);
            }



            //add successful defense cross times to JSON
            try {
                data.put("successfulDefenseCrossTimesTele", successDefenseTimes);
            } catch (JSONException jsone) {
                Log.e("JSON error", "Failed to add successful defense times to JSON");
                Toast.makeText(this, "Error in defense data", Toast.LENGTH_LONG).show();
                return false;
            }


            JSONArray failDefenseTimes = new JSONArray();
            for (int i = 0; i < failCrossTimes.size(); i++) {
                JSONArray tmp = new JSONArray();
                for (int j = 0; j < failCrossTimes.get(i).size(); j++) {
                    tmp.put(failCrossTimes.get(i).get(j));
                }
                failDefenseTimes.put(tmp);
            }



            //add successful defense fail times to JSON
            try {
                data.put("failedDefenseCrossTimesTele", failDefenseTimes);
            } catch (JSONException jsone) {
                Log.e("JSON error", "Failed to add failed defense times to JSON");
                Toast.makeText(this, "Error in defense data", Toast.LENGTH_LONG).show();
                return false;
            }


            //add data in other counters
            List<View> currentTextViews = counterCreator.getComponentViews();
            List<String> counterVarNames = new ArrayList<>(Arrays.asList("numGroundIntakesTele",
                    "numHighShotsMadeTele", "numHighShotsMissedTele", "numLowShotsMadeTele", "numLowShotsMissedTele",
                    "numShotsBlockedTele"));
            for (int i = 0; i < currentTextViews.size(); i++) {
                try {
                    data.put(counterVarNames.get(i), Integer.parseInt(((TextView) currentTextViews.get(i)).getText().toString()));
                } catch (JSONException jsone) {
                    Log.e("JSON error", "Failed to add counter" + Integer.toString(i) + " num to JSON");
                    Toast.makeText(this, "Error in Counter number " + Integer.toString(i), Toast.LENGTH_LONG).show();
                    return false;
                }
            }


            //add auto data to JSON
            JSONObject autoData;
            try {
                autoData = new JSONObject(autoJSON);
            } catch (JSONException jsone) {
                Log.e("JSON error", "Error in auto data?");
                Toast.makeText(this, "Failure in Auto Data", Toast.LENGTH_LONG).show();
                return false;
            }



            Iterator<String> autoKeys = autoData.keys();
            while (autoKeys.hasNext()) {
                String key = autoKeys.next();
                try {
                    data.put(key, autoData.get(key));
                } catch (JSONException jsone) {
                    Log.e("JSON error", "Error in auto data?");
                    Toast.makeText(this, "Failure in Auto Data", Toast.LENGTH_LONG).show();
                    return false;
                }
            }



            try {
                if (scoutNumber < 4) {
                    data.put("alliance", "red");
                } else {
                    data.put("alliance", "blue");
                }
            } catch (JSONException jsone) {
                Log.e("JSON error", "Error in scoutNumber");
                Toast.makeText(this, "Failure in Scout Number", Toast.LENGTH_LONG).show();
                return false;
            }



            //wrap all the data with match number
            JSONObject finalData = new JSONObject();
            try {
                finalData.put(Integer.toString(teamNumber) + "Q" + Integer.toString(matchNumber), data);
            } catch (JSONException jsone) {
                Log.e("JSON error", "Error data");
                Toast.makeText(this, "Error in data", Toast.LENGTH_LONG).show();
                return false;
            }



            //we now send data in mainactivity, so errors appear there
            //send data to bluetooth
            /*new ConnectThread(this, superName, uuid,
                    "Test-Data_" + new SimpleDateFormat("MM-dd-yyyy-H:mm:ss", Locale.US).format(new Date()) + ".txt",
                    finalData.toString() + "\n").start();*/
            Log.i("JSON data", finalData.toString());
            //move on to next match and restart main activity
            matchNumber++;
            startActivity(new Intent(this, MainActivity.class).putExtra("matchNumber", matchNumber)
                    .putExtra("overridden", overridden).putExtra("scoutName", scoutName).putExtra("matchData", finalData.toString()));
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        final Activity context = this;
        new AlertDialog.Builder(this)
                .setTitle("Stop Scouting")
                .setMessage("If you go back now, all data will be lost.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(context, AutoActivity.class).putExtra("matchNumber", matchNumber).putExtra("overridden", overridden).putExtra("scoutName", scoutName));
                    }
                })
                .show();
    }
}
