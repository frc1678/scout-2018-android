package com.example.evan.scout;

/**
 * Created by Calvin on 5/31/17.
 */

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.FormatFlagsConversionMismatchException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class DataManager {
    public static String subTitle = "";
    public static JSONObject collectedData = new JSONObject();
    public static JSONObject sideData = new JSONObject();
    public static JSONArray climbDataArray = new JSONArray();
    public static JSONArray autoAllianceSwitchDataArray = new JSONArray();
    public static JSONArray teleAllianceSwitchDataArray = new JSONArray();
    public static JSONArray teleOpponentSwitchDataArray = new JSONArray();
    public static JSONArray autoScaleDataArray = new JSONArray();
    public static JSONArray teleScaleDataArray = new JSONArray();

    public static void addZeroTierJsonData(String key0, Object value0){
        addToCollectedData(key0, null, value0);
    }

    public static void addOneTierJsonData(boolean side, String key, List<String> keys1,List<Object> values1){
        JSONObject data1 = new JSONObject();

        for(int i = 0; i < keys1.size(); i++){
            try {
                data1.put(keys1.get(i), values1.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(side){
            addToSideData(key, data1, null);
        }else{
            addToCollectedData(key, data1, null);
        }
    }

    private static void addToCollectedData(String key,JSONObject jsonData,Object data){
        if(data == null){
            try {
                collectedData.put(key,jsonData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            try {
                Log.e("collectedDataMAPLIST", data.toString());
                collectedData.put(key, data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public static void addToSideData(String key,JSONObject jsonData,Object data){
        sideData = new JSONObject();
        if(data == null){
            try {
                sideData.put(key,jsonData);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            try {
                sideData.put(key, data);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
