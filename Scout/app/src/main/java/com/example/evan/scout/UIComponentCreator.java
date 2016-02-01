package com.example.evan.scout;

import android.app.Activity;
import android.app.Dialog;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
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

    public LinearLayout getButtonRow(final List<List<Long>> successTimes, final List<List<Long>> failTimes, int index) {
        //add textview counters to layout
        LinearLayout textViewLayout = new LinearLayout(context);
        textViewLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.25f));
        textViewLayout.setOrientation(LinearLayout.VERTICAL);
        final TextView successText = new TextView(context);
        successText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        successText.setText("S: " + Integer.toString(successTimes.get(index).size()));
        final TextView failText = new TextView(context);
        failText.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        failText.setText("F: " + Integer.toString(failTimes.get(index).size()));
        textViewLayout.addView(successText);
        textViewLayout.addView(failText);


        //add layout to row
        LinearLayout row = new LinearLayout(context);
        row.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 0.25f));
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.addView(textViewLayout);


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
                dialog.setTitle("Attempt Defense " + Integer.toString(buttonNum+1));
                RelativeLayout dialogLayout = (RelativeLayout) context.getLayoutInflater().inflate(R.layout.dialog, null);
                Button success = (Button) dialogLayout.findViewById(R.id.successButton);
                success.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //add time
                        successTimes.get(buttonNum).add(Calendar.getInstance().getTimeInMillis() - startTime);
                        //increment counter
                        successText.setText("S: " + (Integer.parseInt(successText.getText().toString().replaceFirst("S: ", "")) + 1));
                        //dismiss dialog
                        dialog.dismiss();
                    }
                });
                Button failure = (Button) dialogLayout.findViewById(R.id.failButton);
                failure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //add time
                        failTimes.get(buttonNum).add(Calendar.getInstance().getTimeInMillis() - startTime);
                        //increment counter
                        failText.setText("F: " + (Integer.parseInt(failText.getText().toString().replaceFirst("F: ", "")) + 1));
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
        row.addView(defenseButton);
        return row;
    }
}
