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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AutoActivity extends AppCompatActivity {
    //class to get toggle buttons and save values to access later
    private UIComponentCreator toggleCreator;
    //class to get and save counters
    private UIComponentCreator counterCreator;
    //list of successful cross times for each defense
    /*private List<List<Long>> successCrossTimes;
    //list of failed cross times for each defense
    private List<List<Long>> failCrossTimes;*/


    private List<List<Map<Long, Boolean>>> combinedDefenseCrosses;


    private int matchNumber;
    private boolean overridden;
    private int teamNumber;
    private String scoutName;
    private int scoutNumber;
    private JSONObject data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //get fields from previous activity
        matchNumber = getIntent().getIntExtra("matchNumber", 1);
        overridden = getIntent().getBooleanExtra("overridden", false);
        teamNumber = getIntent().getIntExtra("teamNumber", -1);
        scoutName = getIntent().getStringExtra("scoutName");
        scoutNumber = getIntent().getIntExtra("scoutNumber", 1);


        //set the title to have the team number
        setTitle("Scout Team " + Integer.toString(teamNumber));
        //set action bar color
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


        //the ui will look to these values for the starting values when the app starts
        List<Integer> toggleValues = new ArrayList<>();

        List<Integer> counterValues = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            counterValues.add(0);
        }

        boolean reached = false;


        //if the user pressed 'back' from the teleop activity we have to reset the data to as it was
        String autoJSON = getIntent().getStringExtra("autoJSON");
        if (autoJSON != null) {
            Log.i("JSON at auto activity", autoJSON);
            try {
                //parse the JSONObject
                data = new JSONObject(autoJSON);
                JSONArray toggles = data.getJSONArray("ballsIntakedAuto");
                for (int i = 0; i < toggles.length(); i++) {
                    toggleValues.add(i, toggles.getInt(i));
                }

                List<String> counterNames = new ArrayList<>(Arrays.asList("numBallsKnockedOffMidlineAuto",
                        "numHighShotsMadeAuto", "numHighShotsMissedAuto", "numLowShotsMadeAuto", "numLowShotsMissedAuto"));
                for (int i = 0; i < counterNames.size(); i++) {
                    counterValues.add(i, data.getInt(counterNames.get(i)));
                }

                /*JSONArray successTimes = data.getJSONArray("successfulDefenseCrossTimesAuto");
                for (int i = 0; i < successTimes.length(); i++) {
                    for (int j = 0; j < successTimes.getJSONArray(i).length(); j++) {
                        successCrossTimes.get(i).add(successTimes.getJSONArray(i).getLong(j));
                    }
                }

                JSONArray failTimes = data.getJSONArray("failedDefenseCrossTimesAuto");
                for (int i = 0; i < failTimes.length(); i++) {
                    for (int j = 0; j < failTimes.getJSONArray(i).length(); j++) {
                        failCrossTimes.get(i).add(failTimes.getJSONArray(i).getLong(j));
                    }
                }*/



                JSONArray defenseTimes = data.getJSONArray("defenseTimesAuto");
                for (int i = 0; i < defenseTimes.length(); i++) {
                    for (int j = 0; j < defenseTimes.getJSONArray(i).length(); j++) {
                        String key = defenseTimes.getJSONArray(i).getJSONObject(j).keys().next();
                        Map<Long, Boolean> map = new HashMap<>();
                        map.put(Long.parseLong(key), defenseTimes.getJSONArray(i).getJSONObject(j).getBoolean(key));
                        combinedDefenseCrosses.get(i).add(map);
                    }
                }
                reached = data.getBoolean("didReachAuto");
            } catch (JSONException jsone) {
                Log.e("JSON error", "Failed to parse previous auto data");
                Toast.makeText(this, "Invalid data from previous activity", Toast.LENGTH_LONG).show();
                data = null;
            }
        }


        ToggleButton reachButton = (ToggleButton) findViewById(R.id.autoReachedDefenseToggle);
        reachButton.setChecked(reached);



        //populate the row of toggle buttons for ball intakes on midline
        LinearLayout intakeLayout = (LinearLayout) findViewById(R.id.autoIntakeButtonLinearLayout);
        toggleCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList("1 Intaked", "2 Intaked",
                "3 Intaked", "4 Intaked", "5 Intaked", "6 Intaked")));
        for (int i = 0; i < 6; i++) {
            boolean checked = (toggleValues.indexOf(i) != -1);
            intakeLayout.addView(toggleCreator.getNextToggleButton(ViewGroup.LayoutParams.WRAP_CONTENT, checked));
        }


        //fill row of defense buttons with textview counters
        LinearLayout defenseLayout = (LinearLayout) findViewById(R.id.autoDefenseButtonLinearLayout);
        UIComponentCreator.UIButtonCreator buttonCreator = new UIComponentCreator.UIButtonCreator(this, new ArrayList<>(Arrays.asList("Defense 1", "Defense 2", "Defense 3", "Defense 4",
                "Defense 5")));
        //empty relative layout to space out buttons
        for (int i = 0; i < 5; i++) {
            buttonCreator.addButtonRow(defenseLayout, combinedDefenseCrosses, i);
        }



        //populate counters for everything
        LinearLayout rowLayout = (LinearLayout) findViewById(R.id.autoCounterLinearLayout);
        counterCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList("Balls Knocked Off Mid",
                 "High Shots Made",  "High Shots Missed",  "Low Shots Made",
                 "Low Shots Missed")));

        for (int i = 0; i < 5; i++) {
            rowLayout.addView(counterCreator.getNextTitleRow(1));
            rowLayout.addView(counterCreator.getNextCounterRow(1, counterValues.get(i)));
        }
    }



    private void updateData() {
        //json object to store auto data
        if (data == null) {
            data = new JSONObject();
        }

        //add reach toggle
        ToggleButton hasReachedToggle = (ToggleButton) findViewById(R.id.autoReachedDefenseToggle);
        try {
            data.put("didReachAuto", hasReachedToggle.isChecked());
        } catch (JSONException jsone) {
            Log.e("JSON error", "Failed to add reached button state to Json");
            Toast.makeText(this, "Invalid data in reach toggle", Toast.LENGTH_LONG).show();
            return;
        }

        //add ball intake toggles
        List<View> intakeButtonList = toggleCreator.getComponentViews();
        JSONArray ballsIntakedAuto = new JSONArray();
        int counter = 0;
        for (int i = 0; i < intakeButtonList.size(); i++) {
            try {
                if (((ToggleButton) intakeButtonList.get(i)).isChecked()) {
                    ballsIntakedAuto.put(counter, i);
                    counter++;
                }
            } catch (JSONException jsone) {
                Log.e("JSON error", "Failed to add intake button state to JSON");
                Toast.makeText(this, "Invalid data in intake toggle", Toast.LENGTH_LONG).show();
                return;
            }
        }


        try {
            data.put("ballsIntakedAuto", ballsIntakedAuto);
        } catch (JSONException jsone) {
            Log.e("JSON error", "Failed to add balls intaked toggles to JSON");
            Toast.makeText(this, "Error in intake toggles", Toast.LENGTH_LONG).show();
            return;
        }


        //add successful defense cross times to JSON
        /*JSONArray successDefenseTimes = new JSONArray();
        for (int i = 0; i < successCrossTimes.size(); i++) {
            JSONArray tmp = new JSONArray();
            for (int j = 0; j < successCrossTimes.get(i).size(); j++) {
                tmp.put(successCrossTimes.get(i).get(j));
            }
            successDefenseTimes.put(tmp);
        }


        try {
            data.put("successfulDefenseCrossTimesAuto", successDefenseTimes);
        } catch (JSONException jsone) {
            Log.e("JSON error", "Failed to add successful defense times to JSON");
            Toast.makeText(this, "Error in defense data", Toast.LENGTH_LONG).show();
            return;
        }


        //add successful defense fail times to JSON
        JSONArray failDefenseTimes = new JSONArray();
        for (int i = 0; i < failCrossTimes.size(); i++) {
            JSONArray tmp = new JSONArray();
            for (int j = 0; j < failCrossTimes.get(i).size(); j++) {
                tmp.put(failCrossTimes.get(i).get(j));
            }
            failDefenseTimes.put(tmp);
        }


        try {
            data.put("failedDefenseCrossTimesAuto", failDefenseTimes);
        } catch (JSONException jsone) {
            Log.e("JSON error", "Failed to add failed defense times to JSON");
            Toast.makeText(this, "Error in defense data", Toast.LENGTH_LONG).show();
            return;
        }*/


        try {
            JSONArray defenseTimes = new JSONArray();
            for (int i = 0; i < combinedDefenseCrosses.size(); i++) {
                JSONArray tmp = new JSONArray();
                for (int j = 0; j < combinedDefenseCrosses.get(i).size(); j++) {
                    JSONObject tmp2 = new JSONObject();
                    Map.Entry<Long, Boolean> firstEntry = combinedDefenseCrosses.get(i).get(j).entrySet().iterator().next();
                    tmp2.put(Long.toString(firstEntry.getKey()), firstEntry.getValue());
                    tmp.put(tmp2);
                }
                defenseTimes.put(tmp);
            }
            data.put("defenseTimesAuto", defenseTimes);
        } catch (JSONException jsone) {
            Log.e("JSON error", "Failed to add defense times to JSON");
            Toast.makeText(this, "Error in defense data", Toast.LENGTH_LONG).show();
            return;
        }


        //add all the data in other counters
        List<View> currentTextViews = counterCreator.getComponentViews();
        List<String> JsonCounterNames = new ArrayList<>(Arrays.asList("numBallsKnockedOffMidlineAuto",
                "numHighShotsMadeAuto", "numHighShotsMissedAuto", "numLowShotsMadeAuto", "numLowShotsMissedAuto"));
        for (int i = 0; i < currentTextViews.size(); i++) {
            try {
                data.put(JsonCounterNames.get(i), Integer.parseInt(((TextView) currentTextViews.get(i)).getText().toString()));
            } catch (JSONException jsone) {
                Log.e("JSON error", "Failed to add counter" + Integer.toString(i) + " num to JSON");
                Toast.makeText(this, "Error in Counter number " + Integer.toString(i), Toast.LENGTH_LONG).show();
                return;
            }
        }
    }



    //add button to action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.auto_menu, menu);
        return true;
    }



    //move on to teleop when action bar button clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.buttonNext) {
            updateData();


            //send it all to teleop activity
            startActivity(new Intent(this, TeleopActivity.class).putExtra("autoJSON", data.toString())
            .putExtra("matchNumber", matchNumber).putExtra("overridden", overridden).putExtra("teamNumber", teamNumber).putExtra("scoutName", scoutName)
            .putExtra("scoutNumber", scoutNumber));
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
                        startActivity(new Intent(context, MainActivity.class).putExtra("matchNumber", matchNumber).putExtra("overridden", overridden).putExtra("scoutName", scoutName));
                    }
                })
                .show();
    }
}
