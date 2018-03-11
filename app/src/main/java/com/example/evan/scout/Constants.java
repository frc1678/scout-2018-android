package com.example.evan.scout;

import java.util.HashMap;
import java.util.Map;

public class Constants {
    public static final Map<String, String> KEYS_TO_TITLES;
    public static int numCubesFumbledAuto = 0;
    public static int numCubesFumbledTele = 0;
    public static int numGroundIntakeTele= 0;
    public static int numPortalIntakeTele = 0;
    public static int numAlliancePlatformIntakeAuto = 0;
    public static int numAlliancePlatformIntakeTele = 0;
    public static int numOpponentPlatformIntakeAuto= 0;
    public static int numOpponentPlatformIntakeTele = 0;
    public static int numSpilledCubesAuto = 0;
    public static int numSpilledCubesTele = 0;
    public static int numExchageInput = 0;
    public static int numGroundPortalIntakeTele=0;
    public static int numHumanPortalIntakeTele= 0;
    public static int numGroundPyramidIntakeAuto= 0;
    public static int numnGroundPyramidIntakeTele= 0;
    public static int numElevatedPyramidIntakeAuto= 0;
    public static int numElevatedPyramidIntakeTele= 0;
    public static int numReturnIntake= 0;
    public static int totalNumScaleFoul = 0;
    public static final String COLOR_GREEN = "#00c610";
    public static final String COLOR_RED = "#F9613F";
    public static final String COLOR_BLUE = "#53A4F7";
    public static final String COLOR_LIGHTBLUE = "#A1CCF8";
    public static final String COLOR_LIGHTRED = "#FA9B86";
    public static final String COLOR_GREY = "#b3b3b3";

    static {
        //replace with new datapoints
        Map<String, String> initialKeysToTitles = new HashMap<String, String>() {{
            //New constants
            //counters
            put("numCubesFumbledAuto", "Cubes Fumbled");
            put("numCubesFumbledTele", "Cubes Fumbled");
            put("numExchangeInput", "Exchange Input");
            put("numGroundIntakeTele", "Ground Intake");
            put("numGroundPortalIntakeTele", "Ground Portal Intake");
            put("numHumanPortalIntakeTele", "Human Portal Intake");
            put("numReturnIntake", "Return Intake");
            put("numSpilledCubesAuto", "Cubes Spilled");
            put("numSpilledCubesTele", "Cubes Spilled");
            put("totalNumScaleFoul","Scale Fouls");
            //start positions
            put("startingPositionLeft", "Left");
            put("startingPositionCenter", "Center");
            put("startingPositionRight", "Right");
            //platform ids
            put("1", "1");
            put("2", "2");
            put("3", "3");
            put("4", "4");
            put("5", "5");
            put("6", "6");

            put("didGetDisabled", "Disabled");
            put("didGetIncapacitated", "Incap");
            put("didMakeAutoRun", "Auto Run Made");
            put("didCrossAutoZone", "Auto Zone Crossed");
            put("didPark", "Parked");
            put("numOpponentPlatformIntakeAuto", "Opponent Platform Intake Auto");
            put("numOpponentPlatformIntakeTele", "Opponent Platform Intake Tele");
            put("pyramidAttemptAuto", "Pyramid");
            put("pyramidAttemptTele", "Pyramid");
            put("numAlliancePlatformIntakeAuto", "Alliance Platform Intake Auto");
            put("numAlliancePlatformIntakeTele", "Alliance Platform Intake Tele");
            put("allianceSwitchAttemptAuto", "Alliance Switch Attempt Auto");
            put("allianceSwitchAttemptTele", "Alliance Switch Attempt Tele");
            put("opponentSwitchAttemptTele", "Opponent Switch Attempt Tele");
            put("scaleAttemptAuto", "Scale");
            put("scaleAttemptTele", "Scale");
            put("startingPosition", "Starting Position");
            put("climb", "Climbs");
            //put("opponentSwitchAttemptAuto", "Opponent Switch Attempt Auto");
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
