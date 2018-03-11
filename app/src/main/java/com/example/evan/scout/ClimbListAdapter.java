package com.example.evan.scout;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by HiBye300 on 3/10/2018.
 */

public class ClimbListAdapter extends BaseAdapter {

    public static LayoutInflater layoutInflater = null;
    Dialog listDialog;
    Context activityContext;
    String title;
    JSONArray jsonArray;
    ClimbListAdapter.ListModificationListener listInterface;
    ArrayList<HashMap<String, Object>> dataList;

    public ClimbListAdapter(Activity currentActivity, ArrayList<HashMap<String, Object>> dataList, Dialog listDialog, String title, JSONArray jsonArray, ClimbListAdapter.ListModificationListener lml){
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
            convertView = layoutInflater.inflate(R.layout.climb_list_cv, null);
        }

        if((String) dataList.get(inversePosition).get("liftType") == "activeLift") {
            ((TextView) convertView.findViewById(R.id.cvSRDC)).setText(String.valueOf(dataList.get(inversePosition).get("didClimb")));
            ((TextView) convertView.findViewById(R.id.cvPLT)).setText(String.valueOf(dataList.get(inversePosition).get("partnerLiftType")));
            ((TextView) convertView.findViewById(R.id.cvFTL)).setText(String.valueOf(dataList.get(inversePosition).get("didFailToLift")));
            ((TextView) convertView.findViewById(R.id.cvNRL)).setText(String.valueOf(dataList.get(inversePosition).get("numRobotsLifted")));
            ((TextView) convertView.findViewById(R.id.SRDCText)).setText("Robot Climbed:");
            ((TextView) convertView.findViewById(R.id.PLTText)).setText("Lift Type:");
            ((TextView) convertView.findViewById(R.id.FTLText)).setText("Failed to Lift:");
            ((TextView) convertView.findViewById(R.id.NRLText)).setText("Robots Lifted:");
        }
        if(String.valueOf(dataList.get(inversePosition).get("didSucceed")).equals("true")) {
            ((TextView) convertView.findViewById(R.id.cvResult)).setText("Success"); }
        else if(String.valueOf(dataList.get(inversePosition).get("didSucceed")).equals("false")) {
            ((TextView) convertView.findViewById(R.id.cvResult)).setText("Fail"); }

        if(String.valueOf(dataList.get(inversePosition).get("liftType")).equals("activeLift"))
        ((TextView) convertView.findViewById(R.id.cvClimbType)).setText((String) dataList.get(inversePosition).get("liftType"));
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
        return convertView;
    }

    public interface ListModificationListener{
        void onListChanged(ArrayList<HashMap<String, Object>> returnList);
    }
}
