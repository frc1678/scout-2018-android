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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.json.JSONArray;
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
import static java.lang.String.valueOf;

//class that creates all the ui components I need like togglebuttons, etc.  Also stores all buttons in list to be accessed later
public class UIComponentCreator {
    //list of views that hold data that I need to access later
    private List<View> componentViews;
    private List<RadioButton> radioViews;
    //list of names of buttons, textviews, etc
    private List<String> componentNames;
    private Activity context;
    //counter for list
    private int currentComponent;

    public UIComponentCreator(Activity context, List<String> componentNames) {
        componentViews = new ArrayList<>();
        radioViews = new ArrayList<>();
        this.componentNames = componentNames;
        this.context = context;
        currentComponent = 0;
    }

    public Button getBasicButton(int width, int height, Float textScale) {
        Button button = new Button(context);
        button.setLayoutParams(new LinearLayout.LayoutParams(width, height, 1f));
        if (componentNames != null) {
            button.setText(componentNames.get(currentComponent));
        }
        button.setTextSize(button.getTextSize() * textScale);
        button.setSingleLine(true);
        return button;
    }

    public ToggleButton getToggleButton(int width, boolean value, int color, boolean setName) {
        ToggleButton toggleButton = new ToggleButton(context);
        toggleButton.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if(setName){
            toggleButton.setText(componentNames.get(currentComponent));
            toggleButton.setTextOn(componentNames.get(currentComponent));
            toggleButton.setTextOff(componentNames.get(currentComponent));
        }
        toggleButton.setTextSize(toggleButton.getTextSize() * 1f);
        if(color == 0){     }else{      toggleButton.setBackgroundColor(color); }
        toggleButton.setChecked(value);
        currentComponent++;
        componentViews.add(toggleButton);
        return toggleButton;
    }

    public RadioButton getRadioButton (String radioFBName, String radioFBValue,int width,int height){
        RadioButton radioButton = new RadioButton(context);
        radioButton.setLayoutParams(new LinearLayout.LayoutParams(width, height, 1f));
        radioButton.setTextSize(radioButton.getTextSize() * 1f);
        radioViews.add(radioButton);
        currentComponent++;
        return radioButton;
    }

    public List<RadioButton> getRadioViews() {
        return radioViews;
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
                if ((DataActivity.saveAutoData && DataActivity.activityName.equals("auto")) || (DataActivity.saveTeleData && DataActivity.activityName.equals("tele"))) {
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
                    if (value > 0) {
                        value--;
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
                    value++;
                    valueTV.setText(String.valueOf(value));
                }
            });
            Log.e("counterB", plus.getText().toString());

            currentCounterComponent++;
            super.componentViews.add(valueTV);
            return counterLayout;
        }
    }

    public static class UISwitchCreator extends UIComponentCreator {
        private boolean switchSuccess;
        private float startTime;
        private float endTime;
        private String status;
        int c = 0;
        private int layer;
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

        public void resetSwitchComponent(){     currentSwitchComponent = 0; }
        public int returnSwitchComponent(){     return currentSwitchComponent; }

        public Button addButton(final String switchFBname, final String colorOfSwitch, final JSONArray jsonArray, final String allianceOrOpponent) {
            name = UISwitchCreator.super.componentNames.get(currentSwitchComponent);

            final Button switchButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            if(colorOfSwitch.equals("red")){    switchButton.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTRED));     switchButton.setText("Red Switch"); }
            else if(colorOfSwitch.equals("blue")){    switchButton.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTBLUE));     switchButton.setText("Blue Switch"); }
            switchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final HashMap<String,Object> dataSpace = new HashMap<String, Object>();
                    c = jsonArray.length();
                    layer = 0;
                    status = null;
                    switchSuccess = false;
                    startTime = backgroundTimer.getUpdatedTime();

                    //Create Dialog
                    final Dialog dialog = new Dialog(context);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.switch_dialog, null);
                    //Set Dialog Title
                    TextView titleTV = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    String colorString = colorOfSwitch.substring(0,1).toUpperCase() + colorOfSwitch.substring(1);
                    titleTV.setText(colorString + " Switch Attempt");

                    Button successButton = (Button) dialogLayout.findViewById(R.id.successButton);
                    successButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //calculate time
                            endTime = backgroundTimer.getUpdatedTime();
                            switchSuccess = true;

                            dialog.dismiss();

                            final Dialog successDialog = new Dialog(context);
                            successDialog.setCanceledOnTouchOutside(false);
                            successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            RelativeLayout successDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.switch_success_dialog, null);
                            TextView successTitleTV = (TextView) successDialogLayout.findViewById(R.id.dialogTitle);
                            successTitleTV.setText(name);

                            final RadioGroup switchOwnershipRG = (RadioGroup) successDialogLayout.findViewById(R.id.switchOwnershipRadioGroup);

                            RadioButton ownedRadioButton = (RadioButton) successDialogLayout.findViewById(R.id.switchOwnedRadio);
                            ownedRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(status != "opponentOwned"){
                                        status = "opponentOwned";
                                    }else{
                                        switchOwnershipRG.clearCheck();
                                        status = null;
                                    }
                                }
                            });

                            RadioButton balancedRadioButton = (RadioButton) successDialogLayout.findViewById(R.id.switchBalancedRadio);
                            balancedRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(status != "balanced"){
                                        status = "balanced";
                                    }else{
                                        switchOwnershipRG.clearCheck();
                                        status = null;
                                    }
                                }
                            });

                            RadioButton layer1RadioButton = (RadioButton) successDialogLayout.findViewById(R.id.switchLayer1Radio);
                            layer1RadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    layer = 1;
                                }
                            });

                            RadioButton layer2RadioButton = (RadioButton) successDialogLayout.findViewById(R.id.switchLayer2Radio);
                            layer2RadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    layer = 2;
                                }
                            });

                            RadioButton layer3RadioButton = (RadioButton) successDialogLayout.findViewById(R.id.switchLayer3Radio);
                            layer3RadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    layer = 3;
                                }
                            });

                            Button cancel = (Button) successDialogLayout.findViewById(R.id.cancelButton);
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    successDialog.dismiss();
                                }
                            });

                            Button done = (Button) successDialogLayout.findViewById(R.id.doneButton);
                            done.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (layer != 0) {
                                        endTime = backgroundTimer.getUpdatedTime();
                                        switchSuccess = true;

                                        List<String> switchKeys = Arrays.asList("didSucceed", "startTime", "endTime", "status", "layer");
                                        List<Object> switchValues = new ArrayList<>();
                                        switchValues.clear();
                                        switchValues.add(switchSuccess);
                                        switchValues.add(startTime);
                                        switchValues.add(endTime);
                                        if(status == null){
                                            switchValues.add("owned");
                                        }else{
                                            switchValues.add(status);
                                        }
                                        switchValues.add(layer);

                                        JSONObject tempData = Utils.returnJSONObject(switchKeys, switchValues);
                                        try {
                                            jsonArray.put(c, tempData);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        DataManager.addZeroTierJsonData(switchFBname, jsonArray);
                                        c++;

                                        dataSpace.put(switchKeys.get(0), switchValues.get(0));
                                        dataSpace.put(switchKeys.get(1), switchValues.get(1));
                                        dataSpace.put(switchKeys.get(2), switchValues.get(2));
                                        dataSpace.put(switchKeys.get(3), switchValues.get(3));
                                        dataSpace.put(switchKeys.get(4), switchValues.get(4));

                                        if(DataActivity.activityName.equals("auto")) { DataManager.autoAllianceSwitchDataList.add(dataSpace); }
                                        else if(DataActivity.activityName.equals("tele")) {
                                            if (allianceOrOpponent == "alliance") {
                                                DataManager.teleAllianceSwitchDataList.add(dataSpace);
                                            } else if (allianceOrOpponent == "opponent") {
                                                DataManager.teleOpponentSwitchDataList.add(dataSpace);
                                            }
                                        }
                                        successDialog.dismiss();
                                    } else {
                                        Toast.makeText(context, "Please put the layer!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            successDialog.setContentView(successDialogLayout);
                            successDialog.show();
                        }
                    });

                    Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //calculate time
                            endTime = backgroundTimer.getUpdatedTime();
                            switchSuccess = false;

                            List<String> switchKeys = Arrays.asList("didSucceed", "startTime", "endTime", "status", "layer");
                            List<Object> switchValues = new ArrayList<>();
                            switchValues.clear();
                            switchValues.add(switchSuccess);
                            switchValues.add(startTime);
                            switchValues.add(endTime);
                            if(status == null){
                                status = "owned";
                            }
                            switchValues.add(null);
                            switchValues.add(null); //Added

                            JSONObject tempData = Utils.returnJSONObject(switchKeys, switchValues);
                            try {
                                jsonArray.put(c, tempData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            DataManager.addZeroTierJsonData(switchFBname, jsonArray);
                            c++;

                            dataSpace.put(switchKeys.get(0), switchValues.get(0));
                            dataSpace.put(switchKeys.get(1), switchValues.get(1));
                            dataSpace.put(switchKeys.get(2), switchValues.get(2));
                            dataSpace.put(switchKeys.get(3), switchValues.get(3));
                            dataSpace.put(switchKeys.get(4), switchValues.get(4));

                            if(DataActivity.activityName.equals("auto")) { DataManager.autoAllianceSwitchDataList.add(dataSpace); }
                            else if(DataActivity.activityName.equals("tele")) {
                                if (allianceOrOpponent == "alliance") {
                                    DataManager.teleAllianceSwitchDataList.add(dataSpace);
                                } else if (allianceOrOpponent == "opponent") {
                                    DataManager.teleOpponentSwitchDataList.add(dataSpace);
                                }
                            }
                            dialog.dismiss();
                        }
                    });

                    Button cancel = (Button) dialogLayout.findViewById(R.id.cancelButton);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setContentView(dialogLayout);
                    dialog.show();
                }
            });
            switchButton.setOnLongClickListener(new View.OnLongClickListener() {

                public boolean onLongClick(View v) {

                    int latest = 0;
                    if (DataActivity.activityName.equals("auto")) {
                        latest = DataManager.autoAllianceSwitchDataList.size();
                        Log.e("autoallysizeLatest", latest + "");
                    } else if (DataActivity.activityName.equals("tele")) {
                        if (allianceOrOpponent == "alliance") {
                            latest = DataManager.teleAllianceSwitchDataList.size();
                            Log.e("teleallysizeLatest", latest + "");
                        }else if (allianceOrOpponent == "opponent") {
                            latest = DataManager.teleOpponentSwitchDataList.size();
                            Log.e("teleoppsizeLatest", latest + "");
                        }
                    }
                    Log.e("sizeLatest", latest + "");

                    if (latest > 0) {
                        View switchHistory = ((LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.switch_history_dialog, null);
                        ListView switchList = (ListView) switchHistory.findViewById(R.id.switchListView);
                        switchList.setVisibility(View.VISIBLE);

                        AlertDialog.Builder switchBuilder = new AlertDialog.Builder(context);
                        switchBuilder.setView(switchHistory);
                        switchBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        switchBuilder.setTitle(name);
                        switchBuilder.setCancelable(false);
                        AlertDialog switchDialog = switchBuilder.create();

                        if (DataActivity.activityName.equals("auto")) {
                            switchList.setAdapter(new SwitchListAdapter(context, DataManager.autoAllianceSwitchDataList, switchDialog, name, switchFBname, jsonArray, new SwitchListAdapter.ListModificationListener() {
                                @Override
                                public void onListChanged(ArrayList<HashMap<String, Object>> returnList) {
                                    DataManager.autoAllianceSwitchDataList = returnList;
                                }
                            }));
                        } else if (DataActivity.activityName.equals("tele")) {
                            if (allianceOrOpponent == "alliance") {
                                switchList.setAdapter(new SwitchListAdapter(context, DataManager.teleAllianceSwitchDataList, switchDialog, name, switchFBname, jsonArray, new SwitchListAdapter.ListModificationListener() {
                                    @Override
                                    public void onListChanged(ArrayList<HashMap<String, Object>> returnList) {
                                        DataManager.teleAllianceSwitchDataList = returnList;
                                    }
                                }));
                            } else if (allianceOrOpponent == "opponent") {
                                switchList.setAdapter(new SwitchListAdapter(context, DataManager.teleOpponentSwitchDataList, switchDialog, name, switchFBname, jsonArray, new SwitchListAdapter.ListModificationListener() {
                                    @Override
                                    public void onListChanged(ArrayList<HashMap<String, Object>> returnList) {
                                        DataManager.teleOpponentSwitchDataList = returnList;
                                    }
                                }));
                            }
                        }
                        switchDialog.show();
                    } else {
                        Toast.makeText(context, "No Entries for " + name, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            currentSwitchComponent++;
            super.componentViews.add(switchButton);
            return switchButton;
        }
    }

    public static class UIScaleCreator extends UIComponentCreator {
        int c = 0;
        private String status;
        private int layer;
        private float startTime;
        private float endTime;
        private String name;
        private Activity context;
        private int currentScaleComponent;
        private boolean didSucceed;

        public UIScaleCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            currentScaleComponent = 0;
            this.context = context;
            name = "Scale";
            layer = 0;
        }

        public void resetScaleComponenet(){     currentScaleComponent = 0; }
        public int returnScaleComponenet(){     return currentScaleComponent; }

        public Button addButton(final String scaleFBname, final JSONArray jsonArray) {
            final Button scaleButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            scaleButton.setText(name);
            scaleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final HashMap<String,Object> dataSpace = new HashMap<String, Object>();
                    c = jsonArray.length();
                    layer = 0;
                    status = null;
                    startTime = backgroundTimer.getUpdatedTime();

                    final Dialog dialog = new Dialog(context);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.scale_dialog, null);
                    TextView titleTV = (TextView) dialogLayout.findViewById(R.id.scaleDialogTitle);
                    titleTV.setText(name);

                    Button success = (Button) dialogLayout.findViewById(R.id.scaleSuccessButton);
                    success.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            endTime = backgroundTimer.getUpdatedTime();
                            didSucceed = true;

                            dialog.dismiss();

                            final Dialog successDialog = new Dialog(context);
                            successDialog.setCanceledOnTouchOutside(false);
                            successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            RelativeLayout successDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.scale_success_dialog, null);
                            TextView successTitleTV = (TextView) successDialogLayout.findViewById(R.id.scaleSuccessDialogTitle);
                            successTitleTV.setText(name);

                            final RadioGroup scaleOwnershipRG = (RadioGroup) successDialogLayout.findViewById(R.id.scaleOwnershipRadioGroup);

                            RadioButton ownedRadioButton = (RadioButton) successDialogLayout.findViewById(R.id.scaleOwnedRadio);
                            ownedRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(status != "opponentOwned"){
                                        status = "opponentOwned";
                                    }else{
                                        scaleOwnershipRG.clearCheck();
                                        status = null;
                                    }
                                }
                            });

                            RadioButton balancedRadioButton = (RadioButton) successDialogLayout.findViewById(R.id.scaleBalancedRadio);
                            balancedRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(status != "balanced"){
                                        status = "balanced";
                                    }else{
                                        scaleOwnershipRG.clearCheck();
                                        status = null;
                                    }
                                }
                            });

                            RadioButton layer1RadioButton = (RadioButton) successDialogLayout.findViewById(R.id.scaleLayer1Radio);
                            layer1RadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    layer = 1;
                                }
                            });

                            RadioButton layer2RadioButton = (RadioButton) successDialogLayout.findViewById(R.id.scaleLayer2Radio);
                            layer2RadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    layer = 2;
                                }
                            });

                            RadioButton layer3RadioButton = (RadioButton) successDialogLayout.findViewById(R.id.scaleLayer3Radio);
                            layer3RadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    layer = 3;
                                }
                            });

                            Button cancel = (Button) successDialogLayout.findViewById(R.id.cancelButton);
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    successDialog.dismiss();
                                }
                            });

                            Button done = (Button) successDialogLayout.findViewById(R.id.scaleDoneButton);
                            done.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (layer != 0) {
                                        List<String> scaleKeys = Arrays.asList("didSucceed", "startTime", "endTime", "status", "layer");
                                        List<Object> scaleValues = new ArrayList<>();
                                        scaleValues.clear();
                                        scaleValues.add(didSucceed);
                                        scaleValues.add(startTime);
                                        scaleValues.add(endTime);
                                        if(status == null){
                                            scaleValues.add("owned");
                                        }else{
                                            scaleValues.add(status);
                                        }
                                        scaleValues.add(layer);

                                        JSONObject tempData = Utils.returnJSONObject(scaleKeys, scaleValues);
                                        try {
                                            jsonArray.put(c, tempData);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        DataManager.addZeroTierJsonData(scaleFBname, jsonArray);
                                        c++;

                                        dataSpace.put(scaleKeys.get(0), scaleValues.get(0));
                                        dataSpace.put(scaleKeys.get(1), scaleValues.get(1));
                                        dataSpace.put(scaleKeys.get(2), scaleValues.get(2));
                                        dataSpace.put(scaleKeys.get(3), scaleValues.get(3));
                                        dataSpace.put(scaleKeys.get(4), scaleValues.get(4));

                                        if(DataActivity.activityName.equals("auto")) { DataManager.autoScaleDataList.add(dataSpace); }
                                        else if(DataActivity.activityName.equals("tele")) { DataManager.teleScaleDataList.add(dataSpace); } //END HERE (or one line above)
                                        successDialog.dismiss();

                                    } else {
                                        Toast.makeText(context, "Please put layer!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                            successDialog.setContentView(successDialogLayout);
                            successDialog.show();
                        }
                    });

                    Button failure = (Button) dialogLayout.findViewById(R.id.scaleFailButton);
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            endTime = backgroundTimer.getUpdatedTime();
                            didSucceed = false;

                            List<String> scaleKeys = Arrays.asList("didSucceed", "startTime", "endTime", "status", "layer"); //Changed
                            List<Object> scaleValues = new ArrayList<>();
                            scaleValues.clear();
                            scaleValues.add(didSucceed);
                            scaleValues.add(startTime);
                            scaleValues.add(endTime);
                            scaleValues.add(null); //START
                            scaleValues.add(null); //END

                            JSONObject tempData = Utils.returnJSONObject(scaleKeys, scaleValues);
                            try {
                                jsonArray.put(c, tempData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            DataManager.addZeroTierJsonData(scaleFBname, jsonArray);
                            c++;

                            dataSpace.put(scaleKeys.get(0), scaleValues.get(0));
                            dataSpace.put(scaleKeys.get(1), scaleValues.get(1));
                            dataSpace.put(scaleKeys.get(2), scaleValues.get(2));
                            dataSpace.put(scaleKeys.get(3), scaleValues.get(3)); //START
                            dataSpace.put(scaleKeys.get(4), scaleValues.get(4)); //END

                            if(DataActivity.activityName.equals("auto")) { DataManager.autoScaleDataList.add(dataSpace); }
                            else if(DataActivity.activityName.equals("tele")) { DataManager.teleScaleDataList.add(dataSpace); }
                            dialog.dismiss();
                        }
                    });

                    Button cancel = (Button) dialogLayout.findViewById(R.id.cancelButton);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.setContentView(dialogLayout);
                    dialog.show();
                }
            });
            scaleButton.setOnLongClickListener(new View.OnLongClickListener() {

                public boolean onLongClick(View v) {

                    int latest = 0;
                    if(DataActivity.activityName.equals("auto")){latest = DataManager.autoScaleDataList.size();Log.e("sizeLatest", latest+"");}
                    else if(DataActivity.activityName.equals("tele")){latest = DataManager.teleScaleDataList.size();}

                    if (latest > 0) {
                        View scaleHistory = ((LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.scale_history_dialog, null);
                        ListView scaleList = (ListView) scaleHistory.findViewById(R.id.scaleListView);

                        AlertDialog.Builder scaleBuilder = new AlertDialog.Builder(context);
                        scaleBuilder.setView(scaleHistory);
                        scaleBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        scaleBuilder.setTitle(name);
                        scaleBuilder.setCancelable(false);
                        AlertDialog scaleDialog = scaleBuilder.create();

                        if(DataActivity.activityName.equals("auto")){
                            scaleList.setAdapter(new ScaleListAdapter(context, DataManager.autoScaleDataList, scaleDialog, name, scaleFBname, jsonArray, new ScaleListAdapter.ListModificationListener() {
                                @Override
                                public void onListChanged(ArrayList<HashMap<String, Object>> returnList) {
                                    DataManager.autoScaleDataList = returnList;
                                }
                            }));
                        }
                        else if(DataActivity.activityName.equals("tele")){
                            scaleList.setAdapter(new ScaleListAdapter(context, DataManager.teleScaleDataList, scaleDialog, name, scaleFBname, jsonArray, new ScaleListAdapter.ListModificationListener() {
                                @Override
                                public void onListChanged(ArrayList<HashMap<String, Object>> returnList) {
                                    DataManager.teleScaleDataList = returnList;
                                }
                            }));
                        }

                        scaleDialog.show();
                    } else {
                        Toast.makeText(context, "No Entries for " + name, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                } //END HERE (end of onLongClick)
            });
            currentScaleComponent++;
            super.componentViews.add(scaleButton);
            return scaleButton;
        }
    }

    public static class UIPyramidCreator extends UIComponentCreator {

        private int numGroundPyramidIntake;
        private int numElevatedPyramidIntake;
        private String name;
        private Activity context;
        private int currentPyramidComponent;
        private int tempGroundPyramidIntake = numGroundPyramidIntake;
        private int tempElevatedPyramidIntake = numElevatedPyramidIntake;

        public UIPyramidCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            currentPyramidComponent = 0;
            numElevatedPyramidIntake = 0;
            numGroundPyramidIntake = 0;
            this.context = context;
            name = "LOL";
        }

        public void resetPyramidComponent(){     currentPyramidComponent = 0; }
        public int returnPyramidComponent(){     return currentPyramidComponent; }

        public Button addButton() {
            try{
                if(DataActivity.activityName.equals("auto")){
                    numGroundPyramidIntake = DataManager.autoPyramidDataList.getInt("numGroundPyramidIntakeAuto");
                    numElevatedPyramidIntake = DataManager.autoPyramidDataList.getInt("numElevatedPyramidIntakeAuto");
                }else if(DataActivity.activityName.equals("tele")) {
                    numGroundPyramidIntake = DataManager.telePyramidDataList.getInt("numGroundPyramidIntakeTele");
                    numElevatedPyramidIntake = DataManager.telePyramidDataList.getInt("numElevatedPyramidIntakeTele");
                }
            }catch (JSONException je){
                je.printStackTrace();
            }

            name = UIPyramidCreator.super.componentNames.get(currentPyramidComponent);

            final Button pyramidButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            pyramidButton.setText(name);
            pyramidButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(context);
                    dialog.setCanceledOnTouchOutside(false);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.pyramid_dialog, null);
                    TextView titleTV = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    titleTV.setText(name);

                    Button groundButton = (Button) dialogLayout.findViewById(R.id.groundButton);
                    groundButton.setText("Ground" + " ("+numGroundPyramidIntake+")");
                    groundButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numGroundPyramidIntake += 1;
                            if (numGroundPyramidIntake != 0 || numElevatedPyramidIntake != 0) {
                                try{
                                    if(DataActivity.activityName.equals("auto")){
                                        DataManager.autoPyramidDataList.put("numGroundPyramidIntakeAuto", numGroundPyramidIntake);
                                        DataManager.addZeroTierJsonData("numGroundPyramidIntakeAuto", numGroundPyramidIntake);
                                    }else if(DataActivity.activityName.equals("tele")) {
                                        DataManager.telePyramidDataList.put("numGroundPyramidIntakeTele", numGroundPyramidIntake);
                                        DataManager.addZeroTierJsonData("numGroundPyramidIntakeTele", numGroundPyramidIntake);
                                    }
                                }catch (JSONException je){
                                    je.printStackTrace();
                                }
                                dialog.dismiss();
                            } else {
                                Toast.makeText(context, "Please put pyramid layer", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    Button elevatedButton = (Button) dialogLayout.findViewById(R.id.elevatedButton);
                    elevatedButton.setText("Elevated" + " ("+numElevatedPyramidIntake+")");
                    elevatedButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numElevatedPyramidIntake += 1;
                            if (numGroundPyramidIntake != 0 || numElevatedPyramidIntake != 0) {
                                try{
                                    if(DataActivity.activityName.equals("auto")){
                                        DataManager.autoPyramidDataList.put("numElevatedPyramidIntakeAuto", numElevatedPyramidIntake);
                                        DataManager.addZeroTierJsonData("numElevatedPyramidIntakeAuto", numElevatedPyramidIntake);
                                    }else if(DataActivity.activityName.equals("tele")) {
                                        DataManager.telePyramidDataList.put("numElevatedPyramidIntakeTele", numElevatedPyramidIntake);
                                        DataManager.addZeroTierJsonData("numElevatedPyramidIntakeTele", numElevatedPyramidIntake);
                                    }
                                }catch (JSONException je){
                                    je.printStackTrace();
                                }
                                dialog.dismiss();
                            } else {
                                Toast.makeText(context, "Please put pyramid layer", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    Button cancelButton = (Button) dialogLayout.findViewById(R.id.cancelButton);
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.setContentView(dialogLayout);
                    dialog.show();
                }
            });
            pyramidButton.setOnLongClickListener(new View.OnLongClickListener() {

                public boolean onLongClick(View v){
                    int latest = 0;

                    if(DataActivity.activityName.equals("auto")){latest = DataManager.autoPyramidDataList.length();}
                    else if(DataActivity.activityName.equals("tele")){latest = DataManager.telePyramidDataList.length();}

                    if(latest > 0){
                        View pyramidHistory = ((LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.pyramid_history_dialog, null);
                        AlertDialog.Builder pyramidBuilder = new AlertDialog.Builder(context);
                        pyramidBuilder.setView(pyramidHistory);
                        pyramidBuilder.setTitle(name);
                        pyramidBuilder.setCancelable(false);
                        final AlertDialog pyramidDialog = pyramidBuilder.create();

                        final TextView groundNumberView = (TextView) pyramidHistory.findViewById(R.id.groundNumberView);
                        final TextView elevatedNumberView = (TextView) pyramidHistory.findViewById(R.id.elevatedNumberView);

                        if(DataActivity.activityName.equals("auto")) {
                            try{
                                tempGroundPyramidIntake = DataManager.autoPyramidDataList.getInt("numGroundPyramidIntakeAuto");
                            }catch(JSONException je){
                                je.printStackTrace();
                            }
                            try {
                                tempElevatedPyramidIntake = DataManager.autoPyramidDataList.getInt("numElevatedPyramidIntakeAuto");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        if(DataActivity.activityName.equals("tele")) {
                            try{
                                tempGroundPyramidIntake = DataManager.telePyramidDataList.getInt("numGroundPyramidIntakeTele");
                            }catch(JSONException je){
                                je.printStackTrace();
                            }
                            try {
                                tempElevatedPyramidIntake = DataManager.telePyramidDataList.getInt("numElevatedPyramidIntakeTele");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        groundNumberView.setText(tempGroundPyramidIntake+"");
                        elevatedNumberView.setText(tempElevatedPyramidIntake+"");

                        ((Button) pyramidHistory.findViewById(R.id.groundPlusButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(tempGroundPyramidIntake >= 10){
                                }else{
                                    tempGroundPyramidIntake+= 1;
                                }
                                groundNumberView.setText(valueOf(tempGroundPyramidIntake));
                                elevatedNumberView.setText(valueOf(tempElevatedPyramidIntake));
                            }
                        });
                        ((Button) pyramidHistory.findViewById(R.id.groundMinusButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(tempGroundPyramidIntake <= 0){
                                }else{
                                    tempGroundPyramidIntake-= 1;
                                }
                                groundNumberView.setText(valueOf(tempGroundPyramidIntake));
                                elevatedNumberView.setText(valueOf(tempElevatedPyramidIntake));
                            }
                        });
                        ((Button) pyramidHistory.findViewById(R.id.elevatedPlusButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(tempElevatedPyramidIntake >= 10){
                                }else{
                                    tempElevatedPyramidIntake+= 1;
                                }
                                groundNumberView.setText(valueOf(tempGroundPyramidIntake));
                                elevatedNumberView.setText(valueOf(tempElevatedPyramidIntake));
                            }
                        });
                        ((Button) pyramidHistory.findViewById(R.id.ElevatedMinusButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(tempElevatedPyramidIntake <= 0){
                                }else{
                                    tempElevatedPyramidIntake-= 1;
                                }
                                groundNumberView.setText(valueOf(tempGroundPyramidIntake));
                                elevatedNumberView.setText(valueOf(tempElevatedPyramidIntake));
                            }
                        });
                        ((Button) pyramidHistory.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                tempGroundPyramidIntake = 0;
                                tempElevatedPyramidIntake = 0;
                                pyramidDialog.dismiss();
                            }
                        });
                        ((Button) pyramidHistory.findViewById(R.id.saveButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                numGroundPyramidIntake = tempGroundPyramidIntake;
                                numElevatedPyramidIntake = tempElevatedPyramidIntake;
                                try{
                                    if(DataActivity.activityName.equals("auto")){
                                        DataManager.autoPyramidDataList.put("numGroundPyramidIntakeAuto", tempGroundPyramidIntake);
                                        DataManager.autoPyramidDataList.put("numElevatedPyramidIntakeAuto", tempElevatedPyramidIntake);
                                        DataManager.addZeroTierJsonData("numGroundPyramidIntakeAuto", tempGroundPyramidIntake);
                                        DataManager.addZeroTierJsonData("numElevatedPyramidIntakeAuto", tempElevatedPyramidIntake);
                                    }else if(DataActivity.activityName.equals("tele")){
                                        DataManager.telePyramidDataList.put("numGroundPyramidIntakeTele", tempGroundPyramidIntake);
                                        DataManager.telePyramidDataList.put("numElevatedPyramidIntakeTele", tempElevatedPyramidIntake);
                                        DataManager.addZeroTierJsonData("numGroundPyramidIntakeTele", tempGroundPyramidIntake);
                                        DataManager.addZeroTierJsonData("numElevatedPyramidIntakeTele", tempElevatedPyramidIntake);
                                    }
                                }catch (JSONException je){
                                    je.printStackTrace();
                                }
                                tempGroundPyramidIntake = 0;
                                tempElevatedPyramidIntake = 0;

                                pyramidDialog.dismiss();
                            }
                        });

                        pyramidDialog.show();
                    } else {
                        Toast.makeText(context, "No Entries for "+name, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });

            currentPyramidComponent++;
            super.componentViews.add(pyramidButton);
            return pyramidButton;
        }
    }

    public static class UIEndGameButtonCreator extends UIComponentCreator {
        public boolean didSucceed;
        public boolean didClimb;
        public String liftType;
        public String partnerLiftType;
        public boolean didFailToLift;
        public int numRobotsLifted;
        public float startTime;
        public float endTime;
        private int c;

        private Activity context;

        public UIEndGameButtonCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            this.context = context;
        }

        public Button addButton(boolean climbAdded) {
            c = 0;
            final Button endButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 0.8f);
            final ToggleButton parkButton = getToggleButton(LinearLayout.LayoutParams.MATCH_PARENT, false, 0, false);

            if(!climbAdded){
                //add button to row
                endButton.setText("FTB");
                endButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        numRobotsLifted = 0;
                        startTime = backgroundTimer.getUpdatedTime();

                        final List<String> endKeys = Arrays.asList("didSucceed", "startTime", "endTime");
                        final List<Object> endValues = new ArrayList<>();

                        //display custom dialog with big buttons
                        final Dialog dialog = new Dialog(context);
                        dialog.setCanceledOnTouchOutside(false);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        final LinearLayout dialogLayout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.climb_dialog, null);
                        final TextView title = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                        title.setText("Face The Boss!");

                        Button success = (Button) dialogLayout.findViewById(R.id.successButton);
                        success.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //dialog.dismiss();
                                //climb type dialog
                                endTime = backgroundTimer.getUpdatedTime();
                                didSucceed = true;
                                endValues.add(0, didSucceed);

                                final Dialog climbTypeDialog = new Dialog(context);
                                climbTypeDialog.setCanceledOnTouchOutside(false);
                                climbTypeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                final RelativeLayout ctDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.climb_type, null);
                                final TextView title = (TextView) ctDialogLayout.findViewById(R.id.dialogTitle);
                                title.setText("Climb Type");

                                final RadioButton passiveClimbRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.passiveClimbRadio);
                                passiveClimbRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "passiveClimb";
                                    }
                                });

                                final RadioButton assistedClimbRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.assistedClimbRadio);
                                assistedClimbRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "assistedClimb";
                                    }
                                });

                                final RadioButton activeLiftRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.activeLiftRadio);
                                activeLiftRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "activeLift";
                                    }
                                });

                                final RadioButton independentRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.soloClimbRadio);
                                independentRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "soloClimb";
                                    }
                                });
                                Button doneButton = (Button) ctDialogLayout.findViewById(R.id.doneButton);
                                doneButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        DataManager.sideData = new JSONObject();
                                        //added if
                                        if(!passiveClimbRadioButton.isChecked() && !assistedClimbRadioButton.isChecked() && !activeLiftRadioButton.isChecked() && !independentRadioButton.isChecked()){
                                            Utils.makeToast(context, "Please Input a Climb Type");
                                        }else if (liftType.equals("activeLift")) {
                                            //climbTypeDialog.dismiss();

                                            final List<String> activeKeys = Arrays.asList("didSucceed", "didClimb", "startTime", "endTime", "partnerLiftType", "didFailToLift", "numRobotsLifted");
                                            final List<Object> activeValues = new ArrayList<>();

                                            final Dialog activeLiftDialog = new Dialog(context);
                                            activeLiftDialog.setCanceledOnTouchOutside(false);
                                            activeLiftDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                            final RelativeLayout alDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.active_lift_dialog, null);
                                            final TextView title = (TextView) alDialogLayout.findViewById(R.id.dialogTitle);
                                            title.setText("Partner Lifts");

                                            final CheckBox partnerDidClimbRadioButton = (CheckBox) alDialogLayout.findViewById(R.id.pdidClimbRadio);
                                            partnerDidClimbRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    didClimb = partnerDidClimbRadioButton.isChecked();
                                                }
                                            });

                                            final CheckBox partnerAssistedlyLiftsRadioButton = (CheckBox) alDialogLayout.findViewById(R.id.passistLifts);
                                            final CheckBox partnerPassivelyLiftsRadioButton = (CheckBox) alDialogLayout.findViewById(R.id.ppassiveLifts);
                                            partnerAssistedlyLiftsRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if(partnerAssistedlyLiftsRadioButton.isChecked() &&  partnerPassivelyLiftsRadioButton.isChecked()) { //START
                                                        partnerLiftType = "both";
                                                    }
                                                    if(partnerAssistedlyLiftsRadioButton.isChecked()) {
                                                        partnerLiftType = "assisted";
                                                    }
                                                }
                                            });

                                            partnerPassivelyLiftsRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if(partnerAssistedlyLiftsRadioButton.isChecked() &&  partnerPassivelyLiftsRadioButton.isChecked()){ //START
                                                        partnerLiftType = "both";
                                                    }
                                                    else if(partnerPassivelyLiftsRadioButton.isChecked()){
                                                        partnerLiftType = "passive";
                                                    }
                                                }
                                            });

                                            final CheckBox failedToLiftRadioButton = (CheckBox) alDialogLayout.findViewById(R.id.failedToLift);
                                            failedToLiftRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    didFailToLift = failedToLiftRadioButton.isChecked();
                                                }
                                            });

                                            final TextView numberView = (TextView) alDialogLayout.findViewById(R.id.numberView);

                                            Button minusButton = (Button) alDialogLayout.findViewById(R.id.minusButton);
                                            minusButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (numRobotsLifted > 0) {
                                                        numRobotsLifted -= 1;
                                                    }
                                                    numberView.setText(String.valueOf(numRobotsLifted));
                                                }
                                            });

                                            Button plusButton = (Button) alDialogLayout.findViewById(R.id.plusButton);
                                            plusButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    numRobotsLifted += 1;
                                                    numberView.setText(String.valueOf(numRobotsLifted));
                                                }
                                            });

                                            Button doneButton = (Button) alDialogLayout.findViewById(R.id.doneButton);
                                            doneButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    //added if

                                                    if(partnerDidClimbRadioButton.isChecked() || partnerAssistedlyLiftsRadioButton.isChecked() || partnerPassivelyLiftsRadioButton.isChecked() || failedToLiftRadioButton.isChecked()) {
                                                        DataManager.sideData = new JSONObject();
                                                        activeValues.add(0, didSucceed);
                                                        activeValues.add(1, didClimb);
                                                        activeValues.add(2, startTime);
                                                        activeValues.add(3, endTime);
                                                        activeValues.add(4, partnerLiftType);
                                                        activeValues.add(5, didFailToLift);
                                                        activeValues.add(6, numRobotsLifted);

                                                        DataManager.addOneTierJsonData(true, liftType, activeKeys, activeValues);
                                                        JSONObject tempData = DataManager.sideData;
                                                        try {
                                                            DataManager.climbDataArray.put(c, tempData);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                        DataManager.addZeroTierJsonData("climb", DataManager.climbDataArray);
                                                        c++;
                                                        activeLiftDialog.dismiss();
                                                        climbTypeDialog.dismiss(); //added
                                                        dialog.dismiss(); //added
                                                    }else
                                                        Utils.makeToast(context, "Please Input a Climb Type");

                                                }
                                            });
                                            //added cancel
                                            Button cancel = (Button) alDialogLayout.findViewById(R.id.cancelButton);
                                            cancel.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    activeLiftDialog.dismiss();
                                                }
                                            });

                                            activeLiftDialog.setContentView(alDialogLayout);
                                            activeLiftDialog.show();
                                        } else {
                                            Log.e("TIMEITMETIEMTEITEMI", startTime+"");
                                            Log.e("TIMEITMETIEMTEITEMI", endTime+"");
                                            endValues.add(0, didSucceed);
                                            endValues.add(1, startTime);
                                            endValues.add(2, endTime);
                                            endValues.add(3, partnerLiftType);
                                            endValues.add(4, didFailToLift);
                                            endValues.add(5, numRobotsLifted);

                                            DataManager.addOneTierJsonData(true, liftType, endKeys, endValues);
                                            JSONObject tempData = DataManager.sideData;
                                            try {
                                                DataManager.climbDataArray.put(c, tempData);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            DataManager.addZeroTierJsonData("climb", DataManager.climbDataArray);
                                            c++;
                                            dialog.dismiss(); //added
                                            climbTypeDialog.dismiss(); //added
                                        }
                                    }


                                });
                                //added cancel
                                Button cancel = (Button) ctDialogLayout.findViewById(R.id.cancelButton);
                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        climbTypeDialog.dismiss();
                                    }
                                });

                                climbTypeDialog.setContentView(ctDialogLayout);
                                climbTypeDialog.show();
                            }
                        });

                        Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                        failure.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //dialog.dismiss();
                                //climb type dialog
                                endTime = backgroundTimer.getUpdatedTime();
                                didSucceed = false;
                                endValues.add(0, didSucceed);

                                final Dialog climbTypeDialog = new Dialog(context);
                                climbTypeDialog.setCanceledOnTouchOutside(false);
                                climbTypeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                final RelativeLayout ctDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.climb_type, null);
                                final TextView title = (TextView) ctDialogLayout.findViewById(R.id.dialogTitle);
                                title.setText("Climb Type");

                                final RadioButton passiveClimbRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.passiveClimbRadio);
                                passiveClimbRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "passiveClimb";
                                    }
                                });

                               final RadioButton assistedClimbRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.assistedClimbRadio);
                                assistedClimbRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "assistedClimb";
                                    }
                                });

                                final RadioButton activeLiftRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.activeLiftRadio);
                                activeLiftRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "activeLift";
                                    }
                                });

                               final RadioButton independentRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.soloClimbRadio);
                                independentRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "soloClimb";
                                    }
                                });

                                Button doneButton = (Button) ctDialogLayout.findViewById(R.id.doneButton);
                                doneButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        //added if statement

                                        if(!passiveClimbRadioButton.isChecked() && !assistedClimbRadioButton.isChecked() && !activeLiftRadioButton.isChecked() && !independentRadioButton.isChecked()) {
                                            Utils.makeToast(context, "Please Input a Climb Type");
                                        }
                                        else if (liftType.equals("activeLift")) {
                                            DataManager.sideData = new JSONObject();
                                            //climbTypeDialog.dismiss();

                                            final List<String> activeKeys = Arrays.asList("didSucceed", "didClimb", "startTime", "endTime", "partnerLiftType", "didFailToLift", "numRobotsLifted");
                                            final List<Object> activeValues = new ArrayList<>();

                                            final Dialog activeLiftDialog = new Dialog(context);
                                            activeLiftDialog.setCanceledOnTouchOutside(false);
                                            activeLiftDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                            final RelativeLayout alDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.active_lift_dialog, null);
                                            final TextView title = (TextView) alDialogLayout.findViewById(R.id.dialogTitle);
                                            title.setText("Type of Lifter");

                                            final CheckBox partnerDidClimbRadioButton = (CheckBox) alDialogLayout.findViewById(R.id.pdidClimbRadio);
                                            partnerDidClimbRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    didClimb = partnerDidClimbRadioButton.isChecked();
                                                }
                                            });

                                            final CheckBox partnerAssistedlyLiftsRadioButton = (CheckBox) alDialogLayout.findViewById(R.id.passistLifts);
                                            final CheckBox partnerPassivelyLiftsRadioButton = (CheckBox) alDialogLayout.findViewById(R.id.ppassiveLifts);
                                            partnerAssistedlyLiftsRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if(partnerAssistedlyLiftsRadioButton.isChecked() &&  partnerPassivelyLiftsRadioButton.isChecked()) { //START
                                                        partnerLiftType = "both";
                                                    }
                                                    else if(partnerAssistedlyLiftsRadioButton.isChecked()) {
                                                        partnerLiftType = "assisted";
                                                    }
                                                }
                                            });

                                            partnerPassivelyLiftsRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if(partnerAssistedlyLiftsRadioButton.isChecked() &&  partnerPassivelyLiftsRadioButton.isChecked()) { //START
                                                        partnerLiftType = "both";
                                                    }
                                                    else if(partnerPassivelyLiftsRadioButton.isChecked()){
                                                        partnerLiftType = "passive";
                                                    }
                                                }
                                            });

                                            final CheckBox failedToLiftRadioButton = (CheckBox) alDialogLayout.findViewById(R.id.failedToLift);
                                            failedToLiftRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    didFailToLift = failedToLiftRadioButton.isChecked();
                                                }
                                            });

                                            final TextView numberView = (TextView) alDialogLayout.findViewById(R.id.numberView);

                                            Button minusButton = (Button) alDialogLayout.findViewById(R.id.minusButton);
                                            minusButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    if (numRobotsLifted > 0) {
                                                        numRobotsLifted -= 1;
                                                    }
                                                    numberView.setText(String.valueOf(numRobotsLifted));
                                                }
                                            });

                                            Button plusButton = (Button) alDialogLayout.findViewById(R.id.plusButton);
                                            plusButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    numRobotsLifted += 1;
                                                    numberView.setText(String.valueOf(numRobotsLifted));
                                                }
                                            });

                                            Button doneButton = (Button) alDialogLayout.findViewById(R.id.doneButton);
                                            doneButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    //added if statement and then toast
                                                    if(partnerDidClimbRadioButton.isChecked() || partnerAssistedlyLiftsRadioButton.isChecked() || partnerPassivelyLiftsRadioButton.isChecked() || failedToLiftRadioButton.isChecked()) {
                                                        DataManager.sideData = new JSONObject();
                                                        activeValues.add(0, didSucceed);
                                                        activeValues.add(1, didClimb);
                                                        activeValues.add(2, startTime);
                                                        activeValues.add(3, endTime);
                                                        activeValues.add(4, partnerLiftType);
                                                        activeValues.add(5, didFailToLift);
                                                        activeValues.add(6, numRobotsLifted);

                                                        DataManager.addOneTierJsonData(true, liftType, activeKeys, activeValues);
                                                        JSONObject tempData = DataManager.sideData;
                                                        try {
                                                            DataManager.climbDataArray.put(c, tempData);
                                                        } catch (JSONException e) {
                                                            e.printStackTrace();
                                                        }
                                                        DataManager.addZeroTierJsonData("climb", DataManager.climbDataArray);
                                                        c++;
                                                        activeLiftDialog.dismiss();
                                                        climbTypeDialog.dismiss(); //added
                                                        dialog.dismiss(); //added
                                                    }else  Utils.makeToast(context, "Please Input a Climb Type");

                                                    }

                                            });
                                            //added cancel
                                            Button cancel= (Button) alDialogLayout.findViewById(R.id.cancelButton);
                                            cancel.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    activeLiftDialog.dismiss();
                                                }
                                            });

                                            activeLiftDialog.setContentView(alDialogLayout);
                                            activeLiftDialog.show();
                                        } else {
                                            endValues.add(0, didSucceed);
                                            endValues.add(1, startTime);
                                            endValues.add(2, endTime);
                                            endValues.add(3, partnerLiftType);
                                            endValues.add(4, didFailToLift);
                                            endValues.add(5, numRobotsLifted);

                                            DataManager.addOneTierJsonData(true, liftType, endKeys, endValues);
                                            JSONObject tempData = DataManager.sideData;
                                            try {
                                                DataManager.climbDataArray.put(c, tempData);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            DataManager.addZeroTierJsonData("climb", DataManager.climbDataArray);
                                            c++;
                                            climbTypeDialog.dismiss();
                                            dialog.dismiss();
                                        }
                                    }


                                });
                                //added cancel
                                Button cancel = (Button) ctDialogLayout.findViewById(R.id.cancelButton);
                                cancel.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        climbTypeDialog.dismiss();
                                    }
                                });

                                climbTypeDialog.setContentView(ctDialogLayout);
                                climbTypeDialog.show();
                            }
                        });

                        Button cancel = (Button) dialogLayout.findViewById(R.id.cancelButton);
                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });

                        dialog.setContentView(dialogLayout);
                        dialog.show();
                    }
                });

                return endButton;
            }else if(climbAdded){
                parkButton.setText("Park");
                parkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        parkButton.setText("Park");
                        try {       if(DataManager.collectedData.get("climb") != null){     Utils.makeToast(context, "YOU HAVE A CLIMB ENTRY ALREADY!!!");}; } catch (JSONException e) {     e.printStackTrace(); }
                        DataManager.addZeroTierJsonData("didPark", parkButton.isChecked());
                    }
                });
                return parkButton;
            }

            return null;
        }
    }

    public void resetCurrentComponent(){
        currentComponent = 0;
    }
}
