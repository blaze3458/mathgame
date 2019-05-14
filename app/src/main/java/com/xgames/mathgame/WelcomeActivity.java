package com.xgames.mathgame;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class WelcomeActivity extends AppCompatActivity {

    private static final String TAG = "WelcomeActivity";

    Settings settings;
    HighScore easy,medium,hard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        settings = new Settings(this);
        easy = new HighScore("HighScoreEasy",this);
        medium = new HighScore("HighScoreMedium",this);
        hard = new HighScore("HighScoreHard",this);
    }

    @Override
    protected void onStart(){
        super.onStart();

        if(!settings.getApplyChanges())
            settings.setDefaultData();

        if(!easy.isInserted())//EasyScores
            easy.setDefaultData();

        if(!medium.isInserted())//EasyScores
            medium.setDefaultData();

        if(!hard.isInserted())//EasyScores
            hard.setDefaultData();

        openMenu();
    }

    private void openMenu(){

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                    Intent i = new Intent(WelcomeActivity.this, MainMenuActivity.class);
                    startActivity(i);
                }
        }, 1500);
    }
}
