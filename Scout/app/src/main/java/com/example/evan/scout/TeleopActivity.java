package com.example.evan.scout;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class TeleopActivity extends AppCompatActivity {
    private String uuid;
    private String superName;
    private String autoJSON;
    private UIComponentCreator counterCreator;
    private UIComponentCreator toggleCreator;
    private int matchNumber;
    private boolean overridden;
    private int teamNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teleop);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        uuid = getIntent().getStringExtra("uuid");
        superName = getIntent().getStringExtra("superName");
        autoJSON = getIntent().getStringExtra("autoJSON");
        matchNumber = getIntent().getIntExtra("matchNumber", 1);
        overridden = getIntent().getBooleanExtra("overridden", false);
        teamNumber = getIntent().getIntExtra("teamNumber", -1);


        //populate toggles
        toggleCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList("Challenge",
                "Scale", "Disabled", "Incap.")));
        LinearLayout toggleLayout = (LinearLayout) findViewById(R.id.teleopToggleLinear);
        for (int i = 0; i < 4; i++) {
            toggleLayout.addView(toggleCreator.getNextToggleButton());
        }



        //populate counters
        LinearLayout rowLayout = (LinearLayout) findViewById(R.id.teleopCounterLinear);
        counterCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList("Crossed Defense 1", "Ground Intakes",
                "Crossed Defense 2", "High Shots Made", "Crossed Defense 3", "High Shots Missed", "Crossed Defense 4", "Low Shots Made",
                "Crossed Defense 5", "Low Shots Missed", "Shots Blocked")));
        for (int i = 0; i < 5; i++) {
            rowLayout.addView(counterCreator.getNextTitleRow(2));
            rowLayout.addView(counterCreator.getNextCounterRow(2));
        }
        rowLayout.addView(counterCreator.getNextTitleRow(1));
        rowLayout.addView(counterCreator.getNextCounterRow(1));
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
            List<View> currentTextViews = counterCreator.getComponentViews();
            JSONArray timesDefensesCrossedTele = new JSONArray();
            for (int i = 0; i < currentTextViews.size()-1; i++) {
                try {
                    Log.i("Current TextViews", ((TextView)currentTextViews.get(i)).getText().toString());
                    timesDefensesCrossedTele.put(i, Integer.parseInt(((TextView) currentTextViews.get(i)).getText().toString()));
                    currentTextViews.remove(i);
                } catch (JSONException jsone) {
                    Log.e("JSON error", "Failed to add counter" + Integer.toString(i) + " num to JSON");
                    Toast.makeText(this, "Error in Counter number " + Integer.toString(i), Toast.LENGTH_LONG).show();
                    return false;
                }
            }

            try {
                data.put("timesDefensesCrossedTele", timesDefensesCrossedTele);
            } catch (JSONException jsone) {
                Log.e("JSON error", "Failed to add defense crossed counters to JSON");
                Toast.makeText(this, "Error in Defense counters", Toast.LENGTH_LONG).show();
                return false;
            }


            //add data in other counters
            List<String> counterVarNames = new ArrayList<>(Arrays.asList("numGroundIntakedsTele",
                    "numHighShotsMadeTele", "numHighShotsMissedTele", "numLowShotsMadeTele", "numLowShotsMissedTele",
                    "numShotsBlockedTele"));
            for (int i = 0; i < currentTextViews.size(); i++) {
                try {
                    Log.i("Current TextViews", ((TextView)currentTextViews.get(i)).getText().toString()+ ">>>>>>" + Integer.toString(currentTextViews.size()));
                    data.put(counterVarNames.get(i), Integer.parseInt(((TextView) currentTextViews.get(i)).getText().toString()));
                } catch (JSONException jsone) {
                    Log.e("JSON error", "Failed to add counter" + Integer.toString(i) + " num to JSON");
                    Toast.makeText(this, "Error in Counter number " + Integer.toString(i), Toast.LENGTH_LONG).show();
                    return false;
                }
            }


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



            //wrap all the data with match number
            JSONObject finalData = new JSONObject();
            try {
                finalData.put(Integer.toString(teamNumber) + "Q" + Integer.toString(matchNumber), data);
            } catch (JSONException jsone) {
                Log.e("JSON error", "Error data");
                Toast.makeText(this, "Error in data", Toast.LENGTH_LONG).show();
                return false;
            }



            //send data to bluetooth
            new ConnectThread(this, superName, uuid,
                    "Test-Data_" + new SimpleDateFormat("MM-dd-yyyy-H:mm:ss", Locale.US).format(new Date()) + ".txt",
                    finalData.toString() + "\n").start();
            Log.i("JSON data", finalData.toString());
            //move on to next match and restart main activity
            matchNumber++;
            startActivity(new Intent(this, MainActivity.class).putExtra("matchNumber", matchNumber)
                    .putExtra("overridden", overridden));
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, AutoActivity.class).putExtra("matchNumber", matchNumber).putExtra("overridden", overridden));
    }
}
