package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * Created by sam on 1/3/16.
 */
public class bgLoopThread extends Thread {
    private int scoutNumber;
    private DatabaseReference databaseReference;
    public static String scoutName;
    Activity context;

    public bgLoopThread(Activity context, int scoutNumber, DatabaseReference databaseReference){
        this.context = context;
        this.scoutNumber = scoutNumber;
        this.databaseReference = databaseReference;
    }
    public void run() {
        setScoutNameListener(scoutNumber, databaseReference);
    }

    public void setScoutNameListener(final int scoutNumber, final DatabaseReference databaseReference) {
        Log.e("scoutNumber", String.valueOf(scoutNumber));
        if (scoutNumber > 0){
            databaseReference.child("scouts").child(String.valueOf("scout" + scoutNumber)).child("currentUser").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null && !dataSnapshot.getValue().toString().equals("")) {
                        final String tempScoutName = dataSnapshot.getValue().toString();
                        if(scoutName.equals(tempScoutName)) {
                            new AlertDialog.Builder(context)
                                    .setTitle("")
                                    .setMessage("Are you " + tempScoutName + "?")
                                    .setCancelable(false)
                                    .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            scoutName = tempScoutName;
                                            DataManager.addZeroTierJsonData("scoutName", scoutName);
                                            databaseReference.child("scouts").child("scout" + scoutNumber).child("scoutStatus").setValue("confirmed");
                                            Log.e("tempScoutName", tempScoutName);
                                            scoutName = tempScoutName;
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();
                        }else if(scoutName.equals(tempScoutName)){
                            scoutName = tempScoutName;
                            DataManager.addZeroTierJsonData("scoutName", scoutName);
                            databaseReference.child("scouts").child("scout" + scoutNumber).child("scoutStatus").setValue("confirmed");
                        }

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void toasts(final String message) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}