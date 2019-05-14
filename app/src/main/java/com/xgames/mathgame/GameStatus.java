package com.xgames.mathgame;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class GameStatus implements Serializable {
    public String Operation;
    public int EmptyCell,Random1,Random2;


    public GameStatus(){}

    public GameStatus(String Operation,int EmptyCell,int Random1,int Random2){

        this.Operation = Operation;
        this.EmptyCell = EmptyCell;
        this.Random1 = Random1;
        this.Random2 = Random2;
    }
}
