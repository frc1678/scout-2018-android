package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

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
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static com.example.evan.scout.MainActivity.spfe;

/**
 * Created by sam on 1/3/16.
 */
public class bgLoopThread extends Thread {
    private int scoutNumber;
    private DatabaseReference databaseReference;
    public static String scoutName;
    MainActivity main;
    Activity context;
    Handler handler;
    List<Integer> btMatchNums = new ArrayList<>();
    ActionBar actionBar;
    EditText matchNumEditText;
    EditText teamNumEditText;
    String tmp_scoutName;
    private Timer timer;
    private TimerTask timerTask;

    public bgLoopThread(Activity context, MainActivity main) {
        this.main = main;
        this.context = context;
    }

    public void run() {
        setChecker();
    }

    public void setChecker() {


                timerTask = new TimerTask() {

                    @Override
                    public void run() {
                        check();
                    }
                };
                startTimer();
//        Log.e("scoutNumber", String.valueOf(scoutNumber));
//        if (scoutNumber > 0) {
//            databaseReference.child("scouts").child(String.valueOf("scout" + scoutNumber)).child("currentUser").addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(final DataSnapshot dataSnapshot) {
//                    if (dataSnapshot.getValue() != null && !dataSnapshot.getValue().toString().equals("")) {
//                        final String tempScoutName = dataSnapshot.getValue().toString();
//                        if (scoutName == null) {
//                            handler = new Handler(Looper.getMainLooper());
//                            Runnable runnable = new Runnable() {
//                                @Override
//                                public void run() {
//                                    View dialogView = LayoutInflater.from(context).inflate(R.layout.alertdialog, null);
//                                    final EditText editText = (EditText) dialogView.findViewById(R.id.scoutNameEditText);
//                                    editText.setText(tempScoutName);
//                                    new AlertDialog.Builder(context)
//                                            .setView(dialogView)
//                                            .setTitle("")
//                                            .setMessage("Are you this person?")
//                                            .setCancelable(false)
//                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
//                                                public void onClick(DialogInterface dialog, int which) {
//                                                    scoutName = editText.getText().toString();
//                                                    DataManager.addZeroTierJsonData("scoutName", scoutName);
//                                                    databaseReference.child("scouts").child("scout" + scoutNumber).child("currentUser").setValue(scoutName);
//                                                    databaseReference.child("scouts").child("scout" + scoutNumber).child("scoutStatus").setValue("confirmed");
//                                                    Log.e("tempScoutName", tempScoutName);
//                                                    scoutName = editText.getText().toString();
//                                                }
//                                            })
//                                            .setIcon(android.R.drawable.ic_dialog_alert)
//                                            .show();
//                                } // This is your code
//                            };
//                            handler.post(runnable);
//                        } else if (scoutName.equals(tempScoutName)) {
//                            //Do Nothing
//                        }
//                    }
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });
//        }
    }

    public void check(){
        if(!main.overridden) {
            try {
                tmp_scoutName = DataManager.collectedData.getString("scoutName");
                Log.e("SCOUTNAME!!!", tmp_scoutName);
            } catch (JSONException e) {
                tmp_scoutName = "(No Name Selected)";
                e.printStackTrace();
            }
            if (!tmp_scoutName.equals("(No Name Selected)")) {
                Log.e("SCOUTNAME!!!22", tmp_scoutName);
                final File dir;
                dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluetooth");
                if (!dir.mkdir()) {
                    Log.i("File Info", "Failed to make Directory. Unimportant");
                }
                final File[] files = dir.listFiles();

                Integer biggestMatchNum = 0;
                btMatchNums.clear();
                Log.e("BTMATCHNUMS", btMatchNums.size()+"");
                for(File tmpFile : files){
                    if(tmpFile != null){
                        Log.e("FILENAME!!!", tmpFile.getName());
                        String fileName = tmpFile.getName();
                        String tmp_matchnumstring = fileName.substring(fileName.indexOf("Q")+1, fileName.indexOf("."));
                        Log.e("FILENAMENUM!!!", tmp_matchnumstring);
                        Integer tmp_matchnum = Integer.parseInt(tmp_matchnumstring);
                        btMatchNums.add(tmp_matchnum);
                    }
                }
                for(int i = 0; i < btMatchNums.size(); i++){
                    Log.e("FILENUMBERS!!!", btMatchNums.toString());
                    if(btMatchNums.get(i) > biggestMatchNum){
                        Log.e("FILENUMBERS!!!", biggestMatchNum+"");
                        biggestMatchNum = btMatchNums.get(i);
                        Log.e("FILENUMBERS!!!", biggestMatchNum+"");
                    }
                }
                for(File tmpFile : files){
                    Log.e("BTBTBTBTBTBT","BTBTBTBTBT");
                    if(tmpFile != null){
                        if(tmpFile.getName().equals("Q"+biggestMatchNum+".txt")){
                            Log.e("FILENAMEBIGGEST!", tmpFile.getName());
                            final String content = readFile(tmpFile.getPath());
                            main.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject totalJson = new JSONObject(content);
                                        JSONObject tmpJson = totalJson.getJSONObject("assignments");
                                        main.firebaseMatchNumber = totalJson.getInt("match");
                                        MainActivity.matchNumber = totalJson.getInt("match");
                                        main.updateMatchEditText(totalJson.getInt("match"));
                                        Log.e("JSON1", tmpJson.toString());
                                        JSONObject scoutJson;
                                        try{
                                            scoutJson = tmpJson.getJSONObject(tmp_scoutName);
                                        }catch(NullPointerException ne){
                                            scoutJson = new JSONObject();
                                            ne.printStackTrace();
                                        }
                                        Log.e("JSON2", scoutJson.toString());
                                        String tmpAc = scoutJson.getString("alliance").toLowerCase();
                                        if(tmpAc.equals("blue")){   MainActivity.allianceColor = "blue";}else if(tmpAc.equals("red")){  MainActivity.allianceColor = "red";}
                                        main.updateAllianceColor();
                                        main.teamNumber = scoutJson.getInt("team");
                                        main.updateTeamEditText(scoutJson.getInt("team"));
                                    } catch (JSONException e) {
                                        Utils.makeToast(context, "Current ScoutName is not Valid!!!");
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                }
            }
        }
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

    public void toasts(final String message) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void startTimer() {
        if(timer != null) {
            return;
        }
        timer = new Timer();
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    public void stopTimer() {
        timer.cancel();
        timer = null;
    }
}