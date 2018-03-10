package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by HiBye300 on 2/3/2018.
 */

public class SwitchListAdapter extends BaseAdapter {
    public static LayoutInflater layoutInflater = null;
    Dialog listDialog;
    Context activityContext;
    String title;
    String switchFBname;
    JSONArray jsonArray;
    String finalStatus;
    SwitchListAdapter.ListModificationListener listInterface;
    ArrayList<HashMap<String, Object>> dataList;

    public SwitchListAdapter(Activity currentActivity, ArrayList<HashMap<String, Object>> dataList, Dialog listDialog, String title, String switchFBname, JSONArray jsonArray, SwitchListAdapter.ListModificationListener lml){
        activityContext = currentActivity;
        this.dataList = dataList;
        this.listDialog = listDialog;
        this.title = title;
        this.switchFBname = switchFBname;
        this.jsonArray = jsonArray;
        listInterface = lml;
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
    public void notifyDataSetChanged(){
        if(dataList.size() > 0){
            super.notifyDataSetChanged();
        } else {
            listDialog.cancel();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        Log.e("RANNNNN", "RANNNNN");
        final int inversePosition = dataList.size() - position - 1;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.switch_list_cv, null);
        }
        if(String.valueOf(dataList.get(inversePosition).get("didSucceed")).equals("true")) {
            ((TextView) convertView.findViewById(R.id.cvResult)).setText("Success");
            ((TextView) convertView.findViewById(R.id.cvLayer)).setText("Layer " + (String.valueOf(dataList.get(inversePosition).get("layer"))));
            if(String.valueOf(dataList.get(inversePosition).get("status")) == "opponentOwned"){
                ((TextView) convertView.findViewById(R.id.cvStatus)).setText("Opponent Owned");
            }else if(String.valueOf(dataList.get(inversePosition).get("status")) == "balanced"){
                ((TextView) convertView.findViewById(R.id.cvStatus)).setText("Balanced");
            }else if(String.valueOf(dataList.get(inversePosition).get("status")) == "owned"){
                ((TextView) convertView.findViewById(R.id.cvStatus)).setText("Owned");
            }else if(String.valueOf(dataList.get(inversePosition).get("status")) == "owned") { //START
                ((TextView) convertView.findViewById(R.id.cvStatus)).setText("Owned");
            } //END
        }
        else if(String.valueOf(dataList.get(inversePosition).get("didSucceed")).equals("false")) {
            ((TextView) convertView.findViewById(R.id.cvResult)).setText("Fail");
            ((TextView) convertView.findViewById(R.id.cvLayer)).setText(String.valueOf(dataList.get(inversePosition).get("layer")));
            ((TextView) convertView.findViewById(R.id.cvStatus)).setText(String.valueOf(dataList.get(inversePosition).get("status")));
        }
        /*else if(String.valueOf(dataList.get(inversePosition).get("didSucceed")).equals("false")) {
            ((TextView) convertView.findViewById(R.id.cvResult)).setText("Fail");
            ((TextView) convertView.findViewById(R.id.cvLayer)).setText(String.valueOf(dataList.get(inversePosition).get("layer")));
        }*/
        ((TextView) convertView.findViewById(R.id.cvStartTime)).setText(String.valueOf(dataList.get(inversePosition).get("startTime")) + " sec");
        ((TextView) convertView.findViewById(R.id.cvEndTime)).setText(String.valueOf(dataList.get(inversePosition).get("endTime")) + " sec");
        ((ImageButton) convertView.findViewById(R.id.cvDelButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            jsonArray.remove(inversePosition);
            dataList.remove(inversePosition);
            listInterface.onListChanged(dataList);
            notifyDataSetChanged();
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View subDialogView = layoutInflater.inflate(R.layout.switch_dialog, null);
                ((TextView) subDialogView.findViewById(R.id.dialogTitle)).setText(title);

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activityContext);
                dialogBuilder.setView(subDialogView);
                dialogBuilder.setCancelable(false);
                final AlertDialog dialog = dialogBuilder.create();
                dialog.setCanceledOnTouchOutside(false);



                ((Button) subDialogView.findViewById(R.id.successButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final View subSuccessDialogView = layoutInflater.inflate(R.layout.switch_success_dialog, null);
                        final RadioGroup switchLayerGroup = (RadioGroup) subSuccessDialogView.findViewById(R.id.switchLayerRadioGroup);
                        final RadioGroup switchStatusGroup = (RadioGroup) subSuccessDialogView.findViewById(R.id.switchOwnershipRadioGroup);
                        ((TextView) subSuccessDialogView.findViewById(R.id.dialogTitle)).setText(title);
                        RadioButton switchLayerButton = (RadioButton) subSuccessDialogView.findViewById(switchLayerGroup.getCheckedRadioButtonId());
                        RadioButton switchOwnedButton = (RadioButton) subSuccessDialogView.findViewById(R.id.switchOwnedRadio);
                        RadioButton switchBalancedButton = (RadioButton) subSuccessDialogView.findViewById(R.id.switchBalancedRadio);



                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activityContext);
                        dialogBuilder.setView(subSuccessDialogView);
                        dialogBuilder.setCancelable(false);
                        final AlertDialog dialog2 = dialogBuilder.create();
                        dialog2.setCanceledOnTouchOutside(false);

                        dialog2.show();
                        switchOwnedButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(finalStatus != "opponentOwned"){
                                    finalStatus = "opponentOwned";
                                }else{
                                    switchStatusGroup.clearCheck();
                                    finalStatus = null;
                                }
                            }
                        });
                        switchBalancedButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(finalStatus != "balanced"){
                                    finalStatus = "balanced";
                                }else{
                                    switchStatusGroup.clearCheck();
                                    finalStatus = null;
                                }
                            }
                        });
                        ((Button) subSuccessDialogView.findViewById(R.id.doneButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                RadioButton switchLayerButton = (RadioButton) subSuccessDialogView.findViewById(switchLayerGroup.getCheckedRadioButtonId());
                                RadioButton switchStatusButton = (RadioButton) subSuccessDialogView.findViewById(switchStatusGroup.getCheckedRadioButtonId());

                                //String finalLayerString = switchLayerButton.getText().toString();
                                Integer finalLayer = 0;
                                String finalStatus = null;

                                if(switchLayerButton != null) { //Changed
                                    String finalLayerString = switchLayerButton.getText().toString(); //Moved to here
                                    if(finalLayerString.equals("Layer 1")) {
                                        finalLayer = 1;
                                    }
                                    else if(finalLayerString.equals("Layer 2")) {
                                        finalLayer = 2;
                                    }
                                    else if (finalLayerString.equals("Layer 3")) {
                                        finalLayer = 3;
                                    }
                                    dataList.get(inversePosition).put("layer", finalLayer);
                                    listInterface.onListChanged(dataList);

                                    //String finalStatusString = switchStatusButton.getText().toString();
                                    if(switchStatusButton != null) { //START
                                        if(switchStatusButton.getText().toString().equals("Opponent Owned")) {
                                            finalStatus = "opponentOwned";
                                        }
                                        else if(switchStatusButton.getText().toString().equals("Balanced")) {
                                            finalStatus = "balanced";
                                        }
                                    }
                                    else if(switchStatusButton == null){ //END
                                        finalStatus = "owned";
                                    }
                                    dataList.get(inversePosition).put("status", finalStatus);
                                    listInterface.onListChanged(dataList);

                                    dataList.get(inversePosition).put("didSucceed", true);

                                    if(finalLayer != 0){
                                        dialog2.dismiss();
                                        listDialog.cancel();
                                    }else{
                                        Utils.makeToast(activityContext, "Please Input Layer!");
                                    }
                                } else {
                                    Toast.makeText(activityContext, "No Radio Button was checked.",
                                            Toast.LENGTH_SHORT).show();
                                }

                                List<String> switchKeys = Arrays.asList("didSucceed", "startTime", "endTime", "status", "layer");
                                List<Object> switchValues = new ArrayList<>();
                                switchValues.add(true);
                                try {
                                    switchValues.add(((JSONObject) jsonArray.get(inversePosition)).get("startTime"));
                                    switchValues.add(((JSONObject) jsonArray.get(inversePosition)).get("endTime"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                switchValues.add(finalStatus);
                                switchValues.add(finalLayer);

                                JSONObject tempData = Utils.returnJSONObject(switchKeys, switchValues);
                                try {
                                    jsonArray.put(inversePosition, tempData);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                DataManager.addZeroTierJsonData(switchFBname, jsonArray);
                            }
                        });
                        ((Button) subSuccessDialogView.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog2.cancel();
                            }
                        });
                        dialog.dismiss();
                        listDialog.cancel();
                    }
                });

                ((Button) subDialogView.findViewById(R.id.failButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dataList.get(inversePosition).put("didSucceed", false);
                        dataList.get(inversePosition).put("layer", null);
                        dataList.get(inversePosition).put("status", null);

                        List<String> switchKeys = Arrays.asList("didSucceed", "startTime", "endTime", "status", "layer"); //Changed
                        List<Object> switchValues = new ArrayList<>();
                        switchValues.clear();

                        switchValues.add(false); //Added
                        try {
                            switchValues.add(((JSONObject) jsonArray.get(inversePosition)).get("startTime"));
                            switchValues.add(((JSONObject) jsonArray.get(inversePosition)).get("endTime"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        switchValues.add(null);
                        switchValues.add(null);

                        JSONObject tempData = Utils.returnJSONObject(switchKeys, switchValues);
                        try {
                            jsonArray.put(inversePosition, tempData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        DataManager.addZeroTierJsonData(switchFBname, jsonArray);

                        dialog.dismiss();
                        listDialog.cancel();
                    }
                });
                dialog.show();
                ((Button) subDialogView.findViewById(R.id.cancelButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.cancel();
                        listDialog.cancel();
                    }
                });
                dialog.show();
                listDialog.cancel();
            }
        });

        return convertView;
    }

    public interface ListModificationListener{
        void onListChanged(ArrayList<HashMap<String, Object>> returnList);
    }
}