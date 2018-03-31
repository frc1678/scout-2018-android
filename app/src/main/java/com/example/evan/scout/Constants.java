package com.example.evan.scout;

import java.util.ArrayList;
import java.util.Arrays;
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
            put("totalNumScaleFoul","Scale Fouls");
            put("didGetDisabled", "Disabled");
            put("didGetIncapacitated", "Incap");
            put("didMakeAutoRun", "Auto Run Made");
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

    public static final ArrayList<String> nestedKeys = new ArrayList<>(Arrays.asList(
            "allianceSwitchAttemptTele",
            "allianceSwitchAttemptAuto",
            "climb",
            "opponentSwitchAttemptTele",
            "scaleAttemptTele",
            "scaleAttemptAuto"
    ));
    public static final ArrayList<String> unnestedKeyWithArrayValue = new ArrayList<>(Arrays.asList(
            "alliancePlatformIntakeAuto",
            "alliancePlatformIntakeTele",
            "opponentPlatformIntakeTele"

    ));
//
    public static final String scoutdata = "{\n" +
            "\t\"1678Q15-12\": {\n" +
            "\t\t\"numCubesFumbledAuto\": 0,\n" +
            "\t\t\"didMakeAutoRun\": true,\n" +
            "\t\t\"scoutName\": \"Erik\",\n" +
            "\t\t\"numGroundIntakeTele\": 6,\n" +
            "\t\t\"matchNumber\": 15,\n" +
            "\t\t\"allianceSwitchAttemptTele\": [{\n" +
            "\t\t\t\"status\": \"opponentOwned\",\n" +
            "\t\t\t\"layer\": 2,\n" +
            "\t\t\t\"endTime\": 100.4,\n" +
            "\t\t\t\"startTime\": 96.37,\n" +
            "\t\t\t\"didSucceed\": true\n" +
            "\t\t}],\n" +
            "\t\t\"climb\": [{\n" +
            "\t\t\t\"activeLift\": {\n" +
            "\t\t\t\t\"numRobotsLifted\": 1,\n" +
            "\t\t\t\t\"partnerLiftType\": \"passive\",\n" +
            "\t\t\t\t\"didFailToLift\": false,\n" +
            "\t\t\t\t\"didClimb\": true,\n" +
            "\t\t\t\t\"startTime\": 114.64,\n" +
            "\t\t\t\t\"didSucceed\": true,\n" +
            "\t\t\t\t\"endTime\": 123.46\n" +
            "\t\t\t}\n" +
            "\t\t}],\n" +
            "\t\t\"numCubesFumbledTele\": 1,\n" +
            "\t\t\"numGroundPyramidIntakeTele\": 0,\n" +
            "\t\t\"numReturnIntake\": 0,\n" +
            "\t\t\"alliancePlatformIntakeAuto\": [false, false, false, false, false, false],\n" +
            "\t\t\"startingPosition\": \"center\",\n" +
            "\t\t\"didPark\": false,\n" +
            "\t\t\"numSpilledCubesAuto\": 0,\n" +
            "\t\t\"opponentSwitchAttemptTele\": [{\n" +
            "\t\t\t\"endTime\": 42.88,\n" +
            "\t\t\t\"startTime\": 40.37,\n" +
            "\t\t\t\"didSucceed\": false\n" +
            "\t\t}, {\n" +
            "\t\t\t\"status\": \"opponentOwned\",\n" +
            "\t\t\t\"layer\": 1,\n" +
            "\t\t\t\"endTime\": 62.1,\n" +
            "\t\t\t\"startTime\": 57.87,\n" +
            "\t\t\t\"didSucceed\": true\n" +
            "\t\t}, {\n" +
            "\t\t\t\"status\": \"opponentOwned\",\n" +
            "\t\t\t\"layer\": 1,\n" +
            "\t\t\t\"endTime\": 76.32,\n" +
            "\t\t\t\"startTime\": 71.32,\n" +
            "\t\t\t\"didSucceed\": true\n" +
            "\t\t}, {\n" +
            "\t\t\t\"status\": \"opponentOwned\",\n" +
            "\t\t\t\"layer\": 1,\n" +
            "\t\t\t\"endTime\": 84.18,\n" +
            "\t\t\t\"startTime\": 80.56,\n" +
            "\t\t\t\"didSucceed\": true\n" +
            "\t\t}],\n" +
            "\t\t\"numGroundPyramidIntakeAuto\": 1,\n" +
            "\t\t\"numExchangeInput\": 0,\n" +
            "\t\t\"totalNumScaleFoul\": 0,\n" +
            "\t\t\"numSpilledCubesTele\": 0,\n" +
            "\t\t\"didGetDisabled\": false,\n" +
            "\t\t\"alliancePlatformIntakeTele\": [true, true, true, false, false, false],\n" +
            "\t\t\"numHumanPortalIntakeTele\": 0,\n" +
            "\t\t\"didGetIncapacitated\": false,\n" +
            "\t\t\"scaleAttemptTele\": [{\n" +
            "\t\t\t\"status\": \"balanced\",\n" +
            "\t\t\t\"layer\": 1,\n" +
            "\t\t\t\"endTime\": 12.5,\n" +
            "\t\t\t\"startTime\": 10.78,\n" +
            "\t\t\t\"didSucceed\": true\n" +
            "\t\t}, {\n" +
            "\t\t\t\"status\": \"balanced\",\n" +
            "\t\t\t\"layer\": 1,\n" +
            "\t\t\t\"endTime\": 20.74,\n" +
            "\t\t\t\"startTime\": 18.39,\n" +
            "\t\t\t\"didSucceed\": true\n" +
            "\t\t}, {\n" +
            "\t\t\t\"status\": \"owned\",\n" +
            "\t\t\t\"layer\": 1,\n" +
            "\t\t\t\"endTime\": 28.45,\n" +
            "\t\t\t\"startTime\": 26.82,\n" +
            "\t\t\t\"didSucceed\": true\n" +
            "\t\t}],\n" +
            "\t\t\"opponentPlatformIntakeTele\": [false, false, false, false, false, false],\n" +
            "\t\t\"numGroundPortalIntakeTele\": 0,\n" +
            "\t\t\"allianceSwitchAttemptAuto\": [{\n" +
            "\t\t\t\"status\": \"balanced\",\n" +
            "\t\t\t\"layer\": 1,\n" +
            "\t\t\t\"endTime\": 6.98,\n" +
            "\t\t\t\"startTime\": 2.86,\n" +
            "\t\t\t\"didSucceed\": true\n" +
            "\t\t}, {\n" +
            "\t\t\t\"status\": \"owned\",\n" +
            "\t\t\t\"layer\": 1,\n" +
            "\t\t\t\"endTime\": 14.08,\n" +
            "\t\t\t\"startTime\": 11.09,\n" +
            "\t\t\t\"didSucceed\": true\n" +
            "\t\t}],\n" +
            "\t\t\"teamNumber\": 1678,\n" +
            "\t\t\"numElevatedPyramidIntakeTele\": 0,\n" +
            "\t\t\"numElevatedPyramidIntakeAuto\": 0\n" +
            "\t}\n" +
            "}";



    //replace with new datapoints
    public static Map<String, String> compressKeys = new HashMap<String, String>() {{
        //New constants
        put("layer", "J");
        put("scaleAttemptAuto", "$");
        put("numCubesFumbledAuto", "F");
        put("didMakeAutoRun", "V");
        put("scoutName", "e");
        put("activeLift", "Q");
        put("numGroundIntakeTele", "g");
        put("matchNumber", "E");
        put("soloClimb", "P");
        put("allianceSwitchAttemptTele", "h");
        put("climb", "G");
        put("numCubesFumbledTele", "q");
        put("numGroundPyramidIntakeTele", "n");
        put("numReturnIntake", "c");
        put("numRobotsLifted", "w");
        put("partnerLiftType", "C");
        put("didFailToLift", "j");
        put("alliancePlatformIntakeAuto", "o");
        put("startingPosition", "m");
        put("didSucceed", "K");
        put("status", "r");
        put("didPark", "B");
        put("numSpilledCubesAuto", "Y");
        put("assistedClimb", "k");
        put("opponentSwitchAttemptTele", "l");
        put("numGroundPyramidIntakeAuto", "i");
        put("numExchangeInput", "L");
        put("didClimb", "v");
        put("totalNumScaleFoul", "z");
        put("startTime", "f");
        put("numSpilledCubesTele", "Z");
        put("didGetDisabled", "T");
        put("alliancePlatformIntakeTele", "I");
        put("numHumanPortalIntakeTele", "p");
        put("didGetIncapacitated", "y");
        put("endTime", "a");
        put("scaleAttemptTele", "S");
        put("opponentPlatformIntakeTele", "t");
        put("numGroundPortalIntakeTele", "M");
        put("allianceSwitchAttemptAuto", "W");
        put("passiveClimb", "D");
        put("teamNumber", "b");
        put("numElevatedPyramidIntakeTele", "R");
        put("numElevatedPyramidIntakeAuto", "H");
    }};


    //replace with new datapoints
    public static Map<String, String> compressValues = new HashMap<String, String>() {{
        //New constants
        put("both", "X");
        put("right", "O");
        put("center", "d");
        put("opponentOwned", "U");
        put("owned", "u");
        put("passive", "x");
        put("active", "s");
        put("balanced", "A");
        put("left", "N");
        put("true", "1");
        put("false", "0");
        put("assisted", "^");
    }};
}
