package com.example.evan.scout;

public class UniversalData {
    private static String uuid;
    private static String superName;
    private static Boolean isOverridden;
    private static String scoutName;
    private static Integer scoutNumber;

    public static synchronized void setScoutNumber(Integer scoutNumber) {UniversalData.scoutNumber = scoutNumber;}
    public static synchronized Integer getScoutNumber() {return scoutNumber;}
    public static synchronized void setScoutName(String scoutName) {UniversalData.scoutName = scoutName;}
    public static synchronized String getScoutName() {return scoutName;}
    public static synchronized void setSuperName(String superName) {UniversalData.superName = superName;}
    public static synchronized String getSuperName() {return superName;}
    public static synchronized void setIsOverridden(Boolean isOverridden) {UniversalData.isOverridden = isOverridden;}
    public static synchronized Boolean getIsOverridden() {return isOverridden;}
    public static synchronized void setUuid(String uuid) {UniversalData.uuid = uuid;}
    public static synchronized String getUuid() {return uuid;}
}
