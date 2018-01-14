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

    public Button getBasicButton(int width, Float textScale) {
        Button button = new Button(context);
        button.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        if (componentNames != null) {
            button.setText(componentNames.get(currentComponent));
        }
        button.setTextSize(button.getTextSize() * textScale);
        return button;
    }

    public ToggleButton getToggleButton(int width, boolean value) {
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
                        if (DataManager.collectedData.getBoolean("didLiftoff") != true) {
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
                                    Log.e("scrub", liftoffTime + "");

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
                        } else if (DataManager.collectedData.getBoolean("didLiftoff") == true) {
                            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                            View liftOffRemoveView = layoutInflater.inflate(R.layout.dialog, null);
                            try {
                                if (DataActivity.saveTeleData && DataActivity.activityName.equals("tele")) {
                                    ((TextView) liftOffRemoveView.findViewById(R.id.liftoffTime)).setText(DataManager.collectedData.getDouble("liftoffTime") + "");
                                } else {
                                    ((TextView) liftOffRemoveView.findViewById(R.id.liftoffTime)).setText(String.valueOf(liftoffTime));
                                }
                            } catch (NullPointerException npe) {
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

    public static class UIScaleCreator extends UIComponentCreator {
        private String status;
        private int layer;
        private long startTime; //TODO time stuff
        private long endTime;
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
                    startTime = System.currentTimeMillis();
                    final HashMap<String, Object> dataSpace = new HashMap<String, Object>();

                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.scale_dialog, null);
                    TextView titleTV = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    titleTV.setText(name);

                    Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            endTime = System.currentTimeMillis(); //TODO time stuff
                            didSucceed = false;

                            int i = 0;
                            List<String> scaleKeys = Arrays.asList("didSucceed", "startTime", "endTime"); //TODO time stuff
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

                    Button cancel = (Button) dialogLayout.findViewById(R.id.cancelButton);
                    cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    Button success = (Button) dialogLayout.findViewById(R.id.successButton);
                    success.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            endTime = System.currentTimeMillis(); //TODO time stuff
                            didSucceed = true;

                            dialog.dismiss();

                            final Dialog successDialog = new Dialog(context);
                            successDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            RelativeLayout successDialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.scale_success_dialog, null);
                            TextView successTitleTV = (TextView) successDialogLayout.findViewById(R.id.dialogTitle);
                            successTitleTV.setText(name);

                            RadioButton ownedBlueRadioButton = (RadioButton) successDialogLayout.findViewById(R.id.ownedBlueRadio);
                            ownedBlueRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    status = "ownedBlue";
                                }
                            });

                            RadioButton ownedRedRadioButton = (RadioButton) successDialogLayout.findViewById(R.id.ownedRedRadio);
                            ownedRedRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    status = "ownedRed";
                                }
                            });

                            RadioButton balancedRadioButton = (RadioButton) successDialogLayout.findViewById(R.id.balancedRadio);
                            balancedRadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    status = "balanced";
                                }
                            });

                            RadioButton layer1RadioButton = (RadioButton) successDialogLayout.findViewById(R.id.layer1Radio);
                            layer1RadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    layer = 1;
                                }
                            });

                            RadioButton layer2RadioButton = (RadioButton) successDialogLayout.findViewById(R.id.layer2Radio);
                            layer2RadioButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    layer = 2;
                                }
                            });

                            RadioButton layer3RadioButton = (RadioButton) successDialogLayout.findViewById(R.id.layer3Radio);
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
        public static class UIPyramidCreator extends UIComponentCreator {

            private int numGroundPyramidIntake;
            private int numElevatedPyramidIntake;
            //private int layer;
            private long startTime;
            private long endTime;
            private long totalTime;
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
                        numElevatedPyramidIntake= 0;
                        numGroundPyramidIntake = 0;
                        final HashMap<String,Object> dataSpace = new HashMap<String, Object>();

                        final Dialog dialog = new Dialog(context);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.pyramid_dialog, null);
                        TextView titleTV = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                        titleTV.setText(name);

                        //final TextView numberView = (TextView) dialogLayout.findViewById(R.id.numberView);


                        //Button groundIntake = (Button) dialogLayout.findViewById(R.id.successButton);
                        //groundIntake.setOnClickListener(new View.OnClickListener() {
                        //@Override
                        //public void onClick(View v) {



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



                        if(numGroundPyramidIntake != 0 && numElevatedPyramidIntake!= 0){
                            endTime = System.currentTimeMillis();
                            totalTime = endTime - startTime;

                            int i = 0;
                            List<String> pyramidKeys = Arrays.asList("numGroundPyramidIntake", "numElevatedPyramidIntake" /*"time"*/);
                            List<Object> pyramidValues = new ArrayList<>();
                            pyramidValues.clear();
                            //pyramidValues.add();
                            pyramidValues.add(numElevatedPyramidIntake);
                            pyramidValues.add(numGroundPyramidIntake);
                            //pyramidValues.add(totalTime/1000);

                            dataSpace.put(pyramidKeys.get(0), pyramidValues.get(0));
                            dataSpace.put(pyramidKeys.get(1), pyramidValues.get(1));
                            dataSpace.put(pyramidKeys.get(2), pyramidValues.get(2));

                            if(DataManager.collectedData.has(pyramidFBname)){
                                try {
                                    DataManager.sideData = DataManager.collectedData.getJSONObject(pyramidFBname);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                DataManager.addOneTierJsonData(true, i+"", pyramidKeys, pyramidValues);
                                DataManager.addZeroTierJsonData(pyramidFBname,DataManager.sideData);
                            }else{
                                DataManager.sideData = new JSONObject();
                                DataManager.addOneTierJsonData(true, i+"", pyramidKeys, pyramidValues);
                                DataManager.addZeroTierJsonData(pyramidFBname,DataManager.sideData);
                            }



                            dialog.dismiss();
                        }else{
                            Toast.makeText(context, "Please put pyramid layer", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                    /*Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.dismiss();
                }
            });*/
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
                ;
                currentPyramidComponent++;
                super.componentViews.add(pyramidButton);
                return pyramidButton;
            }
        }
    }
}