package com.example.evan.scout;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final Map<String, String> KEYS_TO_TITLES;
//    public static int highShotAuto = 0;
//    public static int lowShotAuto = 0;
//    public static int highShotTele = 0;
//    public static int lowShotTele = 0;
    public static int cubesFumbledAuto = 0;
    public static int cubesFumbledTele = 0;
    public static int groundIntakeTele= 0;
    public static int portalIntakeTele = 0;
    public static int bluePlatformIntakeAuto = 0;
    public static int bluePlatformIntakeTele = 0;
    public static int redPlatformIntakeAuto= 0;
    public static int redPlatformIntakeTele = 0;
    public static int spilledCubesAuto = 0;
    public static int spilledCubesTele = 0;
    public static int pyramidIntakeAuto = 0;
    public static int pyramidIntakeTele = 0;
    public static final String COLOR_GREEN = "#65C423";
    public static final String COLOR_RED = "#ff0000";
    public static final String COLOR_BLUE = "#0000ff";
    static {
        //replace with new datapoints
        Map<String, String> initialKeysToTitles = new HashMap<String, String>() {{
            //New constants
            put("didGetDisabled", "Disabled");
            put("didGetIncapacitated", "Incapacitated");
            put("didMakeAutoRun", "Auto Run Made");
            put("didPark", "Parked");
            put("numCubesFumbledAuto", "Cubes Fumbled Auto");
            put("numCubesFumbledTele", "Cubes Fumbled Tele");
            put("numGroundIntakeTele", "Ground Intake Tele");
            put("numPortalIntakeTele", "Portal Intake Tele");
            put("numBluePlatformIntakeAuto", "Blue Platform Intake Auto");
            put("numBluePlatformIntakeTele", "Blue Platform Intake Tele");
            put("numRedPlatformIntakeAuto", "Red Platform Intake Auto");
            put("numRedPlatformIntakeTele", "Red Platform Intake Tele");
            put("redSwitchAttemptAuto", "Red Switch Attempt Auto");
            put("redSwitchAttemptTele", "Red Switch Attempt Tele");
            put("blueSwitchAttemptAuto", "Blue Switch Attempt Auto");
            put("blueSwitchAttemptTele", "Blue Switch Attempt Tele");
            put("scaleAttemptAuto", "Scale Attempt Auto");
            put("scaleAttemptTele", "Scale Attempt Tele");
            put("numExchageInput", "Exchange Inputs");
            put("numSpilledCubesAuto", "Spilled Cubes Auto");
            put("numSpilledCubesTele", "Spilled Cubes Tele");
            put("numPyramidIntakeAuto", "Pyramid Intakes Auto");
            put("numPyramidIntakeTele", "Pyramid Intakes Tele");
            put("climb", "Climbs");

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
