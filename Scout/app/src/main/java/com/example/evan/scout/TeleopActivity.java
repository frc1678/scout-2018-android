package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TeleopActivity extends AppCompatActivity {
    private String uuid;
    private String superName;
    private String autoJSON;
    private List<ToggleButton> toggleList;
    private List<Button> plusCounterButtons;
    private List<TextView> currentTextViews;
    private List<Button> minusCounterButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teleop);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        uuid = getIntent().getStringExtra("uuid");
        superName = getIntent().getStringExtra("superName");
        autoJSON = getIntent().getStringExtra("autoJSON");
        toggleList = new ArrayList<>();
        plusCounterButtons = new ArrayList<>();
        currentTextViews = new ArrayList<>();
        minusCounterButtons = new ArrayList<>();

        List<String> toggleNames = new ArrayList<>();
        toggleNames.add("Challenge");
        toggleNames.add("Scale");
        toggleNames.add("Disabled");
        toggleNames.add("Incap.");
        LinearLayout toggleLayout = (LinearLayout) findViewById(R.id.teleopToggleLinear);
        for (int i = 0; i < 4; i++) {
            ToggleButton toggleButton = new ToggleButton(this);
            toggleButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            toggleButton.setText(toggleNames.get(i));
            toggleButton.setTextOn(toggleNames.get(i));
            toggleButton.setTextOff(toggleNames.get(i));
            toggleLayout.addView(toggleButton);
            toggleList.add(toggleButton);
        }

        List<String> counterNames = new ArrayList<>();
        counterNames.add("Crossed Defense 1");
        counterNames.add("Ground Intakes");
        counterNames.add("Crossed Defense 2");
        counterNames.add("High Shots Made");
        counterNames.add("Crossed Defense 3");
        counterNames.add("High Shots Missed");
        counterNames.add("Crossed Defense 4");
        counterNames.add("Low Shots Made");
        counterNames.add("Crossed Defense 5");
        counterNames.add("Low Shots Missed");
        counterNames.add("Shots Blocked");
        LinearLayout rowLayout = (LinearLayout) findViewById(R.id.teleopCounterLinear);
        int nameOfCurrentCounter = 0;

        for (int i = 0; i < 12; i++) {
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            row.setOrientation(LinearLayout.HORIZONTAL);
            if ((i % 2) == 0) {
                int limit;
                if (i == 10) {
                    limit = 1;
                } else {
                    limit = 2;
                }
                for (int j = 0; j < limit; j++) {
                    TextView name = new TextView(this);
                    name.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                    name.setGravity(Gravity.CENTER);
                    name.setText(counterNames.get(nameOfCurrentCounter));
                    nameOfCurrentCounter++;
                    row.addView(name);
                }
            } else {
                int limit;
                if (i == 11) {
                    limit = 1;
                } else {
                    limit = 2;
                }
                for (int j = 0; j < limit; j++) {
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
        getMenuInflater().inflate(R.menu.teleop_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.teleopSendButton) {
            JSONObject data = new JSONObject();


            List<String> toggleVariableNames = new ArrayList<>();
            toggleVariableNames.add("didChallenge");
            toggleVariableNames.add("didScale");
            toggleVariableNames.add("wasDisabled");
            toggleVariableNames.add("wasIncap");
            for (int i = 0; i < toggleList.size(); i++) {
                ToggleButton toggleButton = toggleList.get(i);
                try {
                    data.put(toggleVariableNames.get(i), toggleButton.isChecked());
                } catch (JSONException jsone) {
                    Log.e("JSON error", "Failed to add toggle " + Integer.toString(i) + " to JSON");
                    Toast.makeText(this, "Invalid data in counter" + Integer.toString(i), Toast.LENGTH_LONG).show();
                    return false;
                }
            }

            List<String> counterVarNames = new ArrayList<>();
            counterVarNames.add("numDefense1Crossed");
            counterVarNames.add("numGroundIntakes");
            counterVarNames.add("numDefense2Crossed");
            counterVarNames.add("numHighShotsMade");
            counterVarNames.add("numDefense3Crossed");
            counterVarNames.add("numHighShotsMissed");
            counterVarNames.add("numDefense4Crossed");
            counterVarNames.add("numLowShotsMade");
            counterVarNames.add("numDefense5Crossed");
            counterVarNames.add("numLowShotsMissed");
            counterVarNames.add("numShotsBlocked");
            for (int i = 0; i < currentTextViews.size(); i++) {
                TextView numCounter = currentTextViews.get(i);
                try {
                    data.put(counterVarNames.get(i), Integer.parseInt(numCounter.getText().toString()));
                } catch (JSONException jsone) {
                    Log.e("JSON error", "Failed to add counter" + Integer.toString(i) + " num to JSON");
                    Toast.makeText(this, "Error in Counter number " + Integer.toString(i), Toast.LENGTH_LONG).show();
                    return false;
                }
            }


            try {
                data.put("autoData", autoJSON);
            } catch (JSONException jsone) {
                Log.e("JSON error", "Error in auto data?");
                Toast.makeText(this, "Failure in Auto Data", Toast.LENGTH_LONG);
                return false;
            }


            final Activity context = this;
            new ConnectThread(this, superName, uuid, "Test-Data_" + new SimpleDateFormat("MM-dd-yyyy-H:mm:ss", Locale.US).format(new Date()) + ".txt", data.toString() + "\n") {
                @Override
                public void onFileWriteFinish() {
                    context.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(context, MainActivity.class)/*.putExtra("FromTeleop", true)*/);
                        }
                    });
                }
            }.start();
        }
        return true;
    }
}
