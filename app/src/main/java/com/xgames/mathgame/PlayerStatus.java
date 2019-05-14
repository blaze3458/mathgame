package com.xgames.mathgame;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class PlayerStatus implements Serializable {
    public String playerFromStatus,playerToStatus;

    public PlayerStatus(){}

    public PlayerStatus(String playerFromStatus,String playerToStatus){
        this.playerFromStatus = playerFromStatus;
        this.playerToStatus = playerToStatus;
    }
}
