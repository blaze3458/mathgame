package com.xgames.mathgame;

import android.content.Context;
import android.content.SharedPreferences;

public class HighScore extends HighScoreData {

    SharedPreferences pref;

    public HighScore(String scoreTable,Context context){
        pref = context.getSharedPreferences(scoreTable,Context.MODE_PRIVATE);
        loadData();
    }

    public void setDefaultData(){
        SharedPreferences.Editor ed = pref.edit();
        setUserName(0,"NULL");
        setScore(0,0);
        setUserName(1,"NULL");
        setScore(1,0);
        setUserName(2,"NULL");
        setScore(2,0);
        setUserName(3,"NULL");
        setScore(3,0);
        setUserName(4,"NULL");
        setScore(4,0);
        setUserName(5,"NULL");
        setScore(5,0);
        setUserName(6,"NULL");
        setScore(6,0);
        setUserName(7,"NULL");
        setScore(7,0);
        setUserName(8,"NULL");
        setScore(8,0);
        setUserName(9,"NULL");
        setScore(9,0);

        setInserted(false);

        ed.putString("userName1","NULL");
        ed.putInt("score1",0);
        ed.putString("userName2","NULL");
        ed.putInt("score2",0);
        ed.putString("userName3","NULL");
        ed.putInt("score3",0);
        ed.putString("userName4","NULL");
        ed.putInt("score4",0);
        ed.putString("userName5","NULL");
        ed.putInt("score5",0);
        ed.putString("userName6","NULL");
        ed.putInt("score6",0);
        ed.putString("userName7","NULL");
        ed.putInt("score7",0);
        ed.putString("userName8","NULL");
        ed.putInt("score8",0);
        ed.putString("userName9","NULL");
        ed.putInt("score9",0);
        ed.putString("userName10","NULL");
        ed.putInt("score10",0);

        ed.putBoolean("inserted",false);
        ed.commit();
    }

    private void loadData(){
        setUserName(0,pref.getString("userName1","NULL"));
        setScore(0,pref.getInt("score1",0));
        setUserName(1,pref.getString("userName2","NULL"));
        setScore(1,pref.getInt("score2",0));
        setUserName(2,pref.getString("userName3","NULL"));
        setScore(2,pref.getInt("score3",0));
        setUserName(3,pref.getString("userName4","NULL"));
        setScore(3,pref.getInt("score4",0));
        setUserName(4,pref.getString("userName5","NULL"));
        setScore(4,pref.getInt("score5",0));
        setUserName(5,pref.getString("userName6","NULL"));
        setScore(5,pref.getInt("score6",0));
        setUserName(6,pref.getString("userName7","NULL"));
        setScore(6,pref.getInt("score7",0));
        setUserName(7,pref.getString("userName8","NULL"));
        setScore(7,pref.getInt("score8",0));
        setUserName(8,pref.getString("userName9","NULL"));
        setScore(8,pref.getInt("score9",0));
        setUserName(9,pref.getString("userName10","NULL"));
        setScore(9,pref.getInt("score10",0));
        setInserted(pref.getBoolean("inserted",false));
    }

    public void insertNewScore(String newUserName,int newScore){
        if(newScore < scores[9])
            return;
        int index = insertScore(newScore);
        insertUserName(newUserName,index);
        updateData();
    }

    private int insertScore(int newScore){
        int index = 0;
        int[] tempArr =  new int[10];
        System.arraycopy(scores,0,tempArr,0,scores.length);
        for(int i = 9; i>= 0; i--){
            int score = scores[i];
            if(newScore >= score)
                index = i;
        }

        scores[index] = newScore;

        for(int k = index; k < 9; k++){
            int temp = tempArr[k];
            scores[k+1] = temp;
        }
        return index;
    }

    private void insertUserName(String newUserName,int index){
        String[] temArr = new String[10];
        System.arraycopy(userNames,0,temArr,0,userNames.length);
        userNames[index] = newUserName;

        for(int i = index; i<9; i++){
            String temp = temArr[i];
            userNames[i+1] = temp;
        }
    }

    public void updateData(){
        SharedPreferences.Editor ed = pref.edit();

        setInserted(true);

        ed.putString("userName1",getUserName(0));
        ed.putInt("score1",getScore(0));
        ed.putString("userName2",getUserName(1));
        ed.putInt("score2",getScore(1));
        ed.putString("userName3",getUserName(2));
        ed.putInt("score3",getScore(2));
        ed.putString("userName4",getUserName(3));
        ed.putInt("score4",getScore(3));
        ed.putString("userName5",getUserName(4));
        ed.putInt("score5",getScore(4));
        ed.putString("userName6",getUserName(5));
        ed.putInt("score6",getScore(5));
        ed.putString("userName7",getUserName(6));
        ed.putInt("score7",getScore(6));
        ed.putString("userName8",getUserName(7));
        ed.putInt("score8",getScore(7));
        ed.putString("userName9",getUserName(8));
        ed.putInt("score9",getScore(8));
        ed.putString("userName10",getUserName(9));
        ed.putInt("score10",getScore(9));
        ed.putBoolean("inserted",true);
        ed.apply();
    }

    public HighScoreData getHighScoreData(){
        return super.getHighScoreData();
    }
}
