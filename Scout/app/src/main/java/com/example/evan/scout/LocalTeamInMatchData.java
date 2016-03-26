package com.example.evan.scout;

import java.util.List;

public class LocalTeamInMatchData extends TeamInMatchData {
    public static class DefenseCrossingStruct {
        public Float time;
        public Boolean isSuccessful;
    }
    List<List<Utils.TwoValueStruct<Float, Boolean>>> defenseTimesAuto;
    List<List<Utils.TwoValueStruct<Float, Boolean>>> defenseTimesTele;
    List<Boolean> isBallIntaked;
}
