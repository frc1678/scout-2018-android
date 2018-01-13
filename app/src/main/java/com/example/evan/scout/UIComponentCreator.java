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

    public static class UISwitchCreator extends UIComponentCreator {
        private boolean switchSuccess;
        private float startTime;
        private float endTime;
        private String name;
        private Activity context;
        private int currentSwitchComponent;

        public UISwitchCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            currentSwitchComponent = 0;
            switchSuccess = false;
            this.context = context;
            name = "LOL";
        }
        public Button addButton(final String switchFBname) {
            name = UISwitchCreator.super.componentNames.get(currentSwitchComponent);

            final Button switchButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            switchButton.setText(name);
            switchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchSuccess = false;
                    startTime = backgroundTimer.getUpdatedTime();

                    //Create Dialog
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.switch_dialog, null);
                    //Set Dialog Title
                    TextView titleTV = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    titleTV.setText(name);

                    Button successButton = (Button) dialogLayout.findViewById(R.id.successButton);
                    successButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                //calculate time
                                endTime = backgroundTimer.getUpdatedTime();
                                switchSuccess = true;

                                int i = 0;
                                List<String> switchKeys = Arrays.asList("didSucceed", "startTime", "endTime");
                                List<Object> switchValues = new ArrayList<>();
                                switchKeys.clear();
                                switchValues.add(switchSuccess);
                                switchValues.add(startTime);
                                switchValues.add(endTime);

                                if(DataManager.collectedData.has(switchFBname)){
                                    try {
                                        DataManager.sideData = DataManager.collectedData.getJSONObject(switchFBname);
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                    DataManager.addOneTierJsonData(true, i+"", switchKeys, switchValues);
                                    DataManager.addZeroTierJsonData(switchFBname,DataManager.sideData);
                                }else{
                                    DataManager.sideData = new JSONObject();
                                    DataManager.addOneTierJsonData(true, i+"", switchKeys, switchValues);
                                    DataManager.addZeroTierJsonData(switchFBname,DataManager.sideData);
                                }

                                dialog.dismiss();
                            }
                    });

                    Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //calculate time
//                                endTime = System.currentTimeMillis();
                            switchSuccess = true;

                            int i = 0;
                            List<String> switchKeys = Arrays.asList("didSucceed", "startTime", "endTime");
                            List<Object> switchValues = new ArrayList<>();
                            switchValues.clear();
                            switchValues.add(switchSuccess);
                            switchValues.add(startTime);
                            switchValues.add(endTime);

                            if(DataManager.collectedData.has(switchFBname)){
                                try {
                                    DataManager.sideData = DataManager.collectedData.getJSONObject(switchFBname);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                DataManager.addOneTierJsonData(true, i+"", switchKeys, switchValues);
                                DataManager.addZeroTierJsonData(switchFBname,DataManager.sideData);
                            }else{
                                DataManager.sideData = new JSONObject();
                                DataManager.addOneTierJsonData(true, i+"", switchKeys, switchValues);
                                DataManager.addZeroTierJsonData(switchFBname,DataManager.sideData);
                            }

                            dialog.dismiss();
                        }
                    });

                    dialog.setContentView(dialogLayout);
                    dialog.show();
                }
            });
//            switchButton.setOnLongClickListener(new View.OnLongClickListener() {
//
//                public boolean onLongClick(View v){
//                    if((DataActivity.saveAutoData && DataActivity.activityName.equals("auto")) || (DataActivity.saveTeleData && DataActivity.activityName.equals("tele"))){
//                        if(DataActivity.activityName.equals("auto")){
//                            DataActivity.saveAutoData = false;
//                        }else if(DataActivity.activityName.equals("tele")){
//                            DataActivity.saveTeleData = false;
//                        }
//                    }
//
//                    int latest = 0;
//
//                    if(latest > 0){
//                        View switchHistory = ((LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.shots_history_dialog, null);
//                        ListView switchList = (ListView) shotsHistory.findViewById(R.id.shotsListView);
//
//                        AlertDialog.Builder switchBuilder = new AlertDialog.Builder(context);
//                        switchBuilder.setView(switchHistory);
//                        switchBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.cancel();
//                            }
//                        });
//                        switchBuilder.setTitle(name);
//                        switchBuilder.setCancelable(false);
//                        AlertDialog shotDialog = switchBuilder.create();
//
//                        shotDialog.show();
//                    } else {
//                        Toast.makeText(context, "No Entries for "+name, Toast.LENGTH_SHORT).show();
//                    }
//                    return true;
//                }
//            });
            currentSwitchComponent++;
            super.componentViews.add(switchButton);
            return switchButton;
        }
    }
}