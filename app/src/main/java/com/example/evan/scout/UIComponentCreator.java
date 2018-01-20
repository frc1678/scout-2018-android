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
import android.widget.RadioGroup;
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

    public Button getBasicButton(int width, Float textScale) {
        Button button = new Button(context);
        button.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (componentNames != null) {
            button.setText(componentNames.get(currentComponent));
        }
        button.setTextSize(button.getTextSize() * textScale);
        return button;
    }

    public ToggleButton getToggleButton(int width, boolean value, int color) {
        ToggleButton toggleButton = new ToggleButton(context);
        toggleButton.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT, 0.6f));
        toggleButton.setText(componentNames.get(currentComponent));
        toggleButton.setTextOn(componentNames.get(currentComponent));
        toggleButton.setTextOff(componentNames.get(currentComponent));
        toggleButton.setTextSize(toggleButton.getTextSize() * 1f);
        toggleButton.setBackgroundColor(color);
        toggleButton.setChecked(value);
        currentComponent++;
        componentViews.add(toggleButton);
        return toggleButton;
    }

    public RadioButton getRadioButton (String radioFBName, String radioFBValue,int width){
        RadioButton radioButton = new RadioButton(context);
        radioButton.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT, 0.6f));
        radioButton.setTextSize(radioButton.getTextSize() * 1f);
        //radioButton.setClickable(true);
        //radioButton.setChecked(false);
        currentComponent++;
        radioViews.add(radioButton);
        return radioButton;
    }

    private View.OnClickListener radioListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DataManager.addZeroTierJsonData("radioFBName", "radioFBValue" );
        }
    };
    public List<View> getComponentViews() {
        return componentViews;
    }
    public List<RadioButton> getRadioViews() {
        return radioViews;
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

        public Button addButton(final String switchFBname, final String colorOfSwitch) {
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
                    titleTV.setText(colorOfSwitch + " Switch Attempt");

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
                            RelativeLayout successDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.scale_success_dialog, null);
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

                            Button done = (Button) successDialog.findViewById(R.id.doneButton);
                            done.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (status != null && layer != 0) {
                                        int i = 0;
                                        List<String> scaleKeys = Arrays.asList("didSucceed", "startTime", "endTime", "status", "layer"); //TODO time stuff
                                        List<Object> scaleValues = new ArrayList<>();
                                        scaleValues.clear();
                                        scaleValues.add(switchSuccess);
                                        scaleValues.add(startTime);
                                        scaleValues.add(endTime);
                                        scaleValues.add(status);
                                        scaleValues.add(layer);

                                        if (DataManager.collectedData.has(switchFBname)) {
                                            try {
                                                DataManager.sideData = DataManager.collectedData.getJSONObject(switchFBname);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            DataManager.addOneTierJsonData(true, i + "", scaleKeys, scaleValues);
                                            DataManager.addZeroTierJsonData(switchFBname, DataManager.sideData);
                                        } else {
                                            DataManager.sideData = new JSONObject();
                                            DataManager.addOneTierJsonData(true, i + "", scaleKeys, scaleValues);
                                            DataManager.addZeroTierJsonData(switchFBname, DataManager.sideData);
                                        }

                                        successDialog.dismiss();

                                    } else {
                                        Toast.makeText(context, "Please put ownership and/or layer", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

                    Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //calculate time
                            endTime = backgroundTimer.getUpdatedTime();
                            switchSuccess = false;

                            int i = 0;
                            List<String> switchKeys = Arrays.asList("didSucceed", "startTime", "endTime", "status", "layer");
                            List<Object> switchValues = new ArrayList<>();
                            switchValues.clear();
                            switchValues.add(switchSuccess);
                            switchValues.add(startTime);
                            switchValues.add(endTime);
                            switchValues.add(null);
                            switchValues.add(null);

                            if (DataManager.collectedData.has(switchFBname)) {
                                try {
                                    DataManager.sideData = DataManager.collectedData.getJSONObject(switchFBname);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                DataManager.addOneTierJsonData(true, i + "", switchKeys, switchValues);
                                DataManager.addZeroTierJsonData(switchFBname, DataManager.sideData);
                            } else {
                                DataManager.sideData = new JSONObject();
                                DataManager.addOneTierJsonData(true, i + "", switchKeys, switchValues);
                                DataManager.addZeroTierJsonData(switchFBname, DataManager.sideData);
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

    public static class UIScaleCreator extends UIComponentCreator {
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
            name = "Scale Attempt";
            layer = 0;
        }

        public Button addButton(final String scaleFBname) {

            final Button scaleButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            scaleButton.setText(name);
            scaleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                            endTime = System.currentTimeMillis(); //TODO time stuff
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

                            Button cancel = (Button) successDialogLayout.findViewById(R.id.scaleCancelButton);
                            cancel.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    successDialog.dismiss();
                                }
                            });

                            Button done = (Button) successDialog.findViewById(R.id.scaleDoneButton);
                            done.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if (status != null && layer != 0) {
                                        int i = 0;
                                        List<String> scaleKeys = Arrays.asList("didSucceed", "startTime", "endTime", "status", "layer"); //TODO time stuff
                                        List<Object> scaleValues = new ArrayList<>();
                                        scaleValues.clear();
                                        scaleValues.add(didSucceed);
                                        scaleValues.add(startTime);
                                        scaleValues.add(endTime);
                                        scaleValues.add(status);
                                        scaleValues.add(layer);

                                        if (DataManager.collectedData.has(scaleFBname)) {
                                            try {
                                                DataManager.sideData = DataManager.collectedData.getJSONObject(scaleFBname);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            DataManager.addOneTierJsonData(true, i + "", scaleKeys, scaleValues);
                                            DataManager.addZeroTierJsonData(scaleFBname, DataManager.sideData);
                                        } else {
                                            DataManager.sideData = new JSONObject();
                                            DataManager.addOneTierJsonData(true, i + "", scaleKeys, scaleValues);
                                            DataManager.addZeroTierJsonData(scaleFBname, DataManager.sideData);
                                        }

                                        successDialog.dismiss();

                                    } else {
                                        Toast.makeText(context, "Please put ownership and/or layer", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    });

                    Button failure = (Button) dialogLayout.findViewById(R.id.scaleFailButton);
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            endTime = backgroundTimer.getUpdatedTime();
                            didSucceed = false;

                            int i = 0;
                            List<String> scaleKeys = Arrays.asList("didSucceed", "startTime", "endTime");
                            List<Object> scaleValues = new ArrayList<>();
                            scaleValues.clear();
                            scaleValues.add(didSucceed);
                            scaleValues.add(startTime);
                            scaleValues.add(endTime);

                            if (DataManager.collectedData.has(scaleFBname)) {
                                try {
                                    DataManager.sideData = DataManager.collectedData.getJSONObject(scaleFBname);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                DataManager.addOneTierJsonData(true, i + "", scaleKeys, scaleValues);
                                DataManager.addZeroTierJsonData(scaleFBname, DataManager.sideData);
                            } else {
                                DataManager.sideData = new JSONObject();
                                DataManager.addOneTierJsonData(true, i + "", scaleKeys, scaleValues);
                                DataManager.addZeroTierJsonData(scaleFBname, DataManager.sideData);
                            }
                            dialog.dismiss();
                        }
                    });

                    Button cancel = (Button) dialogLayout.findViewById(R.id.scaleCancelButton);
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

        public Button addButton(final String pyramidFBname) {
            name = UIPyramidCreator.super.componentNames.get(currentPyramidComponent);

            final Button pyramidButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            pyramidButton.setText(name);
            pyramidButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    numElevatedPyramidIntake = 0;
                    numGroundPyramidIntake = 0;

                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.pyramid_dialog, null);
                    TextView titleTV = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    titleTV.setText(name);

                    Button groundButton = (Button) dialogLayout.findViewById(R.id.elevatedButton);
                    groundButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numElevatedPyramidIntake += 1;
                        }
                    });

                    Button elevatedButton = (Button) dialogLayout.findViewById(R.id.groundButton);
                    elevatedButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            numGroundPyramidIntake += 1;
                        }
                    });
                    Button cancelButton = (Button) dialogLayout.findViewById(R.id.cancelButton);
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    if (numGroundPyramidIntake != 0 && numElevatedPyramidIntake != 0) {
                        int i = 0;
                        List<String> pyramidKeys = Arrays.asList("numGroundPyramidIntake", "numElevatedPyramidIntake");
                        List<Object> pyramidValues = new ArrayList<>();
                        pyramidValues.clear();
                        pyramidValues.add(numElevatedPyramidIntake);
                        pyramidValues.add(numGroundPyramidIntake);

                        if (DataManager.collectedData.has(pyramidFBname)) {
                            try {
                                DataManager.sideData = DataManager.collectedData.getJSONObject(pyramidFBname);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            DataManager.addOneTierJsonData(true, i + "", pyramidKeys, pyramidValues);
                            DataManager.addZeroTierJsonData(pyramidFBname, DataManager.sideData);
                        } else {
                            DataManager.sideData = new JSONObject();
                            DataManager.addOneTierJsonData(true, i + "", pyramidKeys, pyramidValues);
                            DataManager.addZeroTierJsonData(pyramidFBname, DataManager.sideData);
                        }

                        dialog.dismiss();
                    } else {
                        Toast.makeText(context, "Please put pyramid layer", Toast.LENGTH_SHORT).show();
                    }
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
        private Activity context;

        public UIEndGameButtonCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            this.context = context;
        }

        public Button addButton(final ToggleButton button1) {
            //add button to row
            final Button climbButton = getBasicButton(LinearLayout.LayoutParams.MATCH_PARENT, 1f);
            climbButton.setText("Face The Boss");
            climbButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startTime = backgroundTimer.getUpdatedTime();
                    button1.setChecked(false);

                    final List<String> endKeys = Arrays.asList("didSucceed", "startTime", "endTime");
                    final List<Object> endValues = new ArrayList<>();

                    //display custom dialog with big buttons
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    final RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.climb_dialog, null);
                    final TextView title = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    title.setText("Face The Boss!");

                    Button success = (Button) dialogLayout.findViewById(R.id.successButton);
                    success.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //climb type dialog
                            endTime = backgroundTimer.getUpdatedTime();
                            didSucceed = true;
                            endValues.add(0, didSucceed);

                            final Dialog climbTypeDialog = new Dialog(context);
                            climbTypeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            final RelativeLayout ctDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.climb_type, null);
                            final TextView title = (TextView) ctDialogLayout.findViewById(R.id.dialogTitle);
                            title.setText("Climb Type");

                            RadioButton passiveLiftRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.passiveLiftRadio);
                            passiveLiftRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    liftType = "passiveLift";

                                    DataManager.sideData = new JSONObject();
                                    DataManager.addOneTierJsonData(true, liftType, endKeys, endValues);
                                    DataManager.addZeroTierJsonData("climb", DataManager.sideData);
                                }
                            });

                            RadioButton assistedLiftRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.assistLiftRadio);
                            assistedLiftRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    liftType = "assistLift";

                                    DataManager.sideData = new JSONObject();
                                    DataManager.addOneTierJsonData(true, liftType, endKeys, endValues);
                                    DataManager.addZeroTierJsonData("climb", DataManager.sideData);
                                }
                            });

                            RadioButton activeLiftRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.activeLiftRadio);
                            activeLiftRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    liftType = "activeLift";
                                }
                            });

                            RadioButton independentRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.climbRadio);
                            independentRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    liftType = "climb";

                                    DataManager.sideData = new JSONObject();
                                    DataManager.addOneTierJsonData(true, liftType, endKeys, endValues);
                                    DataManager.addZeroTierJsonData("climb", DataManager.sideData);
                                }
                            });
                            Button doneButton = (Button) dialogLayout.findViewById(R.id.doneButton);
                            doneButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    DataManager.sideData = new JSONObject();
                                    if (liftType.equals("activeLift")) {

                                        final List<String> activeKeys = Arrays.asList("didSucceed", "startTime", "endTime", "partnerLiftType", "didFailToLift", "numRobotsLifted");
                                        final List<Object> activeValues = new ArrayList<>();

                                        final Dialog activeLiftDialog = new Dialog(context);
                                        activeLiftDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                                        final RelativeLayout ctDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.climb_type, null);
                                        final TextView title = (TextView) ctDialogLayout.findViewById(R.id.dialogTitle);
                                        title.setText("Partner Lifts");

                                        RadioButton partnerDidClimbRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.pdidClimbRadio);
                                        partnerDidClimbRadioButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                didClimb = true;
                                            }
                                        });

                                        RadioButton partnerAssistedlyLiftsRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.passistLifts);
                                        partnerAssistedlyLiftsRadioButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                partnerLiftType = "assistLift";
                                            }
                                        });

                                        RadioButton partnerPassivelyLiftsRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.ppassiveLifts);
                                        partnerPassivelyLiftsRadioButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                partnerLiftType = "passiveLift";
                                            }
                                        });

                                        RadioButton failedToLiftRadioButton = (RadioButton) ctDialogLayout.findViewById(R.id.failedToLift);
                                        failedToLiftRadioButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                didFailToLift = true;
                                            }
                                        });

                                        final TextView numberView = (TextView) dialogLayout.findViewById(R.id.numberView);

                                        Button minusButton = (Button) dialogLayout.findViewById(R.id.minusButton);
                                        minusButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (numRobotsLifted > 0) {
                                                    numRobotsLifted -= 1;
                                                }
                                                numberView.setText(String.valueOf(numRobotsLifted));
                                            }
                                        });

                                        Button plusButton = (Button) dialogLayout.findViewById(R.id.plusButton);
                                        plusButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                numRobotsLifted += 1;
                                                numberView.setText(String.valueOf(numRobotsLifted));
                                            }
                                        });

                                        Button doneButton = (Button) dialogLayout.findViewById(R.id.doneButton);
                                        doneButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                DataManager.sideData = new JSONObject();
                                                endValues.add(0, didSucceed);
                                                endValues.add(1, startTime);
                                                endValues.add(2, endTime);
                                                endValues.add(3, partnerLiftType);
                                                endValues.add(4, didFailToLift);
                                                endValues.add(5, numRobotsLifted);

                                                DataManager.addOneTierJsonData(true, liftType, endKeys, endValues);
                                                DataManager.addZeroTierJsonData("climb", DataManager.sideData);
                                                activeLiftDialog.dismiss();
                                            }
                                        });

                                    } else {
                                        endValues.add(0, didSucceed);
                                        endValues.add(1, startTime);
                                        endValues.add(2, endTime);
                                        endValues.add(3, partnerLiftType);
                                        endValues.add(4, didFailToLift);
                                        endValues.add(5, numRobotsLifted);

                                        DataManager.addOneTierJsonData(true, liftType, endKeys, endValues);
                                        DataManager.addZeroTierJsonData("climb", DataManager.sideData);
                                        climbTypeDialog.dismiss();
                                    }
                                }


                            });

                            Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                            failure.getBackground().setColorFilter(Color.parseColor("#FFC8C8"), PorterDuff.Mode.MULTIPLY);
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
                }
            });
            return climbButton;
        }
    }
}
