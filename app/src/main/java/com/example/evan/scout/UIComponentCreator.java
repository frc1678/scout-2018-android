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
                            failure.getBackground().setColorFilter(Color.parseColor("#FFC8C7"), PorterDuff.Mode.MULTIPLY);
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