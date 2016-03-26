package com.example.evan.scout;


import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class Utils {
    public static Object getField(Object object, String field) throws Exception {
        List<String> fields = Arrays.asList(field.split("\\."));
        Object parentObject = object;
        for (String nestedField : fields) {
            if (parentObject instanceof List) {
                parentObject = Array.get(parentObject, Integer.parseInt(nestedField));
            } else {
                Field value = parentObject.getClass().getDeclaredField(nestedField);
                value.setAccessible(true);
                parentObject = value.get(parentObject);
            }
        }
        return parentObject;
    }
    public void setField(Object object, String field, Object value) throws Exception {
        List<String> fields = Arrays.asList(field.split("\\."));
        Object parentObject = object;
        for (String nestedField : fields) {
            if (parentObject instanceof List) {
                parentObject = Array.get(parentObject, Integer.parseInt(nestedField));
            } else {
                Field nestedValue = parentObject.getClass().getDeclaredField(nestedField);
                nestedValue.setAccessible(true);
                parentObject = nestedValue.get(parentObject);
            }
        }
    }
    public static class TwoValueStruct<K, V> {
        public TwoValueStruct(){}
        public TwoValueStruct(K value1, V value2) {this.value1 = value1; this.value2 = value2;}
        public K value1; public V value2;
    }
}
