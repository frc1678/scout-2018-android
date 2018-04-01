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
    public static float offset;
    public static boolean timerReady = true;
    public int showTime;
    public static float updatedTime;
    public static float trackedTime;
    public static float dialogTime;
    public static boolean stopTimer = false;
    public CountDownTimer matchTimer;
    public static String timerActivity;
    public static boolean offsetAllowed = true;

    Activity context;
    public backgroundTimer(Activity context){
        this.context = context;
    }
    public void setMatchTimer(){
        timerReady = false;
        startTimer();
    }
    private void startTimer(){
        offset = 0f;
        trackedTime = 0f;
        updatedTime = 0f;
        stopTimer = false;
        matchTimer = new CountDownTimer(400000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                float countDownTime = (400000f - millisUntilFinished)/1000f;
                Log.e("REORTY", countDownTime+"");
                trackedTime = countDownTime + offset;
                if (trackedTime <= 0f) {
                    offsetAllowed = false;
                }else if ((trackedTime >= 0f) && (trackedTime <= 15f)){
                    offsetAllowed = true;
                    updatedTime = trackedTime;
                    dialogTime = Float.parseFloat(String.format("%.2f", updatedTime));
                    timerActivity = "auto";
                    showTime = (int) updatedTime;
                    MenuItem timerView = currentMenu.findItem(R.id.timerView);
                    timerView.setTitle("AutoTime: "+showTime+" / 15");
                }else if((trackedTime >= 15f) && (trackedTime <= 150f)){
                    offsetAllowed = true;
                    timerActivity = "tele";
                    updatedTime = trackedTime - 15f;
                    if((105f <= updatedTime) && (updatedTime <= 135f)){
                        timerActivity = "FTB";
                        dialogTime = Float.parseFloat(String.format("%.2f", updatedTime - 105f));
                        showTime = (int) (updatedTime - 105f);
                        MenuItem timerView = currentMenu.findItem(R.id.timerView);
                        timerView.setTitle("FTB: "+showTime+" / 30");
                    }else if(updatedTime <=105f){
                        dialogTime = Float.parseFloat(String.format("%.2f", updatedTime));
                        showTime = (int) updatedTime;
                        MenuItem timerView = currentMenu.findItem(R.id.timerView);
                        timerView.setTitle("TeleTime: "+showTime+" / 135");
                    }
                }else if(trackedTime >= 150f){
                    offsetAllowed = false;
                    updatedTime = 135f;
                    showTime = (int) (updatedTime - 105f);
                    MenuItem timerView = currentMenu.findItem(R.id.timerView);
                    timerView.setTitle("FTB: "+showTime+" / 30");
                    offset = 0f;
                    timerReady = true;
                    matchTimer.cancel();
                    matchTimer = null;
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
}