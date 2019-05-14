package com.xgames.mathgame;

import com.google.firebase.database.IgnoreExtraProperties;

import java.io.Serializable;

@IgnoreExtraProperties
public class User implements Serializable {

    public String userName,photoUrl,userUID;

    public User(){}

    public User(String userUID,String userName,String photoUrl){
        this.userUID = userUID;
        this.userName = userName;
        this.photoUrl = photoUrl;
    }
}
