package com.example.evan.scout;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.evan.scout.bgLoopThread.scoutName;
//
public class MainActivity extends AppCompatActivity {
    protected ScoutApplication app;

    //the database declaration
    private DatabaseReference databaseReference;

    public static String allianceColor;
    public static String capAllianceColor;

    private ActionBar actionBar;

    //the id of the scout.  1-3 is red, 4+ is blue
    public int scoutNumber;

    //the team that the scout will be scouting
    public int teamNumber;

    //the current match number
    public static int matchNumber;
    public int firebaseMatchNumber;

    //boolean if the schedule has been overridden
    public boolean overridden = false;

    public static backgroundTimer bgTimer;

    public File matchDir;

    private boolean foundIt;

    private HighSecurityPassword hsp;

    //all of the menuItems
    MenuItem overrideItem;

    EditText matchNumberEditText;
    EditText teamNumberEditText;
    public EditText searchBar;

    ListView listView;
    Spinner spinner;
    ArrayAdapter<String> adapter;
    ArrayAdapter<CharSequence> spinnerAdapter;

    //Shared Preference for scoutNumber
    SharedPreferences sharedPreferences;
    static SharedPreferences.Editor spfe;

    //set the context
    private final MainActivity context = this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //resets all firebase datanames

        hsp = new HighSecurityPassword(context, context);

        if(!DataActivity.saveAutoData){
            DataManager.collectedData = new JSONObject();
            DataManager.resetAutoSwitchData();
            DataManager.resetTeleSwitchData();
            DataManager.resetAutoPyramidData();
            DataManager.resetTelePyramidData();
            DataManager.resetAutoScaleData();
            DataManager.resetTeleScaleData();
            DataManager.resetAutoAlliancePlatformArrays();
            DataManager.resetTeleAlliancePlatformArrays();
            DataManager.resetTeleOpponentPlatformArrays();
            Utils.resetAllDataNull();
        }

        Log.e("INTEGERBOOLEANMAPLISt", DataManager.collectedData.toString());

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        MainActivity main = this;

        matchDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/d_match");

        bgTimer = new backgroundTimer(context);

        if(DataManager.subTitle != null){Log.e("subTitle", DataManager.subTitle);}

        databaseReference = FirebaseDatabase.getInstance().getReference();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //get the scout number from shared preferences, otherwise ask the user to set it
        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        spfe = sharedPreferences.edit();
        if(!sharedPreferences.contains("scoutNumber")) {
            Log.e("no previous", "scout number");
            setScoutNumber();
        }else if(sharedPreferences.contains("scoutNumber")){
            scoutNumber = sharedPreferences.getInt("scoutNumber", 0);
        }
        overridden = sharedPreferences.getBoolean("overridden", false);

        bgLoopThread bgLT = new bgLoopThread(context , scoutNumber, databaseReference, main);
        bgLT.start();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView = (ListView) findViewById(R.id.view_files_received);
        listView.setAdapter(adapter);

        if(sharedPreferences.contains("scoutName")){
            scoutName = sharedPreferences.getString("scoutName", "");
            DataManager.addZeroTierJsonData("scoutName", scoutName);
            Log.e("Last Scout name used", scoutName);
        }

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(returnDrawable());
        }

                    matchNumberEditText = (EditText)findViewById(R.id.matchNumTextEdit);
                    teamNumberEditText = (EditText) findViewById(R.id.teamNumEdit);
                    ValueEventListener matchListener = new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                if(context instanceof MainActivity){
                                    firebaseMatchNumber = dataSnapshot.getValue(Integer.class);
                                    setTeamNumber();
                                    updateAllianceColor(firebaseMatchNumber);
                                    Log.e("IMPORTANTLOG-MATCHNUM", firebaseMatchNumber+"");
                                    if(!overridden){
                                        matchNumber = firebaseMatchNumber;
                                        matchNumberEditText.setText(matchNumber+"");
                                    }
                                }
                            } else {
                                Log.e("SHIT!!!", "SHIT!!!");
                                firebaseMatchNumber = -1;
                                matchNumber = -1;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };
                    databaseReference.child("currentMatchNum").addValueEventListener(matchListener);

                    if(!internetCheck()){
                        if(sharedPreferences.getString("allianceColor", "notfound").equals("blue")){
                            setActionBarColor("blue");
                            allianceColor = "blue";
                        }else if(sharedPreferences.getString("allianceColor", "notfound").equals("red")){
                            setActionBarColor("red");
                            allianceColor = "red";
                        }else{
                            setActionBarColor("green");
                            allianceColor = "notfound";
                        }
                    }

                    if(overridden){
                        teamNumber = sharedPreferences.getInt("teamNumber", -1);
                        matchNumber = sharedPreferences.getInt("matchNumber", -1);
                    }

                    teamNumberEditText.setText(String.valueOf(teamNumber));
                    matchNumberEditText.setEnabled(false);
                    findViewById(R.id.teamNumEdit).setEnabled(false);
                    setMatchNumber();

        updateListView();
        listenForResendClick();
        setTitle("Scout");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        bgTimer.currentMenu = menu;
        getMenuInflater().inflate(R.menu.main_menu, menu);
        overrideItem = menu.findItem(R.id.mainOverride);
        if(overridden) {
            spfe.putBoolean("overridden", true);
            findViewById(R.id.matchNumTextEdit).setEnabled(true);
            findViewById(R.id.teamNumEdit).setEnabled(true);
            overrideItem.setTitle("Automate");
        }else {
            spfe.putBoolean("overridden", false);
            findViewById(R.id.matchNumTextEdit).setEnabled(false);
            findViewById(R.id.teamNumEdit).setEnabled(false);
            overrideItem.setTitle("Override");
            updateAllianceColor(firebaseMatchNumber);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.allianceColorButton) {
            setAllianceColor();

            return true;
        }

        if(id == R.id.beginTimerButton && bgTimer.timerReady) {
            bgTimer.setMatchTimer();
            item.setEnabled(false);
        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.setScoutIDButton) {
            setScoutNumber();
            return true;
        }

        if(id == R.id.currentScout){
            View dialogView = LayoutInflater.from(context).inflate(R.layout.alertdialog, null);
            TextView nameView= (TextView) dialogView.findViewById(R.id.nameView);
            spinner = (Spinner) dialogView.findViewById(R.id.nameList);
            spinnerAdapter= ArrayAdapter.createFromResource(this, R.array.name_arrays, android.R.layout.simple_spinner_dropdown_item);
            ArrayAdapter.createFromResource(this, R.array.name_arrays, android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(spinnerAdapter);
            spinner.setOnItemSelectedListener(new OnItemSelectedListener(){
                public void onItemSelected(AdapterView<?> parent, View view, int position, long arg3){
//                    String sOptions= parent.getItemAtPosition(position).toString();
//                    Toast.makeText(MainActivity.this, sOptions, Toast.LENGTH_LONG).show();
                }
                public void onNothingSelected(AdapterView<?> parent){

                }
            });

            if(scoutName != null){
                nameView.setText(scoutName);
            }else{
                nameView.setText("");
            }
            AlertDialog scoutNameAlertDialog;
            scoutNameAlertDialog = new AlertDialog.Builder(context)
                    .setView(dialogView)
                    .setTitle("")
                    .setMessage("Are you this person?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            final String spinString=spinner.getSelectedItem().toString();
                            if(spinString.equals("(No Name Selected)")){
                                scoutName=spinString;
                                DataManager.addZeroTierJsonData("scoutName", scoutName);
                                databaseReference.child("scouts").child("scout" + scoutNumber).child("currentUser").setValue(scoutName);
                                databaseReference.child("scouts").child("scout" + scoutNumber).child("scoutStatus").setValue("confirmed");
                                scoutName = spinString;
                                spfe.putString("scoutName", scoutName);
                                spfe.commit();
                                Utils.makeToast(context, "Please Input a Valid Scout Name");
                            } else{
                                scoutName=spinString;
                                DataManager.addZeroTierJsonData("scoutName", scoutName);
                                databaseReference.child("scouts").child("scout" + scoutNumber).child("currentUser").setValue(scoutName);
                                databaseReference.child("scouts").child("scout" + scoutNumber).child("scoutStatus").setValue("confirmed");
                                scoutName = spinString;
                                spfe.putString("scoutName", scoutName);
                                spfe.commit();
                                if(scoutName!=spinString){
                                    Utils.makeToast(context, "Please Input a Valid Scout Name");
                                }
                            }
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
            scoutNameAlertDialog.setCanceledOnTouchOutside(false);
        }

        if (id == R.id.mainOverride){
            if(overridden){
                matchNumber = firebaseMatchNumber;
                setTeamNumber();
                setMatchNumber();
                overridden = false;
                spfe.putBoolean("overridden", false);
                findViewById(R.id.matchNumTextEdit).setEnabled(false);
                findViewById(R.id.teamNumEdit).setEnabled(false);
                overrideItem.setTitle("Override");
                updateAllianceColor(firebaseMatchNumber);
            } else {
                overridden=true;
                spfe.putBoolean("overridden", true);
                findViewById(R.id.matchNumTextEdit).setEnabled(true);
                findViewById(R.id.teamNumEdit).setEnabled(true);
                overrideItem.setTitle("Automate");
            }
        }
        return true;
    }

    //this method will get the match number and set it from firebase
    public void setMatchNumber(){
        matchNumberEditText.setText(String.valueOf(matchNumber));
        matchNumberEditText.setTextColor(Color.parseColor("black"));
    }

    public void setTeamNumber(){

        ValueEventListener teamNumListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //will set the match number and will update it as long as it exists, if not, a value of -1 will be assigned
                if(dataSnapshot.getValue() != null && !overridden){
                    teamNumber = Integer.parseInt(dataSnapshot.getValue().toString());
                    DataManager.addZeroTierJsonData("teamNumber", teamNumber);

                    teamNumberEditText = (EditText) findViewById(R.id.teamNumEdit);
                    teamNumberEditText.setText(String.valueOf(teamNumber));

                    if(firebaseMatchNumber != 0){
                        updateAllianceColor(firebaseMatchNumber);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("firebase", "database Error");
            }
        };
        databaseReference.child("scouts").child("scout" + scoutNumber).child("team").addValueEventListener(teamNumListener);
    }

    //display dialog to set scout number
    private void setScoutNumber() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        if (scoutNumber == -1) {
            editText.setHint("Scout ID");
        } else {
            editText.setHint(Integer.toString(scoutNumber));
        }
        AlertDialog scoutIDAlertDialog;
        scoutIDAlertDialog = new AlertDialog.Builder(this)
                .setTitle("Set Scout ID")
                .setView(editText)
                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String text = editText.getText().toString();
                            if (text.equals("")) {
                                if (scoutNumber == -1) {
                                    throw new NumberFormatException();
                                }
                            } else {
                                int tmpScoutNumber = Integer.parseInt(text);
                                if ((tmpScoutNumber < 1) || (tmpScoutNumber > 18)) {
                                    throw new NumberFormatException();
                                }
                                scoutNumber = tmpScoutNumber;
                                spfe.putInt("scoutNumber", scoutNumber).apply();
                                Log.e("saved S#", scoutNumber + "");
                                Log.e("scoutNumber", "saved");
                            }
                        } catch (NumberFormatException nfe) {
                            // Do Nothing
                        }
                        setMatchNumber();
                        setTeamNumber();
                        teamNumberEditText = (EditText) findViewById(R.id.teamNumEdit);
                        teamNumberEditText.setText(String.valueOf(teamNumber));
                    }
                })
                .show();
        scoutIDAlertDialog.setCanceledOnTouchOutside(false);
    }


    //starts next activity and adds all the name and match and such
    public void startScout(View view) {
        if(allianceColor != "blue" && allianceColor != "red"){
            Utils.makeToast(context, "Please Input Alliance Color, Current Color is: "+allianceColor);
        }else if(allianceColor == "blue" || allianceColor == "red"){
            if (overridden) {
                //if the schedule has been overridden we will use the values that the user has set
                EditText teamNumEditText = (EditText) findViewById(R.id.teamNumEdit);
                if (teamNumber == -1) {
                    Toast.makeText(getBaseContext(), "Please set your team number and try again",
                            Toast.LENGTH_LONG).show();
                } else {
                    if (teamNumEditText.getText().toString().equals("")) { //START
                        Toast.makeText(getBaseContext(), "Make sure your team is set and try again",
                                Toast.LENGTH_LONG).show();
                    } else {
                        teamNumber = Integer.parseInt(teamNumEditText.getText().toString());
                    } //END
                    EditText matchNumEditText = (EditText) findViewById(R.id.matchNumTextEdit);
                    if (matchNumEditText.getText().toString().equals("")) {
                        Toast.makeText(getBaseContext(), "Make sure your match is set and try again",
                                Toast.LENGTH_LONG).show();
                    } else {
                        matchNumber = Integer.parseInt(matchNumEditText.getText().toString());
                        if (scoutNumber <= 0) {
                            setScoutNumber();
                            Toast.makeText(getBaseContext(), "Please set your number and try again",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Log.e("MATCHNUMBER1", matchNumber+"");
                            DataManager.subTitle = teamNumber + "Q" + matchNumber + "-" + scoutNumber;
                            if (matchNumber <= 0) {
                                setMatchNumber();
                                Toast.makeText(getBaseContext(), "Make sure your match is set and try again",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                Log.e("MATCHNUMBER2", matchNumber+"");
                                DataManager.subTitle = teamNumber + "Q" + matchNumber + "-" + scoutNumber;
                                if (teamNumber <= 0) {
                                    setTeamNumber();
                                    Toast.makeText(getBaseContext(), "Make sure your team is set and try again",
                                            Toast.LENGTH_LONG).show();
                                } else {
                                    try {
                                        if (!scoutName.equals("(No Name Selected)")) {
                                            EditText matchNumberEditText = (EditText) findViewById(R.id.matchNumTextEdit);
                                            String ovrrdTeamStr = ((EditText) findViewById(R.id.teamNumEdit)).getText().toString();
                                            Intent intent = new Intent(this, AutoActivity.class);
                                            if(ovrrdTeamStr != null && !ovrrdTeamStr.equals("")) {
                                                Integer ovrrdTeamNum = Integer.parseInt(ovrrdTeamStr);
                                                if(ovrrdTeamNum > 0) {
                                                    matchNumber = Integer.parseInt(matchNumberEditText.getText().toString());
                                                    Log.e("matchnumber", String.valueOf(matchNumber));
                                                    DataManager.subTitle = ovrrdTeamNum + "Q" + matchNumberEditText.getText().toString() + "-" + scoutNumber;
                                                    DataManager.addZeroTierJsonData("scoutName", scoutName);
                                                    intent.putExtra("matchNumber", matchNumber).putExtra("overridden", overridden)
                                                            .putExtra("teamNumber", ovrrdTeamNum).putExtra("scoutName", scoutName).putExtra("scoutNumber", scoutNumber);
                                                    intent.setAction("returningNoSavedData");
                                                    spfe.putBoolean("overridden", overridden);
                                                    spfe.putInt("teamNumber", teamNumber);
                                                    spfe.putInt("matchNumber", matchNumber);
                                                    spfe.putString("allianceColor", allianceColor);
                                                    spfe.commit();
                                                    DataManager.addZeroTierJsonData("teamNumber", teamNumber);
                                                    DataManager.addZeroTierJsonData("matchNumber", matchNumber);
                                                    if(bgTimer.timerReady){     Utils.makeToast(context, "REMEMBER TO CLICK START TIMER!");}
                                                    startActivity(intent);
                                                } else {
                                                    Toast.makeText(getBaseContext(), "Choose a Valid Team", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                Toast.makeText(getBaseContext(), "Choose a Valid Team", Toast.LENGTH_SHORT).show();
                                            }
                                        } else {
                                            Toast.makeText(getBaseContext(), "Input a Valid Name",
                                                    Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (NullPointerException npe) {
                                        Toast.makeText(getBaseContext(), "Input a Valid Name",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (scoutNumber <= 0) {
                    setScoutNumber();
                    Toast.makeText(getBaseContext(), "Please set your number and try again",
                            Toast.LENGTH_LONG).show();
                } else {
                    Log.e("MATCHNUMBER4", matchNumber+"");
                    DataManager.subTitle = teamNumber + "Q" + matchNumber + "-" + scoutNumber;
                    if (teamNumber <= 0) {
                        setTeamNumber();
                        Toast.makeText(getBaseContext(), "Make sure your team is set and try again",
                                Toast.LENGTH_LONG).show();
                    } else {

                        Intent intent = new Intent(this, AutoActivity.class);
                        EditText matchNumberEditText = (EditText) findViewById(R.id.matchNumTextEdit);
                        Log.e("MATCHNUMBER5", matchNumber+"");
                        DataManager.subTitle = teamNumber + "Q" + matchNumberEditText.getText().toString() + "-" + scoutNumber;
                        DataManager.addZeroTierJsonData("scoutName", scoutName);
                        intent.setAction("returningNoSavedData");
                        spfe.putString("scoutName", scoutName);
                        spfe.putInt("teamNumber", teamNumber);
                        spfe.putInt("matchNumber", matchNumber);
                        spfe.putString("allianceColor", allianceColor);
                        spfe.commit();
                        DataManager.addZeroTierJsonData("teamNumber", teamNumber);
                        DataManager.addZeroTierJsonData("matchNumber", matchNumber);
                        if(bgTimer.timerReady){     Utils.makeToast(context, "REMEMBER TO CLICK START TIMER!");}
                        startActivity(intent);
                    }
                }
            }
        }
    }
    public void setActionBarColor(String color){
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if(color.equals("red")){
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(Constants.COLOR_RED)));
            }else if(color.equals("blue")){
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(Constants.COLOR_BLUE)));
            }else if(color.equals("green")){
                actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor(Constants.COLOR_GREEN)));
            }
        }
    }

    public void listenForResendClick(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString();
                name = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/scout_data/" + name;

                final String fileName = name;
                final String[] nameOfResendMatch = name.split("Q");
                AlertDialog resendAlertDialog;
                resendAlertDialog = new AlertDialog.Builder(context)
                        .setTitle("RESEND DATA?")
                        .setMessage("RESEND " + "Q" + nameOfResendMatch[1] + "?")
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String content = readFile(fileName);
                                Log.e("XXXX","XXXX");
                                JSONObject scoutData;
                                try {
                                    scoutData = new JSONObject(content);
                                } catch (JSONException jsone) {
                                    Log.e("File Error", "no valid JSON in the file");
                                    Toast.makeText(context, "Not a valid JSON", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                List<JSONObject> dataPoints = new ArrayList<>();
                                dataPoints.add(scoutData);
                                resendScoutData(dataPoints);
                            }
                        }).show();
                resendAlertDialog.setCanceledOnTouchOutside(false);
            }
        });
    }

    public void resendAllClicked(View view) {
        AlertDialog resendAllAlertDialog;
        resendAllAlertDialog = new AlertDialog.Builder(this)
                .setTitle("RESEND ALL?")
                .setMessage("RESEND ALL DATA?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        List<JSONObject> dataPoints = new ArrayList<>();
                        for (int i = 0; i < adapter.getCount(); i++) {
                            String content;
                            String name = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/scout_data/" + adapter.getItem(i);
                            content = readFile(name);
                            if (content != null) {
                                try {
                                    JSONObject data = new JSONObject(content);
                                    dataPoints.add(data);
                                } catch (JSONException jsone) {
                                    Log.i("JSON info", "Failed to parse JSON for resend all. unimportant");
                                }
                            }
                        }
                        resendScoutData(dataPoints);

                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
        resendAllAlertDialog.setCanceledOnTouchOutside(false);
    }

    public void resendScoutData(final List<JSONObject> dataPoints) {
        new Thread() {
            @Override
            public void run() {
                //read data from file
                for (int j = 0; j < dataPoints.size(); j++) {
                    String keyName = "faulty";
                    Log.e("Test 2", "assign file data to Json");
                    JSONObject scoutData = dataPoints.get(j);
                    try {
                        //TODO im assuming here that scoutnum doesnt change
                        keyName = scoutData.getString("teamNumber")+"Q"+scoutData.getString("matchNumber")+"-"+scoutNumber;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    String jsonString = scoutData.toString();
                    Map<String, Object> jsonMap = new Gson().fromJson(jsonString, new TypeToken<HashMap<String, Object>>() {}.getType());
                    databaseReference.child("TempTeamInMatchDatas").child(keyName).setValue(jsonMap);
                }
                toasts("Resent Scout data!", false);
            }
        }.start();
    }

    public String readFile(String name) {
        BufferedReader file;
        try {
            file = new BufferedReader(new InputStreamReader(new FileInputStream(
                    new File(name))));
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Open File");
            Toast.makeText(context, "Failed To Open File", Toast.LENGTH_LONG).show();
            return null;
        }
        String dataOfFile = "";
        String buf;
        try {
            while ((buf = file.readLine()) != null) {
                dataOfFile = dataOfFile.concat(buf + "\n");
            }
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Read From File");
            Toast.makeText(context, "Failed To Read From File", Toast.LENGTH_LONG).show();
            return null;
        }
        Log.i("fileData", dataOfFile);
        return dataOfFile;
    }

    public void getScoutData(View view) {
        searchBar = (EditText) findViewById(R.id.searchEditText);
        searchBar.setFocusable(false);
        //listenForFileListClick();
        updateListView();
        searchBar.setFocusableInTouchMode(true);
    }

    public void checkPass(View view) {
        hsp.initiatePasswordChain();
    }

    public void updateListView() {

        final EditText searchBar = (EditText)findViewById(R.id.searchEditText);
        final File dir;
        dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/scout_data");
        if (!dir.mkdir()) {
            Log.i("File Info", "Failed to make Directory. Unimportant");
        }
        final File[] files = dir.listFiles();
        adapter.clear();
        Log.e("DEBUGGING", files.toString());
        for (File tmpFile : files) {
            adapter.add(tmpFile.getName());
        }
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence Register, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                if (searchBar.getText().toString().equals("")){
                    adapter.clear();
                    searchBar.setFocusable(false);
                    for (File tmpFile : files) {
                        adapter.add(tmpFile.getName());
                    }
                    searchBar.setFocusableInTouchMode(true);
                    adapter.sort(new Comparator<String>() {
                        @Override
                        public int compare(String lhs, String rhs) {
                            File lhsFile = new File(dir, lhs);
                            File rhsFile = new File(dir, rhs);
                            Date lhsDate = new Date(lhsFile.lastModified());
                            Date rhsDate = new Date(rhsFile.lastModified());
                            return rhsDate.compareTo(lhsDate);
                        }
                    });
                }else{
                    for (int i = 0; i < adapter.getCount();){
                        if(adapter.getItem(i).startsWith((searchBar.getText().toString()).toUpperCase()) || adapter.getItem(i).contains((searchBar.getText().toString()).toUpperCase())){
                            i++;
                        }else{
                            adapter.remove(adapter.getItem(i));
                        }
                    }
                }
            }
        });
        adapter.sort(new Comparator<String>() {
            @Override
            public int compare(String lhs, String rhs) {
                File lhsFile = new File(dir, lhs);
                File rhsFile = new File(dir, rhs);
                Date lhsDate = new Date(lhsFile.lastModified());
                Date rhsDate = new Date(rhsFile.lastModified());
                return rhsDate.compareTo(lhsDate);
            }
        });
        adapter.notifyDataSetChanged();
    }

    public void toasts(final String message, boolean isLongMessage) {
        if (!isLongMessage) {
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                }
            });
        }else{
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    public void onBackPressed(){
        //Do Nothing
        Toast.makeText(getBaseContext(), "Cannot Complete Operation", Toast.LENGTH_SHORT).show();
    }
    public boolean internetCheck(){
        boolean connected;
        boolean isBluetooth = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        connected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

        return connected;
    }

    public void setAllianceColor(){
        final Dialog colorDialog = new Dialog(context);
        colorDialog.setCanceledOnTouchOutside(false);
        colorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        RelativeLayout colorDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.color_dialog, null);
        //Set Dialog Title

        TextView titleTV = (TextView) colorDialogLayout.findViewById(R.id.dialogTitle);
        titleTV.setText("Alliance Color");

        Button redButton = (Button) colorDialogLayout.findViewById(R.id.redButton);
        redButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allianceColor = "red";
                spfe.putString("allianceColor", allianceColor);
                capAllianceColor = allianceColor.substring(0,1).toUpperCase() + allianceColor.substring(1);
                setActionBarColor("red");
                colorDialog.dismiss();
            }
        });
        Button blueButton = (Button) colorDialogLayout.findViewById(R.id.blueButton);
        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                allianceColor = "blue";
                spfe.putString("allianceColor", allianceColor);
                capAllianceColor = allianceColor.substring(0,1).toUpperCase() + allianceColor.substring(1);
                setActionBarColor("blue");
                colorDialog.dismiss();
            }
        });
        colorDialog.setContentView(colorDialogLayout);
        colorDialog.show();
    }

    public Drawable returnDrawable(){
        Drawable actionBarBackgroundColor = null;
        if(allianceColor != null){
            if(allianceColor.equals("red")){
                actionBarBackgroundColor = new ColorDrawable(Color.parseColor(Constants.COLOR_RED));
            }else if(allianceColor.equals("blue")){
                actionBarBackgroundColor = new ColorDrawable(Color.parseColor(Constants.COLOR_BLUE));
            }
        }
        return actionBarBackgroundColor;
    }

    public void updateAllianceColor(int mNum){
        foundIt = false;
        final String s_matchNumber = String.valueOf(mNum);
        Log.e("PLEZZZZZ", s_matchNumber);
        Log.e("PLEZZZZZ", mNum+"");
        if(!overridden){
            try{
                for(int i = 0; i < 3; i++){
                    final String num = String.valueOf(i);
                    databaseReference.child("Matches").child(s_matchNumber).child("blueAllianceTeamNumbers").child(num).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot != null){
                                if(dataSnapshot.getValue() != null){
                                    if(teamNumber != 0 || teamNumber != -1){
                                        if(teamNumber == Integer.parseInt(dataSnapshot.getValue().toString()) && !foundIt){
                                            Log.e("PLEZZZZZ", s_matchNumber);
                                            Log.e("PLEZZZZZ", "blueAllianceTeamNumbers");
                                            Log.e("PLEZZZZZ", dataSnapshot.getValue().toString());
                                            allianceColor = "blue";
                                            spfe.putString("allianceColor", allianceColor);
                                            capAllianceColor = allianceColor.substring(0,1).toUpperCase() + allianceColor.substring(1);
                                            setActionBarColor("blue");
                                            foundIt = true;
                                        }else if(foundIt){
                                        }else{
                                            allianceColor = "notfound";
                                            spfe.putString("allianceColor", allianceColor);
                                        }
                                    }else{
                                        Log.e("SHIT1", "SHIT1");
                                        allianceColor = "notfound";
                                        spfe.putString("allianceColor", allianceColor);
                                    }
                                }else{
                                    Log.e("SHIT2", "SHIT2");
                                }
                            }else{
                                Log.e("SHIT3", "SHIT3");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                            if(!foundIt){
                                Log.e("SHIT4", "SHIT4");
                                setActionBarColor("green");
                                allianceColor = "notfound";
                                spfe.putString("allianceColor", allianceColor);
                            }
                        }
                    });
                    databaseReference.child("Matches").child(s_matchNumber).child("redAllianceTeamNumbers").child(num).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot != null){
                                if(dataSnapshot.getValue() != null){
                                    if(teamNumber != 0 || teamNumber != -1 && !foundIt) {
                                        if(teamNumber == Integer.parseInt(dataSnapshot.getValue().toString())){
                                            Log.e("PLEZZZZZ", s_matchNumber);
                                            Log.e("PLEZZZZZ", "redAllianceTeamNumbers");
                                            Log.e("PLEZZZZZ", dataSnapshot.getValue().toString());
                                            allianceColor = "red";
                                            spfe.putString("allianceColor", allianceColor);
                                            capAllianceColor = allianceColor.substring(0,1).toUpperCase() + allianceColor.substring(1);
                                            setActionBarColor("red");
                                            foundIt = true;
                                        }else if(foundIt){
                                        }else{
                                            allianceColor = "notfound";
                                            spfe.putString("allianceColor", allianceColor);
                                        }
                                    }else{
                                        Log.e("SHIT5", "SHIT5");
                                        allianceColor = "notfound";
                                        spfe.putString("allianceColor", allianceColor);
                                    }
                                }else{
                                    Log.e("SHIT6", "SHIT6");
                                }
                            }else{
                                Log.e("SHIT7", "SHIT7");
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError firebaseError) {
                            if(!foundIt){
                                Log.e("SHIT8", "SHIT8");
                                setActionBarColor("green");
                                allianceColor = "notfound";
                                spfe.putString("allianceColor", allianceColor);
                            }
                        }
                    });
                }
                if((allianceColor == null || allianceColor == "notfound") && !foundIt){
                    setActionBarColor("green");
                    allianceColor = "notfound";
                    spfe.putString("allianceColor", allianceColor);
                }
            }catch(DatabaseException de){
                Log.e("SHIT9", "SHIT9");
                if(!foundIt){
                    setActionBarColor("green");
                    allianceColor = "notfound";
                    spfe.putString("allianceColor", allianceColor);
                }
            }
        }else if(overridden && (allianceColor != "blue" && allianceColor != "red")){
            setActionBarColor("green");
            allianceColor = "notfound";
        }
    }
}
