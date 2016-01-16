package com.example.evan.scout;

import android.app.Activity;
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
import android.widget.Toolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AutoActivity extends AppCompatActivity {
    private String uuid;
    private String superName;
    private List<ToggleButton> intakeButtonList;
    private List<Button> plusCounterButtons;
    private List<Button> minusCounterButtons;
    private List<TextView> currentTextViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        uuid = getIntent().getStringExtra("uuid");
        superName = getIntent().getStringExtra("superName");
        intakeButtonList = new ArrayList<>();
        plusCounterButtons = new ArrayList<>();
        minusCounterButtons = new ArrayList<>();
        currentTextViews = new ArrayList<>();



        //populate the row of toggle buttons for ball intakes on midline
        LinearLayout intakeLayout = (LinearLayout) findViewById(R.id.autoIntakeButtonLinearLayout);
        for (int i = 0; i < 6; i++) {
            ToggleButton intakeButton = new ToggleButton(this);
            intakeButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            intakeButton.setText(Integer.toString(i + 1) + " Intaked");
            intakeButton.setTextOn(Integer.toString(i + 1) + " Intaked");
            intakeButton.setTextOff(Integer.toString(i + 1) + " Intaked");
            intakeLayout.addView(intakeButton);
            intakeButtonList.add(intakeButton);
        }



        //populate counters for everything
        List<String> counterNames = new ArrayList<>();
        counterNames.add("Crossed Defense 1");
        counterNames.add("Balls Knocked Off Mid");
        counterNames.add("Crossed Defense 2");
        counterNames.add("High Shots Made");
        counterNames.add("Crossed Defense 3");
        counterNames.add("High Shots Missed");
        counterNames.add("Crossed Defense 4");
        counterNames.add("Low Shots Made");
        counterNames.add("Crossed Defense 5");
        counterNames.add("Low Shots Missed");
        LinearLayout rowLayout = (LinearLayout) findViewById(R.id.autoCounterLinearLayout);
        int nameOfCurrentCounter = 0;

        for (int i = 0; i < 10; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.setOrientation(LinearLayout.HORIZONTAL);
            if ((i % 2) == 0) {
                for (int j = 0; j < 2; j++) {
                    TextView name = new TextView(this);
                    name.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    name.setGravity(Gravity.CENTER);
                    name.setText(counterNames.get(nameOfCurrentCounter));
                    nameOfCurrentCounter++;
                    row.addView(name);
                }
            } else {
                for (int j = 0; j < 2; j++) {
                    final Button minus = new Button(this);
                    minus.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    minus.setText("-");
                    minus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int prevNum = Integer.parseInt(currentTextViews.get(minusCounterButtons.indexOf(minus)).getText().toString());
                            if (prevNum > 0) {
                                prevNum--;
                            }
                            currentTextViews.get(minusCounterButtons.indexOf(minus)).setText(Integer.toString(prevNum));
                        }
                    });
                    row.addView(minus);
                    minusCounterButtons.add(minus);

                    TextView currentCount = new TextView(this);
                    currentCount.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    currentCount.setText("0");
                    currentCount.setGravity(Gravity.CENTER);
                    row.addView(currentCount);
                    currentTextViews.add(currentCount);

                    final Button plus = new Button(this);
                    plus.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    plus.setText("+");
                    plus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int prevNum = Integer.parseInt(currentTextViews.get(plusCounterButtons.indexOf(plus)).getText().toString());
                            if (prevNum < 3) {
                                prevNum++;
                            }
                            currentTextViews.get(minusCounterButtons.indexOf(minus)).setText(Integer.toString(prevNum));
                        }
                    });
                    row.addView(plus);
                    plusCounterButtons.add(plus);
                }
            }
            rowLayout.addView(row);
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

            for (int i = 0; i < intakeButtonList.size(); i++) {
                ToggleButton hasIntakedToggle = intakeButtonList.get(i);
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

            for (int i = 0; i < currentTextViews.size(); i++) {
                TextView numCounter = currentTextViews.get(i);
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
