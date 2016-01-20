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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoActivity extends AppCompatActivity {
    private String uuid;
    private String superName;
    private UIComponentCreator toggleCreator;
    private UIComponentCreator counterCreator;
    private int matchNumber;
    private boolean overridden;
    private int teamNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        uuid = getIntent().getStringExtra("uuid");
        superName = getIntent().getStringExtra("superName");
        matchNumber = getIntent().getIntExtra("matchNumber", 1);
        overridden = getIntent().getBooleanExtra("overridden", false);
        teamNumber = getIntent().getIntExtra("teamNumber", -1);



        //populate the row of toggle buttons for ball intakes on midline
        LinearLayout intakeLayout = (LinearLayout) findViewById(R.id.autoIntakeButtonLinearLayout);
        toggleCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList("1 Intaked", "2 Intaked",
                "3 Intaked", "4 Intaked", "5 Intaked", "6 Intaked")));
        for (int i = 0; i < 6; i++) {
            intakeLayout.addView(toggleCreator.getNextToggleButton());
        }



        //populate counters for everything
        LinearLayout rowLayout = (LinearLayout) findViewById(R.id.autoCounterLinearLayout);
        counterCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList("Crossed Defense 1", "Balls Knocked Off Mid",
                "Crossed Defense 2", "High Shots Made", "Crossed Defense 3", "High Shots Missed", "Crossed Defense 4", "Low Shots Made",
                "Crossed Defense 5", "Low Shots Missed")));

        for (int i = 0; i < 5; i++) {
            rowLayout.addView(counterCreator.getNextTitleRow(2));
            rowLayout.addView(counterCreator.getNextCounterRow(2));
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

            //json object to store auto data
            JSONObject data = new JSONObject();

            //add reach toggle
            ToggleButton hasReachedToggle = (ToggleButton) findViewById(R.id.autoReachedDefenseToggle);
            try {
                data.put("didReachAuto", hasReachedToggle.isChecked());
            } catch (JSONException jsone) {
                Log.e("JSON error", "Failed to add reached button state to Json");
                Toast.makeText(this, "Invalid data in reach toggle", Toast.LENGTH_LONG).show();
                return false;
            }

            //add ball intake toggles
            List<View> intakeButtonList = toggleCreator.getComponentViews();
            JSONArray ballsIntakedAuto = new JSONArray();
            for (int i = 0; i < intakeButtonList.size(); i++) {
                try {
                    ballsIntakedAuto.put(i, ((ToggleButton) intakeButtonList.get(i)).isChecked());
                } catch (JSONException jsone) {
                    Log.e("JSON error", "Failed to add intake button state to JSON");
                    Toast.makeText(this, "Invalid data in intake toggle", Toast.LENGTH_LONG).show();
                    return false;
                }
            }


            try {
                data.put("ballsIntakedAuto", ballsIntakedAuto);
            } catch (JSONException jsone) {
                Log.e("JSON error", "Failed to add balls intaked toggles to JSON");
                Toast.makeText(this, "Error in intake toggles", Toast.LENGTH_LONG).show();
                return false;
            }


            //add all the defense crossed counters
            List<View> currentTextViews = counterCreator.getComponentViews();
            JSONArray timesDefensesCrossedAuto = new JSONArray();
            for (int i = 0; i < currentTextViews.size(); i++) {
                try {
                    timesDefensesCrossedAuto.put(i, Integer.parseInt(((TextView) currentTextViews.get(i)).getText().toString()));
                    currentTextViews.remove(i);
                } catch (JSONException jsone) {
                    Log.e("JSON error", "Failed to add counter" + Integer.toString(i) + " num to JSON");
                    Toast.makeText(this, "Error in Counter number " + Integer.toString(i), Toast.LENGTH_LONG).show();
                    return false;
                }
            }

            try {
                data.put("timesDefensesCrossedAuto", timesDefensesCrossedAuto);
            } catch (JSONException jsone) {
                Log.e("JSON error", "Failed to add defense crossed counters to JSON");
                Toast.makeText(this, "Error in Defense counters", Toast.LENGTH_LONG).show();
                return false;
            }



            //add all the data in other counters
            List<String> JsonCounterNames = new ArrayList<>(Arrays.asList("numBallsKnockedOffMidlineAuto",
                    "numHighShotsMadeAuto", "numHighShotsMissedAuto", "numLowShotsMadeAuto", "numLowShotsMissedAuto"));
            for (int i = 0; i < currentTextViews.size(); i++) {
                try {
                    data.put(JsonCounterNames.get(i/2), Integer.parseInt(((TextView) currentTextViews.get(i)).getText().toString()));
                } catch (JSONException jsone) {
                    Log.e("JSON error", "Failed to add counter" + Integer.toString(i) + " num to JSON");
                    Toast.makeText(this, "Error in Counter number " + Integer.toString(i), Toast.LENGTH_LONG).show();
                    return false;
                }
            }

            //send it all to teleop activity
            startActivity(new Intent(this, TeleopActivity.class).putExtra("uuid", uuid).putExtra("superName", superName).putExtra("autoJSON", data.toString())
            .putExtra("matchNumber", matchNumber).putExtra("overridden", overridden).putExtra("teamNumber", teamNumber));
        }
        return true;
    }




    @Override
    public void onBackPressed() {
        Log.i("matchNumber", Integer.toString(matchNumber));
        startActivity(new Intent(this, MainActivity.class).putExtra("matchNumber", matchNumber).putExtra("overridden", overridden));
    }
}
