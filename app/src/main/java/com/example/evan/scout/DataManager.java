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
    public static ArrayList<HashMap<String, Object>> climbDataList = new ArrayList<HashMap<String, Object>>();

    public static JSONArray autoAllianceSwitchDataArray = new JSONArray();
    public static JSONArray teleAllianceSwitchDataArray = new JSONArray();
    public static JSONArray teleOpponentSwitchDataArray = new JSONArray();

    public static JSONArray autoScaleDataArray = new JSONArray();
    public static JSONArray teleScaleDataArray = new JSONArray();

    public static JSONArray alliancePlatformTakenAuto = new JSONArray();
    public static JSONArray alliancePlatformTakenTele = new JSONArray();
    public static JSONArray opponentPlatformTakenTele = new JSONArray();

    public static ArrayList<HashMap<String, Object>> autoScaleDataList = new ArrayList<HashMap<String, Object>>();
    public static ArrayList<HashMap<String, Object>> teleScaleDataList = new ArrayList<HashMap<String, Object>>();

    public static ArrayList<HashMap<String, Object>> autoAllianceSwitchDataList = new ArrayList<HashMap<String, Object>>();
    public static ArrayList<HashMap<String, Object>> teleAllianceSwitchDataList = new ArrayList<HashMap<String, Object>>();
    public static ArrayList<HashMap<String, Object>> teleOpponentSwitchDataList = new ArrayList<HashMap<String, Object>>();

    public static JSONObject autoPyramidDataList = new JSONObject();
    public static JSONObject telePyramidDataList = new JSONObject();

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
    public static void resetTeleOpponentPlatformArrays(){
        for(int i = 0; i <=5; i++){
            try {
                opponentPlatformTakenTele.put(i, false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public static void resetTeleAlliancePlatformArrays(){
        for(int i = 0; i <=5; i++){
            try {
                alliancePlatformTakenTele.put(i, false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public static void resetAutoAlliancePlatformArrays(){
        for(int i = 0; i <=5; i++){
            try {
                alliancePlatformTakenAuto.put(i, false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
    public static void resetAutoScaleData(){
        autoScaleDataArray = new JSONArray();
        autoScaleDataList = new ArrayList<>();
    }
    public static void resetTeleScaleData(){
        teleScaleDataArray = new JSONArray();
        teleScaleDataList = new ArrayList<>();
    }
    public static void resetAutoSwitchData(){
        autoAllianceSwitchDataArray = new JSONArray();
        autoAllianceSwitchDataList = new ArrayList<>();
    }
    public static void resetTeleSwitchData(){
        teleAllianceSwitchDataArray = new JSONArray();
        teleOpponentSwitchDataArray = new JSONArray();
        teleAllianceSwitchDataList = new ArrayList<>();
        teleOpponentSwitchDataList = new ArrayList<>();
    }
    public static void resetAutoPyramidData(){
        autoPyramidDataList = new JSONObject();
    }
    public static void resetTelePyramidData(){
        telePyramidDataList = new JSONObject();
    }
}
