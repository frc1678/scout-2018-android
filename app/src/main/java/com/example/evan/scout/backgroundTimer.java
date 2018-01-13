package com.example.evan.scout;

import android.app.Activity;
import android.os.CountDownTimer;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Calvin on 1/13/18.
 */

public class backgroundTimer extends Thread{
    public boolean timerReady = true;
    public static float updatedTime;
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
        updatedTime = 0;
        matchTimer = new CountDownTimer(15000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                updatedTime = 15000 - millisUntilFinished;
                Log.e("updatedTime", (15000 - millisUntilFinished)+"");
            }

            @Override
            public void onFinish() {
                startTimerTele();
            }
        };
    }
    private void startTimerTele(){
        updatedTime = 0;
        matchTimer = new CountDownTimer(135000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                updatedTime = 135000 - millisUntilFinished;
                Log.e("updatedTime", (135000 - millisUntilFinished)+"");
            }

            @Override
            public void onFinish() {
                timerReady = true;
            }
        };
    }
    public static float getUpdatedTime(){
        return updatedTime;
    }
}
