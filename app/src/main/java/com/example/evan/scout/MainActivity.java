package com.example.evan.scout;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.SyncStateContract;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.example.evan.scout.bgLoopThread.scoutName;

/**
 * Created by Calvin on 7/26/17.
 */

// 8/31/17 8:28

public class MainActivity extends AppCompatActivity {
    protected ScoutApplication app;

    public Integer currentMatchNumber;

    //the database declaration
    private DatabaseReference databaseReference;

    public static String teamColor;

    //the id of the scout.  1-3 is red, 4+ is blue
    public int scoutNumber;

    //the team that the scout will be scouting
    public int teamNumber;

    //the current match number
    public static int matchNumber;

    //boolean if the schedule has been overridden
    public boolean overridden = false;

    //all of the menuItems
    MenuItem overrideItem;

    EditText matchNumberEditText;
    EditText searchBar;

    ListView listView;
    ArrayAdapter<String> adapter;

    //Shared Preference for scoutNumber
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    private boolean bluetoothOff = false;
    private boolean bluetoothOn = false;

    //set the context
    private final MainActivity context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        //get the scout number from shared preferences, otherwise ask the user to set it
        sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        if(!sharedPreferences.contains("scoutNumber")) {
            Log.e("no previous", "scout number");
            setScoutNumber();
        }else if(sharedPreferences.contains("scoutNumber")){
            scoutNumber = sharedPreferences.getInt("scoutNumber", 0);
        }

        bgLoopThread bgLT = new bgLoopThread(context , scoutNumber, databaseReference);
        bgLT.start();

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView = (ListView) findViewById(R.id.view_files_received);
        listView.setAdapter(adapter);

        if(sharedPreferences.contains("scoutName")){
            scoutName = sharedPreferences.getString("scoutName", "");
            DataManager.addZeroTierJsonData("scoutName", scoutName);
            Log.e("Last Scout name used", scoutName);
        }

        currentMatchNumber = -1;
        new MatchNumListener(new MatchNumListener.MatchFirebaseInterface() {
            @Override
            public void onMatchChanged() {
                currentMatchNumber = MatchNumListener.currentMatchNumber;
                Log.e("MEME", currentMatchNumber+"");
                findColor();
                if(!overridden) {
                    setMatchNumber();
                }
            }
        });

        //get and set match number from firebase
        setMatchNumber();

        //get and set team number to scout from firebase
        setTeamNumber();
//--------------------------------------------------------------------------------------------------
                    EditText teamNumberEditText = (EditText) findViewById(R.id.teamNumEdit);
                    teamNumberEditText.setText(String.valueOf(teamNumber));

                    //block the edittext from being edited until overridden
                    matchNumberEditText = (EditText)findViewById(R.id.matchNumTextEdit);
                    matchNumberEditText.setEnabled(false);
                    findViewById(R.id.teamNumEdit).setEnabled(false);

        updateListView();
        listenForResendClick();
        setTitle("Scout");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        overrideItem = menu.findItem(R.id.mainOverride);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.setScoutIDButton) {
            setScoutNumber();
            return true;
        }

        if(id == R.id.currentScout){
            Toast.makeText(getBaseContext(), scoutName, Toast.LENGTH_SHORT).show();
        }

        if (id == R.id.mainOverride){
            if(overridden){
                setTeamNumber();
                setMatchNumber();
                overridden = false;
                findViewById(R.id.matchNumTextEdit).setEnabled(false);
                findViewById(R.id.teamNumEdit).setEnabled(false);
                overrideItem.setTitle("Override Schedule");
            } else {
                overridden=true;
                findViewById(R.id.matchNumTextEdit).setEnabled(true);
                findViewById(R.id.teamNumEdit).setEnabled(true);
                overrideItem.setTitle("Automate Schedule");
            }
        }
        return true;
    }

    //this method will get the match number and set it from firebase
    public void setMatchNumber(){
        EditText matchNumberEditText = (EditText) findViewById(R.id.matchNumTextEdit);
        matchNumberEditText.setText(String.valueOf(currentMatchNumber));
        matchNumberEditText.setTextColor(Color.parseColor("black"));
    }

    public void setTeamNumber(){

        ValueEventListener matchNumberListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //will set the match number and will update it as long as it exists, if not, a value of -1 will be assigned
                if(dataSnapshot.getValue() != null){
                    teamNumber = Integer.parseInt(dataSnapshot.getValue().toString());
                } else {
                    teamNumber = -1;
                }
                if(!overridden) {
                    EditText teamNumberEditText = (EditText) findViewById(R.id.teamNumEdit);
                    teamNumberEditText.setText(String.valueOf(teamNumber));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.i("firebase", "database Error");
                teamNumber = -1;
                if(!overridden) {
                    EditText matchNumberEditText = (EditText) findViewById(R.id.matchNumTextEdit);
                    matchNumberEditText.setText(String.valueOf(teamNumber));
                }
            }
        };
        databaseReference.child("scouts").child("scout" + scoutNumber).child("team").addValueEventListener(matchNumberListener);
    }

    public void findColor(){
        for(int i = 0; i < 3; i++){
            final int num = i;
            databaseReference.child("Matches").child(currentMatchNumber+"").child("blueAllianceTeamNumbers").child(i+"").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(Integer.parseInt(dataSnapshot.getValue().toString()) == teamNumber){
                        setActionBarColor(Constants.COLOR_BLUE);
                        teamColor = "blue";
                        Log.e("CALLED!!!", teamColor);
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });
            databaseReference.child("Matches").child(currentMatchNumber+"").child("redAllianceTeamNumbers").child(i+"").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(Integer.parseInt(dataSnapshot.getValue().toString()) == teamNumber){
                        setActionBarColor(Constants.COLOR_RED);
                        teamColor = "red";
                        Log.e("CALLED!!!", teamColor);
                    }
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {

                }
            });
        }
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
        new AlertDialog.Builder(this)
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
                                editor.putInt("scoutNumber", scoutNumber).apply();
                                Log.e("saved S#", scoutNumber + "");
                                Log.e("scoutNumber", "saved");
                            }
                        } catch (NumberFormatException nfe) {
                            // Do Nothing
                        }
                        setMatchNumber();
                        setTeamNumber();
                        EditText teamNumberEditText = (EditText) findViewById(R.id.teamNumEdit);
                        teamNumberEditText.setText(String.valueOf(teamNumber));
                    }
                })
                .show();
    }


    //starts next activity and adds all the name and match and such
    public void startScout(View view) {
        Log.i("HATRED", "STARTSCOUT");
        if (overridden) {
            Log.i("HATRED", "ISOVERRIDEN");
            //if the schedule has been overridden we will use the values that the user has set
            EditText teamNumEditText = (EditText) findViewById(R.id.teamNumEdit);
            if (teamNumEditText.getText().toString().equals("")) {
                Toast.makeText(getBaseContext(), "Please set your team number and try again",
                        Toast.LENGTH_LONG).show();
            } else {
                teamNumber = Integer.parseInt(teamNumEditText.getText().toString());
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
                        DataManager.subTitle = teamNumber + "Q" + currentMatchNumber + "-" + scoutNumber;
                        if (matchNumber <= 0) {
                            setMatchNumber();
                            Toast.makeText(getBaseContext(), "Make sure your match is set and try again",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            DataManager.subTitle = teamNumber + "Q" + currentMatchNumber + "-" + scoutNumber;
                            if (teamNumber <= 0) {
                                setTeamNumber();
                                Toast.makeText(getBaseContext(), "Make sure your team is set and try again",
                                        Toast.LENGTH_LONG).show();
                            } else {
                                try {
                                    if (!scoutName.equals("")) {
                                        EditText matchNumber = (EditText) findViewById(R.id.matchNumTextEdit);
                                        String ovrrdTeamStr = ((EditText) findViewById(R.id.teamNumEdit)).getText().toString();
                                        Intent intent = new Intent(this, AutoActivity.class);
                                        if(ovrrdTeamStr != null && !ovrrdTeamStr.equals("")) {
                                            Integer ovrrdTeamNum = Integer.parseInt(ovrrdTeamStr);
                                            if(ovrrdTeamNum > 0) {
                                                DataManager.subTitle = ovrrdTeamNum + "Q" + Integer.parseInt(matchNumber.getText().toString()) + "-" + scoutNumber;
                                                DataManager.addZeroTierJsonData("scoutName", scoutName);
                                                intent.putExtra("matchNumber", Integer.parseInt(matchNumber.getText().toString())).putExtra("overridden", overridden)
                                                .putExtra("teamNumber", ovrrdTeamNum).putExtra("scoutName", scoutName).putExtra("scoutNumber", scoutNumber);
                                                intent.setAction("returningNoSavedData");
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
            Log.i("HATRED", "ISNOTOVERRIDEN");
            if (scoutNumber <= 0) {
                setScoutNumber();
                Toast.makeText(getBaseContext(), "Please set your number and try again",
                        Toast.LENGTH_LONG).show();
            } else {
                DataManager.subTitle = teamNumber + "Q" + currentMatchNumber + "-" + scoutNumber;
                if (teamNumber <= 0) {
                    setTeamNumber();
                    Toast.makeText(getBaseContext(), "Make sure your team is set and try again",
                            Toast.LENGTH_LONG).show();
                } else {
                    Intent intent = new Intent(this, AutoActivity.class);
                    EditText matchNumber = (EditText) findViewById(R.id.matchNumTextEdit);
                    DataManager.subTitle = teamNumber + "Q" + currentMatchNumber + "-" + scoutNumber;
                    DataManager.addZeroTierJsonData("scoutName", scoutName);
                    intent.setAction("returningNoSavedData");
                    SharedPreferences.Editor spfe = sharedPreferences.edit();
                    spfe.putString("lastScoutName", scoutName);
                    spfe.commit();
                    startActivity(intent);

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
            }
        }
    }

//    public void setScoutNameListener() {
//        Log.e("scoutNumber", String.valueOf(scoutNumber));
//        if (scoutNumber > 0){
//            databaseReference.child("scouts").child(String.valueOf("scout" + scoutNumber)).child("currentUser").addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(final DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.getValue() != null && !dataSnapshot.getValue().toString().equals("")) {
//                        final String tempScoutName = dataSnapshot.getValue().toString();
//                        if(!sharedPreferences.getString("scoutName", " ").equals(tempScoutName)) {
//                            new AlertDialog.Builder(context)
//                                    .setTitle("")
//                                    .setMessage("Are you " + tempScoutName + "?")
//                                    .setCancelable(false)
//                                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            scoutName = tempScoutName;
//                                            DataManager.addZeroTierJsonData("scoutName", scoutName);
//                                            databaseReference.child("scouts").child("scout" + scoutNumber).child("scoutStatus").setValue("confirmed");
//                                            Log.e("tempScoutName", tempScoutName);
//                                            sharedPreferences.edit().remove("scoutName").apply();
//                                            editor.putString("scoutName", tempScoutName).commit();
//                                        }
//                                    })
//                                    .setIcon(android.R.drawable.ic_dialog_alert)
//                                    .show();
//                        }else if(sharedPreferences.getString("scoutName", " ").equals(tempScoutName)){
//                            scoutName = tempScoutName;
//                            DataManager.addZeroTierJsonData("scoutName", scoutName);
//                            databaseReference.child("scouts").child("scout" + scoutNumber).child("scoutStatus").setValue("confirmed");
//                        }
//
//                    } else {
//                        //setScoutName();
//                    }
//                    EditText teamNumberEditText = (EditText) findViewById(R.id.teamNumEdit);
//                    teamNumberEditText.setText(String.valueOf(teamNumber));
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }
//    }

    public void listenForResendClick(){
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = parent.getItemAtPosition(position).toString();
                name = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/scout_data/" + name;

                final String fileName = name;
                final String[] nameOfResendMatch = name.split("Q");
                new AlertDialog.Builder(context)
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
                                JSONObject superData;
                                try {
                                    superData = new JSONObject(content);
                                } catch (JSONException jsone) {
                                    Log.e("File Error", "no valid JSON in the file");
                                    Toast.makeText(context, "Not a valid JSON", Toast.LENGTH_LONG).show();
                                    return;
                                }
                                List<JSONObject> dataPoints = new ArrayList<>();
                                dataPoints.add(superData);
                                resendScoutData(dataPoints);
                            }
                        }).show();
            }
        });
    }

    public void resendAllClicked(View view) {
        new AlertDialog.Builder(this)
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
    }

    public void resendScoutData(final List<JSONObject> dataPoints) {
        new Thread() {
            @Override
            public void run() {
                //read data from file
                for (int j = 0; j < dataPoints.size(); j++) {
                    Log.e("Test 2", "assign file data to Json");
                    JSONObject superData = dataPoints.get(j);

                    Utils.SendFirebaseData(databaseReference, superData);
                }
                toasts("Resent Super data!", false);
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

    public void updateListView() {

        final EditText searchBar = (EditText)findViewById(R.id.searchEditText);
        final File dir;
        dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/scout_data");
        if (!dir.mkdir()) {
            Log.i("File Info", "Failed to make Directory. Unimportant");
        }
        final File[] files = dir.listFiles();
        adapter.clear();
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
}
