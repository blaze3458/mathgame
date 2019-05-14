package com.xgames.mathgame;

import android.os.Parcel;
import android.os.Parcelable;

public class HighScoreData implements Parcelable {
    protected int[] scores = new int[10];
    protected String[] userNames = new String[10];
    protected Boolean inserted;

    public HighScoreData(){}

    protected HighScoreData(Parcel in) {
        scores = in.createIntArray();
        userNames = in.createStringArray();
        byte tmpInserted = in.readByte();
        inserted = tmpInserted == 0 ? null : tmpInserted == 1;
    }

    public static final Creator<HighScoreData> CREATOR = new Creator<HighScoreData>() {
        @Override
        public HighScoreData createFromParcel(Parcel in) {
            return new HighScoreData(in);
        }

        @Override
        public HighScoreData[] newArray(int size) {
            return new HighScoreData[size];
        }
    };

    public String getUserName(int index){return userNames[index];}

    public int getScore(int index){return scores[index];}

    public Boolean isInserted() { return inserted; }

    public void setUserName(int index, String name){ userNames[index] = name; }

    public void setScore(int index,int value){ scores[index] = value;}

    public void setInserted(Boolean flag){inserted = flag;}

    public String[] getUserNames(){return userNames;}

    public int[] getScores(){return scores;}

    public HighScoreData getHighScoreData(){ return this; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(scores);
        dest.writeStringArray(userNames);
        dest.writeByte((byte) (inserted == null ? 0 : inserted ? 1 : 2));
    }
}
