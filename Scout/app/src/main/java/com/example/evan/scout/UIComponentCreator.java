package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

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


    public View getNextTitleRow(int titles) {
        LinearLayout row = new LinearLayout(context);
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int j = 0; j < titles; j++) {
            TextView textView = new TextView(context);
            textView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            textView.setGravity(Gravity.CENTER);
            textView.setText(componentNames.get(currentComponent));
            currentComponent++;
            row.addView(textView);
        }
        return row;
    }


    public View getNextCounterRow(int counters, int value) {
        LinearLayout row = new LinearLayout(context);
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        row.setOrientation(LinearLayout.HORIZONTAL);
        for (int j = 0; j < counters; j++) {
            final TextView currentCount = new TextView(context);
            currentCount.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            currentCount.setText(Integer.toString(value));
            currentCount.setGravity(Gravity.CENTER);
            componentViews.add(currentCount);


            Button minus = new Button(context);
            minus.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            minus.setText("-");
            minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int prevNum = Integer.parseInt(currentCount.getText().toString());
                    if (prevNum > 0) {
                        prevNum--;
                    }
                    currentCount.setText(Integer.toString(prevNum));
                }
            });



            final Button plus = new Button(context);
            plus.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
            plus.setText("+");
            plus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int prevNum = Integer.parseInt(currentCount.getText().toString());
                    prevNum++;
                    currentCount.setText(Integer.toString(prevNum));
                }
            });
            row.addView(minus);
            row.addView(currentCount);
            row.addView(plus);
        }
        return row;
    }


    public List<View> getComponentViews() {
        return componentViews;
    }


    public ToggleButton getNextToggleButton (int width, boolean value) {
        ToggleButton toggleButton = new ToggleButton(context);
        toggleButton.setLayoutParams(new LinearLayout.LayoutParams(width, ViewGroup.LayoutParams.WRAP_CONTENT, 0.6f));
        toggleButton.setText(componentNames.get(currentComponent));
        toggleButton.setTextOn(componentNames.get(currentComponent));
        toggleButton.setTextOff(componentNames.get(currentComponent));
        toggleButton.setTextSize(toggleButton.getTextSize() * 0.4f);
        toggleButton.setChecked(value);
        currentComponent++;
        componentViews.add(toggleButton);
        return toggleButton;
    }

    public Button getNextDefenseButton() {
        Button button = new Button(context);
        button.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        button.setText(componentNames.get(currentComponent));
        button.setTextSize(button.getTextSize() * 0.4f);
        currentComponent++;
        return button;
    }




    //sub class specifically for creating defense buttons
    public static class UIButtonCreator extends UIComponentCreator {
        //this is to indicate whether the last time entered was a success or fail
        private List<Boolean> lastSuccessOrFail;
        private Activity context;
        public UIButtonCreator(Activity context, List<String> componentNames) {
            super(context, componentNames);
            this.context = context;
            lastSuccessOrFail = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                lastSuccessOrFail.add(i, null);
            }
        }



        public void addButtonRow(LinearLayout column, final List<List<Long>> successTimes, final List<List<Long>> failTimes, int index) {
            //add textview counters to layout
            LinearLayout textViewLayout = new LinearLayout(context);
            textViewLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.25f));
            textViewLayout.setOrientation(LinearLayout.HORIZONTAL);
            final TextView successText = new TextView(context);
            successText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
            successText.setGravity(Gravity.CENTER);
            successText.setText("S: " + Integer.toString(successTimes.get(index).size()));
            final TextView failText = new TextView(context);
            failText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.5f));
            failText.setGravity(Gravity.CENTER);
            failText.setText("F: " + Integer.toString(failTimes.get(index).size()));
            textViewLayout.addView(successText);
            textViewLayout.addView(failText);


            //add button to row
            Button defenseButton = getNextDefenseButton();
            defenseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //first find out what button was clicked, based on the name of the button
                    final int buttonNum = Integer.parseInt(((Button) v).getText().toString().replaceAll("Defense ", "")) - 1;
                    //next get the time in milliseconds
                    final Long startTime = Calendar.getInstance().getTimeInMillis();


                    //display custom dialog with big buttons
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.dialog, null);
                    TextView title = (TextView) dialogLayout.findViewById(R.id.dialogTitle);
                    title.setText("Attempt Defense " + Integer.toString(buttonNum + 1));
                    Button success = (Button) dialogLayout.findViewById(R.id.successButton);
                    success.getBackground().setColorFilter(Color.parseColor("#C8FFC8"), PorterDuff.Mode.MULTIPLY);
                    success.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            lastSuccessOrFail.add(buttonNum, true);
                            //add time
                            successTimes.get(buttonNum).add(Calendar.getInstance().getTimeInMillis() - startTime);
                            //increment counter
                            successText.setText("S: " + successTimes.get(buttonNum).size());
                            //dismiss dialog
                            dialog.dismiss();
                        }
                    });
                    Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                    failure.getBackground().setColorFilter(Color.parseColor("#FFC8C8"), PorterDuff.Mode.MULTIPLY);
                    failure.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Long time = Calendar.getInstance().getTimeInMillis() - startTime;
                            lastSuccessOrFail.add(buttonNum, false);
                            //add time
                            failTimes.get(buttonNum).add(time);
                            //increment counter
                            failText.setText("F: " + failTimes.get(buttonNum).size());
                            //dismiss dialog
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
            //if they hold the button, give them a dialog to undo the last action for that button
            defenseButton.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final int buttonNum = Integer.parseInt(((Button) v).getText().toString().replaceAll("Defense ", "")) - 1;
                    new AlertDialog.Builder(context)
                            .setTitle("Undo Defense Attempt")
                            .setMessage("Are you sure you want to undo the last defense attempt for this button?")
                            .setNegativeButton("Cancel", null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    //if they click yes, redo the last change
                                    if (lastSuccessOrFail.get(buttonNum) != null) {
                                        try {
                                            if (lastSuccessOrFail.get(buttonNum)) {
                                                successTimes.get(buttonNum).remove(successTimes.get(buttonNum).size() - 1);
                                                successText.setText("S: " + successTimes.get(buttonNum).size());
                                                lastSuccessOrFail.add(buttonNum, null);
                                            } else {
                                                failTimes.get(buttonNum).remove(failTimes.get(buttonNum).size() - 1);
                                                failText.setText("F: " + failTimes.get(buttonNum).size());
                                                lastSuccessOrFail.add(buttonNum, null);
                                            }
                                        } catch (IndexOutOfBoundsException ioobe) {
                                            Log.e("User Error", "Scout tried to undo action that didn't exist. Unimportant");
                                        }
                                    }
                                }
                            })
                            .show();
                    return true;
                }
            });



            column.addView(defenseButton);
            column.addView(textViewLayout);
        }
    }
}
