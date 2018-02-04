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
//        button.setGravity(Gravity.CENTER_HORIZONTAL);
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

        public Button addButton(final String switchFBname, final String colorOfSwitch, final JSONArray jsonArray) {
            name = UISwitchCreator.super.componentNames.get(currentSwitchComponent);

            final Button switchButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            if(colorOfSwitch.equals("red")){    switchButton.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTRED));     switchButton.setText("Red Switch"); }
            else if(colorOfSwitch.equals("blue")){    switchButton.setBackgroundColor(Color.parseColor(Constants.COLOR_LIGHTBLUE));     switchButton.setText("Blue Switch"); }
            switchButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    c = jsonArray.length();
                    status = null;
                    switchSuccess = false;
                    startTime = backgroundTimer.getUpdatedTime();

                    //Create Dialog
                    final Dialog dialog = new Dialog(context);
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
                            successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            RelativeLayout successDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.switch_success_dialog, null);
                            TextView successTitleTV = (TextView) successDialogLayout.findViewById(R.id.dialogTitle);
                            successTitleTV.setText(name);

                            RadioButton ownedRadioButton = (RadioButton) successDialogLayout.findViewById(R.id.switchOwnedRadio);
                            ownedRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    status = "ownedBlue";
                                }
                            });

                            RadioButton balancedRadioButton = (RadioButton) successDialogLayout.findViewById(R.id.switchBalancedRadio);
                            balancedRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    status = "balanced";
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
                                            switchValues.add(null);
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

                                        successDialog.dismiss();
                                    } else {
                                        Toast.makeText(context, "Please put ownership and/or layer", Toast.LENGTH_SHORT).show();
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
                            switchValues.add(null);
                            switchValues.add(null);

                            JSONObject tempData = Utils.returnJSONObject(switchKeys, switchValues);
                            try {
                                jsonArray.put(c, tempData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            DataManager.addZeroTierJsonData(switchFBname, jsonArray);
                            c++;

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
                    status = null;
                    startTime = backgroundTimer.getUpdatedTime();

                    final Dialog dialog = new Dialog(context);
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
                            successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            RelativeLayout successDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.scale_success_dialog, null);
                            TextView successTitleTV = (TextView) successDialogLayout.findViewById(R.id.scaleSuccessDialogTitle);
                            successTitleTV.setText(name);

                            RadioButton ownedRadioButton = (RadioButton) successDialogLayout.findViewById(R.id.scaleOwnedRadio);
                            ownedRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    status = "owned";
                                }
                            });

                            RadioButton balancedRadioButton = (RadioButton) successDialogLayout.findViewById(R.id.scaleBalancedRadio);
                            balancedRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    status = "balanced";
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
                                        List<String> scaleKeys = Arrays.asList("didSucceed", "startTime", "endTime", "status", "layer"); //TODO time stuff
                                        List<Object> scaleValues = new ArrayList<>();
                                        scaleValues.clear();
                                        scaleValues.add(didSucceed);
                                        scaleValues.add(startTime);
                                        scaleValues.add(endTime);
                                        if(status == null){
                                            scaleValues.add(null);
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

                                        successDialog.dismiss();

                                    } else {
                                        Toast.makeText(context, "Please put ownership and/or layer", Toast.LENGTH_SHORT).show();
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

                            List<String> scaleKeys = Arrays.asList("didSucceed", "startTime", "endTime");
                            List<Object> scaleValues = new ArrayList<>();
                            scaleValues.clear();
                            scaleValues.add(didSucceed);
                            scaleValues.add(startTime);
                            scaleValues.add(endTime);

                            JSONObject tempData = Utils.returnJSONObject(scaleKeys, scaleValues);
                            try {
                                jsonArray.put(c, tempData);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            DataManager.addZeroTierJsonData(scaleFBname, jsonArray);
                            c++;

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

            /*scaleButton.setOnLongClickListener(new View.OnLongClickListener() {

                public boolean onLongClick(View v) {
                    if ((DataActivity.saveAutoData && DataActivity.activityName.equals("auto")) || (DataActivity.saveTeleData && DataActivity.activityName.equals("tele"))) {
                        if (DataActivity.activityName.equals("auto")) {
                            DataActivity.saveAutoData = false;
                        } else if (DataActivity.activityName.equals("tele")) {
                            DataActivity.saveTeleData = false;
                        }
                    }

                    int latest = 0;

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

                        scaleDialog.show();
                    } else {
                        Toast.makeText(context, "No Entries for " + name, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });*/
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
            numElevatedPyramidIntake = 0;
            numGroundPyramidIntake = 0;

            name = UIPyramidCreator.super.componentNames.get(currentPyramidComponent);

            final Button pyramidButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            pyramidButton.setText(name);
            pyramidButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.pyramid_dialog, null);
                    TextView titleTV = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    titleTV.setText(name);

                    Button groundButton = (Button) dialogLayout.findViewById(R.id.elevatedButton);
                    groundButton.setText("Ground" + " ("+numGroundPyramidIntake+")");
                    groundButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numGroundPyramidIntake += 1;
                            if (numGroundPyramidIntake != 0 || numElevatedPyramidIntake != 0) {
                                List<String> pyramidKeys = Arrays.asList("numGroundPyramidIntake"+DataActivity.capActivityName, "numElevatedPyramidIntake"+DataActivity.capActivityName);
                                List<Object> pyramidValues = new ArrayList<>();
                                pyramidValues.clear();
                                pyramidValues.add(numGroundPyramidIntake);
                                pyramidValues.add(numElevatedPyramidIntake);

                                for(int i = 0; i < 2; i++){     DataManager.addZeroTierJsonData(pyramidKeys.get(i), pyramidValues.get(i)); }

                                dialog.dismiss();
                            } else {
                                Toast.makeText(context, "Please put pyramid layer", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    Button elevatedButton = (Button) dialogLayout.findViewById(R.id.groundButton);
                    elevatedButton.setText("Elevated" + " ("+numElevatedPyramidIntake+")");
                    elevatedButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numElevatedPyramidIntake += 1;
                            if (numGroundPyramidIntake != 0 || numElevatedPyramidIntake != 0) {
                                List<String> pyramidKeys = Arrays.asList("numGroundPyramidIntake"+DataActivity.capActivityName, "numElevatedPyramidIntake"+DataActivity.capActivityName);
                                List<Object> pyramidValues = new ArrayList<>();
                                pyramidValues.clear();
                                pyramidValues.add(numGroundPyramidIntake);
                                pyramidValues.add(numElevatedPyramidIntake);

                                for(int i = 0; i < 2; i++){     DataManager.addZeroTierJsonData(pyramidKeys.get(i), pyramidValues.get(i)); }

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
            /*shotButton.setOnLongClickListener(new View.OnLongClickListener() {

                public boolean onLongClick(View v){
                    if((DataActivity.saveAutoData && DataActivity.activityName.equals("auto")) || (DataActivity.saveTeleData && DataActivity.activityName.equals("tele"))){
                        if(DataActivity.activityName.equals("auto")){
                            DataActivity.saveAutoData = false;
                        }else if(DataActivity.activityName.equals("tele")){
                            DataActivity.saveTeleData = false;
                        }
                    }

                    int latest = 0;

                    if(latest > 0){
                        //View shotsHistory = ((LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE)).inflate(R.layout.shots_history_dialog, null);
                        //ListView shotList = (ListView) shotsHistory.findViewById(R.id.shotsListView);

                        AlertDialog.Builder pyramidBuilder = new AlertDialog.Builder(context);
                        pyramidBuilder.setView(shotsHistory);
                        pyramidBuilder.setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        pyramidBuilder.setTitle(name);
                        pyramidBuilder.setCancelable(false);
                        AlertDialog pyramidDialog = pyramidBuilder.create();

                        pyramidDialog.show();
                    } else {
                        Toast.makeText(context, "No Entries for "+name, Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            }*/

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
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        final LinearLayout dialogLayout = (LinearLayout) context.getLayoutInflater().inflate(R.layout.climb_dialog, null);
                        final TextView title = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                        title.setText("Face The Boss!");

                        Button success = (Button) dialogLayout.findViewById(R.id.successButton);
                        success.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                                //climb type dialog
                                endTime = backgroundTimer.getUpdatedTime();
                                didSucceed = true;
                                endValues.add(0, didSucceed);

                                final Dialog climbTypeDialog = new Dialog(context);
                                climbTypeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                final RelativeLayout ctDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.climb_type, null);
                                final TextView title = (TextView) ctDialogLayout.findViewById(R.id.dialogTitle);
                                title.setText("Climb Type");

                                RadioButton passiveClimbRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.passiveClimbRadio);
                                passiveClimbRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "passiveClimb";
                                    }
                                });

                                RadioButton assistedClimbRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.assistedClimbRadio);
                                assistedClimbRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "assistedClimb";
                                    }
                                });

                                RadioButton activeLiftRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.activeLiftRadio);
                                activeLiftRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "activeLift";
                                    }
                                });

                                RadioButton independentRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.soloClimbRadio);
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
                                        if (liftType.equals("activeLift")) {
                                            climbTypeDialog.dismiss();

                                            final List<String> activeKeys = Arrays.asList("didSucceed", "didClimb", "startTime", "endTime", "partnerLiftType", "didFailToLift", "numRobotsLifted");
                                            final List<Object> activeValues = new ArrayList<>();

                                            final Dialog activeLiftDialog = new Dialog(context);
                                            activeLiftDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                            final RelativeLayout alDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.active_lift_dialog, null);
                                            final TextView title = (TextView) alDialogLayout.findViewById(R.id.dialogTitle);
                                            title.setText("Partner Lifts");

                                            RadioButton partnerDidClimbRadioButton = (RadioButton) alDialogLayout.findViewById(R.id.pdidClimbRadio);
                                            partnerDidClimbRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    didClimb = true;
                                                }
                                            });

                                            RadioButton partnerAssistedlyLiftsRadioButton = (RadioButton) alDialogLayout.findViewById(R.id.passistLifts);
                                            partnerAssistedlyLiftsRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    partnerLiftType = "assisted";
                                                }
                                            });

                                            RadioButton partnerPassivelyLiftsRadioButton = (RadioButton) alDialogLayout.findViewById(R.id.ppassiveLifts);
                                            partnerPassivelyLiftsRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    partnerLiftType = "passive";
                                                }
                                            });

                                            RadioButton failedToLiftRadioButton = (RadioButton) alDialogLayout.findViewById(R.id.failedToLift);
                                            failedToLiftRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    didFailToLift = true;
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
                                            climbTypeDialog.dismiss();
                                        }
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
                                dialog.dismiss();
                                //climb type dialog
                                endTime = backgroundTimer.getUpdatedTime();
                                didSucceed = false;
                                endValues.add(0, didSucceed);

                                final Dialog climbTypeDialog = new Dialog(context);
                                climbTypeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                final RelativeLayout ctDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.climb_type, null);
                                final TextView title = (TextView) ctDialogLayout.findViewById(R.id.dialogTitle);
                                title.setText("Climb Type");

                                RadioButton passiveClimbRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.passiveClimbRadio);
                                passiveClimbRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "passiveClimb";
                                    }
                                });

                                RadioButton assistedClimbRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.assistedClimbRadio);
                                assistedClimbRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "assistedClimb";
                                    }
                                });

                                RadioButton activeLiftRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.activeLiftRadio);
                                activeLiftRadioButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        liftType = "activeLift";
                                    }
                                });

                                RadioButton independentRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.soloClimbRadio);
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
                                        if (liftType.equals("activeLift")) {
                                            climbTypeDialog.dismiss();

                                            final List<String> activeKeys = Arrays.asList("didSucceed", "didClimb", "startTime", "endTime", "partnerLiftType", "didFailToLift", "numRobotsLifted");
                                            final List<Object> activeValues = new ArrayList<>();

                                            final Dialog activeLiftDialog = new Dialog(context);
                                            activeLiftDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                            final RelativeLayout alDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.active_lift_dialog, null);
                                            final TextView title = (TextView) alDialogLayout.findViewById(R.id.dialogTitle);
                                            title.setText("Partner Lifts");

                                            RadioButton partnerDidClimbRadioButton = (RadioButton) alDialogLayout.findViewById(R.id.pdidClimbRadio);
                                            partnerDidClimbRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    didClimb = true;
                                                }
                                            });

                                            RadioButton partnerAssistedlyLiftsRadioButton = (RadioButton) alDialogLayout.findViewById(R.id.passistLifts);
                                            partnerAssistedlyLiftsRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    partnerLiftType = "assisted";
                                                }
                                            });

                                            RadioButton partnerPassivelyLiftsRadioButton = (RadioButton) alDialogLayout.findViewById(R.id.ppassiveLifts);
                                            partnerPassivelyLiftsRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    partnerLiftType = "passive";
                                                }
                                            });

                                            RadioButton failedToLiftRadioButton = (RadioButton) alDialogLayout.findViewById(R.id.failedToLift);
                                            failedToLiftRadioButton.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    didFailToLift = true;
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
                                        }
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
