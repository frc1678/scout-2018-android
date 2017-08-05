//package com.example.evan.scout;
//
//import android.app.AlertDialog;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.ActivityInfo;
//import android.graphics.Color;
//import android.graphics.drawable.ColorDrawable;
//import android.graphics.drawable.Drawable;
//import android.support.v7.app.ActionBar;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.text.Editable;
//import android.text.InputType;
//import android.text.TextWatcher;
//import android.util.Log;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.ListView;
//import android.widget.TextView;
//import android.widget.Toast;
//
//
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import org.jcodec.containers.mp4.boxes.Edit;
//import org.jcodec.movtool.Util;
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.io.FileReader;
//import java.text.SimpleDateFormat;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.List;
//import java.util.Locale;
//
//
//public class MainActivity extends AppCompatActivity {
//    //current match the scout is on
//    private int matchNumber;
//    private int currentMatchNumber;
//
//    private int teamNumber;
//
//    //whether the automatic match progression is overridden or not
//    private static volatile boolean overridden = false;
//
//    //shared preferences to receive previous matchNumber, scoutNumber
//    private SharedPreferences preferences;
//    SharedPreferences.Editor editor;
//    private static final String PREFERENCES_FILE = "com.example.evan.scout";
//
//    //the id of the scout.  1-3 is red, 4-6 is blue
//    private int scoutNumber;
//
//    //initials of scout scouting
//    private String scoutName;
//
//    private EditText scoutTeamText;
//    private EditText scoutMatchText;
//
//    //save a reference to this activity for subclasses
//    private final MainActivity context = this;
//    private DatabaseReference databaseReference;
//
//    private List<String> matchNumPath = Arrays.asList("currentMatchNum");
//    private List<String> scoutPath = new ArrayList<>();
//    private List<String> teamPath = new ArrayList<>();
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        //lock screen horizontal
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        databaseReference = FirebaseDatabase.getInstance().getReference();
//
//        scoutTeamText = (EditText) findViewById(R.id.teamNumber1Edit);
//        scoutTeamText.setFocusableInTouchMode(false);
//        scoutMatchText = (EditText) findViewById(R.id.matchNumberText);
//        scoutMatchText.setFocusableInTouchMode(false);
//
//        //get any values received from other activities
//        preferences = getSharedPreferences(PREFERENCES_FILE, 0); //TODO
//        overridden = getIntent().getBooleanExtra("overridden", false);
//        matchNumber = getIntent().getIntExtra("matchNumber", -1);
//
//                    //scout initials
//                    if(getIntent().getStringExtra("scoutName")!=null){
//                        scoutName = getIntent().getStringExtra("scoutName");
//                    }else{
//                        scoutName = "NO_NAME_ASSIGNED";
//                    }
//
//                    scoutNumber = preferences.getInt("scoutNumber", -1);
//                    if (scoutNumber == -1) {
//                        setScoutNumber();
//                    }
//                    scoutPath.add("scouts"); scoutPath.add("scout" + scoutNumber); scoutPath.add("currentScout");
//                    scoutName = Utils.GetFirebaseData(databaseReference, scoutPath);
//
//        if (!overridden) {
//            Log.e("HCINT","preclude");
//            setMatchNumber();
//            //otherwise, save it to hard disk
//        } else {
//            matchNumber++;
//
//            editor = preferences.edit();
//            editor.putInt("matchNumber", matchNumber);
//            editor.commit();
//        }
//        setMatchNumber();
//    }
//
//    //update actionbar at top of screen, either giving them the option to override or automate
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.main_menu, menu);
//        if (overridden) {
//            MenuItem item = menu.findItem(R.id.mainOverride);
//            item.setTitle("Automate Schedule");
//        }
//        return true;
//    }
//
//
//
//    //onclicks for buttons on actionbar
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        //override button
//        if (item.getItemId() == R.id.mainOverride) {
//            Log.e("override", "clicked");
//            if (overridden) {
//                MainActivity.overridden = false;
//                item.setTitle("Override Schedule");
//                setTeamNumber();
//                setMatchNumber();
//
//            } else {
//                MainActivity.overridden = true;
//                item.setTitle("Overridden");
//                scoutTeamText = (EditText) findViewById(R.id.teamNumber1Edit);
//                scoutTeamText.setFocusableInTouchMode(true);
//                scoutMatchText = (EditText) findViewById(R.id.matchNumberText);
//                scoutMatchText.setFocusableInTouchMode(true);
//            }
//            //set scout id button
//        } else if (item.getItemId() == R.id.setScoutIDButton) {
//            setScoutNumber();
//
//
//        }else if (item.getItemId() == R.id.setScoutName) {
//            setScoutName(null);
//        }
//
//        return true;
//    }
//
//    //scout button on ui
//    public void startScoutButton (View view) {
//        startScout(view);
//    }
//
//    public void startScout(View view) {
//        Log.i("HATRED", "STARTSCOUT");
//        if (overridden) {
//            Log.i("HATRED", "ISOVERRIDEN");
//            //if the schedule has been overridden we will use the values that the user has set
//            EditText teamNumEditText = (EditText) findViewById(R.id.teamNumber1Edit);
//            if (teamNumEditText.getText().toString().equals("")) {
//                Toast.makeText(getBaseContext(), "Please set your team number and try again",
//                        Toast.LENGTH_LONG).show();
//            } else {
//                teamNumber = Integer.parseInt(teamNumEditText.getText().toString());
//                EditText matchNumEditText = (EditText) findViewById(R.id.matchNumberText);
//                if (matchNumEditText.getText().toString().equals("")) {
//                    Toast.makeText(getBaseContext(), "Make sure your match is set and try again",
//                            Toast.LENGTH_LONG).show();
//                } else {
//                    matchNumber = Integer.parseInt(matchNumEditText.getText().toString());
//                    if (scoutNumber <= 0) {
//                        setScoutNumber();
//                        Toast.makeText(getBaseContext(), "Please set your number and try again",
//                                Toast.LENGTH_LONG).show();
//                    } else {
////                        teamInMatchData.setScoutNumber(scoutNumber);
//                        if (matchNumber <= 0) {
//                            setMatchNumber();
//                            Toast.makeText(getBaseContext(), "Make sure your match is set and try again",
//                                    Toast.LENGTH_LONG).show();
//                        } else {
////                            teamInMatchData.setMatchNumber(Integer.parseInt(matchNumEditText.getText().toString()));
//                            if (teamNumber <= 0) {
//                                setTeamNumber();
//                                Toast.makeText(getBaseContext(), "Make sure your team is set and try again",
//                                        Toast.LENGTH_LONG).show();
//                            } else {
//                                try {
//                                    if (!scoutName.equals("")) {
//                                        EditText matchNumber = (EditText) findViewById(R.id.matchNumberText);
//                                        String ovrrdTeamStr = ((EditText) findViewById(R.id.teamNumber1Edit)).getText().toString();
//                                        Intent intent = new Intent(this, AutoActivity.class);
//                                        if(ovrrdTeamStr != null && !ovrrdTeamStr.equals("")) {
//                                            Integer ovrrdTeamNum = Integer.parseInt(ovrrdTeamStr);
//                                            if(ovrrdTeamNum > 0) {
////                                                teamInMatchData.setTeamNumber(ovrrdTeamNum);
////                                                teamInMatchData.setTitle(Integer.parseInt(matchNumber.getText().toString()), ovrrdTeamNum, ("-" + scoutNumber));
////                                                teamInMatchData.setScoutName(scoutName);
////                                                intent.putExtra("teamInMatchData", teamInMatchData);
//                                                Log.e("HATRED", scoutName);
//                                                intent.setAction("returningNoSavedData");
//                                                startActivity(intent);
//                                            } else {
//                                                Toast.makeText(getBaseContext(), "Choose a Valid Team", Toast.LENGTH_SHORT).show();
//                                            }
//                                        } else {
//                                            Toast.makeText(getBaseContext(), "Choose a Valid Team", Toast.LENGTH_SHORT).show();
//                                        }
//                                    } else {
//                                        Toast.makeText(getBaseContext(), "Input a Valid Name",
//                                                Toast.LENGTH_SHORT).show();
//                                    }
//                                } catch (NullPointerException npe) {
//                                    Toast.makeText(getBaseContext(), "Input a Valid Name",
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        } else {
//            Log.i("HATRED", "ISNOTOVERRIDEN");
//            if (scoutNumber <= 0) {
//                setScoutNumber();
//                Toast.makeText(getBaseContext(), "Please set your number and try again",
//                        Toast.LENGTH_LONG).show();
//            } else {
////                teamInMatchData.setScoutNumber(scoutNumber);
////                teamInMatchData.setMatchNumber(Integer.parseInt(matchNumberEditText.getText().toString()));
//                if (teamNumber <= 0) {
//                    setTeamNumber();
//                    Toast.makeText(getBaseContext(), "Make sure your team is set and try again",
//                            Toast.LENGTH_LONG).show();
//                } else {
//                    final Intent nextActivity = new Intent(context, AutoActivity.class)
//                            .putExtra("matchNumber", matchNumber).putExtra("overridden", overridden)
//                            .putExtra("teamNumber", teamNumber).putExtra("scoutName", scoutName).putExtra("scoutNumber", scoutNumber);
////                    teamInMatchData.setTeamNumber(teamNumber);
////                    teamInMatchData.setTitle(Integer.parseInt(matchNumber.getText().toString()), teamNumber, ("-" + scoutNumber));
////                    teamInMatchData.setScoutName(scoutName);
////                    intent.putExtra("teamInMatchData", teamInMatchData);
//                    Log.e("teamInMatchData Scout", scoutName);
////                    intent.setAction("returningNoSavedData");
//                    SharedPreferences.Editor spfe = preferences.edit();
//                    spfe.putString("lastScoutName", scoutName);
//                    spfe.commit();
//                    setScoutName(new Runnable() {
//                        @Override
//                        public void run() {
//                            startActivity(nextActivity.putExtra("scoutName", scoutName));
//                        }
//                    });
//
//                }
//            }
//        }
//    }
//
////    public void startScout(String editJSON, int matchNumber, int teamNumber) {
////        if (teamNumber == -1) {
////            try {
////                scoutTeamText = (EditText) findViewById(R.id.teamNumber1Edit);
////                teamNumber = Integer.parseInt(scoutTeamText.getText().toString());
////                scoutMatchText = (EditText) findViewById(R.id.matchNumberText);
////                matchNumber = Integer.parseInt(scoutMatchText.getText().toString());
////            } catch (NumberFormatException nfe) {
////                Toast.makeText(this, "Please enter valid team numbers", Toast.LENGTH_LONG).show();
////                return;
////            }
////        }
////
////        final Intent nextActivity = new Intent(context, AutoActivity.class)
////                .putExtra("matchNumber", matchNumber).putExtra("overridden", overridden)
////                .putExtra("teamNumber", teamNumber).putExtra("scoutName", scoutName).putExtra("scoutNumber", scoutNumber).putExtra("previousData", editJSON);
////        setScoutName(new Runnable() {
////            @Override
////            public void run() {
////                startActivity(nextActivity.putExtra("scoutName", scoutName));
////            }
////        });
////    }
//
//    public void setMatchNumber() {
//        ValueEventListener valueEL = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.getValue() != null){
//                    currentMatchNumber = dataSnapshot.getValue(Integer.class);
//                    Log.e("HCINT", "there was data");
//                }else{
//                    currentMatchNumber = -1;
//                    Log.e("HCINT", "there wasn't NO data");
//                }
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e("HCINT", "cancled - error");
//                //Do Nothing.
//            }
//        };
//        databaseReference.child("currentMatchNum").addValueEventListener(valueEL);
//        scoutMatchText.setText(String.valueOf(currentMatchNumber));
//    }
//
//    public void setTeamNumber() {
////        teamPath.add("scouts"); teamPath.add("scout" + scoutNumber); teamPath.add("team");
////
////        teamNumber = Integer.parseInt(Utils.GetFirebaseData(databaseReference, teamPath));
////        scoutTeamText.setText(teamNumber);
//        ValueEventListener tvalueEL = new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if(dataSnapshot.getValue() != null){
//                    teamNumber = dataSnapshot.getValue(Integer.class);
//                    Log.e("HCINT", "there was data");
//                }else{
//                    teamNumber = -1;
//                    Log.e("HCINT", "there wasn't NO data");
//                }
//            }
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.e("HCINT", "cancled - error");
//                //Do Nothing.
//            }
//        };
//        Log.e("HCINT", "AGHHH");
//        databaseReference.child("scouts/" + "scout" + scoutNumber +"/team").addValueEventListener(tvalueEL);
//        scoutTeamText.setText(String.valueOf(teamNumber));
//    }
//
//    //display dialog to set scout number
//    private void setScoutNumber() {
//        final EditText editText = new EditText(this);
//        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
//        if (scoutNumber == -1) {
//            editText.setHint("Scout ID");
//        } else {
//            editText.setHint(Integer.toString(scoutNumber));
//        }
//        new AlertDialog.Builder(this)
//                .setTitle("Set Scout ID")
//                .setView(editText)
//                .setPositiveButton("Done", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        try {
//                            String text = editText.getText().toString();
//                            if (text.equals("")) {
//                                if (scoutNumber == -1) {
//                                    throw new NumberFormatException();
//                                }
//                            } else {
//                                int tmpScoutNumber = Integer.parseInt(text);
//                                if ((tmpScoutNumber < 1) || (tmpScoutNumber > 6)) {
//                                    throw new NumberFormatException();
//                                }
//                                scoutNumber = tmpScoutNumber;
//                            }
//                        } catch (NumberFormatException nfe) {
//                            setScoutNumber();
//                        }
//
//                        setMatchNumber();
//                        setTeamNumber();
//                        setScoutNameListener();
//
//                        SharedPreferences.Editor speditor = preferences.edit();
//                        speditor.putInt("scoutNumber", scoutNumber);
//                        speditor.commit();
//                    }
//                })
//                .show();
//    }
//
//    //in order to redisplay the dialog to ask for scout initials, we start a new method, and recursively call the method if the input is wrong
//    //on Finish is what to happen on click
//    public void setScoutNameListener() {
//        Log.e("scoutNumber", String.valueOf(scoutNumber));
//        if (scoutNumber > 0){
//            databaseReference.child("scouts").child(String.valueOf("scout" + scoutNumber)).child("currentUser").addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(final DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.getValue() != null && !dataSnapshot.getValue().toString().equals("")) {
//                        final String tempScoutName = dataSnapshot.getValue().toString();
//                        if(!preferences.getString("scoutName", " ").equals(tempScoutName)) {
//                            new AlertDialog.Builder(context)
//                                    .setTitle("")
//                                    .setMessage("Are you " + tempScoutName + "?")
//                                    .setCancelable(false)
//                                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            scoutName = tempScoutName;
//                                            databaseReference.child("scouts").child("scout" + scoutNumber).child("scoutStatus").setValue("confirmed");
//                                            Log.e("tempScoutName", tempScoutName);
//                                            preferences.edit().remove("scoutName").apply();
//                                            editor.putString("scoutName", tempScoutName).commit();
//                                        }
//                                    })
//                                    .setIcon(android.R.drawable.ic_dialog_alert)
//                                    .show();
//                        }else if(preferences.getString("scoutName", " ").equals(tempScoutName)){
//                            scoutName = tempScoutName;
//                            databaseReference.child("scouts").child("scout" + scoutNumber).child("scoutStatus").setValue("confirmed");
//                        }
//
//                    } else {
//                        //setScoutName();
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }
//    }
//}
