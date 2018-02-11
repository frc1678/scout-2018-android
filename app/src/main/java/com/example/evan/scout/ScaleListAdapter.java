package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
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

/**
 * Created by Emily Lin on 1/24/2018.
 */

public class ScaleListAdapter extends BaseAdapter {

    public static LayoutInflater layoutInflater = null;
    Dialog listDialog;
    Context activityContext;
    String title;
    String scaleFBname;
    JSONArray jsonArray;
    ListModificationListener listInterface;
    ArrayList<HashMap<String, Object>> dataList;

    public ScaleListAdapter(Activity currentActivity, ArrayList<HashMap<String, Object>> dataList, Dialog listDialog, String title, String scaleFBname, JSONArray jsonArray, ListModificationListener lml){
        activityContext = currentActivity;
        this.dataList = dataList;
        this.listDialog = listDialog;
        this.title = title;
        listInterface = lml;
        this.scaleFBname = scaleFBname;
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
    public void notifyDataSetChanged(){
        if(dataList.size() > 0){
            super.notifyDataSetChanged();
        } else {
            listDialog.cancel();
        }
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final int inversePosition = dataList.size() - position - 1;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.scale_list_cv, null);
        }
        if(String.valueOf(dataList.get(inversePosition).get("didSucceed")).equals("true")) {
            ((TextView) convertView.findViewById(R.id.cvResult)).setText("Success");
            ((TextView) convertView.findViewById(R.id.cvLayer)).setText("Layer " + (String.valueOf(dataList.get(inversePosition).get("layer"))));
            if(String.valueOf(dataList.get(inversePosition).get("status")) == "opponentOwned"){
                ((TextView) convertView.findViewById(R.id.cvStatus)).setText("Opponent Owned");
            }
            else if(String.valueOf(dataList.get(inversePosition).get("status")) == "balanced"){
                ((TextView) convertView.findViewById(R.id.cvStatus)).setText("Balanced");
            }
        }
        else if(String.valueOf(dataList.get(inversePosition).get("didSucceed")).equals("false")) {
            ((TextView) convertView.findViewById(R.id.cvResult)).setText("Fail");
            ((TextView) convertView.findViewById(R.id.cvLayer)).setText(String.valueOf(dataList.get(inversePosition).get("layer")));
            ((TextView) convertView.findViewById(R.id.cvStatus)).setText(String.valueOf(dataList.get(inversePosition).get("status")));
        }
        ((TextView) convertView.findViewById(R.id.cvStartTime)).setText(String.valueOf(dataList.get(inversePosition).get("startTime")) + " sec");
        ((TextView) convertView.findViewById(R.id.cvEndTime)).setText(String.valueOf(dataList.get(inversePosition).get("endTime")) + " sec");
        ((ImageButton) convertView.findViewById(R.id.cvDelButton)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dataList.remove(inversePosition);
                listInterface.onListChanged(dataList);
                notifyDataSetChanged();
            }
        });
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final View subDialogView = layoutInflater.inflate(R.layout.scale_dialog, null);
                ((TextView) subDialogView.findViewById(R.id.scaleDialogTitle)).setText(title);

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activityContext);
                dialogBuilder.setView(subDialogView);
                dialogBuilder.setCancelable(false);
                final AlertDialog dialog = dialogBuilder.create();
                dialog.setCanceledOnTouchOutside(false);

                ((Button) subDialogView.findViewById(R.id.scaleSuccessButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final View subSuccessDialogView = layoutInflater.inflate(R.layout.scale_success_dialog, null);
                        final RadioGroup scaleLayerGroup = (RadioGroup) subSuccessDialogView.findViewById(R.id.scaleLayerRadioGroup);
                        final RadioGroup scaleStatusGroup = (RadioGroup) subSuccessDialogView.findViewById(R.id.scaleOwnershipRadioGroup);
                        ((TextView) subSuccessDialogView.findViewById(R.id.scaleSuccessDialogTitle)).setText(title);

                        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(activityContext);
                        dialogBuilder.setView(subSuccessDialogView);
                        dialogBuilder.setCancelable(false);
                        final AlertDialog dialog2 = dialogBuilder.create();
                        dialog2.setCanceledOnTouchOutside(false);

                        dialog2.show();

                        ((Button) subSuccessDialogView.findViewById(R.id.scaleDoneButton)).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                RadioButton scaleLayerButton = (RadioButton) subSuccessDialogView.findViewById(scaleLayerGroup.getCheckedRadioButtonId());
                                RadioButton scaleStatusButton = (RadioButton) subSuccessDialogView.findViewById(scaleStatusGroup.getCheckedRadioButtonId());

                                String finalLayerString = scaleLayerButton.getText().toString();
                                Integer finalLayer = 0;
                                String finalStatus = null;

                                if(scaleLayerButton != null && scaleStatusButton != null) {
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

                                    String finalStatusString = scaleStatusButton.getText().toString();
                                    if(finalStatusString.equals("Opponent Owned")) {
                                        finalStatus = "opponentOwned";
                                    }
                                    else if(finalStatusString.equals("Balanced")) {
                                        finalStatus = "balanced";
                                    }
                                    dataList.get(inversePosition).put("status", finalStatus);
                                    listInterface.onListChanged(dataList);

                                    dataList.get(inversePosition).put("didSucceed", true);

                                    dialog2.dismiss();
                                    listDialog.cancel();
                                } else {
                                    Toast.makeText(activityContext, "No Radio Button was checked.",
                                            Toast.LENGTH_SHORT).show();
                                }

                                List<String> scaleKeys = Arrays.asList("didSucceed", "startTime", "endTime", "status", "layer");
                                List<Object> scaleValues = new ArrayList<>();
                                scaleValues.clear();

                                try {
                                    scaleValues.add(((JSONObject) jsonArray.get(inversePosition)).getBoolean("didSucceed"));
                                    scaleValues.add(((JSONObject) jsonArray.get(inversePosition)).get("startTime"));
                                    scaleValues.add(((JSONObject) jsonArray.get(inversePosition)).get("endTime"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                if(scaleStatusButton.getText().toString() == null){
                                    scaleValues.add(null);
                                }else{
                                    scaleValues.add(scaleStatusButton.getText().toString());
                                }
                                scaleValues.add(finalLayer);

                                JSONObject tempData = Utils.returnJSONObject(scaleKeys, scaleValues);
                                try {
                                    jsonArray.put(inversePosition, tempData);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                DataManager.addZeroTierJsonData(scaleFBname, jsonArray);
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

                ((Button) subDialogView.findViewById(R.id.scaleFailButton)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dataList.get(inversePosition).put("didSucceed", false);
                        dataList.get(inversePosition).put("layer", null);
                        dataList.get(inversePosition).put("status", null);

                        List<String> scaleKeys = Arrays.asList("didSucceed", "startTime", "endTime");
                        List<Object> scaleValues = new ArrayList<>();
                        scaleValues.clear();

                        try {
                            scaleValues.add(((JSONObject) jsonArray.get(inversePosition)).getBoolean("didSucceed"));
                            scaleValues.add(((JSONObject) jsonArray.get(inversePosition)).get("startTime"));
                            scaleValues.add(((JSONObject) jsonArray.get(inversePosition)).get("endTime"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        scaleValues.add(null);
                        scaleValues.add(null);

                        JSONObject tempData = Utils.returnJSONObject(scaleKeys, scaleValues);
                        try {
                            jsonArray.put(inversePosition, tempData);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        DataManager.addZeroTierJsonData(scaleFBname, jsonArray);

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