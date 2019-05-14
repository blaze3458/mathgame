package com.xgames.mathgame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class SingleMathGameActivity extends AppCompatActivity implements View.OnClickListener,View.OnTouchListener{

    private static final String START = "START";
    private static final String FINISH = "FINISH";
    private static final String STOP = "STOP";
    private static final String ADDITION = "addition";
    private static final String SUBTRACTION = "subtraction";
    private static final String MULTIPLICATION = "multiplication";
    private static final String DIVISION = "division";


    EditText generic,generic2,generic3;
    TextView tScore,tRemainTime,tPointer;
    KeyboardView keyboardView;
    Button bPlayAgain;

    Settings settings;
    HighScore easyScore,mediumScore,hardScore;

    GameThread thread;

    ArrayList<String> operations;

    int random1,random2,emptyCell,score;

    int minValue,maxValue;
    String currentOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_math_game);

        operations = new ArrayList<>();

        initializeElements();
        initalizeGameSettings();
        startGame();

        easyScore = new HighScore("HighScoreEasy",this);
        mediumScore = new HighScore("HighScoreMedium",this);
        hardScore = new HighScore("HighScoreHard",this);
    }

    private void initializeElements(){
        generic = findViewById(R.id.generic);
        generic2 = findViewById(R.id.generic2);
        generic3 = findViewById(R.id.generic3);
        tScore = findViewById(R.id.score);
        tRemainTime = findViewById(R.id.remainTime);
        tPointer = findViewById(R.id.pointer);
        LinearLayout myLayout = findViewById(R.id.key_layout);
        keyboardView = new KeyboardView(this,generic);
        bPlayAgain = findViewById(R.id.playAgainButton);

        keyboardView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        myLayout.addView(keyboardView);

        findViewById(R.id.okButton).setOnClickListener(this);
        findViewById(R.id.playAgainButton).setOnClickListener(this);

        generic.setOnTouchListener(this);
        generic2.setOnTouchListener(this);
        generic3.setOnTouchListener(this);

        generic.setEnabled(false);
        generic2.setEnabled(false);
        generic3.setEnabled(false);
        bPlayAgain.setVisibility(View.INVISIBLE);
    }

    private void initalizeGameSettings(){
        settings = new Settings(this);

        if(settings.getAddition())
            operations.add(ADDITION);
        if(settings.getSubtraction())
            operations.add(SUBTRACTION);
        if(settings.getMultiplication())
            operations.add(MULTIPLICATION);
        if(settings.getDivision())
            operations.add(DIVISION);

        minValue = settings.getMinValue();
        maxValue = settings.getMaxValue();
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if(i == R.id.okButton){
            if(thread.getGameStatus().equals(START)) {
                String res = keyboardView.getCustomInputText();
                if(res.length() < 0 || res.equals("") || res.length() >= 11)
                    return;

                int result = Integer.parseInt(res);
                compareResults(calculateResult(result));

                generateRandomQuestion();
            }
        }else if(i == R.id.playAgainButton){
            playAgain();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int i = v.getId();

        if(i == R.id.generic) {
            int inType = generic.getInputType();
            generic.setInputType(InputType.TYPE_NULL);
            generic.onTouchEvent(event);
            generic.setInputType(inType);
        }else if(i == R.id.generic2) {
            int inType = generic.getInputType();
            generic2.setInputType(InputType.TYPE_NULL);
            generic2.onTouchEvent(event);
            generic2.setInputType(inType);
        }else if(i == R.id.generic3) {
            int inType = generic3.getInputType();
            generic3.setInputType(InputType.TYPE_NULL);
            generic3.onTouchEvent(event);
            generic3.setInputType(inType);
        }
        return true;
    }

    private void generateRandomQuestion(){
        int randomOperation = getRandom(0,operations.size());
        String operation = this.currentOperation = operations.get(randomOperation);

        int random_1 = this.random1 = getRandom(minValue,maxValue);
        int random_2 = this.random2 = getRandom(minValue,maxValue);
        int emptyCell = this.emptyCell = getRandom(0,3);

        writeQuestion(operation,random_1,random_2,emptyCell);
    }

    private void writeQuestion(String operation,int random1,int random2,int emptyCell){
        writeOperation(operation);
        writeNumbers(operation,random1,random2,emptyCell);
    }

    private void writeOperation(String operation){
        if(operation.equals(ADDITION))
            tPointer.setText(R.string.addition);
        else if(operation.equals(SUBTRACTION))
            tPointer.setText(R.string.subtraction);
        else if(operation.equals(MULTIPLICATION))
            tPointer.setText(R.string.multiplication);
        else
            tPointer.setText(R.string.division);
    }

    private void writeNumbers(String operation,int random1,int random2,int emptyCell){
        int big = getBig(random1,random2);
        int small = getSmall(random1,random2);

        if(emptyCell == 0){
            generic.setEnabled(true);
            generic.requestFocus();
            generic.setText(null);
            keyboardView.setEditText(generic);
            if(operation.equals(ADDITION) || operation.equals(MULTIPLICATION)) {
                generic3.setText(String.valueOf(big));
                generic3.setEnabled(false);

                generic2.setText(String.valueOf(small));
                generic2.setEnabled(false);
            }else{
                generic3.setText(String.valueOf(random1));
                generic3.setEnabled(false);

                generic2.setText(String.valueOf(random2));
                generic2.setEnabled(false);
            }
        }
        else if(emptyCell == 1){
            generic2.setEnabled(true);
            generic2.requestFocus();
            generic2.setText(null);
            keyboardView.setEditText(generic2);
            if(operation.equals(ADDITION) || operation.equals(MULTIPLICATION)) {
                generic3.setText(String.valueOf(big));
                generic3.setEnabled(false);

                generic.setText(String.valueOf(small));
                generic.setEnabled(false);
            }else if(operation.equals(SUBTRACTION) || operation.equals(DIVISION)){
                generic3.setText(String.valueOf(small));
                generic3.setEnabled(false);

                generic.setText(String.valueOf(big));
                generic.setEnabled(false);
            }else{
                generic3.setText(String.valueOf(random1));
                generic3.setEnabled(false);

                generic.setText(String.valueOf(random2));
                generic.setEnabled(false);
            }
        }
        else{
            generic3.setEnabled(true);
            generic3.requestFocus();
            generic3.setText(null);
            keyboardView.setEditText(generic3);
            if(operation.equals(SUBTRACTION) || operation.equals(DIVISION)){
                generic.setText(String.valueOf(big));
                generic.setEnabled(false);

                generic2.setText(String.valueOf(small));
                generic2.setEnabled(false);
            }else{
                generic.setText(String.valueOf(random1));
                generic.setEnabled(false);

                generic2.setText(String.valueOf(random2));
                generic2.setEnabled(false);
            }
        }
    }

    private boolean calculateResult(int userResult){
        int big = getBig(random1,random2);
        int small = getSmall(random1,random2);

        int result = 0;

        if(emptyCell == 0){

            if(currentOperation.equals(ADDITION)) {
                result = big - small;
            }else if(currentOperation.equals(MULTIPLICATION)){
                result =  big / small;
            }else if(currentOperation.equals(DIVISION)) {
                result = random1 * random2;
            }else{
                result = random1 + random2;
            }
        }
        else if(emptyCell == 1){

            if(currentOperation.equals(ADDITION) || currentOperation.equals(SUBTRACTION)) {
                result = big-small;
            }else if(currentOperation.equals(MULTIPLICATION)){
                result = big / small;
            }else{
                result = big / small;
                return calculateCloseNumbers(userResult,result);
            }

        }
        else{
            if(currentOperation.equals(SUBTRACTION)){
                result = big - small;
            }else if(currentOperation.equals(MULTIPLICATION)){
                result = big * small;
            }else if(currentOperation.equals(DIVISION)){
                result = big / small;
            }
            else{
                result = random1 + random2;
            }
        }
        return userResult == result;
    }

    private boolean calculateCloseNumbers(int userResult,int result){
        int big = getBig(userResult,result);
        int small = getSmall(userResult,result);

        if((big - small) <= 1)
            return true;

        return false;
    }

    private void compareResults(boolean trueResult){
        if(trueResult){
            score +=1;
            tScore.setText(String.valueOf(score));
        }
    }

    private void playAgain(){
        generic.setEnabled(false);
        generic2.setEnabled(false);
        generic3.setEnabled(false);
        bPlayAgain.setVisibility(View.INVISIBLE);
        tScore.setText("0");

        startGame();
    }

    private void startGame(){
        thread = new GameThread(settings);
        score = 0;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                generateRandomQuestion();
                thread.start();
            }
        }, 3000);
    }

    private void finishGame(){
        bPlayAgain.setVisibility(View.VISIBLE);
        thread.interrupt();
        setScore();
    }

    private void setScore(){

        if(operations.size() <= 2){
            if(easyScore.getScore(9) < score)
                insertScore("EASY");
            //easyScore.insertNewScore("Deneme",score);
        }
        else if(operations.size() > 2 && ((maxValue - minValue) > 5 && maxValue < 20)){
            if(mediumScore.getScore(9) < score)
                insertScore("MEDIUM");
            //mediumScore.insertNewScore("Deneme",score);
        }
        else if(operations.size() == 4 && maxValue > 20){
            if(hardScore.getScore(9) < score)
                insertScore("HARD");
            //hardScore.insertNewScore("Deneme",score);
        }
    }

    private void insertScore(final String table){

        View v = getLayoutInflater().inflate(R.layout.username_input,null);
        final EditText input = v.findViewById(R.id.userName);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Enter a username");
        alertDialog.setView(v);
        alertDialog.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userName = input.getText().toString();
                if(table.equals("EASY"))
                    easyScore.insertNewScore(userName,score);
                else if(table.equals("MEDIUM"))
                    mediumScore.insertNewScore(userName,score);
                else if(table.equals("HARD"))
                    hardScore.insertNewScore(userName,score);
            }
        });
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alertDialog.setCancelable(false);
        alertDialog.show();
    }


    private class GameThread extends Thread{

        Settings settings;

        int remainTime;
        String gameStatus;

        public GameThread(Settings settings){
            this.settings = settings;
            gameStatus = STOP;
        }

        @Override
        public void run(){
            gameStatus = START;
            remainTime = this.settings.getTime();
            while(remainTime > 0){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        remainTime--;
                        tRemainTime.setText(String.valueOf(remainTime));
                    }
                });

                try{
                    Thread.sleep(1000);
                }
                catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    finishGame();
                }
            });
            gameStatus = FINISH;
        }

        public String getGameStatus(){ return gameStatus;}
    }

    private int getRandom(int low,int high){
        Random r = new Random();
        int result = r.nextInt(high-low) + low;

        return result;
    }

    private int getBig(int b,int t){
        if(b > t)
            return b;
        return t;
    }

    private int getSmall(int b,int t){
        if(b < t)
            return b;
        return t;
    }
}
