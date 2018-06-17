package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static java.lang.String.valueOf;

/**
 * Created by Citrus Circuits Scout Programmers on 3/10/2018.
 */

public class ClimbListAdapter extends BaseAdapter {

    public static LayoutInflater layoutInflater = null;
    Dialog listDialog;
    Context activityContext;
    String title;
    JSONArray jsonArray;
    ClimbListAdapter.ListModificationListener listInterface;
    ArrayList<HashMap<String, Object>> dataList;

    String liftType;
    Boolean didClimb;
    String partnerLiftType;
    Boolean didSucceed;
    Boolean didFailToLift;
    Integer numRobotsLifted = 0;

    public ClimbListAdapter(Activity currentActivity, ArrayList<HashMap<String, Object>> dataList, Dialog listDialog, String title, JSONArray jsonArray, ClimbListAdapter.ListModificationListener lml) {
        activityContext = currentActivity;
        this.dataList = dataList;
        this.listDialog = listDialog;
        this.title = title;
        listInterface = lml;
        this.jsonArray = jsonArray;
        layoutInflater = (LayoutInflater) activityContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
            return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public void notifyDataSetChanged() {
        if (dataList.size() > 0) {
            super.notifyDataSetChanged();
        } else {
            listDialog.cancel();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int inversePosition = dataList.size() - position - 1;
        final int inversePositionArray = jsonArray.length() - position - 1;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.climb_list_cv, null);
        }

        if ((String) dataList.get(inversePosition).get("liftType") == "activeLift") {
            ((TextView) convertView.findViewById(R.id.cvSRDC)).setText(valueOf(dataList.get(inversePosition).get("didClimb")));
            ((TextView) convertView.findViewById(R.id.cvPLT)).setText(valueOf(dataList.get(inversePosition).get("partnerLiftType")));
            ((TextView) convertView.findViewById(R.id.cvFTL)).setText(valueOf(dataList.get(inversePosition).get("didFailToLift")));
            ((TextView) convertView.findViewById(R.id.cvNRL)).setText(valueOf(dataList.get(inversePosition).get("numRobotsLifted")));
            ((TextView) convertView.findViewById(R.id.SRDCText)).setText("Robot Climbed:");
            ((TextView) convertView.findViewById(R.id.PLTText)).setText("Lift Type:");
            ((TextView) convertView.findViewById(R.id.FTLText)).setText("Failed to Lift:");
            ((TextView) convertView.findViewById(R.id.NRLText)).setText("Robots Lifted:");
        }
        if (valueOf(dataList.get(inversePosition).get("didSucceed")).equals("true")) {
            ((TextView) convertView.findViewById(R.id.cvResult)).setText("Success");
        } else if (valueOf(dataList.get(inversePosition).get("didSucceed")).equals("false")) {
            ((TextView) convertView.findViewById(R.id.cvResult)).setText("Fail");
        }

        if (valueOf(dataList.get(inversePosition).get("liftType")).equals("activeLift")) {
            ((TextView) convertView.findViewById(R.id.cvClimbType)).setText("Active Lift");
        } else if (valueOf(dataList.get(inversePosition).get("liftType")).equals("soloClimb")) {
            ((TextView) convertView.findViewById(R.id.cvClimbType)).setText("Solo Climb");
        } else if (valueOf(dataList.get(inversePosition).get("liftType")).equals("assistedClimb")) {
            ((TextView) convertView.findViewById(R.id.cvClimbType)).setText("Assisted Climb");
        } else if (valueOf(dataList.get(inversePosition).get("liftType")).equals("passiveClimb")) {
            ((TextView) convertView.findViewById(R.id.cvClimbType)).setText("Passive Climb");
        }
        ((TextView) convertView.findViewById(R.id.cvStartTime)).setText(valueOf(dataList.get(inversePosition).get("startTime")) + " sec");
        ((TextView) convertView.findViewById(R.id.cvEndTime)).setText(valueOf(dataList.get(inversePosition).get("endTime")) + " sec");
        ((ImageButton) convertView.findViewById(R.id.cvDelButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataList.remove(inversePosition);
                jsonArray.remove(inversePositionArray);
                Log.e("JSONARRAYDELETED", jsonArray.toString());
                listInterface.onListChanged(jsonArray);
                notifyDataSetChanged();
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final List<String> activeKeys = Arrays.asList("didSucceed", "didClimb", "startTime", "endTime", "partnerLiftType", "didFailToLift", "numRobotsLifted");
                final List<Object> activeValues = new ArrayList<>();

                final List<String> endKeys = Arrays.asList("didSucceed", "startTime", "endTime");
                final List<Object> endValues = new ArrayList<>();

                listDialog.dismiss();

                final View subDialogView = layoutInflater.inflate(R.layout.climb_dialog, null);
                ((TextView) subDialogView.findViewById(R.id.dialogTitle)).setText(title);

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activityContext);
                dialogBuilder.setView(subDialogView);
                dialogBuilder.setCancelable(false);
                final AlertDialog dialog = dialogBuilder.create();
                dialog.setCanceledOnTouchOutside(false);

                dialog.show();

                ((Button) subDialogView.findViewById(R.id.successButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dataList.get(inversePosition).put("didSucceed", true);
                        didSucceed = true;

                        final View subSuccessDialogView = layoutInflater.inflate(R.layout.climb_type, null);
                        ((TextView) subSuccessDialogView.findViewById(R.id.dialogTitle)).setText(title);

                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activityContext);
                        dialogBuilder.setView(subSuccessDialogView);
                        dialogBuilder.setCancelable(false);
                        final AlertDialog dialog2 = dialogBuilder.create();
                        dialog2.setCanceledOnTouchOutside(false);

                        dialog2.show();

                        ((Button) subSuccessDialogView.findViewById(R.id.doneButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                final RadioButton passiveClimbRadioButton = (RadioButton) subSuccessDialogView.findViewById(R.id.passiveClimbRadio);
                                final RadioButton assistedClimbRadioButton = (RadioButton) subSuccessDialogView.findViewById(R.id.assistedClimbRadio);
                                final RadioButton activeLiftRadioButton = (RadioButton) subSuccessDialogView.findViewById(R.id.activeLiftRadio);
                                final RadioButton independentRadioButton = (RadioButton) subSuccessDialogView.findViewById(R.id.soloClimbRadio);

                                if(!passiveClimbRadioButton.isChecked() && !assistedClimbRadioButton.isChecked() && !activeLiftRadioButton.isChecked() && !independentRadioButton.isChecked()) {
                                    Utils.makeToast(activityContext, "Please Input a Climb Type");

                                } else {
                                    if (activeLiftRadioButton.isChecked()) {
                                        dataList.get(inversePosition).put("liftType", "activeLift");
                                        liftType = "activeLift";
                                        final View subActiveDialogView = layoutInflater.inflate(R.layout.active_lift_dialog, null);
                                        ((TextView) subActiveDialogView.findViewById(R.id.dialogTitle)).setText(title);

                                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activityContext);
                                        dialogBuilder.setView(subActiveDialogView);
                                        dialogBuilder.setCancelable(false);
                                        final AlertDialog dialog3 = dialogBuilder.create();
                                        dialog3.setCanceledOnTouchOutside(false);
                                        dialog3.show();

                                        final CheckBox SRDCCheckBox = (CheckBox) subActiveDialogView.findViewById(R.id.pdidClimbRadio);
                                        final CheckBox passiveCheckBox = (CheckBox) subActiveDialogView.findViewById(R.id.passistLifts);
                                        final CheckBox assistedCheckBox = (CheckBox) subActiveDialogView.findViewById(R.id.ppassiveLifts);
                                        final CheckBox FTLCheckBox = (CheckBox) subActiveDialogView.findViewById(R.id.failedToLift);

                                        final TextView numberView = (TextView) subActiveDialogView.findViewById(R.id.numberView);

                                        Button minusButton = (Button) subActiveDialogView.findViewById(R.id.minusButton);
                                        minusButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (numRobotsLifted > 0) {
                                                    numRobotsLifted -= 1;
                                                }
                                                numberView.setText(valueOf(numRobotsLifted));
                                            }
                                        });

                                        Button plusButton = (Button) subActiveDialogView.findViewById(R.id.plusButton);
                                        plusButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                numRobotsLifted += 1;
                                                numberView.setText(valueOf(numRobotsLifted));
                                            }
                                        });

                                        dataList.get(inversePosition).put("numRobotsLifted", numRobotsLifted);

                                        ((Button) subActiveDialogView.findViewById(R.id.doneButton)).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                if (passiveCheckBox.isChecked() || assistedCheckBox.isChecked()) {
                                                    didClimb = SRDCCheckBox.isChecked();
                                                    dataList.get(inversePosition).put("didClimb", didClimb);
                                                    didFailToLift = FTLCheckBox.isChecked();
                                                    dataList.get(inversePosition).put("didFailToLift", didFailToLift);
                                                    if (passiveCheckBox.isChecked() && !assistedCheckBox.isChecked()) {
                                                        dataList.get(inversePosition).put("partnerLiftType", "passive");
                                                        partnerLiftType = "passive";
                                                    } else if (assistedCheckBox.isChecked() && !passiveCheckBox.isChecked()) {
                                                        dataList.get(inversePosition).put("partnerLiftType", "assisted");
                                                        partnerLiftType = "assisted";
                                                    } else if (passiveCheckBox.isChecked() && assistedCheckBox.isChecked()) {
                                                        dataList.get(inversePosition).put("partnerLiftType", "both");
                                                        partnerLiftType = "both";
                                                    }
                                                    dataList.get(inversePosition).put("numRobotsLifted", numRobotsLifted);

                                                    DataManager.sideData = new JSONObject();

                                                    activeValues.add(0, didSucceed);
                                                    activeValues.add(1, didClimb);
                                                    activeValues.add(2, dataList.get(inversePosition).get("startTime"));
                                                    activeValues.add(3, dataList.get(inversePosition).get("endTime"));
                                                    activeValues.add(4, partnerLiftType);
                                                    activeValues.add(5, didFailToLift);
                                                    activeValues.add(6, numRobotsLifted);

                                                    DataManager.addOneTierJsonData(true, liftType, activeKeys, activeValues);
                                                    JSONObject tempData = DataManager.sideData;
                                                    try {
                                                        jsonArray.put(inversePosition, tempData);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    DataManager.addZeroTierJsonData("climb", jsonArray);

                                                    dialog.dismiss();
                                                    dialog2.dismiss();
                                                    dialog3.dismiss();
                                                } else {
                                                    Utils.makeToast(activityContext, "Please Input a Partner Lift Type");
                                                }
                                            }
                                        });
                                        ((Button) subActiveDialogView.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog3.dismiss();
                                            }
                                        });
                                    } else {
                                        if (passiveClimbRadioButton.isChecked()) {
                                            dataList.get(inversePosition).put("liftType", "passiveClimb");
                                            liftType = "passiveClimb";
                                        }
                                        else if (assistedClimbRadioButton.isChecked()) {
                                            dataList.get(inversePosition).put("liftType", "assistedClimb");
                                            liftType = "assistedClimb";
                                        }
                                        else if (independentRadioButton.isChecked()) {
                                            dataList.get(inversePosition).put("liftType", "soloClimb");
                                            liftType = "soloClimb";
                                        }
                                        endValues.add(0, didSucceed);
                                        endValues.add(1, dataList.get(inversePosition).get("startTime"));
                                        endValues.add(2, dataList.get(inversePosition).get("endTime"));
                                        endValues.add(3, partnerLiftType);
                                        endValues.add(4, didFailToLift);
                                        endValues.add(5, numRobotsLifted);

                                        DataManager.addOneTierJsonData(true, liftType, endKeys, endValues);
                                        JSONObject tempData = DataManager.sideData;
                                        try {
                                            jsonArray.put(inversePosition, tempData);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        DataManager.addZeroTierJsonData("climb", jsonArray);

                                        dialog.dismiss();
                                        dialog2.dismiss();
                                    }
                                }
                            }
                        });
                        ((Button) subSuccessDialogView.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog2.dismiss();
                            }
                        });
                    }
                });

                ((Button) subDialogView.findViewById(R.id.failButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        dataList.get(inversePosition).put("didSucceed", false);
                        didSucceed = false;

                        final View subSuccessDialogView = layoutInflater.inflate(R.layout.climb_type, null);
                        final RadioGroup climbTypeGroup = (RadioGroup) subSuccessDialogView.findViewById(R.id.radioGroup);
                        ((TextView) subSuccessDialogView.findViewById(R.id.dialogTitle)).setText(title);

                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activityContext);
                        dialogBuilder.setView(subSuccessDialogView);
                        dialogBuilder.setCancelable(false);
                        final AlertDialog dialog2 = dialogBuilder.create();
                        dialog2.setCanceledOnTouchOutside(false);

                        dialog2.show();

                        ((Button) subSuccessDialogView.findViewById(R.id.doneButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                final RadioButton passiveClimbRadioButton = (RadioButton) subSuccessDialogView.findViewById(R.id.passiveClimbRadio);
                                final RadioButton assistedClimbRadioButton = (RadioButton) subSuccessDialogView.findViewById(R.id.assistedClimbRadio);
                                final RadioButton activeLiftRadioButton = (RadioButton) subSuccessDialogView.findViewById(R.id.activeLiftRadio);
                                final RadioButton independentRadioButton = (RadioButton) subSuccessDialogView.findViewById(R.id.soloClimbRadio);

                                if(!passiveClimbRadioButton.isChecked() && !assistedClimbRadioButton.isChecked() && !activeLiftRadioButton.isChecked() && !independentRadioButton.isChecked()) {
                                    Utils.makeToast(activityContext, "Please Input a Climb Type");

                                } else {
                                    if (activeLiftRadioButton.isChecked()) {
                                        dataList.get(inversePosition).put("liftType", "activeLift");
                                        liftType = "activeLift";
                                        final View subActiveDialogView = layoutInflater.inflate(R.layout.active_lift_dialog, null);
                                        ((TextView) subActiveDialogView.findViewById(R.id.dialogTitle)).setText(title);

                                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activityContext);
                                        dialogBuilder.setView(subActiveDialogView);
                                        dialogBuilder.setCancelable(false);
                                        final AlertDialog dialog3 = dialogBuilder.create();
                                        dialog3.setCanceledOnTouchOutside(false);
                                        dialog3.show();

                                        final CheckBox SRDCCheckBox = (CheckBox) subActiveDialogView.findViewById(R.id.pdidClimbRadio);
                                        final CheckBox passiveCheckBox = (CheckBox) subActiveDialogView.findViewById(R.id.passistLifts);
                                        final CheckBox assistedCheckBox = (CheckBox) subActiveDialogView.findViewById(R.id.ppassiveLifts);
                                        final CheckBox FTLCheckBox = (CheckBox) subActiveDialogView.findViewById(R.id.failedToLift);

                                        final TextView numberView = (TextView) subActiveDialogView.findViewById(R.id.numberView);

                                        Button minusButton = (Button) subActiveDialogView.findViewById(R.id.minusButton);
                                        minusButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (numRobotsLifted > 0) {
                                                    numRobotsLifted -= 1;
                                                }
                                                numberView.setText(valueOf(numRobotsLifted));
                                            }
                                        });

                                        Button plusButton = (Button) subActiveDialogView.findViewById(R.id.plusButton);
                                        plusButton.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                numRobotsLifted += 1;
                                                numberView.setText(valueOf(numRobotsLifted));
                                            }
                                        });

                                        dataList.get(inversePosition).put("numRobotsLifted", numRobotsLifted);

                                        ((Button) subActiveDialogView.findViewById(R.id.doneButton)).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                if (passiveCheckBox.isChecked() || assistedCheckBox.isChecked()) {
                                                    didClimb = SRDCCheckBox.isChecked();
                                                    dataList.get(inversePosition).put("didClimb", didClimb);
                                                    didFailToLift = FTLCheckBox.isChecked();
                                                    dataList.get(inversePosition).put("didFailToLift", didFailToLift);
                                                    if (passiveCheckBox.isChecked() && !assistedCheckBox.isChecked()) {
                                                        dataList.get(inversePosition).put("partnerLiftType", "passive");
                                                        partnerLiftType = "passive";
                                                    } else if (assistedCheckBox.isChecked() && !passiveCheckBox.isChecked()) {
                                                        dataList.get(inversePosition).put("partnerLiftType", "assisted");
                                                        partnerLiftType = "assisted";
                                                    } else if (passiveCheckBox.isChecked() && assistedCheckBox.isChecked()) {
                                                        dataList.get(inversePosition).put("partnerLiftType", "both");
                                                        partnerLiftType = "both";
                                                    }
                                                    dataList.get(inversePosition).put("numRobotsLifted", numRobotsLifted);

                                                    final List<String> activeKeys = Arrays.asList("didSucceed", "didClimb", "startTime", "endTime", "partnerLiftType", "didFailToLift", "numRobotsLifted");
                                                    final List<Object> activeValues = new ArrayList<>();

                                                    DataManager.sideData = new JSONObject();

                                                    activeValues.add(0, didSucceed);
                                                    activeValues.add(1, didClimb);
                                                    activeValues.add(2, dataList.get(inversePosition).get("startTime"));
                                                    activeValues.add(3, dataList.get(inversePosition).get("endTime"));
                                                    activeValues.add(4, partnerLiftType);
                                                    activeValues.add(5, didFailToLift);
                                                    activeValues.add(6, numRobotsLifted);

                                                    DataManager.addOneTierJsonData(true, liftType, activeKeys, activeValues);
                                                    JSONObject tempData = DataManager.sideData;
                                                    try {
                                                        jsonArray.put(inversePosition, tempData);
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    DataManager.addZeroTierJsonData("climb", jsonArray);

                                                    dialog.dismiss();
                                                    dialog2.dismiss();
                                                    dialog3.dismiss();
                                                } else {
                                                    Utils.makeToast(activityContext, "Please Input a Partner Lift Type");
                                                }
                                            }
                                        });
                                        ((Button) subActiveDialogView.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                dialog3.dismiss();
                                            }
                                        });
                                    } else {
                                        if (passiveClimbRadioButton.isChecked()) {
                                            dataList.get(inversePosition).put("liftType", "passiveClimb");
                                        }
                                        else if (assistedClimbRadioButton.isChecked()) {
                                            dataList.get(inversePosition).put("liftType", "assistedClimb");
                                        }
                                        else if (independentRadioButton.isChecked()) {
                                            dataList.get(inversePosition).put("liftType", "soloClimb");
                                        }

                                        endValues.add(0, didSucceed);
                                        endValues.add(1, dataList.get(inversePosition).get("startTime"));
                                        endValues.add(2, dataList.get(inversePosition).get("endTime"));
                                        endValues.add(3, partnerLiftType);
                                        endValues.add(4, didFailToLift);
                                        endValues.add(5, numRobotsLifted);

                                        DataManager.addOneTierJsonData(true, liftType, endKeys, endValues);
                                        JSONObject tempData = DataManager.sideData;
                                        try {
                                            jsonArray.put(inversePosition, tempData);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        DataManager.addZeroTierJsonData("climb", jsonArray);

                                        dialog.dismiss();
                                        dialog2.dismiss();
                                    }
                                }
                            }
                        });
                        ((Button) subSuccessDialogView.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog2.dismiss();
                            }
                        });
                    }
                });
                ((Button) subDialogView.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                listInterface.onListChanged(jsonArray);
            }
        });
        return convertView;
    }
    public interface ListModificationListener {
        void onListChanged(JSONArray returnArray);
    }
}
