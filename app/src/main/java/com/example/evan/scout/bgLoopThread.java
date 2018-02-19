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

import org.w3c.dom.Text;

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
    MainActivity main;
    Activity context;
    Handler snHandler;
    Handler icHandler;

    public bgLoopThread(Activity context, int scoutNumber, DatabaseReference databaseReference, MainActivity mainActivity) {
        this.context = context;
        this.scoutNumber = scoutNumber;
        this.databaseReference = databaseReference;
        main = mainActivity;
    }

    public void run() {
        setScoutNameListener(scoutNumber, databaseReference);
        setInternetListener();
    }

    public void setScoutNameListener(final int scoutNumber, final DatabaseReference databaseReference) {
        Log.e("scoutNumber", String.valueOf(scoutNumber));
        if (scoutNumber > 0) {
            databaseReference.child("scouts").child(String.valueOf("scout" + scoutNumber)).child("currentUser").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    if (dataSnapshot.getValue() != null && !dataSnapshot.getValue().toString().equals("")) {
                        final String tempScoutName = dataSnapshot.getValue().toString();
                        if (scoutName == null) {
                            handler = new Handler(Looper.getMainLooper());
                            Runnable runnable = new Runnable() {
                                @Override
                                public void run() {
                                    View dialogView = LayoutInflater.from(context).inflate(R.layout.alertdialog, null);
                                    final EditText editText = (EditText) dialogView.findViewById(R.id.scoutNameEditText);
                                    editText.setText(tempScoutName);
                                    new AlertDialog.Builder(context)
                                            .setView(dialogView)
                                            .setTitle("")
                                            .setMessage("Are you this person?")
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    scoutName = editText.getText().toString();
                                                    DataManager.addZeroTierJsonData("scoutName", scoutName);
                                                    databaseReference.child("scouts").child("scout" + scoutNumber).child("currentUser").setValue(scoutName);
                                                    databaseReference.child("scouts").child("scout" + scoutNumber).child("scoutStatus").setValue("confirmed");
                                                    Log.e("tempScoutName", tempScoutName);
                                                    scoutName = editText.getText().toString();
                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                                } // This is your code
                            };
                            handler.post(runnable);
                        } else if (scoutName.equals(tempScoutName)) {
                            //Do Nothing
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public void setInternetListener(){
        handler = new Handler(Looper.getMainLooper());
      
        int delay = 5000;

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.e("connection", Boolean.toString(main.internetCheck()));
                if (!main.internetCheck()){
                    View dialogView = LayoutInflater.from(context).inflate(R.layout.internetdialog, null);
                    final TextView content = (TextView) dialogView.findViewById(R.id.messageTextViewInternet);
                    final AnimationDrawable drawable = new AnimationDrawable();
                    final Handler handler = new Handler();
                    drawable.addFrame(new ColorDrawable(Color.RED), 100);
                    drawable.addFrame(new ColorDrawable(Color.parseColor("#FA9B86")), 100);
                    drawable.setOneShot(false);
                    dialogView.setBackgroundDrawable(drawable);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            drawable.start();
                        }
                    }, 100);
//                     title.setTextColor(Color.parseColor(Constants.COLOR_RED));
//                   // new AlertDialog.Builder(context)
                    final Dialog daedalusDialog= new Dialog(context);
                    daedalusDialog.setContentView(dialogView);
                    //setView(dialogView)
//                            sexyDialog.setTitle("WARNING!!!WARNING!!!WARNING!!!WARNING!!!WARNING!!!WARNING!!!WARNING!!!WARNING!!!WARNING!!!WARNING!!!WARNING!!!WARNING!!!WARNING!!!WARNING!!!");
                    content.setText("YOU ARE NOT CONNECTED TO INTERNET");
                    daedalusDialog.setCancelable(false);

                    daedalusDialog.setTitle("WARNING!!!");

                    Button ok= (Button) dialogView.findViewById(R.id.okButton);
//                             ok.setBackgroundColor(Color.parseColor("BBEDC5"));
                    ok.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            daedalusDialog.dismiss();
                        }
                    });

                    //sexyDialog.setIcon(android.R.drawable.ic_dialog_alert);
                    daedalusDialog.show();
                }
            } // This is your code
        };
        handler.postDelayed(runnable, delay);
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