package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import java.util.UUID;

import static com.example.evan.scout.MainActivity.allianceColor;
import static com.example.evan.scout.MainActivity.mode;
import static com.example.evan.scout.MainActivity.modeItem;
import static com.example.evan.scout.MainActivity.sharedPreferences;
import static com.example.evan.scout.MainActivity.spfe;

/**
 * Created by Citrus Circuits Scout Programmers on 1/3/16.
 */
public class bgLoopThread extends Thread {
    public static String scoutName;
    private File bluetoothDir;
    MainActivity main;
    Activity context;
    String tmp_scoutName;
    public static String scoutLetter = "";
    public static int cycleNumber;
    public String previousLetter = "";
    private boolean substitute = false;
    Integer sprRanking;

    public bgLoopThread(Activity context, MainActivity main) {
        this.main = main;
        this.context = context;
        bluetoothDir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluetooth");
    }

    public void run() {
    }

    public void qrData(){
        destroyDuplicates();

        getLetter();

        useQR();

        obtainQRData();
    }

    public void useQR(){
        String qrString = sharedPreferences.getString("qrString", "");
        Log.e("QRSTRING", qrString);
        if(!qrString.equals("")){
            try{
                if(qrString.contains(scoutLetter)){
                    sprRanking = qrString.indexOf(scoutLetter) - qrString.indexOf("|");
                    previousLetter = scoutLetter;
                    MainActivity.spfe.putString("previousLetter", previousLetter);
                    MainActivity.spfe.commit();
                    substitute = false;
                }else if(!qrString.contains(scoutLetter)){
                    previousLetter = sharedPreferences.getString("previousLetter", "");
                    if(!previousLetter.equals("")){
                        scoutLetter = previousLetter;
                    }else{
                        toasts("Substitute Failed!");
                    }
                    sprRanking = qrString.indexOf(scoutLetter) - qrString.indexOf("|");
                    substitute = true;
                }else{
                    toasts("Current scout name isn't in this match, sorry.");
                }
            }catch (NullPointerException ne){
                ne.printStackTrace();
            }
        }else{
            Log.e("QRString not set", "NOT SET");
        }
    }

    public void getLetter(){
        try {
            tmp_scoutName = DataManager.collectedData.getString("scoutName");
            Log.e("SCOUTNAME", tmp_scoutName);
        } catch (JSONException e) {
            tmp_scoutName = "(No Name Selected)";
            e.printStackTrace();
        }

        if (!tmp_scoutName.equals("(No Name Selected)")) {
            if (!bluetoothDir.exists()) {
                bluetoothDir.mkdir();
                Log.i("File Info", "Failed to make Directory. Unimportant");
                Log.e("No Files", "No Files from Bluetooth");
                main.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        MainActivity.allianceColor = "not found";
                        main.updateAllianceColor();
                    }
                });
            }
            final File[] files = bluetoothDir.listFiles();

            if(files == null){
                return;
            }

            for(File tmpFile : files){
                if(tmpFile != null){
                    if(tmpFile.getName().equals("QRAssignments.txt")){
                        Log.e("BACKUPFILENAME", tmpFile.getName());
                        final String content = readFile(tmpFile.getPath());

                        try {
                            JSONObject backupJson = new JSONObject(content);
                            JSONObject letterJson = backupJson.getJSONObject("letters");
                            scoutLetter = letterJson.getString(scoutName);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }else{
                    Log.e("No Files", "No Files from Bluetooth");
                    toasts("No Backup File! Scream at Server People!");
                    main.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.allianceColor = "not found";
                            main.updateAllianceColor();
                        }
                    });
                    return;
                }
            }
        }else {
            Utils.makeToast(context, "Input Valid Scout Name!");
        }
    }

    public void obtainQRData(){
        try {
            tmp_scoutName = DataManager.collectedData.getString("scoutName");
            Log.e("SCOUTNAME", tmp_scoutName);
        } catch (JSONException e) {
            tmp_scoutName = "(No Name Selected)";
            e.printStackTrace();
        }
        if (!tmp_scoutName.equals("(No Name Selected)")) {
            updateMatchNumber();
            if(MainActivity.matchNumber >= 1){
                Log.e("SCOUTNAME", tmp_scoutName);
                if (!bluetoothDir.exists()) {
                    bluetoothDir.mkdir();
                    Log.i("File Info", "Failed to make Directory. Unimportant");
                    Log.e("No Files", "No Files from Bluetooth");
                    main.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.allianceColor = "not found";
                            main.updateAllianceColor();
                        }
                    });
                }
                final File[] files = bluetoothDir.listFiles();

                if(files == null){
                    return;
                }

                for(File tmpFile : files){
                    if(tmpFile != null){
                        if(tmpFile.getName().equals("QRAssignments.txt")){
                            Log.e("QR File Name", tmpFile.getName());
                            final String content = readFile(tmpFile.getPath());

                            main.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    try {
                                        JSONObject totalJson = new JSONObject(content);
                                        JSONObject matchJson = totalJson.getJSONObject("matches");
                                        JSONObject tmpJson = matchJson.getJSONObject("match" + MainActivity.matchNumber);
                                        JSONObject sprJson = tmpJson.getJSONObject(sprRanking+"");
                                        Log.e("QRTEST", tmpJson.toString());

                                        Log.e("JSON2", sprJson.toString());
                                        String tmpAc = sprJson.getString("alliance").toLowerCase();
                                        if(tmpAc.equals("blue")){   MainActivity.allianceColor = "blue";}else if(tmpAc.equals("red")){  MainActivity.allianceColor = "red";}
                                        main.updateAllianceColor();
                                        main.teamNumber = sprJson.getInt("team");
                                        main.updateTeamEditText(sprJson.getInt("team"));
                                        if(substitute){
                                            toasts("SUCCESS QR DATA!");
                                        }else{
                                            toasts("SUCCESS QR DATA!");
                                        }
                                        mode = "QR";
                                        MainActivity.spfe.putString("mode", "QR");
                                        MainActivity.spfe.commit();
                                        mode = sharedPreferences.getString("mode", mode);
                                        if(modeItem != null){
                                            modeItem.setTitle("▓ " + mode + " ▓");
                                        }
                                    } catch (JSONException e) {
                                        toasts("FAIL QR DATA!");
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }else{
                        Log.e("No Files", "No Files from QR");
                        toasts("No QR File! Scream at Server People!");
                        main.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.allianceColor = "not found";
                                main.updateAllianceColor();
                            }
                        });
                        return;
                    }
                }
            }else{
                toasts("Input Valid Match Number!");
            }
        }else {
            toasts("Input Valid Scout Name!");
            main.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.allianceColor = "not found";
                    main.updateAllianceColor();
                }
            });
        }
    }

    public void backup(){
        destroyDuplicates();

        if (MainActivity.scoutNumber >= 1 && MainActivity.scoutNumber <= 18) {
            updateMatchNumber();
            if(MainActivity.matchNumber >= 1){
                if (!bluetoothDir.mkdir()) {
                    Log.i("File Info", "Failed to make Directory. Unimportant");
                    Log.e("No Files", "No Files from Bluetooth");
                    main.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            MainActivity.allianceColor = "not found";
                            main.updateAllianceColor();
                        }
                    });
                }
                final File[] files = bluetoothDir.listFiles();

                for(File tmpFile : files){
                    if(tmpFile != null){
                        if(tmpFile.getName().equals("backupAssignments.txt")){
                            Log.e("BACKUPFILENAME", tmpFile.getName());
                            final String content = readFile(tmpFile.getPath());

                            main.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        JSONObject totalJson = new JSONObject(content);
                                        JSONObject tmpJson = totalJson.getJSONObject("match" + MainActivity.matchNumber);
                                        JSONObject scoutJson;
                                        try{
                                            scoutJson = tmpJson.getJSONObject("scout" + MainActivity.scoutNumber);
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
                                        toasts("Successful Backup!");
                                        MainActivity.spfe.putString("mode", "backup");
                                        MainActivity.spfe.commit();
                                        mode = "backup";
                                        mode = sharedPreferences.getString("mode", mode);
                                        if(modeItem != null){
                                            modeItem.setTitle("▓ " + mode + " ▓");
                                        }
                                    } catch (JSONException e) {
                                        toasts("Fail Backup!");
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }else{
                        Log.e("No Files", "No Files from Bluetooth");
                        toasts("No Backup File! Scream at Server People!");
                        main.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MainActivity.allianceColor = "not found";
                                main.updateAllianceColor();
                            }
                        });
                        return;
                    }
                }
            }else{
                toasts("Input Valid Match Number!");
            }
        }else {
            toasts("Input Valid Scout Number!");
            main.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity.allianceColor = "not found";
                    main.updateAllianceColor();
                }
            });
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

    public void updateMatchNumber() {
        try{
            MainActivity.matchNumber = MainActivity.sharedPreferences.getInt("matchNumber", MainActivity.matchNumber);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void destroyDuplicates() {
        if (!bluetoothDir.mkdir()) {
            Log.i("File Info", "Failed to make Directory. Unimportant");
        }
        final File[] files = bluetoothDir.listFiles();

        if(files == null){
            return;
        }

        for(File tmpFile : files){
            Integer duplicateMark = tmpFile.getName().toString().indexOf("-");
            Log.e("DUPLICATEMARK", duplicateMark+"");
            if(duplicateMark != -1 && (duplicateMark == 2 || duplicateMark == 3)){
                DeleteRecursive(tmpFile);
                Log.e("YES", "YES");
            }else{
                Log.e("No Duplicates", "No Duplicate Files");
            }
        }
        Log.e("exit", "exit");
    }

    private void DeleteRecursive(File fileOrDirectory)
    {
        fileOrDirectory.delete();
    }
}