package com.example.evan.scout;

import java.util.List;
import java.util.Map;

public class DataConverter {
    public static void separateCombinedData(List<List<Map<Long, Boolean>>> combined, List<List<Long>> success, List<List<Long>> fail) {
        for (int i = 0; i < combined.size(); i++) {
            for (int j = 0; j < combined.get(i).size(); i++) {
                Map.Entry<Long, Boolean> firstEntry = combined.get(i).get(j).entrySet().iterator().next();
                if (firstEntry.getValue()) {
                    success.get(i).add(firstEntry.getKey());
                } else {
                    fail.get(i).add(firstEntry.getKey());
                }
            }
        }
    }


    public static void joinSeparatedData(List<List<Long>> success, List<List<Long>> fail, List<List<Map<Long, Boolean>>> combined) {

    }
}
