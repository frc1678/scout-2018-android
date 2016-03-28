package com.example.evan.scout;


import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.FormatFlagsConversionMismatchException;
import java.util.Iterator;
import java.util.List;

public class Utils {
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

    public static String serializeObject(Object object) throws Exception {
        return serializeObject(object, object.getClass(), new JSONObject()).toString();
    }

    private static JSONObject serializeObject(Object object, Class clazz, JSONObject previousData) throws Exception {
        for (Field field : clazz.getDeclaredFields()) {
            try {
                field.setAccessible(true);
                Object value =  field.get(object);
                if (value instanceof List) {
                    JSONArray blah = serializeList((List)value);
                    previousData.put(field.getName(), blah);
                } else {
                    previousData.put(field.getName(), value);
                }
            } catch (NullPointerException npe) {continue;}
        }
        if (clazz.getSuperclass().getSuperclass() != null) {
            return serializeObject(object, clazz.getSuperclass(), previousData);
        }
        return previousData;
    }

    private static JSONArray serializeList(List list) {
        JSONArray array = new JSONArray();
        for (Object object : list) {
            if (object instanceof List) {
                array.put(serializeList((List)object));
            } else {
                array.put(object);
            }
        }
        return array;
    }

    public static Object deserializeObject(String serializedObject, Object object) throws Exception {
        JSONObject data = new JSONObject(serializedObject);
        Iterator<String> keys = data.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (object.getClass().getDeclaredField(key).getType().isAssignableFrom(FormatFlagsConversionMismatchException.class)){
                Log.i("ASD", key);
            }
            if (object.getClass().getDeclaredField(key).getType().isAssignableFrom(List.class)) {
                setField(object, key, deserializeList(data.getJSONArray(key)));
            }
            setField(object, key, data.get(key));
        }
        return object;
    }

    private static List deserializeList(JSONArray list) throws Exception {
        List data = new ArrayList();
        for (int i = 0; i < list.length(); i++) {
            Object value = list.get(i);
            if (value instanceof JSONArray) {
                data.add(deserializeList((JSONArray)value));
            } else if (value instanceof JSONObject) {
                deserializeObject(value.toString(), new TwoValueStruct<Float, Boolean>());
            } else {
                data.add(list.get(i));
            }
        }
        return data;
    }

    public static class TwoValueStruct<K, V> extends JSONObject {
        public TwoValueStruct(){}
        public TwoValueStruct(K value1, V value2) {this.value1 = value1; this.value2 = value2;
            try {
                super.put("value1", value1);
                super.put("value2", value2);
            } catch (JSONException jsone) {
                //dont care
            }
        }
        public K value1; public V value2;
    }
}
