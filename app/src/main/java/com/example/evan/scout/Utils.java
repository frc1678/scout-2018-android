package com.example.evan.scout;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Citrus Circuits Scout Programmers on 7/14/17.
 */

public class Utils {

    public static void checkData(){

    }

    public static void makeToast(Context context, String text){
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    public static void makeDialog(Context context, String title, String message){
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(title);
        dialog.setMessage(message);
        dialog.setNeutralButton("OK", null);
        dialog.create().show();
    }

    public static Object getField(Object object, String field) throws Exception {
        List<String> fields = Arrays.asList(field.split("\\."));
        if (fields.size() == 1) {
            return getDirectField(object, field);
        } else {
            Object parent = getField(object, field.substring(0, field.indexOf("." + fields.get(fields.size() - 1))));
            return getDirectField(parent, fields.get(fields.size() - 1));
        }
    }

    private static Object getDirectField(Object object, String field) throws Exception {
        if (object instanceof List) {return ((List)object).get(Integer.parseInt(field));}
        return findFieldInInheritedFields(object.getClass(), field).get(object);
    }

    private static Field findFieldInInheritedFields(Class clazz, String field) throws Exception {
        try {
            Field value = clazz.getDeclaredField(field);
            value.setAccessible(true);
            return value;
        } catch (NoSuchFieldException nsfe) {
            return findFieldInInheritedFields(clazz.getSuperclass(), field);
        }
    }

    public static void setField(Object object, String field, Object value) throws Exception {
        List<String> fields = Arrays.asList(field.split("\\."));
        Object parent;
        if (fields.size() == 1) {parent = object;}
        else {parent = getField(object, field.substring(0, field.indexOf("." + fields.get(fields.size() - 1))));}
        if (parent instanceof List) {
            ((List)parent).set(Integer.parseInt(fields.get(fields.size() - 1)), value);
        } else {
            Field variableToBeSet = findFieldInInheritedFields(parent.getClass(), fields.get(fields.size() - 1));
            variableToBeSet.setAccessible(true);
            variableToBeSet.set(parent, value);
        }
    }

    public static void toastText(final String text, final int duration) {
        try {
            toastText(text, duration, ScoutApplication.getCurrentActivity());
        } catch (NullPointerException npe) {
            Log.e("Toast Error", "Failed to get Activity to toast");
        }
    }

    public static void toastText(final String text, final int duration, final Activity context) {
        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, text, duration).show();
            }
        });
    }

    public static String readFile(Activity context, String name) {
        BufferedReader file;
        try {
            file = new BufferedReader(new InputStreamReader(new FileInputStream(
                    new File(android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "/Android/MatchData/" + name))));
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Open File");
            Toast.makeText(context, "Failed To Open File", Toast.LENGTH_LONG).show();
            return null;
        }
        String text;
        try {
            text = file.readLine();
        } catch (IOException ioe) {
            Log.e("File Error", "Failed To Read From File");
            Toast.makeText(context, "Failed To Read From File", Toast.LENGTH_LONG).show();
            return null;
        }
        Log.i("JSON after read", text);
        return text;
    }

    public static void resetAllDataNull(){

        DataManager.sideData = new JSONObject();
        DataManager.climbDataArray = new JSONArray();
        DataManager.autoAllianceSwitchDataArray = new JSONArray();
        DataManager.teleAllianceSwitchDataArray = new JSONArray();
        DataManager.teleOpponentSwitchDataArray = new JSONArray();
        DataManager.autoScaleDataArray = new JSONArray();
        DataManager.teleScaleDataArray = new JSONArray();

        DataManager.addZeroTierJsonData("allianceSwitchAttemptAuto", null);
        DataManager.addZeroTierJsonData("allianceSwitchAttemptTele", null);
        DataManager.addZeroTierJsonData("opponentSwitchAttemptTele", null);
        DataManager.addZeroTierJsonData("scaleAttemptAuto", null);
        DataManager.addZeroTierJsonData("scaleAttemptTele", null);
        DataManager.addZeroTierJsonData("climb", null);
        DataManager.addZeroTierJsonData("didGetDisabled", false);
        DataManager.addZeroTierJsonData("didGetIncapacitated", false);
        DataManager.addZeroTierJsonData("didMakeAutoRun", false);
        DataManager.addZeroTierJsonData("didPark", false);
        DataManager.addZeroTierJsonData("alliancePlatformIntakeAuto", returnJSONArray(Arrays.asList(0,1,2,3,4,5), Arrays.asList(false, false, false, false, false, false)));
        DataManager.addZeroTierJsonData("alliancePlatformIntakeTele", returnJSONArray(Arrays.asList(0,1,2,3,4,5), Arrays.asList(false, false, false, false, false, false)));
        DataManager.addZeroTierJsonData("opponentPlatformIntakeTele", returnJSONArray(Arrays.asList(0,1,2,3,4,5), Arrays.asList(false, false, false, false, false, false)));
        DataManager.addZeroTierJsonData("numCubesFumbledAuto", 0);
        DataManager.addZeroTierJsonData("numCubesFumbledTele", 0);
        DataManager.addZeroTierJsonData("numExchangeInput", 0);
        DataManager.addZeroTierJsonData("numGroundIntakeTele", 0);
        DataManager.addZeroTierJsonData("totalNumScaleFoul", 0);
        DataManager.addZeroTierJsonData("numGroundPortalIntakeTele", 0);
        DataManager.addZeroTierJsonData("numHumanPortalIntakeTele", 0);
        DataManager.addZeroTierJsonData("numGroundPyramidIntakeAuto", 0);
        DataManager.addZeroTierJsonData("numGroundPyramidIntakeTele", 0);
        DataManager.addZeroTierJsonData("numElevatedPyramidIntakeAuto", 0);
        DataManager.addZeroTierJsonData("numElevatedPyramidIntakeTele", 0);
        DataManager.addZeroTierJsonData("numReturnIntake", 0);
        DataManager.addZeroTierJsonData("numSpilledCubesAuto", 0);
        DataManager.addZeroTierJsonData("numSpilledCubesTele", 0);
        DataManager.addZeroTierJsonData("startingPosition", null);
    }

    public static JSONArray returnJSONArray(List<Integer> intList, List<Boolean> boolList){
        JSONArray jsonArray = new JSONArray();
        for(int i = 0; i < intList.size(); i++){
            try {
                jsonArray.put(intList.get(i), (Object) boolList.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jsonArray;
    }

    public static JSONObject returnJSONObject(List<String> keyList, List<Object> valueList){
        JSONObject tempObject = new JSONObject();
        for(int i = 0; i < keyList.size(); i++){
            try {
                tempObject.put(keyList.get(i), valueList.get(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return  tempObject;
    }
}
