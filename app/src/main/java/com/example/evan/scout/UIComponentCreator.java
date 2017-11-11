package com.example.evan.scout;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.SystemUpdatePolicy;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.CountDownTimer;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.DEVICE_POLICY_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;

//class that creates all the ui components I need like togglebuttons, etc.  Also stores all buttons in list to be accessed later
public class UIComponentCreator {
    //list of views that hold data that I need to access later
    private List<View> componentViews;
    //list of names of buttons, textviews, etc
    private List<String> componentNames;
    private Activity context;
    //counter for list
    private int currentComponent;

    public UIComponentCreator(Activity context, List<String> componentNames) {
        componentViews = new ArrayList<>();
        this.componentNames = componentNames;
        this.context = context;
        currentComponent = 0;
    }

    public Button getBasicButton (int width, Float textScale) {
        Button button = new Button(context);
        button.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if(componentNames != null){            button.setText(componentNames.get(currentComponent)); }
        button.setTextSize(button.getTextSize() * textScale);
        return button;
    }

    public ToggleButton getToggleButton (int width, boolean value) {
        ToggleButton toggleButton = new ToggleButton(context);
        toggleButton.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT, 0.6f));
        toggleButton.setText(componentNames.get(currentComponent));
        toggleButton.setTextOn(componentNames.get(currentComponent));
        toggleButton.setTextOff(componentNames.get(currentComponent));
        toggleButton.setTextSize(toggleButton.getTextSize() * 1f);
        toggleButton.setChecked(value);
        currentComponent++;
        componentViews.add(toggleButton);
        return toggleButton;
    }

    public List<View> getComponentViews() {
        return componentViews;
    }

    public static class UICounterCreator extends UIComponentCreator {
        private String name;
        private Activity context;
        private int currentCounterComponent;

        public UICounterCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            currentCounterComponent = 0;
            this.context = context;
            name = "";
        }

        public RelativeLayout addCounter(String counterFBname) {
            name = UICounterCreator.super.componentNames.get(currentCounterComponent);
            RelativeLayout counterLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.counter, null);

            TextView titleTV = (TextView) counterLayout.findViewById(R.id.counterTitle);
            titleTV.setText(name);

            final TextView valueTV = (TextView) counterLayout.findViewById(R.id.value);
            try {
                if((DataActivity.saveAutoData && DataActivity.activityName.equals("auto")) || (DataActivity.saveTeleData && DataActivity.activityName.equals("tele"))) {
                    valueTV.setText(DataManager.collectedData.getString(counterFBname));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Button minus = (Button) counterLayout.findViewById(R.id.minus);
            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int value = Integer.parseInt(valueTV.getText().toString());
                    if(value > 0){
                        value --;
                    }
                    valueTV.setText(String.valueOf(value));
                }
            });
            Log.e("counterB", minus.getText().toString());

            Button plus = (Button) counterLayout.findViewById(R.id.plus);
            plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int value = Integer.parseInt(valueTV.getText().toString());
                    value ++;
                    valueTV.setText(String.valueOf(value));
                }
            });
            Log.e("counterB", plus.getText().toString());

            currentCounterComponent++;
            super.componentViews.add(valueTV);
            return counterLayout;
        }
    }

    //sub class specifically for creating defense buttons
    public static class UIButtonCreator extends UIComponentCreator {
        double liftoffTime;
        private Activity context;

        public UIButtonCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            this.context = context;
        }

        public Button addButton(final ToggleButton button1, final ToggleButton button2) {
            //add button to row
            final Button liftOffButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            liftOffButton.setText("Ready For LiftOff");
            liftOffButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if(DataManager.collectedData.getBoolean("didLiftoff") != true){
                            button1.setChecked(false);
                            button2.setChecked(false);

                            //display custom dialog with big buttons
                            final Dialog dialog = new Dialog(context);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.dialog, null);
                            final TextView title = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                            title.setText("Lift Off");

                            final TextView liftoffTimeView = (TextView) dialogLayout.findViewById(R.id.liftoffTime);
                            final CountDownTimer cdt = new CountDownTimer(135000, 100) {
                                @Override
                                public void onTick(long millisUntilFinished) {
                                    double currentSeconds = (135000.0 - millisUntilFinished) / 1000;
                                    liftoffTimeView.setText(String.valueOf(currentSeconds));
                                }

                                @Override
                                public void onFinish() {

                                }
                            };

                            Button success = (Button) dialogLayout.findViewById(R.id.successButton);
                            success.getBackground().setColorFilter(Color.parseColor("#C8FFC8"), PorterDuff.Mode.MULTIPLY);
                            success.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cdt.cancel();

                                    liftoffTime = Double.valueOf(liftoffTimeView.getText().toString());
                                    Log.e("scrub", liftoffTime+"");

                                    DataManager.addZeroTierJsonData("didLiftoff", true);
                                    DataManager.addZeroTierJsonData("liftoffTime", liftoffTime);
                                    //add to sd card
                                    dialog.dismiss();
                                }
                            });

                            Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                            failure.getBackground().setColorFilter(Color.parseColor("#FFC8C8"), PorterDuff.Mode.MULTIPLY);
                            failure.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    cdt.cancel();
                                    dialog.dismiss();
                                }
                            });
                            dialog.setContentView(dialogLayout);
                            dialog.show();
                            cdt.start();
                        }else if(DataManager.collectedData.getBoolean("didLiftoff") == true){
                            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                            View liftOffRemoveView = layoutInflater.inflate(R.layout.dialog, null);
                            try {
                                if(DataActivity.saveTeleData && DataActivity.activityName.equals("tele")){
                                    ((TextView) liftOffRemoveView.findViewById(R.id.liftoffTime)).setText(DataManager.collectedData.getDouble("liftoffTime")+"");
                                }else {
                                    ((TextView) liftOffRemoveView.findViewById(R.id.liftoffTime)).setText(String.valueOf(liftoffTime));
                                }
                            } catch(NullPointerException npe){
                                ((TextView) liftOffRemoveView.findViewById(R.id.liftoffTime)).setText("0.0");
                            }
                            ((TextView) liftOffRemoveView.findViewById(R.id.liftoffTime)).setTextColor(Color.parseColor("#FF0000"));

                            final Dialog dialog = new Dialog(context);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            final TextView title = (TextView) liftOffRemoveView.findViewById(R.id.dialogTitle);
                            title.setText("Undo Liftoff?");

                            Button success = (Button) liftOffRemoveView.findViewById(R.id.successButton);
                            success.setText("Cancel");
                            success.getBackground().setColorFilter(Color.parseColor("#C8FFC8"), PorterDuff.Mode.MULTIPLY);
                            success.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    dialog.cancel();
                                }
                            });

                            Button failure = (Button) liftOffRemoveView.findViewById(R.id.failButton);
                            failure.setText("Remove");
                            failure.getBackground().setColorFilter(Color.parseColor("#C8FFC8"), PorterDuff.Mode.MULTIPLY);
                            failure.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DataManager.addZeroTierJsonData("didLiftoff", false);
                                    DataManager.addZeroTierJsonData("liftoffTime", 0);
                                    dialog.cancel();
                                }
                            });

                            dialog.setContentView(liftOffRemoveView);
                            dialog.show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return liftOffButton;
        }
    }

    public static class UIGearCreator extends UIComponentCreator {
        private Activity context;
        private int numGearsLiftOne;
        private int numGearsLiftTwo;
        private int numGearsLiftThree;

        public UIGearCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            numGearsLiftOne = 0;
            numGearsLiftTwo = 0;
            numGearsLiftThree = 0;
            this.context = context;
        }

        public Button addButton(){
            final Button gearButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            gearButton.setText("Gear Placed");
            gearButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.gear_dialog, null);
                    TextView titleTV = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    titleTV.setText("Which Lift?");

                    Button liftOneButton = (Button) dialogLayout.findViewById(R.id.liftOneButton);
                    liftOneButton.setText("Lift 1 ("+numGearsLiftOne+")");
                    liftOneButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numGearsLiftOne += 1;
                            dialog.dismiss();
                        }
                    });

                    Button liftTwoButton = (Button) dialogLayout.findViewById(R.id.liftTwoButton);
                    liftTwoButton.setText("Lift 2 ("+numGearsLiftTwo+")");
                    liftTwoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numGearsLiftTwo += 1;
                            dialog.dismiss();
                        }
                    });

                    Button liftThreeButton = (Button) dialogLayout.findViewById(R.id.liftThreeButton);
                    liftThreeButton.setText("Lift 3 ("+numGearsLiftThree+")");
                    liftThreeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numGearsLiftThree += 1;
                            dialog.dismiss();
                        }
                    });

                    Button cancel = (Button) dialogLayout.findViewById(R.id.cancelButton);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //dismiss dialog
                            dialog.dismiss();
                        }
                    });

                    dialog.setContentView(dialogLayout);
                    dialog.show();
                }
            });

            gearButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.gear_dialog, null);
                    TextView titleTV = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    titleTV.setText("Which Lift?");

                    Button liftOneButton = (Button) dialogLayout.findViewById(R.id.liftOneButton);
                    liftOneButton.setBackgroundColor(Color.parseColor("#ff9999"));
                    liftOneButton.setText("Lift 1 ("+numGearsLiftOne+")");
                    liftOneButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(numGearsLiftOne > 0){
                                numGearsLiftOne -= 1;
                                dialog.dismiss();
                            }
                        }
                    });

                    Button liftTwoButton = (Button) dialogLayout.findViewById(R.id.liftTwoButton);
                    liftTwoButton.setBackgroundColor(Color.parseColor("#ff9999"));
                    liftTwoButton.setText("Lift 2 ("+numGearsLiftTwo+")");
                    liftTwoButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(numGearsLiftTwo > 0){
                                numGearsLiftTwo -= 1;
                                dialog.dismiss();
                            }

                        }
                    });

                    Button liftThreeButton = (Button) dialogLayout.findViewById(R.id.liftThreeButton);
                    liftThreeButton.setBackgroundColor(Color.parseColor("#ff9999"));
                    liftThreeButton.setText("Lift 3 ("+numGearsLiftThree+")");
                    liftThreeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(numGearsLiftThree > 0){
                                numGearsLiftThree -= 1;
                                dialog.dismiss();
                            }
                        }
                    });

                    Button cancel = (Button) dialogLayout.findViewById(R.id.cancelButton);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //dismiss dialog
                            dialog.dismiss();
                        }
                    });

                    dialog.setContentView(dialogLayout);
                    dialog.show();

                    return true;
                }
            });

            return gearButton;
        }

        public int getNumGearsLiftOne() {   return numGearsLiftOne;}
        public int getNumGearsLiftTwo() {   return numGearsLiftTwo;}
        public int getNumGearsLiftThree() {   return numGearsLiftThree;}
        public void setNumGearsLiftOne(int i) {   numGearsLiftOne = i;}
        public void setNumGearsLiftTwo(int i) {   numGearsLiftTwo = i;}
        public void setNumGearsLiftThree(int i) {   numGearsLiftThree = i;}
    }

    public static class UIShotCreator extends UIComponentCreator {
        private int shotsMade;
        private String position;
        private long startTime;
        private long endTime;
        private long totalTime;
        private String name;
        private Activity context;
        private int currentShotComponent;

        public UIShotCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            currentShotComponent = 0;
            shotsMade = 0;
            this.context = context;
            name = "LOL";
        }
        public Button addButton(final String shotFBname) {
            name = UIShotCreator.super.componentNames.get(currentShotComponent);
            int index1 = name.indexOf("t")+1;
            int index2 = name.indexOf("S");
            String buttonName = name.substring(0, index1);
            final String dialogName = name.substring(0,1).toUpperCase() + name.substring(1, index2) + " Shooting";
            final String titleName = name.substring(0,1).toUpperCase() + name.substring(1, index2) + " Shots";
            final String height = name.substring(0,index2);
            final String dataName = name;

            final Button shotButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            shotButton.setText(buttonName);
            shotButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    shotsMade = 0;
                    startTime = System.currentTimeMillis();
                    final HashMap<String,Object> dataSpace = new HashMap<String, Object>();

                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.shot_dialog, null);
                    TextView titleTV = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    titleTV.setText(dialogName);

                    final TextView numberView = (TextView) dialogLayout.findViewById(R.id.numberView);

                    Button minusTenButton = (Button) dialogLayout.findViewById(R.id.minusTenButton);
                    minusTenButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(shotsMade >= 10){
                                shotsMade -= 10;
                            }else{
                                shotsMade = 0;
                            }
                            numberView.setText(String.valueOf(shotsMade));
                        }
                    });

                    Button minusButton = (Button) dialogLayout.findViewById(R.id.minusButton);
                    minusButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(shotsMade > 0){
                                shotsMade -= 1;
                            }
                            numberView.setText(String.valueOf(shotsMade));
                        }
                    });

                    Button plusTenButton = (Button) dialogLayout.findViewById(R.id.plusTenButton);
                    plusTenButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shotsMade += 10;
                            numberView.setText(String.valueOf(shotsMade));
                        }
                    });

                    Button plusButton = (Button) dialogLayout.findViewById(R.id.plusButton);
                    plusButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            shotsMade += 1;
                            numberView.setText(String.valueOf(shotsMade));
                        }
                    });

                    RadioButton keyRadioButton = (RadioButton) dialogLayout.findViewById(R.id.keyRadio);
                    keyRadioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            position = "Key";
                        }
                    });

                    RadioButton hopperRadioButton = (RadioButton) dialogLayout.findViewById(R.id.hopperRadio);
                    hopperRadioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            position = "Hopper";
                        }
                    });

                    RadioButton allianceWallRadioButton = (RadioButton) dialogLayout.findViewById(R.id.allianceWallRadio);
                    allianceWallRadioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            position = "Alliance Wall";
                        }
                    });

                    RadioButton otherRadioButton = (RadioButton) dialogLayout.findViewById(R.id.otherRadio);
                    otherRadioButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            position = "Other";
                        }
                    });

                    Button success = (Button) dialogLayout.findViewById(R.id.successButton);
                    success.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(position != null){
                                endTime = System.currentTimeMillis();
                                totalTime = endTime - startTime;

                                int i = 0;
                                List<String> shotKeys = Arrays.asList("numShots", "position", "time");
                                List<Object> shotValues = new ArrayList<>();
                                shotValues.clear();
                                shotValues.add(shotsMade);
                                shotValues.add(position);
                                shotValues.add(totalTime/1000);

                                dataSpace.put(shotKeys.get(0), shotValues.get(0));
                                dataSpace.put(shotKeys.get(1), shotValues.get(1));
                                dataSpace.put(shotKeys.get(2), shotValues.get(2));

                                switch(dataName) {
                                    case "highShotAuto" :
                                        DataActivity.highShotAutoDataList.add(dataSpace);
                                        i = Constants.highShotAuto;
                                        Constants.highShotAuto = i+1;
                                        break;
                                    case "lowShotAuto" :
                                        DataActivity.lowShotAutoDataList.add(dataSpace);
                                        i = Constants.lowShotAuto;
                                        Constants.lowShotAuto = i+1;
                                        break;
                                    case "highShotTele" :
                                        DataActivity.highShotTeleDataList.add(dataSpace);
                                        i = Constants.highShotTele;
                                        Constants.highShotTele = i+1;
                                        break;
                                    case "lowShotTele" :
                                        DataActivity.lowShotTeleDataList.add(dataSpace);
                                        i = Constants.lowShotTele;
                                        Constants.lowShotTele = i+1;
                                        break;
                                }

                                if(DataManager.collectedData.has(shotFBname)){
                                    try {
                                        DataManager.sideData = DataManager.collectedData.getJSONObject(shotFBname);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    DataManager.addOneTierJsonData(true, i+"", shotKeys, shotValues);
                                    DataManager.addZeroTierJsonData(shotFBname,DataManager.sideData);
                                }else{
                                    DataManager.sideData = new JSONObject();
                                    DataManager.addOneTierJsonData(true, i+"", shotKeys, shotValues);
                                    DataManager.addZeroTierJsonData(shotFBname,DataManager.sideData);
                                }

                                position = null;

                                dialog.dismiss();
                            }else{
                                Toast.makeText(context, "Please put shot location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.setContentView(dialogLayout);
                    dialog.show();
                }
            });
            shotButton.setOnLongClickListener(new View.OnLongClickListener() {

                public boolean onLongClick(View v){
                    if((DataActivity.saveAutoData && DataActivity.activityName.equals("auto")) || (DataActivity.saveTeleData && DataActivity.activityName.equals("tele"))){
                        try {
                            Log.e("testAuto", DataActivity.activityName+Boolean.toString(DataActivity.saveAutoData));
                            Log.e("testTele", DataActivity.activityName+Boolean.toString(DataActivity.saveTeleData));

                            if(shotFBname.equals("highShotTimesForBoilerAuto")){
                                DataActivity.highShotAutoDataList = new ArrayList<>();
                            }else if(shotFBname.equals("lowShotTimesForBoilerAuto")){
                                DataActivity.lowShotAutoDataList = new ArrayList<>();
                            }else if(shotFBname.equals("highShotTimesForBoilerTele")){
                                DataActivity.highShotTeleDataList = new ArrayList<>();
                            }else if(shotFBname.equals("lowShotTimesForBoilerTele")){
                                DataActivity.lowShotTeleDataList = new ArrayList<>();
                            }
                            if(DataActivity.rejected){
                                DataActivity.highShotAutoDataList = new ArrayList<>();
                                DataActivity.lowShotAutoDataList = new ArrayList<>();
                                DataActivity.highShotTeleDataList = new ArrayList<>();
                                DataActivity.lowShotTeleDataList = new ArrayList<>();
                                DataManager.collectedData.remove("highShotTimesForBoilerAuto");
                                DataManager.collectedData.remove("lowShotTimesForBoilerAuto");
                                DataManager.collectedData.remove("highShotTimesForBoilerTele");
                                DataManager.collectedData.remove("lowShotTimesForBoilerTele");
                            }else{
                                for(int i = 0; i < DataManager.collectedData.getJSONObject(shotFBname).length();i++){
                                    JSONObject tempContainer = DataManager.collectedData.getJSONObject(shotFBname).getJSONObject(i+"");
                                    final HashMap<String,Object> dataSpace = new HashMap<String, Object>();
                                    dataSpace.put("numShots", tempContainer.getInt("numShots"));
                                    dataSpace.put("position", tempContainer.getString("position"));
                                    dataSpace.put("time", tempContainer.getLong("time"));
                                    if(shotFBname.equals("highShotTimesForBoilerAuto")){
                                        DataActivity.highShotAutoDataList.add(dataSpace);
                                    }else if(shotFBname.equals("lowShotTimesForBoilerAuto")){
                                        DataActivity.lowShotAutoDataList.add(dataSpace);
                                    }else if(shotFBname.equals("highShotTimesForBoilerTele")){
                                        DataActivity.highShotTeleDataList.add(dataSpace);
                                    }else if(shotFBname.equals("lowShotTimesForBoilerTele")){
                                        DataActivity.lowShotTeleDataList.add(dataSpace);
                                    }
                                }
                            }


                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                        if(DataActivity.activityName.equals("auto")){
                            DataActivity.saveAutoData = false;
                        }else if(DataActivity.activityName.equals("tele")){
                            DataActivity.saveTeleData = false;
                        }
                    }

                    int latest = 0;

                    if(shotFBname.equals("highShotTimesForBoilerAuto")){
                        latest = DataActivity.highShotAutoDataList.size();
                    }else if(shotFBname.equals("lowShotTimesForBoilerAuto")){
                        latest = DataActivity.lowShotAutoDataList.size();
                    }else if(shotFBname.equals("highShotTimesForBoilerTele")){
                        latest = DataActivity.highShotTeleDataList.size();
                    }else if(shotFBname.equals("lowShotTimesForBoilerTele")){
                        latest = DataActivity.lowShotTeleDataList.size();
                    }

                    if(latest > 0){
                        View shotsHistory = ((LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.shots_history_dialog, null);
                        ListView shotList = (ListView) shotsHistory.findViewById(R.id.shotsListView);

                        AlertDialog.Builder shotBuilder = new AlertDialog.Builder(context);
                        shotBuilder.setView(shotsHistory);
                        shotBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        shotBuilder.setTitle(titleName);
                        shotBuilder.setCancelable(false);
                        AlertDialog shotDialog = shotBuilder.create();

                        if(shotFBname.equals("highShotTimesForBoilerAuto")){
                            shotList.setAdapter(new ShotListAdapter(context, DataActivity.highShotAutoDataList, shotDialog, titleName, new ShotListAdapter.ListModificationListener() {
                                @Override
                                public void onListChanged(ArrayList<HashMap<String, Object>> returnList) {
                                    DataActivity.highShotAutoDataList = returnList;
                                }
                            }));
                        }else if(shotFBname.equals("lowShotTimesForBoilerAuto")){
                            shotList.setAdapter(new ShotListAdapter(context, DataActivity.lowShotAutoDataList, shotDialog, titleName, new ShotListAdapter.ListModificationListener() {
                                @Override
                                public void onListChanged(ArrayList<HashMap<String, Object>> returnList) {
                                    DataActivity.lowShotAutoDataList = returnList;
                                }
                            }));
                        }else if(shotFBname.equals("highShotTimesForBoilerTele")){
                            shotList.setAdapter(new ShotListAdapter(context, DataActivity.highShotTeleDataList, shotDialog, titleName, new ShotListAdapter.ListModificationListener() {
                                @Override
                                public void onListChanged(ArrayList<HashMap<String, Object>> returnList) {
                                    DataActivity.highShotTeleDataList = returnList;
                                }
                            }));
                        }else if(shotFBname.equals("lowShotTimesForBoilerTele")){
                            shotList.setAdapter(new ShotListAdapter(context, DataActivity.lowShotTeleDataList, shotDialog, titleName, new ShotListAdapter.ListModificationListener() {
                                @Override
                                public void onListChanged(ArrayList<HashMap<String, Object>> returnList) {
                                    DataActivity.lowShotTeleDataList = returnList;
                                }
                            }));
                        }

                        shotDialog.show();
                    } else {
                        Toast.makeText(context, "No Entries for "+titleName, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            currentShotComponent++;
            super.componentViews.add(shotButton);
            return shotButton;
        }
    }
}