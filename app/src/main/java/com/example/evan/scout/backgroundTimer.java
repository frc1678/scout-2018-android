package com.example.evan.scout;

import android.app.Activity;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.firebase.database.DatabaseReference;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Calvin on 1/13/18.
 */

public class backgroundTimer extends Thread{
    public Menu currentMenu;
    public boolean timerReady = true;
    public int showTime;
    public static float updatedTime;
    public static boolean stopTimer = false;
    public CountDownTimer matchTimer;
    Activity context;

    public backgroundTimer(Activity context){
        this.context = context;
    }
    public void setMatchTimer(){
        timerReady = false;
        startTimer();
    }
    private void startTimer(){
        Log.e("TIMERCALLED","CALLED START TIMER!!!!!");
        updatedTime = 0f;
        stopTimer = false;
        matchTimer = new CountDownTimer(15000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(stopTimer){
                    matchTimer.cancel();
                }else{
                    float MUF = millisUntilFinished;
                    updatedTime = (15000f - MUF)/1000f;
                    updatedTime = Float.parseFloat(String.format("%.2f", updatedTime));
                    showTime = (int) updatedTime;
                    MenuItem timerView = currentMenu.findItem(R.id.timerView);
                    timerView.setTitle("AutoTime: "+showTime+" / 15");
                }
            }

            @Override
            public void onFinish() {
                startTimerTele();
            }
        };
        matchTimer.start();
    }
    private void startTimerTele(){
        Log.e("TIMERCALLED","CALLED tele START TIMER!!!!!");
        updatedTime = 0f;
        matchTimer = new CountDownTimer(135000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(stopTimer){
                    matchTimer.cancel();
                }else {
                    float MUF = millisUntilFinished;
                    updatedTime = (135000f - MUF)/1000f;
                    updatedTime = Float.parseFloat(String.format("%.2f", updatedTime));
                    showTime = (int) updatedTime;
                    MenuItem timerView = currentMenu.findItem(R.id.timerView);
                    timerView.setTitle("TeleTime: "+showTime+" / 135");
                }
            }

            @Override
            public void onFinish() {
                timerReady = true;
            }
        };
        matchTimer.start();
    }
    public static float getUpdatedTime(){
        return updatedTime;
    }
    public static void stopTimer(){        stopTimer = true; updatedTime = 0f;}
}
