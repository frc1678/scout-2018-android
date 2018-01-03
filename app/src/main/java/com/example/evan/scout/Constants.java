package com.example.evan.scout;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final Map<String, String> KEYS_TO_TITLES;
    public static int highShotAuto = 0; //todo change
    public static int lowShotAuto = 0; //todo change
    public static int highShotTele = 0; //todo change
    public static int lowShotTele = 0; //todo change
    public static final String COLOR_GREEN = "#65C423";
    static {
        Map<String, String> initialKeysToTitles = new HashMap<String, String>() {{
            put("didGetDisabled", "Disabled");
            put("didGetIncapacitated", "Incapacitated");
            put("numHoppersUsedAuto", "Hoppers Used Auto"); //todo change
            put("highShotAuto", "highShotAuto"); //todo change
            put("lowShotAuto", "lowShotAuto"); //todo change
            put("highShotTele", "highShotTele"); //todo change
            put("lowShotTele", "lowShotTele"); //todo change
            put("numGroundGearIntakesTele", "Ground Gear Intakes"); //todo change
            put("numHumanGearIntakesTele", "Human Gear Intakes"); //todo change
            put("numGearsFumbledTele", "Gears Fumbled Tele"); //todo change
            put("numGearsEjectedTele", "Gears Ejected Tele"); //todo change
            put("numHoppersUsedTele", "Hoppers Used Tele"); //todo change
            put("gearPlacedAuto", "Gear Placed"); //todo change
            put("gearPlacedTele", "Gear Placed"); //todo change
            put("readyForLiftOff", "Ready For LiftOff"); //todo change
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
    }
}
