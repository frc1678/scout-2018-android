package com.example.evan.scout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TeleopActivity extends DataActivity {
    public String activityName(){   return "tele"; }
    public List<String> getToggleData() {
        return formatList(Arrays.asList("didGetIncapacitated", "didGetDisabled"));
    }
    public List<String> getCounterData() {
        return formatList(Arrays.asList("numCubesFumbledTele", "numSpilledCubesTele", "numGroundIntakeTele", "totalNumScaleFoul",
                "numExchangeInput", "numReturnIntake", "numGroundPortalIntakeTele", "numHumanPortalIntakeTele"));
    }
    public List<String> getSwitchData() { return formatList(Arrays.asList("allianceSwitchAttemptTele", "opponentSwitchAttemptTele")); }
    public List<String> getScaleData() { return formatList(Arrays.asList("scaleAttemptTele")); }
    public List<String> getPyramidData() { return formatList(Arrays.asList("pyramidAttemptTele")); }
    public List<String> getRadioData() { return null; }
    public List<String> getPlatformData(){
        return formatList(Arrays.asList("1", "2", "3", "4", "5", "6"));
    }

    public Integer getToggleXML() { return R.id.teleToggleLayout; }
    public Integer getCounterOneXML() { return R.id.teleCounterLayoutOne; }
    public Integer getCounterTwoXML() { return R.id.teleCounterLayoutTwo; }
    public Integer getAttemptOneXML() { return R.id.teleAttemptLayoutOne; }
    public Integer getAttemptTwoXML() { return R.id.teleAttemptLayoutTwo; }
    public Integer getRadioXML() { return null; }
    public Integer getPlatformOneXML() { return R.id.telePlatformCubeLayoutOne; }
    public Integer getPlatformTwoXML() { return R.id.telePlatformCubeLayoutTwo; }
    public Integer getEndGameXML() { return R.id.endGameLayout; }

    public Class getNextActivityClass() {return MainActivity.class;}
    public Class getPreviousActivityClass() {return AutoActivity.class;}
    public int getActionBarMenu() {return R.menu.teleop_menu;}
    private List<String> formatList(List<String> startList) {
        List<String> returnList = new ArrayList<>();
        for (String dataPoint : startList) {
            if (dataPoint.contains("_NUMBER")) {
                Integer limit = Integer.parseInt(dataPoint.split("/")[1].substring(0, 1));
                String modKey = dataPoint.substring(0, dataPoint.indexOf("/")) + dataPoint.substring(dataPoint.indexOf("/") + 2);
                for (int i = 0; i < limit; i++) {
                    returnList.add(modKey.replaceAll("_NUMBER", Integer.toString(i)));
                }
            } else {
                returnList.add(dataPoint);
            }
        }
        return returnList;
    }
}
