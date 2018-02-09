package com.example.evan.scout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutoActivity extends DataActivity {
    public String activityName(){   return "auto"; }
    public List<String> getToggleData() {
        return formatList(Arrays.asList("didMakeAutoRun", "didCrossAutoZone"));
    }
    public List<String> getCounterData() { return formatList(Arrays.asList("numSpilledCubesAuto", "numCubesFumbledAuto")); }
    public List<String> getSwitchData() { return formatList(Arrays.asList("allianceSwitchAttemptAuto")); }
    public List<String> getScaleData() { return formatList(Arrays.asList("scaleAttemptAuto")); }
    public List<String> getPyramidData() { return formatList(Arrays.asList("pyramidAttemptAuto")); }
    public List<String> getRadioData() { return formatList(Arrays.asList("startPositionRight", "startPositionCenter", "startPositionLeft")); }
    public List<String> getPlatformData(){
        return formatList(Arrays.asList("1", "2", "3", "4", "5", "6"));
    }

    public Integer getToggleXML() { return R.id.autoToggleLayout; }
    public Integer getCounterOneXML() { return R.id.autoCounterLayout; }
    public Integer getCounterTwoXML() { return null; }
    public Integer getAttemptOneXML() { return R.id.autoAttemptLayout; }
    public Integer getAttemptTwoXML() { return null; }
    public Integer getRadioXML() { return R.id.autoStartPositionLayout;}
    public Integer getPlatformOneXML() { return R.id.autoPlatformCubeLayoutOne; }
    public Integer getPlatformTwoXML() { return null; }
    public Integer getEndGameXML() { return null; }
    @Override
    public Class getNextActivityClass() {return TeleopActivity.class;}
    public Class getPreviousActivityClass() {return MainActivity.class;}
    public int getActionBarMenu() {return R.menu.auto_menu;}
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
