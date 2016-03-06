package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeleopActivity extends AppCompatActivity {
    //data, in JSON format in a string, from auto activity
    //list of successful cross times for each defense
    /*private List<List<Long>> successCrossTimes;
    //list of failed cross times for each defense
    private List<List<Long>> failCrossTimes;*/

    private List<List<Map<Long, Boolean>>> combinedDefenseCrosses;

    //class to get and save counters
    private UIComponentCreator counterCreator;
    //class to get toggle buttons and save values to access later
    private UIComponentCreator toggleCreator;
    private int matchNumber;
    private boolean overridden;
    private int teamNumber;
    private String scoutName;
    private int scoutNumber;
    private JSONObject data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teleop);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //get fields from previous activity
        matchNumber = getIntent().getIntExtra("matchNumber", 1);
        overridden = getIntent().getBooleanExtra("overridden", false);
        teamNumber = getIntent().getIntExtra("teamNumber", -1);
        scoutName = getIntent().getStringExtra("scoutName");
        scoutNumber = getIntent().getIntExtra("scoutNumber", 1);



        setTitle("Scout Team " + Integer.toString(teamNumber));
        if (scoutNumber < 4) {
            //change actionbar color
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                //red
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#C40000")));
            }
        } else {
            //change actionbar color
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                //blue
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#4169e1")));
            }
        }



        //init lists
        /*successCrossTimes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            successCrossTimes.add(i, new ArrayList<Long>());
        }
        failCrossTimes = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            failCrossTimes.add(i, new ArrayList<Long>());
        }*/


        combinedDefenseCrosses = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            combinedDefenseCrosses.add(i, new ArrayList<Map<Long, Boolean>>());
        }



        List<Boolean> toggleValues = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            toggleValues.add(false);
        }

        List<Integer> counterValues = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            counterValues.add(0);
        }


        String autoJSON = getIntent().getStringExtra("autoJSON");
        Log.i("JSON at teleop activity", autoJSON);
        try {
            data = new JSONObject(autoJSON);
            List<String> toggleNames = new ArrayList<>(Arrays.asList("didChallengeTele", "didScaleTele",
                    "didGetDisabled", "didGetIncapacitated"));
            for (int i = 0; i < toggleNames.size(); i++) {
                toggleValues.add(i, data.getBoolean(toggleNames.get(i)));
            }

            List<String> counterNames = new ArrayList<>(Arrays.asList("numGroundIntakesTele",
                    "numHighShotsMadeTele", "numHighShotsMissedTele", "numLowShotsMadeTele", "numLowShotsMissedTele",
                    "numShotsBlockedTele"));
            for (int i = 0; i < counterNames.size(); i++) {
                counterValues.add(i, data.getInt(counterNames.get(i)));
            }

            /*JSONArray successTimes = data.getJSONArray("successfulDefenseCrossTimesTele");
            for (int i = 0; i < successTimes.length(); i++) {
                for (int j = 0; j < successTimes.getJSONArray(i).length(); j++) {
                    successCrossTimes.get(i).add(successTimes.getJSONArray(i).getLong(j));
                }
            }

            JSONArray failTimes = data.getJSONArray("failedDefenseCrossTimesTele");
            for (int i = 0; i < failTimes.length(); i++) {
                for (int j = 0; j < failTimes.getJSONArray(i).length(); j++) {
                    failCrossTimes.get(i).add(failTimes.getJSONArray(i).getLong(j));
                }
            }*/


            JSONArray defenseTimes = data.getJSONArray("defenseTimesTele");
            for (int i = 0; i < defenseTimes.length(); i++) {
                for (int j = 0; j < defenseTimes.getJSONArray(i).length(); j++) {
                    String key = defenseTimes.getJSONObject(i).keys().next();
                    Map<Long, Boolean> map = new HashMap<>();
                    map.put(Long.parseLong(key), defenseTimes.getJSONObject(i).getBoolean(key));
                    combinedDefenseCrosses.get(i).add(map);
                }
            }
        } catch (JSONException jsone) {
            Log.i("JSON info", "Failed to read teleop data.  Unimportant");
        }


        //populate toggles
        toggleCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList("Challenge",
                "Scale", "Disabled", "Incap.")));
        LinearLayout toggleLayout = (LinearLayout) findViewById(R.id.teleToggleButtonLinearLayout);
        //empty relative layout to space out buttons
        RelativeLayout fillerSpace = new RelativeLayout(this);
        fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.75f));
        toggleLayout.addView(fillerSpace);
        for (int i = 0; i < 2; i++) {
            final ToggleButton button1 = toggleCreator.getNextToggleButton(ViewGroup.LayoutParams.MATCH_PARENT, toggleValues.get(i*2));
            toggleLayout.addView(button1);
            fillerSpace = new RelativeLayout(this);
            fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.75f));
            toggleLayout.addView(fillerSpace);

            final ToggleButton button2 = toggleCreator.getNextToggleButton(ViewGroup.LayoutParams.MATCH_PARENT, toggleValues.get(i*2+1));
            toggleLayout.addView(button2);
            fillerSpace = new RelativeLayout(this);
            fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.75f));
            toggleLayout.addView(fillerSpace);

            button1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        button2.setChecked(false);
                    }
                }
            });
            button2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        button1.setChecked(false);
                    }
                }
            });
        }



        //fill row of defense buttons and textview counters
        LinearLayout defenseLayout = (LinearLayout) findViewById(R.id.teleDefenseButtonLinearLayout);
        UIComponentCreator.UIButtonCreator buttonCreator = new UIComponentCreator.UIButtonCreator(this, new ArrayList<>(Arrays.asList("Defense 1", "Defense 2", "Defense 3", "Defense 4",
                "Defense 5")));
        for (int i = 0; i < 5; i++) {
            buttonCreator.addButtonRow(defenseLayout, combinedDefenseCrosses, i);
        }




        //populate counters
        LinearLayout rowLayout = (LinearLayout) findViewById(R.id.teleCounterLinearLayout);
        counterCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList( "Ground Intakes",
                 "High Shots Made",  "High Shots Missed",  "Low Shots Made",
                 "Low Shots Missed", "Shots Blocked")));
        for (int i = 0; i < 6; i++) {
            rowLayout.addView(counterCreator.getNextTitleRow(1));
            rowLayout.addView(counterCreator.getNextCounterRow(1, counterValues.get(i)));
        }
    }



    public void updateData() {
        //json object to store everything
        if (data == null) {
            data = new JSONObject();
        }



        try {
            data.put("scoutName", scoutName);
        } catch (JSONException jsone) {
            Log.e("JSON error", "Failed to add scout name to JSON");
            Toast.makeText(this, "Invalid data in scout name", Toast.LENGTH_LONG).show();
            return;
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
                return;
            }
        }



        //add defenses crossed counters
        /*JSONArray successDefenseTimes = new JSONArray();
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
            return;
        }


        JSONArray failDefenseTimes = new JSONArray();
        for (int i = 0; i < failCrossTimes.size(); i++) {
            JSONArray tmp = new JSONArray();
            for (int j = 0; j < failCrossTimes.get(i).size(); j++) {
                tmp.put(failCrossTimes.get(i).get(j));
            }
            failDefenseTimes.put(tmp);
        }



        //add failed defense cross times to JSON
        try {
            data.put("failedDefenseCrossTimesTele", failDefenseTimes);
        } catch (JSONException jsone) {
            Log.e("JSON error", "Failed to add failed defense times to JSON");
            Toast.makeText(this, "Error in defense data", Toast.LENGTH_LONG).show();
            return;
        }*/



        try {
            JSONArray defenseTimes = new JSONArray();
            for (int i = 0; i < combinedDefenseCrosses.size(); i++) {
                JSONArray tmp = new JSONArray();
                for (int j = 0; j < combinedDefenseCrosses.get(i).size(); i++) {
                    JSONObject tmp2 = new JSONObject();
                    Map.Entry<Long, Boolean> firstEntry = combinedDefenseCrosses.get(i).get(j).entrySet().iterator().next();
                    tmp2.put(Long.toString(firstEntry.getKey()), firstEntry.getValue());
                    tmp.put(tmp2);
                }
            }
            data.put("defenseTimesTele", defenseTimes);
        } catch (JSONException jsone) {
            Log.e("JSON error", "Failed to add defense times to JSON");
            Toast.makeText(this, "Error in defense data", Toast.LENGTH_LONG).show();
            return;
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
                return;
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
            updateData();



            //wrap all the data with match number
            JSONObject finalData = new JSONObject();
            try {
                finalData.put(Integer.toString(teamNumber) + "Q" + Integer.toString(matchNumber), data);
            } catch (JSONException jsone) {
                Log.e("JSON error", "Error data");
                Toast.makeText(this, "Error in data", Toast.LENGTH_LONG).show();
                return false;
            }



            //move on to next match and restart main activity
            matchNumber++;
            startActivity(new Intent(this, MainActivity.class).putExtra("matchNumber", matchNumber)
                    .putExtra("overridden", overridden).putExtra("scoutName", scoutName).putExtra("matchData", finalData.toString())
                    .putExtra("matchName", "Q" + Integer.toString(matchNumber-1) + "_" + Integer.toString(teamNumber)));
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        final Activity context = this;
        new AlertDialog.Builder(this)
                .setTitle("Stop Scouting")
                .setMessage("Are you sure you want to go back? (your data will be saved)")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        updateData();
                        startActivity(new Intent(context, AutoActivity.class).putExtra("matchNumber", matchNumber).putExtra("overridden", overridden).putExtra("scoutName", scoutName)
                        .putExtra("autoJSON", data.toString()).putExtra("teamNumber", teamNumber).putExtra("scoutNumber", scoutNumber));
                    }
                })
                .show();
    }
}
