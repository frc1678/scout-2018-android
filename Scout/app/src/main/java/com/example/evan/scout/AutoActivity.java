package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class AutoActivity extends AppCompatActivity {
    //class to get toggle buttons and save values to access later
    private UIComponentCreator toggleCreator;
    //class to get and save counters
    private UIComponentCreator counterCreator;
    //list of successful cross times for each defense
    private List<List<Long>> successCrossTimes;
    //list of failed cross times for each defense
    private List<List<Long>> failCrossTimes;
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
        setContentView(R.layout.activity_auto);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        //get fields from previous activity
        matchNumber = getIntent().getIntExtra("matchNumber", 1);
        overridden = getIntent().getBooleanExtra("overridden", false);
        teamNumber = getIntent().getIntExtra("teamNumber", -1);
        scoutName = getIntent().getStringExtra("scoutName");
        uuid = getIntent().getStringExtra("uuid");
        superName = getIntent().getStringExtra("superName");
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



        //populate the row of toggle buttons for ball intakes on midline
        LinearLayout intakeLayout = (LinearLayout) findViewById(R.id.autoIntakeButtonLinearLayout);
        toggleCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList("1 Intaked", "2 Intaked",
                "3 Intaked", "4 Intaked", "5 Intaked", "6 Intaked")));
        for (int i = 0; i < 6; i++) {
            intakeLayout.addView(toggleCreator.getNextToggleButton(ViewGroup.LayoutParams.WRAP_CONTENT));
        }


        //fill row of defense buttons
        final Activity context = this;
        LinearLayout defenseLayout = (LinearLayout) findViewById(R.id.autoDefenseButtonLinearLayout);
        UIComponentCreator buttonCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList("Defense 1", "Defense 2", "Defense 3", "Defense 4",
                "Defense 5")));
        //empty relative layout to space out buttons
        RelativeLayout fillerSpace = new RelativeLayout(this);
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


                    final Dialog dialog = new Dialog(context);
                    dialog.setTitle("Attempt Defense" + Integer.toString(buttonNum+1));
                    RelativeLayout dialogLayout = (RelativeLayout) getLayoutInflater().inflate(R.layout.dialog, null);
                    Button success = (Button) dialogLayout.findViewById(R.id.successButton);
                    success.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            successCrossTimes.get(buttonNum).add(Calendar.getInstance().getTimeInMillis() - startTime);
                            dialog.dismiss();
                        }
                    });
                    Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            failCrossTimes.get(buttonNum).add(Calendar.getInstance().getTimeInMillis() - startTime);
                            dialog.dismiss();
                        }
                    });
                    Button cancel = (Button) dialogLayout.findViewById(R.id.cancelButton);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setContentView(dialogLayout);
                    dialog.show();



                    /*new AlertDialog.Builder(context)
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
                            .show();*/
                }
            });
            defenseLayout.addView(button);
            fillerSpace = new RelativeLayout(this);
            fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
            defenseLayout.addView(fillerSpace);
        }



        //populate counters for everything
        LinearLayout rowLayout = (LinearLayout) findViewById(R.id.autoCounterLinearLayout);
        counterCreator = new UIComponentCreator(this, new ArrayList<>(Arrays.asList("Balls Knocked Off Mid",
                 "High Shots Made",  "High Shots Missed",  "Low Shots Made",
                 "Low Shots Missed")));

        for (int i = 0; i < 5; i++) {
            rowLayout.addView(counterCreator.getNextTitleRow(1));
            rowLayout.addView(counterCreator.getNextCounterRow(1));
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


            //add successful defense cross times to JSON
            JSONArray successDefenseTimes = new JSONArray();
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
                return false;
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
                return false;
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
                    return false;
                }
            }

            //send it all to teleop activity
            startActivity(new Intent(this, TeleopActivity.class).putExtra("autoJSON", data.toString())
            .putExtra("matchNumber", matchNumber).putExtra("overridden", overridden).putExtra("teamNumber", teamNumber).putExtra("scoutName", scoutName)
            .putExtra("uuid", uuid).putExtra("superName", superName).putExtra("scoutNumber", scoutNumber));
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
