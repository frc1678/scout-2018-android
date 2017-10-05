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
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
    public int matchNumber;

    //boolean if the schedule has been overridden
    public boolean overridden = false;

    //all of the menuItems
    MenuItem overrideItem;

    EditText matchNumberEditText;

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
                if(!overridden) {
                    setMatchNumber();
                }
            }
        });

        //get and set match number from firebase
        setMatchNumber();

        //get and set team number to scout from firebase
        setTeamNumber();
        EditText teamNumberEditText = (EditText) findViewById(R.id.teamNumEdit);
        teamNumberEditText.setText(String.valueOf(teamNumber));

        //block the edittext from being edited until overridden
        matchNumberEditText = (EditText)findViewById(R.id.matchNumTextEdit);
        matchNumberEditText.setEnabled(false);
        findViewById(R.id.teamNumEdit).setEnabled(false);

        //set title
        databaseReference.child("scouts").child("scout" + scoutNumber).child("currentUser").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    scoutName = dataSnapshot.getValue(String.class);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Do Nothing.
            }
        });

        databaseReference.child("Matches").child(matchNumber+"").child("blueAllianceTeamNumbers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    if(teamNumber == (int)dataSnapshot.getValue()){
                        setActionBarColor(Constants.COLOR_BLUE);
                        teamColor = "blue";
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        databaseReference.child("Matches").child(matchNumber+"").child("redAllianceTeamNumbers").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.getValue() != null){
                    if(teamNumber == (int)dataSnapshot.getValue()){
                        setActionBarColor(Constants.COLOR_RED);
                        teamColor = "red";
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

    @Override
    public void onBackPressed(){
        //Do Nothing
        Toast.makeText(getBaseContext(), "Cannot Complete Operation", Toast.LENGTH_SHORT).show();
    }
}
