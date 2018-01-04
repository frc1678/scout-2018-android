package com.example.evan.scout;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final Map<String, String> KEYS_TO_TITLES;
    public static int highShotAuto = 0;
    public static int lowShotAuto = 0;
    public static int highShotTele = 0;
    public static int lowShotTele = 0;
    public static final String COLOR_GREEN = "#65C423";
    public static final String COLOR_RED = "#ff0000";
    public static final String COLOR_BLUE = "#0000ff";
    static {
        //replace with new datapoints
        Map<String, String> initialKeysToTitles = new HashMap<String, String>() {{
            put("didStartDisabled", "Disabled");
            put("didBecomeIncapacitated", "Incapacitated");
            put("numHoppersUsedAuto", "Hoppers Used Auto");
            put("highShotAuto", "highShotAuto");
            put("lowShotAuto", "lowShotAuto");
            put("highShotTele", "highShotTele");
            put("lowShotTele", "lowShotTele");
            put("numGroundGearIntakesTele", "Ground Gear Intakes");
            put("numHumanGearIntakesTele", "Human Gear Intakes");
            put("numGearsFumbledTele", "Gears Fumbled Tele");
            put("numGearsEjectedTele", "Gears Ejected Tele");
            put("numHoppersUsedTele", "Hoppers Used Tele");
            put("gearPlacedAuto", "Gear Placed");
            put("gearPlacedTele", "Gear Placed");
            put("readyForLiftOff", "Ready For LiftOff");
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

        KEYS_TO_TITLES = new HashMap<>(modKeysToTitles);
    }
}
