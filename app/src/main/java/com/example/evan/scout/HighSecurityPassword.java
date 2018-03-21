package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;

/**
 * Created by Calvin on 2/20/18.
 */

public class HighSecurityPassword {
    private Activity context;
    private MainActivity mainActivity;
    private File dir = new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/scout_data");
    private final String ultraPass = "meme";

    public HighSecurityPassword(Activity context, MainActivity mainActivity){
        this.context = context;
        this.mainActivity = mainActivity;
    }

    public void initiatePasswordChain(){
        final Dialog passwordDialog = new Dialog(context);
        passwordDialog.setCanceledOnTouchOutside(false);
        passwordDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout passwordDialogLayout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.password_dialog, null);
        //Set Dialog Title
        final EditText passwordEditText1 = (EditText) passwordDialogLayout.findViewById(R.id.passwordInput);

        Button enterButton = (Button) passwordDialogLayout.findViewById(R.id.checkPasswordButton);
        enterButton.setBackgroundColor(Color.BLACK);
        enterButton.setTextColor(Color.WHITE);
        enterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!passwordEditText1.getText().toString().equals("")){
                        try {
                            if(passwordEditText1.getText().toString().equals(ultraPass)){
                                if (dir.exists()) {
                                    DeleteRecursive(dir);
                                    mainActivity.searchBar = (EditText) mainActivity.findViewById(R.id.searchEditText);
                                    mainActivity.searchBar.setFocusable(false);
                                    mainActivity.updateListView();
                                    mainActivity.searchBar.setFocusableInTouchMode(true);
                                    Utils.makeToast(context, "YOU ARE PRETTY GOOD AT HACKING I HAVE TO ADMIT...");
                                    passwordDialog.dismiss();
                                }
                            }else{
                                Utils.makeToast(context, "Shoo! Shoo! The password is wrong! Go Away!");
                            }
                        } catch (Exception e) {
                            Log.e("ERROR", "ERROR");
                            e.printStackTrace();
                        }
                }else{
                    Utils.makeToast(context, "Do You Want To Input A Password?!?!");
                }
            }
        });

        Button cancelButton = (Button) passwordDialogLayout.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                passwordDialog.dismiss();
            }
        });
        passwordDialog.setContentView(passwordDialogLayout);
        passwordDialog.show();
    }

    private void DeleteRecursive(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory())
        {
            for (File child : fileOrDirectory.listFiles())
            {
                DeleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }
}
