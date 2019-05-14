package com.xgames.mathgame;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class GameRoom implements Serializable {
    public String playerFromID,playerToID,playerFromName,playerToName;
    public long playerFromScore,playerToScore;
    public GameStatus FromGameStatus,ToGameStatus;
    public PlayerStatus PlayerStatus;

    public GameRoom(){}

    public GameRoom(String playerFromID,String playerFromName, String playerToID, String playerToName,
                    long playerFromScore, long playerToScore,
                    GameStatus FromGameStatus,GameStatus ToGameStatus,PlayerStatus PlayerStatus){
        this.playerFromID = playerFromID;
        this.playerFromName = playerFromName;
        this.playerToID = playerToID;
        this.playerToName = playerToName;
        this.playerFromScore = playerFromScore;
        this.playerToScore = playerToScore;
        this.FromGameStatus = FromGameStatus;
        this.ToGameStatus = ToGameStatus;
        this.PlayerStatus = PlayerStatus;
    }
}
