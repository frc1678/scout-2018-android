package com.example.evan.scout;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        uuid = getIntent().getStringExtra("uuid");
        superName = getIntent().getStringExtra("superName");



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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.auto_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.buttonNext) {

            JSONObject data = new JSONObject();
            ToggleButton hasReachedToggle = (ToggleButton) findViewById(R.id.autoReachedDefenseToggle);
            try {
                data.put("hasReachedDefense", hasReachedToggle.isChecked());
            } catch (JSONException jsone) {
                Log.e("JSON error", "Failed to add reached button state to Json");
                Toast.makeText(this, "Invalid data in reach toggle", Toast.LENGTH_LONG).show();
                return false;
            }

            List<View> intakeButtonList = toggleCreator.getComponentViews();
            for (int i = 0; i < intakeButtonList.size(); i++) {
                ToggleButton hasIntakedToggle = (ToggleButton) intakeButtonList.get(i);
                try {
                    data.put("intakedBall" + Integer.toString((i+1)), hasIntakedToggle.isChecked());
                } catch (JSONException jsone) {
                    Log.e("JSON error", "Failed to add intake button state to JSON");
                    Toast.makeText(this, "Invalid data in intake toggle", Toast.LENGTH_LONG).show();
                    return false;
                }
            }

            List<String> JsonCounterNames = new ArrayList<>();
            JsonCounterNames.add("numDefense1Crossed");
            JsonCounterNames.add("numBallsKnocked");
            JsonCounterNames.add("numDefense2Crossed");
            JsonCounterNames.add("numHighShotsMade");
            JsonCounterNames.add("numDefense3Crossed");
            JsonCounterNames.add("numHighShotsMissed");
            JsonCounterNames.add("numDefense4Crossed");
            JsonCounterNames.add("numLowShotsMade");
            JsonCounterNames.add("numDefense5Crossed");
            JsonCounterNames.add("numLowShotsMissed");

            List<View> currentTextViews = counterCreator.getComponentViews();
            for (int i = 0; i < currentTextViews.size(); i++) {
                TextView numCounter = (TextView) currentTextViews.get(i);
                try {
                    data.put(JsonCounterNames.get(i), Integer.parseInt(numCounter.getText().toString()));
                } catch (JSONException jsone) {
                    Log.e("JSON error", "Failed to add counter" + Integer.toString(i) + " num to JSON");
                    Toast.makeText(this, "Error in Counter number " + Integer.toString(i), Toast.LENGTH_LONG).show();
                    return false;
                }
            }

            startActivity(new Intent(this, TeleopActivity.class).putExtra("uuid", uuid).putExtra("superName", superName).putExtra("autoJSON", data.toString()));
        }
        return true;
    }
}
