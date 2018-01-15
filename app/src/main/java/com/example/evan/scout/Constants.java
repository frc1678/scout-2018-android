package com.example.evan.scout;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final Map<String, String> KEYS_TO_TITLES;
    public static int cubesFumbledAuto = 0;
    public static int cubesFumbledTele = 0;
    public static int groundIntakeTele= 0;
    public static int portalIntakeTele = 0;
    public static int alliancePlatformIntakeAuto = 0;
    public static int alliancePlatformIntakeTele = 0;
    public static int opponentPlatformIntakeAuto= 0;
    public static int opponentPlatformIntakeTele = 0;
    public static int spilledCubesAuto = 0;
    public static int spilledCubesTele = 0;
    public static int exchageInput = 0;
    public static int groundPortalIntakeTele=0;
    public static int humanPortalIntakeTele= 0;
    public static int groundPyramidIntakeAuto= 0;
    public static int groundPyramidIntakeTele= 0;
    public static int elevatedPyramidIntakeAuto= 0;
    public static int elevatedPyramidIntakeTele= 0;
    public static int returnIntake= 0;
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
            put("numOpponentPlatformIntakeAuto", "Blue Platform Intake Auto");
            put("numOpponentPlatformIntakeTele", "Blue Platform Intake Tele");
            put("numCubesFumbledAuto", "Cubes Fumbled Auto");
            put("numCubesFumbledTele", "Cubes Fumbled Tele");
            put("numExchageInput", "Exchange Inputs");
            put("numGroundIntakeTele", "Ground Intake Tele");
            put("numGroundPortalIntakeTele", "Ground Portal Intake Tele");
            put("numHumanPortalIntakeTele", "Human Portal Intake Tele");
            put("numGroundPyramidIntakeAuto", "Ground Pyramid Intakes Auto");
            put("numGroundPyramidIntakeTele", "Ground Pyramid Intakes Tele");
            put("numElevatedPyramidIntakeAuto", "Elevated Pyramid Intakes Auto");
            put("numElevatedPyramidIntakeTele", "Elevated Pyramid Intakes Tele");
            put("numAlliancePlatformIntakeAuto", "Red Platform Intake Auto");
            put("numAlliancePlatformIntakeTele", "Red Platform Intake Tele");
            put("numReturnIntake", "Return Intake");
            put("allianceSwitchAttemptAuto", "Red Switch Attempt Auto");
            put("allianceSwitchAttemptTele", "Red Switch Attempt Tele");
            put("opponentSwitchAttemptAuto", "Blue Switch Attempt Auto");
            put("opponentSwitchAttemptTele", "Blue Switch Attempt Tele");
            put("scaleAttemptAuto", "Scale Attempt Auto");
            put("scaleAttemptTele", "Scale Attempt Tele");
            put("numSpilledCubesAuto", "Spilled Cubes Auto");
            put("numSpilledCubesTele", "Spilled Cubes Tele");
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
