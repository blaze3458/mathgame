package com.xgames.mathgame;

import android.content.Context;
import android.content.SharedPreferences;

public class Settings {

    private int time,minValue,maxValue;
    private Boolean addition,subtraction,multiplication,division,applyChanges;

    SharedPreferences pref;

    public Settings(Context context){
        pref = context.getSharedPreferences("Settings",Context.MODE_PRIVATE);
        loadData();
    }

    public void setDefaultData(){
        SharedPreferences.Editor ed = pref.edit();
        setTime(30);
        setAddition(true);
        setSubtraction(true);
        setMultiplication(false);
        setDivision(false);
        setMinValue(1);
        setMaxValue(10);
        setApplyChanges(false);

        ed.putInt("time",30);
        ed.putBoolean("addition",true);
        ed.putBoolean("subtraction",true);
        ed.putBoolean("multiplication",false);
        ed.putBoolean("division",false);
        ed.putBoolean("applyChanges",false);
        ed.putInt("MinimumValue",1);
        ed.putInt("MaximumValue",10);
        ed.commit();
    }

    private void loadData(){
        time = pref.getInt("time",0);
        addition = pref.getBoolean("addition",false);
        subtraction = pref.getBoolean("subtraction",false);
        multiplication = pref.getBoolean("multiplication",false);
        division = pref.getBoolean("division",false);
        applyChanges = pref.getBoolean("applyChanges",false);
        minValue = pref.getInt("MinimumValue",0);
        maxValue = pref.getInt("MaximumValue",0);
    }

    public void updateData(){
        SharedPreferences.Editor ed = pref.edit();
        ed.putInt("time",time);
        ed.putBoolean("addition",addition);
        ed.putBoolean("subtraction",subtraction);
        ed.putBoolean("multiplication",multiplication);
        ed.putBoolean("division",division);
        ed.putBoolean("applyChanges",applyChanges);
        ed.putInt("MinimumValue",minValue);
        ed.putInt("MaximumValue",maxValue);
        ed.apply();
    }

    public Boolean getApplyChanges() { return applyChanges; }

    public Boolean getAddition() { return addition; }

    public Boolean getDivision() { return division; }

    public Boolean getMultiplication() { return multiplication; }

    public Boolean getSubtraction() { return subtraction; }

    public int getTime() { return time; }

    public int getMinValue() { return minValue; }

    public int getMaxValue() { return maxValue; }

    public void setAddition(Boolean addition) { this.addition = addition; }

    public void setSubtraction(Boolean subtraction) { this.subtraction = subtraction; }

    public void setMultiplication(Boolean multiplication) { this.multiplication = multiplication; }

    public void setDivision(Boolean division) { this.division = division; }

    public void setMaxValue(int maxValue) { this.maxValue = maxValue; }

    public void setMinValue(int minValue) { this.minValue = minValue; }

    public void setApplyChanges(Boolean applyChanges) { this.applyChanges = applyChanges; }

    public void setTime(int time) { this.time = time; }

}
