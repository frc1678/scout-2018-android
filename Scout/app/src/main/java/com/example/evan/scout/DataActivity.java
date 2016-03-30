package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.gson.JsonParseException;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public abstract class DataActivity extends AppCompatActivity {
    public abstract List<String> getToggleData();
    public abstract List<String> getCounterData();
    public abstract List<String> getShotData();
    public abstract List<String> getDefenseData();
    public abstract Class getNextActivityClass();
    public abstract Class getPreviousActivityClass();

    public final Activity context = this;

    private Intent intent;
    private LocalTeamInMatchData collectedData;
    private UIComponentCreator toggleCreator;
    private UIComponentCreator counterCreator;
    private UIComponentCreator.UIShotCreator shotCreator;
    private UIComponentCreator.UIButtonCreator defenseCreator;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
        intent = getIntent();
        parseJson(intent.getStringExtra("previousData"));
        updateUI();
    }

    private void parseJson(String json) {
        try {
            collectedData = (LocalTeamInMatchData)Utils.deserializeClass(json, LocalTeamInMatchData.class);
        } catch (JsonParseException|NullPointerException e) {
            collectedData = new LocalTeamInMatchData();
        }
    }

    private void updateUI() {
        LinearLayout toggleLayout = (LinearLayout)findViewById(R.id.dataActivityToggleLayout);
        List<String> toggleDisplayTitles = new ArrayList<>();
        for (int i = 0; i < getToggleData().size(); i++) {
            toggleDisplayTitles.add(Constants.KEYS_TO_TITLES.get(getToggleData().get(i)));
        }
        toggleCreator = new UIComponentCreator(this, toggleDisplayTitles);
        for (int i = 0; i < getToggleData().size(); i++) {
            try {
                toggleLayout.addView(toggleCreator.getNextToggleButton(LinearLayout.LayoutParams.MATCH_PARENT,
                        (Boolean) Utils.getField(collectedData, getToggleData().get(i))));
            } catch (Exception e) {
                Log.i("Data Error", "Failed to get field from collectedData. Not including toggle");
            }
        }
        LinearLayout counterLayout = (LinearLayout)findViewById(R.id.dataActivityCounterLayout);
        List<String> counterNames = new ArrayList<>();
        for (int i = 0; i < getCounterData().size(); i++) {
            counterNames.add(Constants.KEYS_TO_TITLES.get(getCounterData().get(i)));
        }
        counterCreator = new UIComponentCreator(this, counterNames);
        for (int i = 0; i < getCounterData().size(); i++) {
            try {
                counterLayout.addView(counterCreator.getNextTitleRow(1));
                counterLayout.addView(counterCreator.getNextCounterRow(1, (Integer)Utils.getField(collectedData, getCounterData().get(i))));
            } catch (Exception e) {
                Log.i("Data Error", "Failed to get field from collectedData. Not including counter");
            }
        }
        List<String> shotNames = new ArrayList<>();
        for (int i = 0; i < getShotData().size(); i++) {
            shotNames.add(Constants.KEYS_TO_TITLES.get(getShotData().get(i)));
        }
        shotCreator = new UIComponentCreator.UIShotCreator(this, shotNames);
        for (int i = 0; i < getShotData().size(); i+=2) {
            try {
                shotCreator.addButtonRow(counterLayout, (Integer)Utils.getField(collectedData, getShotData().get(i)),
                        (Integer)Utils.getField(collectedData, getShotData().get(i+1)), i/2);
            } catch (Exception e) {
                Log.i("Data Error", "Failed to get field from collectedData.  Not including shot button");
            }
        }
        LinearLayout defenseLayout = (LinearLayout)findViewById(R.id.dataActivityDefenseLayout);
        List<String> defenseNames = new ArrayList<>();
        for (int i = 0; i < getDefenseData().size(); i++) {
            defenseNames.add(Constants.KEYS_TO_TITLES.get(getDefenseData().get(i)));
        }
        defenseCreator = new UIComponentCreator.UIButtonCreator(this, defenseNames);
        for (int i = 0; i < getDefenseData().size(); i++) {
            try {
                //TODO rewrite addButton to take a sublist of defenseTimes
            } catch (Exception e) {
                Log.i("Data Error", "Failed to get field from collectedData.  Not including defense button");
            }
        }
    }
    private void updateData() {

    }

    private Intent prepareIntent(Class clazz) {
        updateData();
        Intent intent = new Intent(this, clazz);
        intent.putExtras(this.intent);
        try {
            intent.putExtra("previousData", Utils.serializeClass(collectedData));
        } catch (JsonParseException jpe) {
            intent.putExtra("previousData", (String)null);
        }
        return intent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.auto_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.buttonNext) {
            startActivity(prepareIntent(getNextActivityClass()));
        }
        return true;
    }



    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Stop Scouting")
                .setMessage("Are you sure you want to go back now?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(prepareIntent(getPreviousActivityClass()));
                    }
                })
                .show();
    }
}
