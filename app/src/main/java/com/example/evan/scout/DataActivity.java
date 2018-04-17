package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
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
import android.view.Window;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.jcodec.movtool.Util;
import org.json.JSONArray;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.example.evan.scout.MainActivity.bgTimer;
import static com.example.evan.scout.MainActivity.matchNumber;
import static com.example.evan.scout.backgroundTimer.offset;
import static com.example.evan.scout.backgroundTimer.showTime;

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
    public abstract Integer getCounterOneXML();
    public abstract Integer getCounterTwoXML();
    public abstract Integer getVaultXML();
    public abstract Integer getAttemptOneXML();
    public abstract Integer getAttemptTwoXML();
    public abstract Integer getRadioXML();
    public abstract Integer getPlatformOneXML();
    public abstract Integer getPlatformTwoXML();
    public abstract Integer getEndGameXML();

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

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor spfe;

    private Intent intent;
    private UIComponentCreator toggleCreator;
    private UIComponentCreator platformCreator;
    private UIComponentCreator radioCreator;
    private UIComponentCreator.UICounterCreator counterCreator;
    private UIComponentCreator.UISwitchCreator switchCreator;
    private UIComponentCreator.UIScaleCreator scaleCreator;
    private UIComponentCreator.UIPyramidCreator pyramidCreator;
    private UIComponentCreator.UIEndGameButtonCreator endGameCreator;
    private UIComponentCreator.UIVaultCreator vaultCreator;
    private Boolean readyForNextActivity = false;
    private final Object readyForNextActivityLock = new Object();
    DatabaseReference databaseReference;

    Handler handler;
    Dialog timerDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sent = false;

        //check for whether they want to save data
        if(!saveTeleData){

        }

        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);

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
                                boolean autoLineBool = false;
                                boolean autoCrossBool = false;
                                if(saveAutoData){try {autoLineBool = DataManager.collectedData.getBoolean("didMakeAutoRun");} catch (JSONException e) {e.printStackTrace();}}
                                final ToggleButton autoLinePassed = toggleCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, autoLineBool,0,true);
                                if(autoLinePassed.isChecked()){
                                    autoLinePassed.setBackgroundColor(Color.parseColor("#3affb3"));
                                }else if(!autoLinePassed.isChecked()){
                                    autoLinePassed.setBackgroundColor(Color.parseColor("#aaaaaa"));
                                }

                                autoLinePassed.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        DataManager.addZeroTierJsonData("didMakeAutoRun", autoLinePassed.isChecked());
                                        if(autoLinePassed.isChecked()){
                                            autoLinePassed.setBackgroundColor(Color.parseColor("#3affb3"));
                                        }else if(!autoLinePassed.isChecked()){
                                            autoLinePassed.setBackgroundColor(Color.parseColor("#aaaaaa"));
                                        }
                                    }
                                });

                                toggleLayout.addView(autoLinePassed);
                            }
                        }else if (activityName().equals("tele")) {
                            if (getToggleData() != null) {
                                Log.e("toggleSize", getToggleData().size() + "");
                                for (int i = 0; i < getToggleData().size(); i++) {
                                    toggleDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getToggleData().get(i)));
                                }
                                toggleCreator = new UIComponentCreator(this, toggleDisplayTitles);
                                final ToggleButton incap = toggleCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false,0,true);
                                final ToggleButton disabled = toggleCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false,0,true);
                                try {incap.setChecked(DataManager.collectedData.getBoolean("didGetIncapacitated"));} catch (JSONException e) {e.printStackTrace();}
                                try {disabled.setChecked(DataManager.collectedData.getBoolean("didGetDisabled"));} catch (JSONException e) {e.printStackTrace();}
                                incap.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (DataManager.vaultOpen){
                                            incap.setChecked(!incap.isChecked());
                                            Utils.makeToast(context, "PLEASE CLICK DONE FOR THE VAULT!");
                                        }else{
                                            disabled.setChecked(false);
                                            DataManager.addZeroTierJsonData("didGetIncapacitated", incap.isChecked());
                                            DataManager.addZeroTierJsonData("didGetDisabled", disabled.isChecked());
                                        }
                                    }
                                });
                                disabled.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if(DataManager.vaultOpen){
                                            disabled.setChecked(!disabled.isChecked());
                                            Utils.makeToast(context, "PLEASE CLICK DONE FOR THE VAULT!");
                                        }else{
                                            incap.setChecked(false);
                                            DataManager.addZeroTierJsonData("didGetIncapacitated", incap.isChecked());
                                            DataManager.addZeroTierJsonData("didGetDisabled", disabled.isChecked());
                                        }
                                    }
                                });
                                toggleLayout.addView(incap);
                                toggleLayout.addView(disabled);
                            }
                        }
                    }

                                            //-----------------------------Add 12 buttons
                                            try{
                                                if ((getPlatformOneXML() != null && activityName().equals("auto"))){
                                                    LinearLayout platformLayoutOne = (LinearLayout) findViewById(getPlatformOneXML());
                                                    List<String> platformDisplayTitles = new ArrayList<>();
                                                    if (getPlatformData() != null){
                                                        Log.e("platFormOneSizeAuto", getPlatformData().size() + "");
                                                        for (int i = 0; i < getPlatformData().size(); i++) {
                                                            platformDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getPlatformData().get(i)));
                                                        }
                                                        platformCreator = new UIComponentCreator(this, platformDisplayTitles);
                                                        if (MainActivity.allianceColor.equals("red")) {
                                                            for (int i = 0; i <= 5; i++) {
                                                                final int ii = i;
                                                                final ToggleButton platformButtonRed = platformCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false, Color.parseColor(Constants.COLOR_LIGHTRED), true);
                                                                if(DataManager.alliancePlatformTakenAuto.getBoolean(ii)){
                                                                    platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_RED));
                                                                }
                                                                platformButtonRed.setOnClickListener(new View.OnClickListener() {
                                                                    public void onClick(View v) {
                                                                        try {
                                                                            if(!DataManager.alliancePlatformTakenAuto.getBoolean(ii)){
                                                                                DataManager.alliancePlatformTakenAuto.put(ii, true);
                                                                                platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_RED));
                                                                            }else if(DataManager.alliancePlatformTakenAuto.getBoolean(ii)){
                                                                                DataManager.alliancePlatformTakenAuto.put(ii, false);
                                                                                platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTRED));
                                                                            }
                                                                        } catch (JSONException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                });
                                                                platformLayoutOne.addView(platformButtonRed);
                                                                platformLayoutOne.addView(getFillerSpace(5));
                                                            }
                                                        } else if (MainActivity.allianceColor.equals("blue")) {
                                                            for (int i = 0; i <= 5; i++) {
                                                                final int ii = i;
                                                                final ToggleButton platformButtonBlue = platformCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false, Color.parseColor(Constants.COLOR_LIGHTBLUE), true);
                                                                if(DataManager.alliancePlatformTakenAuto.getBoolean(ii)){
                                                                    platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_BLUE));
                                                                }
                                                                platformButtonBlue.setOnClickListener(new View.OnClickListener() {
                                                                    public void onClick(View v) {
                                                                        try {
                                                                            if(!DataManager.alliancePlatformTakenAuto.getBoolean(ii)){
                                                                                DataManager.alliancePlatformTakenAuto.put(ii, true);
                                                                                platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_BLUE));
                                                                            }else if(DataManager.alliancePlatformTakenAuto.getBoolean(ii)){
                                                                                DataManager.alliancePlatformTakenAuto.put(ii, false);
                                                                                platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTBLUE));
                                                                            }
                                                                        } catch (JSONException e) {
                                                                            e.printStackTrace();
                                                                        }
                                                                    }
                                                                });
                                                                platformLayoutOne.addView(platformButtonBlue);
                                                                platformLayoutOne.addView(getFillerSpace(5));
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
                                                        final ToggleButton platformButtonRed = platformCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false, Color.parseColor(Constants.COLOR_LIGHTRED), true);
                                                        if(MainActivity.allianceColor.equals("red") && DataManager.alliancePlatformTakenAuto.getBoolean(ii)){
                                                            DataManager.alliancePlatformTakenTele.put(ii, false);
                                                            platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_RED));
                                                            platformButtonRed.setEnabled(false);
                                                        }else if(MainActivity.allianceColor.equals("red") && DataManager.alliancePlatformTakenTele.getBoolean(ii)){
                                                            platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_RED));
                                                        }else if(MainActivity.allianceColor.equals("blue") && DataManager.opponentPlatformTakenTele.getBoolean(ii)){
                                                            platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_RED));
                                                        }
                                                        platformButtonRed.setOnClickListener(new View.OnClickListener() {
                                                            public void onClick(View v) {
                                                                if(MainActivity.allianceColor.equals("red")){
                                                                    try {
                                                                        if(!DataManager.alliancePlatformTakenTele.getBoolean(ii)){
                                                                            DataManager.alliancePlatformTakenTele.put(ii, true);
                                                                            platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_RED));
                                                                        }else if(DataManager.alliancePlatformTakenTele.getBoolean(ii)){
                                                                            DataManager.alliancePlatformTakenTele.put(ii, false);
                                                                            platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTRED));
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }else if(MainActivity.allianceColor.equals("blue")){
                                                                    try {
                                                                        if(!DataManager.opponentPlatformTakenTele.getBoolean(ii)){
                                                                            DataManager.opponentPlatformTakenTele.put(ii, true);
                                                                            platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_RED));
                                                                        }else if(DataManager.opponentPlatformTakenTele.getBoolean(ii)){
                                                                            DataManager.opponentPlatformTakenTele.put(ii, false);
                                                                            platformButtonRed.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTRED));
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }
                                                        });
                                                        platformLayoutOne.addView(platformButtonRed);
                                                        platformLayoutOne.addView(getFillerSpace(5));
                                                    }
                                                    platformCreator.resetCurrentComponent();
                                                    for (int i = 0; i <= 5; i++) {
                                                        final int ii = i;
                                                        final ToggleButton platformButtonBlue = platformCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false, Color.parseColor(Constants.COLOR_LIGHTBLUE), true);
                                                        if(MainActivity.allianceColor.equals("blue") && DataManager.alliancePlatformTakenAuto.getBoolean(ii)){
                                                            DataManager.alliancePlatformTakenTele.put(ii, false);
                                                            platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_BLUE));
                                                            platformButtonBlue.setEnabled(false);
                                                        }else if(MainActivity.allianceColor.equals("blue") && DataManager.alliancePlatformTakenTele.getBoolean(ii)){
                                                            platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_RED));
                                                        }else if(MainActivity.allianceColor.equals("red") && DataManager.opponentPlatformTakenTele.getBoolean(ii)){
                                                            platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_BLUE));
                                                        }
                                                        platformButtonBlue.setOnClickListener(new View.OnClickListener() {
                                                            public void onClick(View v) {
                                                                if(MainActivity.allianceColor.equals("blue")){
                                                                    try {
                                                                        if(!DataManager.alliancePlatformTakenTele.getBoolean(ii)){
                                                                            DataManager.alliancePlatformTakenTele.put(ii, true);
                                                                            platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_BLUE));
                                                                        }else if(DataManager.alliancePlatformTakenTele.getBoolean(ii)){
                                                                            DataManager.alliancePlatformTakenTele.put(ii, false);
                                                                            platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTBLUE));
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }else if(MainActivity.allianceColor.equals("red")){
                                                                    try {
                                                                        if(!DataManager.opponentPlatformTakenTele.getBoolean(ii)){
                                                                            DataManager.opponentPlatformTakenTele.put(ii, true);
                                                                            platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_BLUE));
                                                                        }else if(DataManager.opponentPlatformTakenTele.getBoolean(ii)){
                                                                            DataManager.opponentPlatformTakenTele.put(ii, false);
                                                                            platformButtonBlue.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTBLUE));
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            }
                                                        });
                                                        platformLayoutTwo.addView(platformButtonBlue);
                                                        platformLayoutTwo.addView(getFillerSpace(5));
                                                    }
                                                }
                                            } catch (JSONException je){
                                                je.printStackTrace();
                                            }

                                            if (getRadioXML() != null) {
                                                RadioGroup radioLayout = (RadioGroup) findViewById(getRadioXML());
                                                List<String> radioDisplayTitles = new ArrayList<>();
                                                if (getRadioData() != null) {
                                                    if (activityName().equals("auto")) {
                                                        if(getRadioData() != null){
                                                            for(int i=0; i< getRadioData().size(); i++){
                                                                radioDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getRadioData().get(i)));
                                                            }
                                                        }
                                                        radioCreator = new UIComponentCreator(this, radioDisplayTitles);
                                                        Log.e("radioSize", radioDisplayTitles.size() + "");
                                                        RadioButton rightStartPosition = radioCreator.getRadioButton("startingPosition", "right", ViewGroup.LayoutParams.MATCH_PARENT, 60);
                                                        RadioButton centerStartPosition = radioCreator.getRadioButton("startingPosition", "center", ViewGroup.LayoutParams.MATCH_PARENT, 60);
                                                        RadioButton leftStartPosition = radioCreator.getRadioButton("startingPosition", "left", ViewGroup.LayoutParams.MATCH_PARENT, 60);


                                                        rightStartPosition.setId(R.id.right); //START
                                                        centerStartPosition.setId(R.id.center);
                                                        leftStartPosition.setId(R.id.left);

                                                        radioLayout.addView(rightStartPosition);
                                                        radioLayout.addView(centerStartPosition);
                                                        radioLayout.addView(leftStartPosition);

                                                        try {
                                                            if(DataManager.collectedData.get("startingPosition").equals("right")) {
                                                                radioLayout.check(R.id.right);
                                                            }
                                                            else if(DataManager.collectedData.get("startingPosition").equals("center")) {
                                                                radioLayout.check(R.id.center);
                                                            }
                                                            else if(DataManager.collectedData.get("startingPosition").equals("left")) {
                                                                radioLayout.check(R.id.left);
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        } //END

                                                        rightStartPosition.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {DataManager.addZeroTierJsonData("startingPosition", "right");}});
                                                        centerStartPosition.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {DataManager.addZeroTierJsonData("startingPosition", "center");}});
                                                        leftStartPosition.setOnClickListener(new View.OnClickListener() {@Override public void onClick(View v) {DataManager.addZeroTierJsonData("startingPosition", "left");}});

                                                        rightStartPosition.setText("Right");
                                                        centerStartPosition.setText("Center");
                                                        leftStartPosition.setText("Left");
                                                    }
                                                }
                                            }
                                            if(activityName().equals("auto")){
                                                LinearLayout counterLayoutOne = (LinearLayout) findViewById(getCounterOneXML());
                                                List<String> counterNames = new ArrayList<>();
                                                counterCreator = new UIComponentCreator.UICounterCreator(this, counterNames);
                                                for (int i = 0; i < getCounterData().size(); i++) {
                                                    Log.e("COUNTERSIZE", getCounterData().get(i)+"");
                                                    counterNames.add(Constants.KEYS_TO_TITLES.get(getCounterData().get(i)));
                                                    counterLayoutOne.addView(counterCreator.addCounter(getCounterData().get(i)));
                                                    counterLayoutOne.addView(getFillerSpace(15));
                                                    TextView numTextView = (TextView) counterCreator.getComponentViews().get(i);
                                                    try {
                                                        numTextView.setText(DataManager.collectedData.get(getCounterData().get(i))+"");
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }else if(activityName().equals("tele")){
                                                LinearLayout counterLayoutOne = (LinearLayout) findViewById(getCounterOneXML());
                                                LinearLayout counterLayoutTwo = (LinearLayout) findViewById(getCounterTwoXML());
                                                LinearLayout vaultLayout = (LinearLayout) findViewById(getVaultXML());
                                                List<String> vaultNames = new ArrayList<>();
                                                vaultCreator = new UIComponentCreator.UIVaultCreator(this, vaultNames);
                                                vaultLayout.addView(vaultCreator.addVaultButton(vaultLayout));
                                                List<String> counterNames = new ArrayList<>();
                                                counterCreator = new UIComponentCreator.UICounterCreator(this, counterNames);
                                                for (int i = 0; i < 4; i++) {
                                                    counterNames.add(Constants.KEYS_TO_TITLES.get(getCounterData().get(i)));
                                                    counterLayoutOne.addView(counterCreator.addCounter(getCounterData().get(i)));
                                                    TextView numTextView = (TextView) counterCreator.getComponentViews().get(i);
                                                    try {
                                                        if(saveTeleData){
                                                            numTextView.setText(DataManager.collectedData.get(getCounterData().get(i))+"");
                                                        }else {
                                                            numTextView.setText(0+"");
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                                for (int i = 4; i < getCounterData().size(); i++) {
                                                    if(getCounterData().get(i).toLowerCase().equals("vault")){

                                                    }else{
                                                        counterNames.add(Constants.KEYS_TO_TITLES.get(getCounterData().get(i)));
                                                        counterLayoutTwo.addView(counterCreator.addCounter(getCounterData().get(i)));
                                                        TextView numTextView = (TextView) counterCreator.getComponentViews().get(i);

                                                        try {
                                                            if(saveTeleData){
                                                                numTextView.setText(DataManager.collectedData.get(getCounterData().get(i))+"");
                                                            }else {
                                                                numTextView.setText(0+"");
                                                            }
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                }
                                            }

                    LinearLayout attemptLayoutOne = (LinearLayout)findViewById(getAttemptOneXML());
                    LinearLayout attemptLayoutTwo = null;
                    if(getAttemptTwoXML() != null){
                        attemptLayoutTwo = (LinearLayout)findViewById(getAttemptTwoXML());
                    }
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
                            Button a_switchButton = switchCreator.addButton("allianceSwitchAttemptAuto", MainActivity.allianceColor, DataManager.autoAllianceSwitchDataArray, "alliance");
                            attemptLayoutOne.addView(a_switchButton);
                        }else if(activityName().equals("tele")){
                            if(MainActivity.allianceColor.equals("red") && (attemptLayoutOne.getChildAt(0)==null && attemptLayoutOne.getChildAt(2)==null)){
                                switchCreator.resetSwitchComponent();
                                Button a_switchButton = switchCreator.addButton("allianceSwitchAttemptTele", "red", DataManager.teleAllianceSwitchDataArray, "alliance");
                                Button o_switchButton = switchCreator.addButton("opponentSwitchAttemptTele", "blue", DataManager.teleOpponentSwitchDataArray, "opponent");
                                attemptLayoutOne.addView(a_switchButton,0);
                                attemptLayoutOne.addView(getFillerSpace(5),1);
                                attemptLayoutOne.addView(o_switchButton,2);
                            }else if(MainActivity.allianceColor.equals("blue") && (attemptLayoutOne.getChildAt(0)==null && attemptLayoutOne.getChildAt(2)==null)){
                                switchCreator.resetSwitchComponent();
                                Button a_switchButton = switchCreator.addButton("allianceSwitchAttemptTele", "blue", DataManager.teleAllianceSwitchDataArray, "alliance");
                                Button o_switchButton = switchCreator.addButton("opponentSwitchAttemptTele", "red", DataManager.teleOpponentSwitchDataArray, "opponent");
                                attemptLayoutOne.addView(o_switchButton,0);
                                attemptLayoutOne.addView(getFillerSpace(5),1);
                                attemptLayoutOne.addView(a_switchButton,2);
                            }
                        }
                    }
                    if(activityName().equals("auto")){
                        scaleCreator.resetScaleComponenet();
                        Button scaleButton = scaleCreator.addButton("scaleAttemptAuto", DataManager.autoScaleDataArray);
                        attemptLayoutOne.addView(scaleButton);
                    }else if(activityName().equals("tele")){
                        scaleCreator.resetScaleComponenet();
                        Button scaleButton = scaleCreator.addButton("scaleAttemptTele", DataManager.teleScaleDataArray);
                        attemptLayoutTwo.addView(scaleButton);
                    }
                    if(activityName().equals("auto")){
                        pyramidCreator.resetPyramidComponent();
                        Button pyramidButton = pyramidCreator.addButton();
                        attemptLayoutOne.addView(pyramidButton);
                    }else if(activityName().equals("tele")){
                        pyramidCreator.resetPyramidComponent();
                        Button pyramidButton = pyramidCreator.addButton();
                        attemptLayoutTwo.addView(pyramidButton);
                    }


                                            if(getEndGameXML() != null){
                                                LinearLayout endGameLayout = (LinearLayout) findViewById(getEndGameXML());
                                                endGameCreator = new UIComponentCreator.UIEndGameButtonCreator(this, switchDisplayTitles);
                                                Button climbButton = (Button) endGameCreator.addButton(false);
                                                ToggleButton parkButton = (ToggleButton) endGameCreator.addButton(true);
                                                endGameLayout.addView(climbButton);
                                                endGameLayout.addView(parkButton);
                                            }
    }

    //for making space between buttons
    private LinearLayout getFillerSpace(int height) {
        LinearLayout fillerSpace = new LinearLayout(this);
        fillerSpace.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height));
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
                try {
                    Log.e("TITT", currentTextViews.size()+"");
                    Log.e("TITT", getCounterData().size()+"");
                    Log.e(getCounterData().get(i)+"TITT" , ((TextView) currentTextViews.get(i)).getText().toString());
                    DataManager.addZeroTierJsonData(getCounterData().get(i), Integer.parseInt(((TextView) currentTextViews.get(i)).getText().toString()));
                } catch (Exception e) {
                    Log.e("Data Error", "Failed to add counter" + Integer.toString(i) + " num to Data");
                    Toast.makeText(this, "Error in Counter number " + Integer.toString(i), Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }

        if(activityName().equals("auto")){
            Log.e("COLLECTEDDATA!!!", DataManager.alliancePlatformTakenAuto.toString());
            DataManager.addZeroTierJsonData("alliancePlatformIntakeAuto", DataManager.alliancePlatformTakenAuto);
            Log.e("COLLECTEDDATA!!!", DataManager.collectedData.toString());
        }else if(activityName().equals("tele")){
            Log.e("COLLECTEDDATA!!!TT", DataManager.alliancePlatformTakenTele.toString());
            Log.e("COLLECTEDDATA!!!TT", DataManager.opponentPlatformTakenTele.toString());
            DataManager.addZeroTierJsonData("alliancePlatformIntakeTele", DataManager.alliancePlatformTakenTele);
            DataManager.addZeroTierJsonData("opponentPlatformIntakeTele", DataManager.opponentPlatformTakenTele);
        }

        if(numSendClicks >= 2){
            new Thread() {
                @Override
                public void run() {
                    if((activityName() == "tele")) {
                        DataManager.addZeroTierJsonData("vault", DataManager.vaultDataArray);
                        DataManager.resetVaultArray();
                        if(MainActivity.mode != null){
                            DataManager.addZeroTierJsonData("mode", MainActivity.mode);
                            int t_cycleNum = MainActivity.sharedPreferences.getInt("cycle", bgLoopThread.cycleNumber);
                            DataManager.addZeroTierJsonData("cycle", t_cycleNum);
                        }

                        if(timerDialog != null && timerDialog.isShowing()){
                            timerDialog.dismiss();
                        }

                        DataManager.qrData = new JSONObject();
                        try {
                            DataManager.qrData.put(MainActivity.teamNumber + "Q" + MainActivity.matchNumber + "-" + MainActivity.scoutNumber, DataManager.collectedData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        String qrScoutData = finalCompressedScoutData(DataManager.qrData);
                        DataManager.addZeroTierJsonData("qrScoutData", qrScoutData);
                        MainActivity.spfe.putString("qrScoutData", qrScoutData);
                        MainActivity.spfe.commit();

                        Log.e("TOTALJSON", DataManager.collectedData.toString());
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

//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                        PrintWriter newFile;
                        try {
                            File newDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/SCOUTS");
                            //make the directory of the file
                            newDir.mkdir();
                            //can delete when doing the actual thing
                            newFile = new PrintWriter(new FileOutputStream(new File(newDir, ("Q" + MainActivity.matchNumber + "_" + MainActivity.scoutNumber + "-" + MainActivity.teamNumber + ".jsontxt"))));
                        } catch (IOException IOE) {
                            Log.e("File error", "Failed to open File");
                            return;
                        }

                        JSONObject sendJson = new JSONObject();
                        try {
                            sendJson.put(MainActivity.teamNumber + "Q" + MainActivity.matchNumber + "-" + MainActivity.scoutNumber, DataManager.collectedData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        newFile.println(sendJson.toString());
                        newFile.close();
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
            }.start();
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

        intent.putExtra("matchName", "Q" + Integer.toString(this.intent.getIntExtra("matchNumber", -1))
                + "_" + Integer.toString(this.intent.getIntExtra("teamNumber", -1)));
        return intent;
    }


@Override
public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(getActionBarMenu(), menu);
    backgroundTimer.currentMenu = menu;
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

        if(!backgroundTimer.timerReady && activityName().equals("auto")) {
            menu.findItem(R.id.beginTimerButton).setEnabled(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        int id = item.getItemId();

        if(id == R.id.timerView) {
            timerDialog = new Dialog(context);
            timerDialog.setCanceledOnTouchOutside(true);
            timerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            final LinearLayout timerDialogLayout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.timer_edit_dialog, null);
            final TextView timerActivityView = (TextView) timerDialogLayout.findViewById(R.id.TimerActivityView);
            final TextView timeView = (TextView) timerDialogLayout.findViewById(R.id.TimerEditView);
            final MenuItem startTimer = (MenuItem) bgTimer.currentMenu.findItem(R.id.beginTimerButton);
            final Button minusButton = (Button) timerDialogLayout.findViewById(R.id.TimerMinusButton);
            final Button plusButton = (Button) timerDialogLayout.findViewById(R.id.TimerPlusButton);
            final Button resetButton = (Button) timerDialogLayout.findViewById(R.id.resetButton);
            Button cancelButton = (Button) timerDialogLayout.findViewById(R.id.cancelButton);

            plusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!backgroundTimer.stopTimer) {
                        offset = offset + 1;
                        timeView.setText(String.valueOf(showTime));
                    }
                }
            });

            minusButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(!(bgTimer.updatedTime < 1 && bgTimer.timerActivity == "Auto")) {
                        offset = offset - 1;
                        timeView.setText(String.valueOf(showTime));
                    }
                }
            });

            handler = new Handler(Looper.getMainLooper());
            final Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    //float updatedTime = backgroundTimer.getUpdatedTime();
                    //bgTimer.currentOffset = offset;
                    timerActivityView.setText(bgTimer.timerActivity);

                    timeView.setText(String.valueOf(showTime));
                    handler.postDelayed(this, 100);
                } // This is your code
            };
            handler.post(runnable);
            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    timerDialog.dismiss();
                    handler.removeCallbacks(runnable);
                }
            });

            resetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(activityName.equals("auto")) {
                        offset = 0;
                        bgTimer.timerReady = true;
                        if(bgTimer.matchTimer != null) {
                            bgTimer.matchTimer.cancel();
                        }
                        bgTimer.matchTimer = null;
                        item.setEnabled(false);
                        item.setTitle("");
                        startTimer.setEnabled(true);
                        timerDialog.dismiss();
                        handler.removeCallbacks(runnable);
                    }
                    else {
                        Utils.makeToast(context, "Cannot Reset Timer on Tele Screen!");
                    }
                }
            });

            timerDialog.setContentView(timerDialogLayout);
            timerDialog.show();
        }

        if(item.getItemId() == R.id.beginTimerButton && bgTimer.timerReady) {
            Menu menu = bgTimer.currentMenu;
            menu.findItem(R.id.timerView).setEnabled(true);
            bgTimer.setMatchTimer();
            item.setEnabled(false);
        }

        if (item.getItemId() == R.id.buttonNext) {
            Log.e("COLLECTEDDATA!!!", DataManager.collectedData.toString());
            rejected = false;

            synchronized (readyForNextActivityLock) {
                if (!readyForNextActivity) {
                    Log.i("Scout Error", "Tried to move on too quickly!");
                    return true;
                }
            }

            if((activityName.equals("tele"))){
                numSendClicks++;
            }

            if(activityName.equals("auto")){
                try {
                    if(DataManager.collectedData.getString("startingPosition") != null) {
                        if (!backgroundTimer.timerReady) {
                            Log.e("Starting Position?", DataManager.collectedData.getString("startingPosition"));
                            startActivity(prepareIntent(getNextActivityClass()));
                        }else{
                            Utils.makeToast(context, "PLEASE START THE TIMER!");

                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    Utils.makeToast(context, "PLEASE INPUT STARTING POSITION!");
                }
            }else if(!activityName.equals("tele")){
                startActivity(prepareIntent(getNextActivityClass()));
            }else if(activityName.equals("tele") && numSendClicks >= 2){
                backgroundTimer.stopTimer();
                if(backgroundTimer.matchTimer != null){
                    backgroundTimer.matchTimer.cancel();
                }
                //added
                int tempMatchNum = sharedPreferences.getInt("matchNumber", matchNumber) + 1;
                MainActivity.spfe.putInt("matchNumber", (tempMatchNum));
                MainActivity.spfe.commit();
                saveAutoData = false;
                saveTeleData = false;

                startActivity(prepareIntent(getNextActivityClass()));
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
                            DataManager.resetTeleOpponentPlatformArrays();
                            DataManager.resetTeleAlliancePlatformArrays();
                            rejected = true;
                            saveAutoData = true;
                            saveTeleData = false;
                            DataManager.resetTeleScaleData();
                            DataManager.resetTeleSwitchData();
                            DataManager.resetTelePyramidData();
                            Intent intent = prepareIntent(getPreviousActivityClass());
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
                            startActivity(intent);
                        }
                    })
                    .show();
        }
    }

    public String finalCompressedScoutData(JSONObject scoutJSON) {
        String compressedData = "";
        try {
            //FIRST, COMPRESS THE DATAPOINTS THAT HAVE NO NESTED KEYS
            Log.e("CHECKPOINT 1", "released");
            JSONObject uncompressed = scoutJSON;
            String headerKey = getHeaderKey(uncompressed.keys());
            JSONObject uncompressedUnderHeaderKey = new JSONObject(uncompressed.getString(headerKey));
            JSONObject compressed = new JSONObject();
            Iterator<?> uncompressedKeys= uncompressedUnderHeaderKey.keys();
            while (uncompressedKeys.hasNext()) {
                Log.e("CHECKPOINT 2", "released");
                String key = (String) uncompressedKeys.next();
                if(!Constants.nestedKeys.contains(key)){
                    Log.e("CHECKPOINT 3", "released");
                    if(key.equals("mode")){
                        Log.e("Mode compressed", Constants.compressKeys.get(key));
                    }
                    if(!Constants.unnestedKeyWithArrayValue.contains(key)){
                        if (Constants.compressValues.containsKey(uncompressedUnderHeaderKey.get(key).toString())){
                            Log.e("CHECKPOINT 4", "released");
                            if(uncompressedUnderHeaderKey.get(key).toString().equals("true") || uncompressedUnderHeaderKey.get(key).toString().equals("false")){
                                Log.e("CHECKPOINT 5", "released");
                                compressed.put(Constants.compressKeys.get(key), Double.parseDouble(Constants.compressValues.get(uncompressedUnderHeaderKey.get(key).toString())));
                            }else{
                                compressed.put(Constants.compressKeys.get(key), Constants.compressValues.get(uncompressedUnderHeaderKey.get(key).toString()));
                            }
                        } else {
                            if(key.equals("scoutName")){
                                compressed.put(Constants.compressKeys.get(key), uncompressedUnderHeaderKey.get(key).toString());
                            }else{
                                compressed.put(Constants.compressKeys.get(key), Double.parseDouble(uncompressedUnderHeaderKey.get(key).toString()));
                            }
                        }
                    }else{
                        JSONArray list = uncompressedUnderHeaderKey.getJSONArray(key);
                        JSONArray compressedList = new JSONArray();
                        for(int i = 0; i < list.length(); ++i){
                            compressedList.put(Integer.parseInt(Constants.compressValues.get(list.get(i).toString())));
                        }
                        compressed.put(Constants.compressKeys.get(key),compressedList);
                    }
                }
            }
            //THEN, COMPRESS DATAPOINTS WITH NESTED KEYS
            for(int i = 0; i < Constants.nestedKeys.size(); i++){
                JSONArray listOfDicts = new JSONArray();
                String nestedKey = Constants.nestedKeys.get(i);
                if(!nestedKey.equals("climb")) {
                    if (uncompressedUnderHeaderKey.has(nestedKey)) {
                        Log.e("nestedKey", nestedKey);
                        JSONArray keyWithNestedKeys = uncompressedUnderHeaderKey.getJSONArray(nestedKey);
                        for (int j = 0; j < keyWithNestedKeys.length(); ++j){
                            JSONObject compressedJ1 = new JSONObject();
                            JSONObject j1 = keyWithNestedKeys.getJSONObject(j);
                            Iterator<?> j1Keys = j1.keys();
                            while (j1Keys.hasNext()) {
                                String key = (String) j1Keys.next();
                                if (Constants.compressValues.containsKey(j1.get(key).toString())) {
                                    if (j1.get(key).toString().equals("true") || j1.get(key).toString().equals("false")) {
                                        compressedJ1.put(Constants.compressKeys.get(key), Double.parseDouble(Constants.compressValues.get(j1.get(key).toString())));
                                    } else {
                                        compressedJ1.put(Constants.compressKeys.get(key), Constants.compressValues.get(j1.get(key).toString()));
                                    }
                                } else {
                                    compressedJ1.put(Constants.compressKeys.get(key), Double.parseDouble(j1.get(key).toString()));
                                }
                            }
                            listOfDicts.put(compressedJ1);
                            Log.e("????", "reached");
                            compressed.put(Constants.compressKeys.get(nestedKey), listOfDicts);
                            Log.e("#####", "reached");
                        }
                    }
                }else{
                    //CLIMB DATA HAS DOUBLE NESTED KEYS
                    if(uncompressedUnderHeaderKey.has(nestedKey)){
                        Log.e("nestedKey", nestedKey);
                        try{
                            for (int k = 0; k < uncompressedUnderHeaderKey.getJSONArray(nestedKey).length(); ++k){
                                JSONObject compressedJ2 = new JSONObject();
                                JSONObject climbData = uncompressedUnderHeaderKey.getJSONArray(nestedKey).getJSONObject(k);
                                JSONObject tempCompressed = new JSONObject();
                                String climbTitle = getHeaderKey(climbData.keys());
                                JSONObject climbDetails = new JSONObject(climbData.get(climbTitle).toString());
                                Iterator<?> j1Keys = climbDetails.keys();
                                while (j1Keys.hasNext()) {
                                    String key = (String) j1Keys.next();
                                    if (Constants.compressValues.containsKey(climbDetails.get(key).toString())) {
                                        if(climbDetails.get(key).toString().equals("true") || climbDetails.get(key).toString().equals("false")){
                                            compressedJ2.put(Constants.compressKeys.get(key), Double.parseDouble(Constants.compressValues.get(climbDetails.get(key).toString())));
                                        }else{
                                            compressedJ2.put(Constants.compressKeys.get(key), Constants.compressValues.get(climbDetails.get(key).toString()));
                                        }
                                    } else {
                                        compressedJ2.put(Constants.compressKeys.get(key), Double.parseDouble(climbDetails.get(key).toString()));
                                    }
                                }
                                tempCompressed.put(Constants.compressKeys.get(climbTitle), compressedJ2);
                                listOfDicts.put(tempCompressed);
                                compressed.put(Constants.compressKeys.get(nestedKey), listOfDicts);
                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
            compressedData = headerKey + "|" + compressed.toString().substring(1, compressed.toString().length()-1).replace("\"", "").replace(" ", "");
            Log.e("FINAL", compressedData.toString());
        } catch (JSONException JE) {
            JE.printStackTrace();
            Log.e("CHECKPOINT", "SOMETHING WENT WRONG");
        }
        return compressedData;
    }

    public String getHeaderKey(Iterator<?> keys) {
        String key = "";
        while (keys.hasNext()) {
            key = (String) keys.next();
        }
        return key;
    }
}