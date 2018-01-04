package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
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
    public abstract List<String> getShotData();
    public abstract Integer getToggleXML();
    public abstract Integer getCounterXML();
    public abstract Integer getShotXML();
    public abstract Integer getOtherXML();
    public abstract Class getNextActivityClass();
    public abstract Class getPreviousActivityClass();
    public abstract int getActionBarMenu();
    public Boolean shouldSpaceToggles() {return false;}
    public Boolean doTogglesDepend() {return false;}

    public static boolean saveTeleData = false;
    public static boolean saveAutoData = false;
    public static String activityName;
    public static boolean rejected = false;

    private boolean sent;

    private int numSendClicks;
    public final Activity context = this;
    File dir;
    PrintWriter file;

    private Intent intent;
    private UIComponentCreator toggleCreator;
    private UIComponentCreator.UICounterCreator counterCreator;
    private UIComponentCreator.UIGearCreator gearCreator;
    private UIComponentCreator.UIButtonCreator liftOffCreator;
    private UIComponentCreator.UIShotCreator shotCreator;
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
        intent = getIntent();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        numSendClicks = 0;

                        Drawable actionBarBackgroundColor;

                        if(MainActivity.teamColor != null){
                            if(MainActivity.teamColor.equals("blue")){
                                actionBarBackgroundColor = new ColorDrawable(Color.parseColor(Constants.COLOR_BLUE));
                            }else if(MainActivity.teamColor.equals("red")){
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
        if(getToggleXML() != null){
            LinearLayout toggleLayout = (LinearLayout)findViewById(getToggleXML());
            List<String> toggleDisplayTitles = new ArrayList<>();
            if(getToggleData() != null) {
                Log.e("toggleSize", getToggleData().size()+"");
                for (int i = 0; i < getToggleData().size(); i++) {
                    toggleDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getToggleData().get(i)));
                }

                toggleCreator = new UIComponentCreator(this, toggleDisplayTitles);
                Log.e("toggleSize", toggleDisplayTitles.size()+"");
                //template version, you can add as many as you want example bellow
                final ToggleButton button1 = toggleCreator.getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false);

                //our game didn't have toggles in auto, so I added this if
                if(saveTeleData && activityName() == "tele"){
                    try {
                        button1.setChecked(DataManager.collectedData.getBoolean(getToggleData().get(0)));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

                //xml layout beutification - :D lol
                liftOffCreator = new UIComponentCreator.UIButtonCreator(this, null);

                for (int i = 0; i < getToggleData().size(); i+=2) {
                    toggleLayout.addView(button1);
                    if (shouldSpaceToggles()) {
                        toggleLayout.addView(getFillerSpace(0.2f));
                    }
                    if (shouldSpaceToggles() && i+1 != getToggleData().size()-1) {
                        toggleLayout.addView(getFillerSpace(0.2f));
                    }

                }
            }
        }

                        //shot stuff, will probably be useless
                        LinearLayout shotLayout = (LinearLayout)findViewById(getShotXML());
                        List<String> shotNames = new ArrayList<>();
                        for (int i = 0; i < getShotData().size(); i++) {
                            shotNames.add(Constants.KEYS_TO_TITLES.get(getShotData().get(i)));
                        }
                        Log.e("shotNames",String.valueOf(shotNames.size()));
                        shotCreator = new UIComponentCreator.UIShotCreator(this, shotNames);
                        for (int i = 0; i < getShotData().size(); i+=2) {
                            Button button1 = null;
                            Button button2 = null;

                            if(activityName() == "auto"){
                                Log.e("first",i+"");
                                button1 = shotCreator.addButton("highShotTimesForBoilerAuto");
                                button2 = shotCreator.addButton("lowShotTimesForBoilerAuto");
                            }else if(activityName() == "tele"){
                                button1 = shotCreator.addButton("highShotTimesForBoilerTele");
                                button2 = shotCreator.addButton("lowShotTimesForBoilerTele");
                            }

                            shotLayout.addView(button1);
                            shotLayout.addView(getFillerSpace(0.3f));
                            shotLayout.addView(button2);
                        }


        //counters
        LinearLayout counterLayout = (LinearLayout) findViewById(getCounterXML());
        List<String> counterNames = new ArrayList<>();
        for (int i = 0; i < getCounterData().size(); i++) {
            counterNames.add(Constants.KEYS_TO_TITLES.get(getCounterData().get(i)));
        }
        counterCreator = new UIComponentCreator.UICounterCreator(this, counterNames);
        for (int i = 0; i < getCounterData().size(); i++) {
            if(i == (getCounterData().size() - 1) && activityName() == "tele"){
            }else{
                counterLayout.addView(counterCreator.addCounter(getCounterData().get(i)));
                if(activityName() == "tele"){
                    Log.e("telecounter", "counterMade"+i);
                }
            }
        }
        counterLayout.addView(getFillerSpace(1f));
    }

    //for beautification
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


                        //some real compicated shit, just kidding - can't find way to custom in menu - if you can then do it :D
                        @Override
                        public boolean onCreateOptionsMenu(Menu menu) {
                            getMenuInflater().inflate(getActionBarMenu(), menu);
                            if (activityName() == "auto") {
                                try {
                                    MenuItem textView = menu.findItem(R.id.teamNumTextView);
                                    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.textview, null);
                                    TextView teamNumTextView = (TextView) linearLayout.findViewById(R.id.normalTV);
                                    teamNumTextView.setText(DataManager.collectedData.getString("teamNumber"));
                                    textView.setActionView(teamNumTextView);
                                    MenuItem button = menu.findItem(R.id.buttonNext);
                                    Button buttonNext = (Button) findViewById(R.id.nextButton);
                                    buttonNext.setText(button.getTitle());
                                    buttonNext.setGravity(Gravity.RIGHT);
                                    button.setActionView(buttonNext);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }else if(activityName() == "tele"){
                                try{
                                    MenuItem textView = menu.findItem(R.id.teamNumTextView);
                                    TextView teamNumTextView = (TextView) findViewById(R.id.normalTV);
                                    teamNumTextView.setText(DataManager.collectedData.getString("teamNumber"));
                                    teamNumTextView.setGravity(Gravity.RIGHT);
                                    textView.setActionView(teamNumTextView);
                                    MenuItem button = menu.findItem(R.id.buttonNext);
                                    Button buttonNext = (Button) findViewById(R.id.nextButton);
                                    buttonNext.setText(button.getTitle());
                                    button.setActionView(buttonNext);
                                }catch(JSONException e){
                                    e.printStackTrace();
                                }
                            }

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