package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class DataActivity extends AppCompatActivity {
    public abstract String activityName();
    public abstract List<String> getToggleData();
    public abstract List<String> getCounterData();
    public abstract List<String> getSwitchData();
    public abstract List<String> getScaleData();
    public abstract List<String> getPyramidData();
    public abstract List<String> getRadioData();
    public abstract List<String> getPlatformData();

    public abstract Integer getToggleXML();
    public abstract Integer getCounterXML();
    public abstract Integer getAttemptXML();
    public abstract Integer getRadioXML();
    public abstract Integer getPlatformOneXML();
    public abstract Integer getPlatformTwoXML();

    public abstract Class getNextActivityClass();
    public abstract Class getPreviousActivityClass();
    public abstract int getActionBarMenu();

    public static boolean saveTeleData = false;
    public static boolean saveAutoData = false;
    public static String activityName;
    public static String capActivityName;
    public static boolean rejected = false;

    private boolean sent;
    private boolean instantiatedPlatformBools = false;

    private int numSendClicks;
    public final Activity context = this;
    File dir;
    PrintWriter file;

    public static boolean[] alliancePlatformTaken = new boolean[6];
    public static boolean[] opponentPlatformTaken = new boolean[6];
    public List<Map<Integer, Boolean>> alliancePlatformValueMapList = new ArrayList<>();
    public List<Map<Integer, Boolean>> opponentPlatformValueMapList = new ArrayList<>();

    private Intent intent;
    private UIComponentCreator toggleCreator;
    private UIComponentCreator platformCreator;
    private UIComponentCreator radioCreator;
    private UIComponentCreator.UICounterCreator counterCreator;
    private UIComponentCreator.UISwitchCreator switchCreator;
    private UIComponentCreator.UIScaleCreator scaleCreator;
    private UIComponentCreator.UIPyramidCreator pyramidCreator;
    private UIComponentCreator.UIEndGameButtonCreator endGameCreator;
    private Boolean readyForNextActivity = false;
    private final Object readyForNextActivityLock = new Object();
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sent = false;

        //check for whether they want to save data
        if(!saveTeleData){

        }

        if(activityName() == "auto"){
            setContentView(R.layout.activity_auto);
        }else if(activityName() == "tele"){
            setContentView(R.layout.activity_teleop);
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        activityName = activityName();
        capActivityName = activityName.substring(0,1).toUpperCase() + activityName.substring(1);

        intent = getIntent();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        numSendClicks = 0;

                        Drawable actionBarBackgroundColor;

                        if(MainActivity.allianceColor != null){
                            if(MainActivity.allianceColor.equals("blue")){
                                actionBarBackgroundColor = new ColorDrawable(Color.parseColor(Constants.COLOR_BLUE));
                            }else if(MainActivity.allianceColor.equals("red")){
                                actionBarBackgroundColor = new ColorDrawable((Color.parseColor(Constants.COLOR_RED)));
                            }else{
                                actionBarBackgroundColor = new ColorDrawable((Color.parseColor(Constants.COLOR_GREEN)));
                            }
                        }else{
                            actionBarBackgroundColor = new ColorDrawable((Color.parseColor(Constants.COLOR_GREEN)));
                        }

                        ActionBar actionBar = getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setBackgroundDrawable(actionBarBackgroundColor);
                        }

        dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/scout_data");


        Executors.newSingleThreadScheduledExecutor().schedule(new Runnable() {
            @Override
            public void run() {
                synchronized (readyForNextActivityLock) {
                    readyForNextActivity = true;
                }
            }
        }, 1, TimeUnit.SECONDS);

        updateUI();
    }

    private void updateUI() {

                    if(getToggleXML()!=null) {
                        LinearLayout toggleLayout = (LinearLayout) findViewById(getToggleXML());
                        List<String> toggleDisplayTitles = new ArrayList<>();
                        if (activityName().equals("auto")) {


                            if (getToggleData() != null) {
                                Log.e("toggleSize", getToggleData().size() + "");
                                for (int i = 0; i < getToggleData().size(); i++) {
                                    toggleDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getToggleData().get(i)));
                                }
                                toggleCreator = new UIComponentCreator(this, toggleDisplayTitles);
                                final ToggleButton autoLinePassed = toggleCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false,0);
                                autoLinePassed.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        DataManager.addZeroTierJsonData("didMakeAutoRun", autoLinePassed.isChecked());
                                    }
                                });
                                toggleLayout.addView(autoLinePassed);
                            }
                        } else if (activityName().equals("tele")) {
                            if (getToggleData() != null) {
                                Log.e("toggleSize", getToggleData().size() + "");
                                for (int i = 0; i < getToggleData().size(); i++) {
                                    toggleDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getToggleData().get(i)));
                                }
                                toggleCreator = new UIComponentCreator(this, toggleDisplayTitles);
                                final ToggleButton incap = toggleCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false,0);
                                final ToggleButton disabled = toggleCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false,0);
                                incap.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        DataManager.addZeroTierJsonData("didGetIncapacitated", incap.isChecked());
                                    }
                                });
                                disabled.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        DataManager.addZeroTierJsonData("didGetDisabled", disabled.isChecked());
                                    }
                                });
                                toggleLayout.addView(incap);
                                toggleLayout.addView(disabled);
                            }
                        }
                    }

                                            if(activityName().equals("auto") && !instantiatedPlatformBools){      for(int i = 0; i <=5; i++){     alliancePlatformTaken[i] = false; opponentPlatformTaken[i] = false;} instantiatedPlatformBools = true;}
                                            //-----------------------------Add 12 buttons
                                            if ((getPlatformOneXML() != null && activityName().equals("auto"))){
                                                LinearLayout platformLayoutOne = (LinearLayout) findViewById(getPlatformOneXML());
                                                List<String> platformDisplayTitles = new ArrayList<>();
                                                if (getPlatformData() != null){
                                                    Log.e("platFormOneSizeAuto", getPlatformData().size() + "");
                                                    for (int i = 0; i < getPlatformData().size(); i++) {
                                                        platformDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getPlatformData().get(i)));
                                                    }
                                                    platformCreator = new UIComponentCreator(this, platformDisplayTitles);
                                                    Log.e("platFormOneSizeAuto", platformDisplayTitles.size() + "");

                                                    if (MainActivity.allianceColor.equals("red")) {
                                                        for (int i = 0; i <= 5; i++) {
                                                            final int ii = i;
                                                            final ToggleButton platformButtonRed = platformCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false, Color.parseColor(Constants.COLOR_LIGHTRED));
                                                            platformButtonRed.setOnClickListener(new View.OnClickListener() {
                                                                public void onClick(View v) {
                                                                    if(!alliancePlatformTaken[ii]){
                                                                        alliancePlatformTaken[ii]=true;
                                                                        Map<Integer, Boolean> platformMap = new HashMap<Integer, Boolean>();
                                                                        platformMap.put(ii, alliancePlatformTaken[ii]);
                                                                        alliancePlatformValueMapList.add(platformMap);
                                                                        platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_RED));
                                                                    }else if(alliancePlatformTaken[ii]){
                                                                        alliancePlatformTaken[ii]=false;
                                                                        Map<Integer, Boolean> platformMap = new HashMap<Integer, Boolean>();
                                                                        platformMap.put(ii, alliancePlatformTaken[ii]);
                                                                        alliancePlatformValueMapList.add(platformMap);
                                                                        platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTRED));
                                                                    }
                                                                }
                                                            });
                                                            platformLayoutOne.addView(platformButtonRed);
                                                            platformLayoutOne.addView(getFillerSpace(0.1f));
                                                        }
                                                    } else if (MainActivity.allianceColor.equals("blue")) {
                                                        for (int i = 0; i <= 5; i++) {
                                                            final int ii = i;
                                                            final ToggleButton platformButtonBlue = platformCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false, Color.parseColor(Constants.COLOR_LIGHTBLUE));
                                                            platformButtonBlue.setOnClickListener(new View.OnClickListener() {
                                                                public void onClick(View v) {
                                                                    if(!alliancePlatformTaken[ii]){
                                                                        alliancePlatformTaken[ii]=true;
                                                                        Map<Integer, Boolean> platformMap = new HashMap<Integer, Boolean>();
                                                                        platformMap.put(ii, alliancePlatformTaken[ii]);
                                                                        alliancePlatformValueMapList.add(platformMap);
                                                                        platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_BLUE));
                                                                    }else if(alliancePlatformTaken[ii]){
                                                                        alliancePlatformTaken[ii]=false;
                                                                        Map<Integer, Boolean> platformMap = new HashMap<Integer, Boolean>();
                                                                        platformMap.put(ii, alliancePlatformTaken[ii]);
                                                                        alliancePlatformValueMapList.add(platformMap);
                                                                        platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTBLUE));
                                                                    }
                                                                }
                                                            });
                                                            platformLayoutOne.addView(platformButtonBlue);
                                                            platformLayoutOne.addView(getFillerSpace(0.1f));
                                                        }
                                                    }
                                                }
                                            }else if(((getPlatformOneXML()!=null && getPlatformTwoXML()!=null) && activityName().equals("tele"))) {
                                                LinearLayout platformLayoutOne = (LinearLayout) findViewById(getPlatformOneXML());
                                                LinearLayout platformLayoutTwo = (LinearLayout) findViewById(getPlatformTwoXML());
                                                List<String> platformDisplayTitles = new ArrayList<>();
                                                if (getPlatformData() != null){
                                                    Log.e("platFormOneSizeTele", getPlatformData().size() + "");
                                                    for (int i = 0; i < getPlatformData().size(); i++) {
                                                        platformDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getPlatformData().get(i)));
                                                    }
                                                    platformCreator = new UIComponentCreator(this, platformDisplayTitles);
                                                    Log.e("platFormOneSizeTele", platformDisplayTitles.size() + "");
                                                }
                                                for (int i = 0; i <= 5; i++) {
                                                    final int ii = i;
                                                    final ToggleButton platformButtonRed = platformCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false, Color.parseColor(Constants.COLOR_LIGHTRED));
                                                    if(MainActivity.allianceColor.equals("red") && alliancePlatformTaken[ii]){
                                                        platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_RED));
                                                        platformButtonRed.setEnabled(false);
                                                    }
                                                    platformButtonRed.setOnClickListener(new View.OnClickListener() {
                                                        public void onClick(View v) {
                                                            if(MainActivity.allianceColor.equals("red")){
                                                                if(!alliancePlatformTaken[ii]){
                                                                    alliancePlatformTaken[ii]=true;
                                                                    Map<Integer, Boolean> platformMap = new HashMap<Integer, Boolean>();
                                                                    platformMap.put(ii, alliancePlatformTaken[ii]);
                                                                    alliancePlatformValueMapList.add(platformMap);
                                                                    platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_RED));
                                                                }else if(alliancePlatformTaken[ii]){
                                                                    alliancePlatformTaken[ii]=false;
                                                                    Map<Integer, Boolean> platformMap = new HashMap<Integer, Boolean>();
                                                                    platformMap.put(ii, alliancePlatformTaken[ii]);
                                                                    alliancePlatformValueMapList.add(platformMap);
                                                                    platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTRED));
                                                                }
                                                            }else if(MainActivity.allianceColor.equals("blue")){
                                                                if(!alliancePlatformTaken[ii]){
                                                                    alliancePlatformTaken[ii]=true;
                                                                    Map<Integer, Boolean> platformMap = new HashMap<Integer, Boolean>();
                                                                    platformMap.put(ii, alliancePlatformTaken[ii]);
                                                                    alliancePlatformValueMapList.add(platformMap);
                                                                    platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_RED));
                                                                }else if(alliancePlatformTaken[ii]){
                                                                    alliancePlatformTaken[ii]=false;
                                                                    Map<Integer, Boolean> platformMap = new HashMap<Integer, Boolean>();
                                                                    platformMap.put(ii, alliancePlatformTaken[ii]);
                                                                    alliancePlatformValueMapList.add(platformMap);
                                                                    platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTRED));
                                                                }
                                                            }
                                                        }
                                                    });
                                                    platformLayoutOne.addView(platformButtonRed);
                                                    platformLayoutOne.addView(getFillerSpace(1f));

                                                }
                                                platformCreator.resetCurrentComponent();
                                                for (int i = 0; i <= 5; i++) {
                                                    final int ii = i;
                                                    final ToggleButton platformButtonBlue = platformCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false, Color.parseColor(Constants.COLOR_LIGHTBLUE));
                                                    if(MainActivity.allianceColor.equals("blue") && alliancePlatformTaken[ii]){
                                                        platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_BLUE));
                                                        platformButtonBlue.setEnabled(false);
                                                    }
                                                    platformButtonBlue.setOnClickListener(new View.OnClickListener() {
                                                        public void onClick(View v) {
                                                            if(MainActivity.allianceColor.equals("blue")){
                                                                if(!alliancePlatformTaken[ii]){
                                                                    alliancePlatformTaken[ii]=true;
                                                                    Map<Integer, Boolean> platformMap = new HashMap<Integer, Boolean>();
                                                                    platformMap.put(ii, alliancePlatformTaken[ii]);
                                                                    alliancePlatformValueMapList.add(platformMap);
                                                                    platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_BLUE));
                                                                }else if(alliancePlatformTaken[ii]){
                                                                    alliancePlatformTaken[ii]=false;
                                                                    Map<Integer, Boolean> platformMap = new HashMap<Integer, Boolean>();
                                                                    platformMap.put(ii, alliancePlatformTaken[ii]);
                                                                    alliancePlatformValueMapList.add(platformMap);
                                                                    platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTBLUE));
                                                                }
                                                            }else if(MainActivity.allianceColor.equals("red")){
                                                                if(!alliancePlatformTaken[ii]){
                                                                    alliancePlatformTaken[ii]=true;
                                                                    Map<Integer, Boolean> platformMap = new HashMap<Integer, Boolean>();
                                                                    platformMap.put(ii, alliancePlatformTaken[ii]);
                                                                    alliancePlatformValueMapList.add(platformMap);
                                                                    platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_BLUE));
                                                                }else if(alliancePlatformTaken[ii]){
                                                                    alliancePlatformTaken[ii]=false;
                                                                    Map<Integer, Boolean> platformMap = new HashMap<Integer, Boolean>();
                                                                    platformMap.put(ii, alliancePlatformTaken[ii]);
                                                                    alliancePlatformValueMapList.add(platformMap);
                                                                    platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTBLUE));
                                                                }
                                                            }
                                                        }
                                                    });
                                                    platformLayoutTwo.addView(platformButtonBlue);
                                                    platformLayoutTwo.addView(getFillerSpace(1f));

                                                }
                                            }
                    if (getRadioXML() != null) {
                        RadioGroup radioLayout = (RadioGroup) findViewById(getRadioXML());
                        List<String> radioDisplayTitles = new ArrayList<>();
                        if (getRadioData() != null) {
                            if (activityName().equals("auto")) {

                                Log.e("radioSize", getRadioData().size() + "");

                                if(getRadioData() != null){
                                    Log.e("AttemptSIZE-Sw",getSwitchData()+"");
                                    for(int i=0; i< getRadioData().size(); i++){
                                        radioDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getRadioData().get(i)));
                                    }
                                }

                                radioCreator = new UIComponentCreator(this, radioDisplayTitles);
                                Log.e("radioSize", radioDisplayTitles.size() + "");

                                final RadioButton rightStartPosition = radioCreator.getRadioButton("startingPosition", "right", LinearLayout.LayoutParams.MATCH_PARENT);
                                final RadioButton leftStartPosition = radioCreator.getRadioButton("startingPosition", "left", LinearLayout.LayoutParams.MATCH_PARENT);
                                final RadioButton centerStartPosition = radioCreator.getRadioButton("startingPosition", "right", LinearLayout.LayoutParams.MATCH_PARENT);


                                radioLayout.addView(rightStartPosition);
                                radioLayout.addView(centerStartPosition);
                                radioLayout.addView(leftStartPosition);

                                rightStartPosition.setText("Right");
                                centerStartPosition.setText("Center");
                                leftStartPosition.setText("Left");
                            }
                        }
                    }
                                            LinearLayout counterLayout = (LinearLayout) findViewById(getCounterXML());
                                            List<String> counterNames = new ArrayList<>();
                                            counterCreator = new UIComponentCreator.UICounterCreator(this, counterNames);
                                            for (int i = 0; i < getCounterData().size(); i++) {
                                                counterNames.add(Constants.KEYS_TO_TITLES.get(getCounterData().get(i)));
                                                counterLayout.addView(counterCreator.addCounter(getCounterData().get(i)));
                                            }counterLayout.addView(getFillerSpace(1f));

                    LinearLayout attemptLayout = (LinearLayout)findViewById(getAttemptXML());
                    List<String> switchDisplayTitles = new ArrayList<>();
                    List<String> scaleDisplayTitles = new ArrayList<>();
                    List<String> pyramidDisplayTitles = new ArrayList<>();
                    if(getSwitchData() != null) {
                        Log.e("AttemptSIZE-Sw", getSwitchData().size() + "");
                        for (int i = 0; i < getSwitchData().size(); i++) {
                            switchDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getSwitchData().get(i)));
                        }
                    }if(getScaleData() != null) {
                        Log.e("AttemptSIZE-Sc", getScaleData().size() + "");
                        for (int i = 0; i < getScaleData().size(); i++) {
                            scaleDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getScaleData().get(i)));
                        }
                    }if(getPyramidData() != null) {
                        Log.e("AttemptSIZE-P", getPyramidData().size() + "");
                        for (int i = 0; i < getPyramidData().size(); i++) {
                            pyramidDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getPyramidData().get(i)));
                        }
                    }
                    switchCreator = new UIComponentCreator.UISwitchCreator(this, switchDisplayTitles);
                    scaleCreator = new UIComponentCreator.UIScaleCreator(this, scaleDisplayTitles);
                    pyramidCreator = new UIComponentCreator.UIPyramidCreator(this, pyramidDisplayTitles);
                    //add reset
                    Log.e("AttemptSIZE-Sw", switchDisplayTitles.size() + "");
                    Log.e("AttemptSIZE-Sc", scaleDisplayTitles.size() + "");
                    Log.e("AttemptSIZE-P", pyramidDisplayTitles.size() + "");
                    for (int i = 0; i < getSwitchData().size(); i++) {
                        if(activityName().equals("auto")){
                            switchCreator.resetSwitchComponent();
                            Button a_switchButton = switchCreator.addButton("allianceSwitchAttemptAuto", MainActivity.allianceColor);
                            attemptLayout.addView(a_switchButton);
                        }else if(activityName().equals("tele")){
                            Log.e("REEEEEEEt", activityName());
                            if(MainActivity.allianceColor.equals("red") && (attemptLayout.getChildAt(0)==null && attemptLayout.getChildAt(1)==null)){
                                switchCreator.resetSwitchComponent();
                                Button a_switchButton = switchCreator.addButton("allianceSwitchAttemptTele", "red");
                                Button o_switchButton = switchCreator.addButton("opponentSwitchAttemptTele", "blue");
                                attemptLayout.addView(a_switchButton,0);
                                attemptLayout.addView(o_switchButton,1);
                            }else if(MainActivity.allianceColor.equals("blue") && (attemptLayout.getChildAt(0)==null && attemptLayout.getChildAt(1)==null)){
                                switchCreator.resetSwitchComponent();
                                Button a_switchButton = switchCreator.addButton("allianceSwitchAttemptTele", "blue");
                                Button o_switchButton = switchCreator.addButton("opponentSwitchAttemptTele", "red");
                                attemptLayout.addView(o_switchButton,0);
                                attemptLayout.addView(a_switchButton,1);
                            }
                        }
                    }
                    if(activityName().equals("auto")){
                        scaleCreator.resetScaleComponenet();
                        Button scaleButton = scaleCreator.addButton("scaleAttemptAuto");
                        attemptLayout.addView(scaleButton);
                    }else if(activityName().equals("tele")){
                        scaleCreator.resetScaleComponenet();
                        Button scaleButton = scaleCreator.addButton("scaleAttemptTele");
                        attemptLayout.addView(scaleButton);
                    }
                    pyramidCreator.resetPyramidComponent();
                    Button pyramidButton = pyramidCreator.addButton();
                    attemptLayout.addView(pyramidButton);
    }

    //for making space between buttons
    private LinearLayout getFillerSpace(Float weight) {
        LinearLayout fillerSpace = new LinearLayout(this);
        fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, weight));
        return fillerSpace;
    }

    private void updateData() throws JSONException {
        if(activityName() == "tele"){
            List<View> toggleList = toggleCreator.getComponentViews();
            for (int i = 0; i < toggleList.size(); i++) {
                ToggleButton toggleButton = (ToggleButton) toggleList.get(i);
                try {
                    Log.e("KEYTOGGLE", getToggleData().get(i));
                    DataManager.addZeroTierJsonData(getToggleData().get(i), toggleButton.isChecked());
                } catch (Exception e) {
                    Log.e("Data Error", "Failed to add toggle " + Integer.toString(i) + " to Data");
                    Toast.makeText(this, "Invalid data in counter" + Integer.toString(i), Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        List<View> currentTextViews = counterCreator.getComponentViews();
        for (int i = 0; i < currentTextViews.size(); i++) {
            if(currentTextViews.get(i) != null){
            }
            try {
                Log.e("keyCOUNTER", getCounterData().get(i));
                DataManager.addZeroTierJsonData(getCounterData().get(i), Integer.parseInt(((TextView) currentTextViews.get(i)).getText().toString()));
            } catch (Exception e) {
                Log.e("Data Error", "Failed to add counter" + Integer.toString(i) + " num to Data");
                Toast.makeText(this, "Error in Counter number " + Integer.toString(i), Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private Intent prepareIntent(Class clazz) {
        Log.e("intentCalled", "called");
        try {
            updateData();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(this, clazz);
        intent.putExtras(this.intent);
        try {
            intent.putExtra("previousData", Utils.serializeClass(DataManager.collectedData));
        } catch (JsonParseException jpe) {
            intent.putExtra("previousData", (String)null);
        }
        intent.putExtra("matchName", "Q" + Integer.toString(this.intent.getIntExtra("matchNumber", -1))
                + "_" + Integer.toString(this.intent.getIntExtra("teamNumber", -1)));
        return intent;
    }


@Override
public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(getActionBarMenu(), menu);
    final LayoutInflater.Factory existingFactory = getLayoutInflater().getFactory();
            try{
                Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
                field.setAccessible(true);
                field.setBoolean(getLayoutInflater(), false);
                getLayoutInflater().setFactory(new LayoutInflater.Factory() {
                    public View onCreateView(String name, Context context, AttributeSet attrs) {
                        try {
                                LayoutInflater li = LayoutInflater.from(context);
                                View view = li.createView(name, null, attrs);

                                        if (existingFactory != null) {
                                        view = existingFactory.onCreateView(name, context, attrs);
                                    }

                                        ((TextView) view).setTextSize(20);

                                        // set the text color
                                                Typeface face = Typeface.createFromAsset(getAssets(),"Technoma.otf");
                                ((TextView) view).setTypeface(face);
                                ((TextView) view).setTextColor(Color.WHITE);

                                        return view;
                            } catch (InflateException e) {
                        //Handle any inflation exception here
                            } catch (ClassNotFoundException e) {
                        //Handle any ClassNotFoundException here
                            }
                        return null;
            }
            });}catch (NoSuchFieldException e) {
            } catch (IllegalArgumentException e) {
            } catch (IllegalAccessException e) {
            }
                MenuItem textView = (MenuItem) menu.findItem(R.id.teamNumTextView);
            try {
                textView.setTitle(DataManager.collectedData.getInt("teamNumber")+"");
            }catch (JSONException je){}
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.buttonNext) {
            rejected = false;

            synchronized (readyForNextActivityLock) {
                if (!readyForNextActivity) {
                    Log.i("Scout Error", "Tried to move on too quickly!");
                    return true;
                }
            }
            Long startTime = Calendar.getInstance().getTimeInMillis();
            Intent intent = prepareIntent(getNextActivityClass());
            Long stopTime = Calendar.getInstance().getTimeInMillis();
            Log.i("Starting next Activity!", "Time to update and serialize data: " + Long.toString(stopTime - startTime) + "ms");

            Log.e("MEMES", Boolean.toString(sent));

            if((activityName() == "tele")){
                numSendClicks++;
                    if(numSendClicks >= 2){
                        saveAutoData = false;
                        saveTeleData = false;

                       //had specific stuff about save or unsave

                        Log.e("collectedData", DataManager.collectedData.toString());
                        Log.e("SUBTITLE", DataManager.subTitle);

                        String jsonString = DataManager.collectedData.toString();
                        Map<String, Object> jsonMap = new Gson().fromJson(jsonString, new TypeToken<HashMap<String, Object>>() {}.getType());
                        Log.e("SUBTITLE", DataManager.subTitle);
                        Log.e("JSONMAP", jsonString);
                        databaseReference.child("TempTeamInMatchDatas").child(DataManager.subTitle).setValue(jsonMap);
                    }
            }

            new Thread() {
                @Override
                public void run() {
                    if((activityName() == "tele")) {
                        if(numSendClicks >= 2) {
                            try {
                                file = null;
                                //make the directory of the file
                                dir.mkdir();
                                //can delete when doing the actual thing
                                file = new PrintWriter(new FileOutputStream(new File(dir, ("Q" + MainActivity.matchNumber + "_" + new SimpleDateFormat("MM-dd-yyyy-H:mm:ss").format(new Date())))));
                            } catch (IOException IOE) {
                                Log.e("File error", "Failed to open File");
                                return;
                            }


                            file.println(DataManager.collectedData.toString());
                            file.close();
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.e("sentBOOL", Boolean.toString(sent));
                                    Toast.makeText(context, "Sent Match Data", Toast.LENGTH_SHORT).show();
                                }
                            });

                            numSendClicks = 0;
                        }
                    }
                }
            }.start();

            if(!activityName.equals("tele")){
                startActivity(intent);
            }else if(activityName.equals("tele") && numSendClicks >= 2){
                startActivity(intent);
            }
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if(activityName() == "auto") {
            new AlertDialog.Builder(this)
                    .setTitle("Stop Scouting")
                    .setMessage("Are you sure you want to go back now?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            saveAutoData = true;
                            Intent intent = prepareIntent(getPreviousActivityClass());
                            if (getPreviousActivityClass() == MainActivity.class) {
                                intent.putExtra("previousData", (String) null);
                            }
                            startActivity(intent);
                        }
                    })
                    .show();
        }else if(activityName() == "tele"){
            new AlertDialog.Builder(this)
                    .setTitle("Save Data?")
                    .setMessage("Do you want to save this data?")
                    .setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            rejected = true;
                            saveAutoData = true;
                            saveTeleData = false;
                            Intent intent = prepareIntent(getPreviousActivityClass());
                            if (getPreviousActivityClass() == MainActivity.class) {
                                intent.putExtra("previousData", (String) null);
                            }
                            startActivity(intent);
                        }
                    })
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            rejected = false;
                            saveAutoData = true;
                            saveTeleData = true;
                            Intent intent = prepareIntent(getPreviousActivityClass());
                            if (getPreviousActivityClass() == MainActivity.class) {
                                intent.putExtra("previousData", (String) null);
                            }
                            startActivity(intent);
                        }
                    })
                    .show();
        }
    }
}