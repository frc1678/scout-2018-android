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
import static java.lang.Long.parseLong;
import static java.lang.Long.valueOf;
/**
 * Created by Calvin on 1/13/18.
 */
public class backgroundTimer extends Thread{
    public Menu currentMenu;
    public static float currentOffset;
    public boolean timerReady = true;
    public int showTime;
    public static float updatedTime;
    public static boolean stopTimer = false;
    public CountDownTimer matchTimer;
    public static String timerActivity;
    Activity context;
    public backgroundTimer(Activity context){
        this.context = context;
    }
    public void setMatchTimer(){
        timerReady = false;
        startTimer();
    }
    private void startTimer(){
        currentOffset = 0;
        DataActivity.offset = 0;
        MainActivity.offset = 0;
        timerActivity = "auto";
        Log.e("TIMERCALLED","CALLED START TIMER!!!!!");
        updatedTime = 0f;
        stopTimer = false;
        matchTimer = new CountDownTimer(100000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(stopTimer){
                    matchTimer.cancel();
                }else{
                    if(DataActivity.activityName == "auto" || DataActivity.activityName == "tele"){
                        currentOffset = DataActivity.offset;
                    } else{
                        currentOffset = MainActivity.offset;
                    }
                    float countDownTime = (100000 - millisUntilFinished)/1000f;
                    updatedTime = countDownTime + currentOffset;
                    updatedTime = Float.parseFloat(String.format("%.2f", updatedTime));
                    if (updatedTime < 0) {
                        matchTimer.cancel();
                        startTimer();
                    }
                    if (updatedTime >= 15){
                        updatedTime = 15;
                        matchTimer.cancel();
                        startTimerTele();
                    }
                    showTime = (int) updatedTime;
                    MenuItem timerView = currentMenu.findItem(R.id.timerView);
                    timerView.setTitle("AutoTime: "+showTime+" / 15");
                }
            }
            @Override
            public void onFinish() {
            }
        };
        matchTimer.start();
    }
    private void startTimerTele(){
        currentOffset = 0;
        DataActivity.offset = 0;
        MainActivity.offset = 0;
        timerActivity = "tele";
        Log.e("TIMERCALLED","CALLED tele START TIMER!!!!!");
        updatedTime = 0f;
        matchTimer = new CountDownTimer(270000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(stopTimer){
                    matchTimer.cancel();
                }else {
                    if(DataActivity.activityName == "auto" || DataActivity.activityName == "tele"){
                        currentOffset = DataActivity.offset;
                    } else{
                        currentOffset = MainActivity.offset;
                    }
                    float countDownTime = (270000 - millisUntilFinished)/1000f;
                    updatedTime = countDownTime + currentOffset;
                    updatedTime = Float.parseFloat(String.format("%.2f", updatedTime));
                    if (updatedTime < 0) {
                        matchTimer.cancel();
                        startTimerTele();
                    }
                    if (updatedTime >= 135){
                        updatedTime = 135;
                        timerReady = true;
                        matchTimer.cancel();
                    }
                    showTime = (int) updatedTime;
                    MenuItem timerView = currentMenu.findItem(R.id.timerView);
                    timerView.setTitle("TeleTime: "+showTime+" / 135");
                }
            }
            @Override
            public void onFinish() {
            }
        };
        matchTimer.start();
    }
    public static float getUpdatedTime(){
        return updatedTime;
    }
    public static void stopTimer(){stopTimer = true; updatedTime = 0f;
    }
}