package com.example.evan.scout;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Intent;
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
    public static Menu currentMenu;
    public static boolean timerReady = true;
    public static int showTime;
    public static float updatedTime;
    public static boolean stopTimer = false;
    public static CountDownTimer matchTimer = null;

    public backgroundTimer(){

    }

    public static void setMatchTimer(){
        timerReady = false;
        stopTimer = false;
        startTimer();
    }

    public static void startTimer(){
        Log.e("TIMERCALLED","CALLED START TIMER!!!!!");
        updatedTime = 0f;
        matchTimer = new CountDownTimer(150000, 10) {
            @Override
            public void onTick(long millisUntilFinished) {
                if(matchTimer != null){
                        float MUF = millisUntilFinished;
                        float tempTime = (150000 - millisUntilFinished)/1000f;
                        if(tempTime <= 15){
                            updatedTime = tempTime;
                            updatedTime = Float.parseFloat(String.format("%.2f", updatedTime));
                            showTime = (int) updatedTime;
                            MenuItem timerView = currentMenu.findItem(R.id.timerView);
                            timerView.setTitle("AutoTime: "+showTime+" / 15");
                        }else if(tempTime <= 150){
                            updatedTime = tempTime - 15f;
                            updatedTime = Float.parseFloat(String.format("%.2f", updatedTime));
                            showTime = (int) updatedTime;
                            MenuItem timerView = currentMenu.findItem(R.id.timerView);
                            timerView.setTitle("TeleTime: "+showTime+" / 135");
                        }
                }
            }

            @Override
            public void onFinish() {
            }
        }.start();
    }

//    public static void startTimerTele(){
//        Log.e("TIMERCALLED","CALLED tele START TIMER!!!!!");
//        updatedTime = 0f;
//        matchTimer = new CountDownTimer(135000, 10) {
//            @Override
//            public void onTick(long millisUntilFinished) {
//                if(matchTimer != null) {
//                    if(stopTimer){
//                        matchTimer.cancel();
//                        matchTimer = null;
//
////                    Intent timerIntent = new Intent("TIMERDONE");
////                    context.sendBroadcast(timerIntent);
//                    }else {
//                        float MUF = millisUntilFinished;
//                        updatedTime = (135000f - MUF)/1000f;
//                        updatedTime = Float.parseFloat(String.format("%.2f", updatedTime));
//                        showTime = (int) updatedTime;
//                        MenuItem timerView = currentMenu.findItem(R.id.timerView);
//                        timerView.setTitle("TeleTime: "+showTime+" / 135");
//                    }
//                }
//            }
//
//            @Override
//            public void onFinish() {}
//        };
//        matchTimer.start();
//    }

    public static float getUpdatedTime(){
        return updatedTime;
    }

//    public void initiateTimer(){
//        trackedTime = 0f;
//        updatedTime = 0f;
//        timerReady = false;
//        if(matchTimer == null){
//            Log.e("TIMERMADE!!!", "TIMERMADE!!!");
//            matchTimer = new CountDownTimer(400000, 10) {
//                @Override
//                public void onTick(long millisUntilFinished) {
//                    float countDownTime = (400000f - millisUntilFinished)/1000f;
//                    trackedTime = countDownTime;
//                    if (trackedTime <= 0f) {
//                    }else if ((trackedTime >= 0f) && (trackedTime <= 15f)){
//                        updatedTime = trackedTime;
//                        dialogTime = Float.parseFloat(String.format("%.2f", updatedTime));
//                        timerActivity = "auto";
//                        showTime = (int) updatedTime;
//                        MenuItem timerView = currentMenu.findItem(R.id.timerView);
//                        timerView.setTitle("AutoTime: "+showTime+" / 15");
//                    }else if((trackedTime >= 15f) && (trackedTime <= 150f)){
//                        timerActivity = "tele";
//                        updatedTime = trackedTime - 15f;
//                        if((105f <= updatedTime) && (updatedTime <= 135f)){
//                            timerActivity = "FTB";
//                            dialogTime = Float.parseFloat(String.format("%.2f", updatedTime - 105f));
//                            showTime = (int) (updatedTime - 105f);
//                            MenuItem timerView = currentMenu.findItem(R.id.timerView);
//                            timerView.setTitle("FTB: "+showTime+" / 30");
//                        }else if(updatedTime <=105f){
//                            dialogTime = Float.parseFloat(String.format("%.2f", updatedTime));
//                            showTime = (int) updatedTime;
//                            MenuItem timerView = currentMenu.findItem(R.id.timerView);
//                            timerView.setTitle("TeleTime: "+showTime+" / 135");
//                        }
//                    }else if(trackedTime >= 150f){
//                        destroyTimer();
//                    }
//                }
//                @Override
//                public void onFinish() {
//                }
//            };
//        }else{
//            Utils.makeToast(context, "Duplicate Instance of Timer!");
//        }
//    }

    public static void stopTimer(){
        Log.e("ANGRYANGRY", matchTimer.toString());
        try{
            stopTimer = true;
            timerReady = true;
        }catch (NullPointerException ne){
            ne.printStackTrace();
        }

    }
}