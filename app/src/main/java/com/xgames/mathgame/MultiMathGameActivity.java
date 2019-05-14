package com.xgames.mathgame;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Random;

public class MultiMathGameActivity extends AppCompatActivity implements View.OnTouchListener,View.OnClickListener {

    private static final String TAG = "MultiMathGameActivity";

    private static final String START = "START";
    private static final String FINISH = "FINISH";
    private static final String STOP = "STOP";
    private static final String RANK_FROM = "FROM";
    private static final String RANK_TO = "TO";
    private static final String GAMEROOM = "GameRoom";
    private static final String LOBBY = "Lobby";
    private static final String FROMSTATUSROOM = "FromGameStatus";
    private static final String TOSTATUSROOM = "ToGameStatus";
    private static final String PLAYERSTATUS = "PlayerStatus";
    private static final String ADDITION = "ADDITION";
    private static final String SUBTRACTION = "SUBTRACTION";
    private static final String INGAME = "INGAME";
    private static final String GAMEFINISH = "GAMEFINISH";
    private static final String LEFTGAME = "LEFTGAME";
    //[UNUSED]
    private static final String MULTIPLICATION = "MULTIPLICATION";
    private static final String DIVISION = "DIVISION";
    //[UNUSED]

    private User user;
    private GameRoom gameRoom;
    private String gameRoomKey,rank;
    private String currentOperation;
    private int random1,random2,emptyCell,tScore,fScore;

    KeyboardView keyboardView;
    TextView tRemainTime,tFromName,tToName,tFromScore,tToScore,tFromSide,tToSide,tFromPointer,tToPointer;
    EditText fromGeneric,fromGeneric2,fromGeneric3;
    EditText toGeneric,toGeneric2,toGeneric3;
    ProgressDialog progressDialog;

    FirebaseDatabase mDatabase;
    FirebaseUser currentUser;
    ValueEventListener toListener,fromListener,toScoreListener,fromScoreListener,gameListener;

    ArrayList<String> operations;
    int minValue,maxValue;

    GameThread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_math_game);

        initializeIntent();
        initializeElements();
        initializeGameSettings();
        initializeFirebase();
        //startGame();
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setMessage("Do you want to return lobby?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                returnLobby(user.userName);
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        alert.setCancelable(false);
        alert.show();
    }

    private void initializeElements(){
        tRemainTime = findViewById(R.id.remain_time);
        tFromName = findViewById(R.id.fromName);
        tFromScore = findViewById(R.id.fromScore);
        tToName = findViewById(R.id.toName);
        tToScore = findViewById(R.id.toScore);
        tFromSide = findViewById(R.id.from_side);
        tToSide = findViewById(R.id.to_side);
        tFromPointer = findViewById(R.id.fromPointer);
        tToPointer = findViewById(R.id.toPointer);

        fromGeneric = findViewById(R.id.fromGeneric);
        fromGeneric2 = findViewById(R.id.fromGeneric2);
        fromGeneric3 = findViewById(R.id.fromGeneric3);
        toGeneric = findViewById(R.id.toGeneric);
        toGeneric2 = findViewById(R.id.toGeneric2);
        toGeneric3 = findViewById(R.id.toGeneric3);
        LinearLayout myLayout = findViewById(R.id.keypad);
        keyboardView = new KeyboardView(this,fromGeneric);
        keyboardView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        myLayout.addView(keyboardView);

        tFromName.setText(String.format("%s's Score :", gameRoom.playerFromName));
        tToName.setText(String.format("%s's Score :", gameRoom.playerToName));
        tFromSide.setText(gameRoom.playerFromName);
        tToSide.setText(gameRoom.playerToName);

        fromGeneric.setOnTouchListener(this);
        fromGeneric2.setOnTouchListener(this);
        fromGeneric3.setOnTouchListener(this);
        toGeneric.setOnTouchListener(this);
        toGeneric2.setOnTouchListener(this);
        toGeneric3.setOnTouchListener(this);

        findViewById(R.id.okButton).setOnClickListener(this);

        tFromScore.setText("0");
        tToScore.setText("0");
    }

    private void initializeIntent(){
        user = (User)getIntent().getSerializableExtra("User");
        gameRoom = (GameRoom)getIntent().getSerializableExtra("GameRoom");
        gameRoomKey = getIntent().getStringExtra("GameRoomKey");
        rank = getIntent().getStringExtra("Rank");
    }

    private void initializeGameSettings(){
        operations = new ArrayList<>();
        operations.add(ADDITION);
        operations.add(SUBTRACTION);

        minValue = 1;
        maxValue = 10;
    }

    private void initializeFirebase(){
        mDatabase = FirebaseDatabase.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if(rank.equals(RANK_FROM)) {
            initializeToListener();
            setPlayerStatus(RANK_FROM,INGAME);
        }
        else {
            initializeFromListener();
            setPlayerStatus(RANK_TO,INGAME);
        }
        initializeGameListener();
    }

    private void initializeToListener(){
        DatabaseReference mRef = mDatabase.getReference();

        toListener = mRef.child(GAMEROOM).child(gameRoomKey).child(TOSTATUSROOM).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GameStatus status = dataSnapshot.getValue(GameStatus.class);
                writeQuestion(status.Operation,status.Random1,status.Random2,status.EmptyCell,RANK_TO);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        toScoreListener = mRef.child(GAMEROOM).child(gameRoomKey).child("playerToScore").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tScore = dataSnapshot.getValue(int.class);
                tToScore.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeFromListener(){
        DatabaseReference mRef = mDatabase.getReference();

        fromListener = mRef.child(GAMEROOM).child(gameRoomKey).child(FROMSTATUSROOM).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GameStatus status = dataSnapshot.getValue(GameStatus.class);
                writeQuestion(status.Operation,status.Random1,status.Random2,status.EmptyCell,RANK_FROM);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        fromScoreListener = mRef.child(GAMEROOM).child(gameRoomKey).child("playerFromScore").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fScore = dataSnapshot.getValue(int.class);
                tFromScore.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeGameListener(){
        DatabaseReference mRef = mDatabase.getReference();
        gameListener = mRef.child(GAMEROOM).child(gameRoomKey).child(PLAYERSTATUS).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                PlayerStatus status = dataSnapshot.getValue(PlayerStatus.class);
                if(status.playerFromStatus.equals(INGAME) && status.playerToStatus.equals(INGAME)) {
                    hideProgress();
                    startGame();
                }
                else if((status.playerFromStatus.equals(GAMEFINISH) && status.playerToStatus.equals(INGAME) && rank.equals(RANK_FROM))||
                        (status.playerFromStatus.equals(INGAME) && status.playerToStatus.equals(GAMEFINISH) && rank.equals(RANK_TO))){
                    waitOpponent();
                }
                else if(status.playerFromStatus.equals(GAMEFINISH) && status.playerToStatus.equals(GAMEFINISH)){
                    hideProgress();
                    showWinner();
                }
                else if((status.playerToStatus.equals(LEFTGAME)  && rank.equals(RANK_FROM))||
                        status.playerFromStatus.equals(LEFTGAME) && rank.equals(RANK_TO)){
                    opponentLeftGame();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void setPlayerStatus(String rank,String value){
        DatabaseReference mRef = mDatabase.getReference();
        if(rank.equals(RANK_FROM))
            mRef.child(GAMEROOM).child(gameRoomKey).child(PLAYERSTATUS).child("playerFromStatus").setValue(value);
        else
            mRef.child(GAMEROOM).child(gameRoomKey).child(PLAYERSTATUS).child("playerToStatus").setValue(value);
    }

    private void generateRandomQuestion(){
        int randomOperation = getRandom(0,operations.size());
        String operation = this.currentOperation = operations.get(randomOperation);

        int random_1 = this.random1 = getRandom(minValue,maxValue);
        int random_2 = this.random2 = getRandom(minValue,maxValue);
        int emptyCell = this.emptyCell = getRandom(0,3);

        writeOnDatabase(operation,random_1,random_2,emptyCell,rank);
        writeQuestion(operation,random_1,random_2,emptyCell,rank);
    }

    private void writeOnDatabase(String operation,int random1,int random2,int emptyCell,String rank){
        DatabaseReference mRef = mDatabase.getReference();

        GameStatus newStatus = new GameStatus(operation,emptyCell,random1,random2);

        if(rank.equals(RANK_FROM))
            mRef.child(GAMEROOM).child(gameRoomKey).child(FROMSTATUSROOM).setValue(newStatus);
        else
            mRef.child(GAMEROOM).child(gameRoomKey).child(TOSTATUSROOM).setValue(newStatus);
    }

    private void setScoreOnData(int score){
        DatabaseReference mRef = mDatabase.getReference();
        if(rank.equals(RANK_FROM))
            mRef.child(GAMEROOM).child(gameRoomKey).child("playerFromScore").setValue(score);
        else
            mRef.child(GAMEROOM).child(gameRoomKey).child("playerToScore").setValue(score);
    }

    private void writeQuestion(String operation,int random1,int random2,int emptyCell,String rank){
        if(rank.equals(RANK_FROM)) {
            writeOperationFrom(operation);
            writeNumbersFrom(operation, random1, random2, emptyCell);
        }
        else {
            writeOperationTo(operation);
            writeNumbersTo(operation, random1, random2, emptyCell);
        }
    }

    private void writeOperationFrom(String operation){
        if(operation.equals(ADDITION))
            tFromPointer.setText(R.string.addition);
        else if(operation.equals(SUBTRACTION))
            tFromPointer.setText(R.string.subtraction);
        else
            tFromPointer.setText("unf");
    }

    private void writeOperationTo(String operation){
        if(operation.equals(ADDITION))
            tToPointer.setText(R.string.addition);
        else if(operation.equals(SUBTRACTION))
            tToPointer.setText(R.string.subtraction);
        else
            tToPointer.setText("unf");
    }

    private void writeNumbersFrom(String operation,int random1,int random2,int emptyCell){
        int big = getBig(random1,random2);
        int small = getSmall(random1,random2);

        if(emptyCell == 0){
            fromGeneric.setEnabled(true);
            fromGeneric.requestFocus();
            fromGeneric.setText(null);
            if(rank.equals(RANK_FROM))
                keyboardView.setEditText(fromGeneric);

            if(operation.equals(ADDITION)) {
                fromGeneric3.setText(String.valueOf(big));
                fromGeneric3.setEnabled(false);

                fromGeneric2.setText(String.valueOf(small));
                fromGeneric2.setEnabled(false);
            }else{
                fromGeneric3.setText(String.valueOf(random1));
                fromGeneric3.setEnabled(false);

                fromGeneric2.setText(String.valueOf(random2));
                fromGeneric2.setEnabled(false);
            }
        }
        else if(emptyCell == 1){
            fromGeneric2.setEnabled(true);
            fromGeneric2.requestFocus();
            fromGeneric2.setText(null);
            if(rank.equals(RANK_FROM))
                keyboardView.setEditText(fromGeneric2);

            if(operation.equals(ADDITION)) {
                fromGeneric3.setText(String.valueOf(big));
                fromGeneric3.setEnabled(false);

                fromGeneric.setText(String.valueOf(small));
                fromGeneric.setEnabled(false);
            }else if(operation.equals(SUBTRACTION)){
                fromGeneric3.setText(String.valueOf(small));
                fromGeneric3.setEnabled(false);

                fromGeneric.setText(String.valueOf(big));
                fromGeneric.setEnabled(false);
            }else{
                fromGeneric3.setText(String.valueOf(random1));
                fromGeneric3.setEnabled(false);

                fromGeneric.setText(String.valueOf(random2));
                fromGeneric.setEnabled(false);
            }
        }
        else{
            fromGeneric3.setEnabled(true);
            fromGeneric3.requestFocus();
            fromGeneric3.setText(null);
            if(rank.equals(RANK_FROM))
                keyboardView.setEditText(fromGeneric3);

            if(operation.equals(SUBTRACTION)){
                fromGeneric.setText(String.valueOf(big));
                fromGeneric.setEnabled(false);

                fromGeneric2.setText(String.valueOf(small));
                fromGeneric2.setEnabled(false);
            }else{
                fromGeneric.setText(String.valueOf(random1));
                fromGeneric.setEnabled(false);

                fromGeneric2.setText(String.valueOf(random2));
                fromGeneric2.setEnabled(false);
            }
        }
    }

    private void writeNumbersTo(String operation,int random1,int random2,int emptyCell){
        int big = getBig(random1,random2);
        int small = getSmall(random1,random2);

        if(emptyCell == 0){
            toGeneric.setEnabled(true);
            toGeneric.requestFocus();
            toGeneric.setText(null);
            if(rank.equals(RANK_TO))
                keyboardView.setEditText(toGeneric);
            if(operation.equals(ADDITION)) {
                toGeneric3.setText(String.valueOf(big));
                toGeneric3.setEnabled(false);

                toGeneric2.setText(String.valueOf(small));
                toGeneric2.setEnabled(false);
            }else{
                toGeneric3.setText(String.valueOf(random1));
                toGeneric3.setEnabled(false);

                toGeneric2.setText(String.valueOf(random2));
                toGeneric2.setEnabled(false);
            }
        }
        else if(emptyCell == 1){
            toGeneric2.setEnabled(true);
            toGeneric2.requestFocus();
            toGeneric2.setText(null);
            if(rank.equals(RANK_TO))
                keyboardView.setEditText(toGeneric2);

            if(operation.equals(ADDITION)) {
                toGeneric3.setText(String.valueOf(big));
                toGeneric3.setEnabled(false);

                toGeneric.setText(String.valueOf(small));
                toGeneric.setEnabled(false);
            }else if(operation.equals(SUBTRACTION)){
                toGeneric3.setText(String.valueOf(small));
                toGeneric3.setEnabled(false);

                toGeneric.setText(String.valueOf(big));
                toGeneric.setEnabled(false);
            }else{
                toGeneric3.setText(String.valueOf(random1));
                toGeneric3.setEnabled(false);

                toGeneric.setText(String.valueOf(random2));
                toGeneric.setEnabled(false);
            }
        }
        else{
            toGeneric3.setEnabled(true);
            toGeneric3.requestFocus();
            toGeneric3.setText(null);
            if(rank.equals(RANK_TO))
                keyboardView.setEditText(toGeneric3);

            if(operation.equals(SUBTRACTION)){
                toGeneric.setText(String.valueOf(big));
                toGeneric.setEnabled(false);

                toGeneric2.setText(String.valueOf(small));
                toGeneric2.setEnabled(false);
            }else{
                toGeneric.setText(String.valueOf(random1));
                toGeneric.setEnabled(false);

                toGeneric2.setText(String.valueOf(random2));
                toGeneric2.setEnabled(false);
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
            if(rank.equals(RANK_FROM)) {
                fScore+= 1;
                tFromScore.setText(String.valueOf(fScore));
                setScoreOnData(fScore);
            }
            else {
                tScore += 1;
                tToScore.setText(String.valueOf(tScore));
                setScoreOnData(tScore);
            }
        }
    }

    private void startGame(){
        thread = new GameThread();
        tScore = 0;
        fScore = 0;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                generateRandomQuestion();
                thread.start();
            }
        }, 3000);
    }

    private void finishGame(){
        thread.interrupt();
        if(rank.equals(RANK_FROM))
            setPlayerStatus(RANK_FROM,GAMEFINISH);
        else
            setPlayerStatus(RANK_TO,GAMEFINISH);
    }

    private void showWinner(){
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        if(tScore > fScore){
            String winner = tToSide.getText().toString();
            alert.setMessage("Winner is "+winner);
        }
        else if(tScore == fScore) {
            alert.setMessage("Draw");
        }
        else{
            String winner = tFromSide.getText().toString();
            alert.setMessage("Winner is "+winner);
        }
        alert.show();
    }

    private void waitOpponent(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Waiting Opponent");
        progressDialog.show();
    }

    private void hideProgress(){
        if(progressDialog!= null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void opponentLeftGame(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Your opponent left game.");
        progressDialog.setCancelable(false);
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Exit Game", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                returnLobby(user.userName);
                finishGame();
                dialog.dismiss();
            }
        });
        progressDialog.show();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int i = v.getId();

        if(i == R.id.fromGeneric) {
            setDisableSoftKeyboad(fromGeneric,event);
        }else if(i == R.id.fromGeneric2) {
            setDisableSoftKeyboad(fromGeneric2,event);
        }else if(i == R.id.fromGeneric3) {
            setDisableSoftKeyboad(fromGeneric3,event);
        }else if(i == R.id.toGeneric) {
            setDisableSoftKeyboad(toGeneric,event);
        }else if(i == R.id.toGeneric2) {
            setDisableSoftKeyboad(toGeneric2,event);
        }else if(i == R.id.toGeneric3) {
            setDisableSoftKeyboad(toGeneric3,event);
        }
        return true;
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
        }
    }

    private void returnLobby(String userName){
        if(userName.isEmpty())
            return;

        String uid = currentUser.getUid();
        String photoUrl = currentUser.getPhotoUrl().toString();

        DatabaseReference mRef = mDatabase.getReference();

        if(rank.equals(RANK_TO)){
            mRef.child(GAMEROOM).child(gameRoomKey).child(FROMSTATUSROOM).removeEventListener(fromListener);
            mRef.child(GAMEROOM).child(gameRoomKey).child("playerFromScore").removeEventListener(fromScoreListener);
        }
        else {
            mRef.child(GAMEROOM).child(gameRoomKey).child(TOSTATUSROOM).removeEventListener(toListener);
            mRef.child(GAMEROOM).child(gameRoomKey).child("playerToScore").removeEventListener(toScoreListener);
        }

        mRef.child(GAMEROOM).child(gameRoomKey).child(PLAYERSTATUS).removeEventListener(gameListener);

        final User newUser = new User(uid,userName,photoUrl);

        setPlayerStatus(rank,LEFTGAME);
        mRef.child(LOBBY).push().setValue(newUser,new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError,
                                   DatabaseReference databaseReference) {
                String uniqueKey = databaseReference.getKey();
                Log.d(TAG,"enterLobby:OnComplete");
                Intent i = new Intent(MultiMathGameActivity.this,LobbyActivity.class);
                i.putExtra("User",newUser);
                i.putExtra("SessionKey",uniqueKey);
                startActivity(i);

            }
        });
    }

    private void setDisableSoftKeyboad(EditText t,MotionEvent event){
        int inType = t.getInputType();
        t.setInputType(InputType.TYPE_NULL);
        t.onTouchEvent(event);
        t.setInputType(inType);
    }

    private class GameThread extends Thread{

        int remainTime;
        String gameStatus;

        public GameThread(){
            gameStatus = STOP;
        }

        @Override
        public void run(){
            gameStatus = START;
            remainTime = 90;
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
