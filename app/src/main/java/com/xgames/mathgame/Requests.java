package com.xgames.mathgame;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class Requests {

    public String From,To,Status,FromName,ToName;

    public Requests(){}

    public Requests(String From,String FromName,String To,String ToName,String Status){
        this.From = From;
        this.FromName = FromName;
        this.To = To;
        this.ToName = ToName;
        this.Status = Status;
    }

}
