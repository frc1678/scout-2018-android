package com.example.evan.scout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
    public static final Map<String, String> KEYS_TO_TITLES;
    public static final Map<String, String> LOCAL_KEYS_TO_FIREBASE_KEYS;
    static {
        Map<String, String> initialKeysToTitles = new HashMap<String, String>() {{
            put("didReachAuto", "Reach Completed");
            put("didChallengeTele", "Challenge");
            put("didScaleTele", "Scale");
            put("didGetDisabled", "Disabled");
            put("didGetIncapacitated", "Incap.");
            put("isBallIntaked._NUMBER/6", "_NUMBER Intaked");
            put("numBallsKnockedOffMidlineAuto", "Balls Knocked Off Mid");
            put("numHighShots_MADE_PERIOD", "High Shot");
            put("numLowShots_MADE_PERIOD", "Low Shot");
            put("miss", "Missed");
            put("made", "Made");
            put("numGroundIntakesTele", "Ground Intakes");
            put("numShotsBlockedTele", "Shots Blocked");
            put("defenseTimes_PERIOD._NUMBER/5", "Defense _NUMBER");
        }};
        Map<String, String> initialLocalKeysToFirebaseKeys = new HashMap<String, String>() {{
            put("_SUCCESS/defenseTimes_PERIOD", "successfulDefenseCrossTimes_PERIOD");
            put("_FAIL/defenseTimes_PERIOD", "failedDefenseCrossTimes_PERIOD");
            put("isBallIntaked", "ballsIntakedAuto");
        }};
        /*this is a lot of data, let me explain:
        this is a map of 'structs' to lists
        The first value of the struct is a string representing the starting value of the data
        The second value of the struct is a list of strings representing the ending values of the data
        the second value of the map is a list of method names, as strings, to execute to get the starting data to the ending data
        */
        Map<Utils.TwoValueStruct<String, List<String>>, List<String>> blah = new HashMap<Utils.TwoValueStruct<String, List<String>>, List<String>>() {{

        }};



        //convert constant syntax
        Map<String, String> modKeysToTitles = new HashMap<>(initialKeysToTitles);
        for (Map.Entry<String, String> entry : initialKeysToTitles.entrySet()) {
            String key = entry.getKey();
            if (key.contains("_NUMBER")) {
                Integer limit = Integer.parseInt(key.split("/")[1].substring(0, 1));
                String modKey = key.substring(0, key.indexOf("/")) + key.substring(key.indexOf("/") + 2);
                for (int i = 0; i < limit; i++) {
                    modKeysToTitles.put(modKey.replaceAll("_NUMBER", Integer.toString(i)), entry.getValue().replaceAll("_NUMBER", Integer.toString(i+1)));
                }
                modKeysToTitles.remove(key);
            }
        }
        initialKeysToTitles = new HashMap<>(modKeysToTitles);
        for (Map.Entry<String, String> entry : initialKeysToTitles.entrySet()) {
            String key = entry.getKey();
            if (key.contains("_MADE")) {
                modKeysToTitles.put(key.replaceAll("_MADE", "Made"), entry.getValue());
                modKeysToTitles.put(key.replaceAll("_MADE", "Missed"), entry.getValue());
                modKeysToTitles.remove(key);
            }
        }
        initialKeysToTitles = new HashMap<>(modKeysToTitles);
        for (Map.Entry<String, String> entry : initialKeysToTitles.entrySet()) {
            String key = entry.getKey();
            if (key.contains("_PERIOD")) {
                modKeysToTitles.put(key.replaceAll("_PERIOD", "Auto"), entry.getValue());
                modKeysToTitles.put(key.replaceAll("_PERIOD", "Tele"), entry.getValue());
                modKeysToTitles.remove(key);
            }
        }
        KEYS_TO_TITLES = new HashMap<>(modKeysToTitles);
        Map<String, String> modLocalKeysToFirebaseKeys = new HashMap<>(initialLocalKeysToFirebaseKeys);
        for (Map.Entry<String, String> entry : initialLocalKeysToFirebaseKeys.entrySet()) {
            String key = entry.getKey();
            if (key.contains("_PERIOD")) {
                modLocalKeysToFirebaseKeys.put(key.replaceAll("_PERIOD", "Auto"), entry.getValue());
                modLocalKeysToFirebaseKeys.put(key.replaceAll("_PERIOD", "Tele"), entry.getValue());
            }
            modKeysToTitles.remove(key);
        }
        LOCAL_KEYS_TO_FIREBASE_KEYS = new HashMap<>(modLocalKeysToFirebaseKeys);
    }
}
