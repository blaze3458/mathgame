package com.xgames.mathgame;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.util.VisibleForTesting;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class LobbyActivity extends AppCompatActivity {


    private static final String TAG = "LobbyActivity";
    private static final String LOBBY = "Lobby";
    private static final String REQUESTS = "Requests";
    private static final String GAMEROOM = "GameRoom";
    private static final String FROM = "From";
    private static final String TO = "To";
    private static final String REQUEST_STATUS_WAIT = "wait";
    private static final String REQUEST_STATUS_DECLINE = "decline";
    private static final String REQUEST_STATUS_ACCEPT = "accept";
    private static final String RESPONSEREQUEST = "The player\'s response is expected.";

    User user;
    String sessionKey;
    //[FIX_ACCEPT_CHALLENGE]
    String checkLastStatus;
    //[FIX_ACCEPT_CHALLENGE]

    ListView lPlayerList;
    TextView tUserName;
    ImageView iUserImage;
    ProgressDialog mProgressDialog,requestProgress;
    AlertDialog.Builder requestAlert;
    FirebaseDatabase mDatabase;
    private ValueEventListener playerListener,requestListener,responseListener;
    playerListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        initializeIntent();
        initializeElements();
        initializeFirebase();
    }

    @Override
    public void onStop(){
        leaveLobby();
        finish();
        super.onStop();
    }

    @Override
    public void onBackPressed(){
        leaveLobby();
        startActivity(new Intent(this,MainMenuActivity.class));
    }

    private void initializeIntent(){
        user = (User) getIntent().getSerializableExtra("User");
        sessionKey = getIntent().getStringExtra("SessionKey");
    }

    private void initializeElements(){
        tUserName = findViewById(R.id.userName);
        iUserImage = findViewById(R.id.userPhoto);
        lPlayerList = findViewById(R.id.playerList);

        tUserName.setText(user.userName);
        Picasso.with(this).load(user.photoUrl).into(iUserImage);
    }

    private void initializeFirebase(){
        mDatabase = FirebaseDatabase.getInstance();

        initializePlayerListListener();
        initializeRequestListener();
        initializeResponseListener();
    }

    private void initializePlayerListListener(){
        DatabaseReference mRef = mDatabase.getReference();

        playerListener = mRef.child(LOBBY).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG,"initializePlayerListListener:DataChange");
                ArrayList<User> playerList = new ArrayList<>();
                for(DataSnapshot data : dataSnapshot.getChildren()){
                    User eUser = data.getValue(User.class);
                    playerList.add(eUser);
                }
                setPlayerList(playerList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeRequestListener(){
        DatabaseReference mRef = mDatabase.getReference();

        requestListener = mRef.child(REQUESTS).orderByChild(TO).equalTo(user.userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    Requests r = data.getValue(Requests.class);
                    //[FIX_ACCEPT_CHALLENGE]
                    checkLastStatus = r.Status;
                    //[FIX_ACCEPT_CHALLENGE]
                    Log.d(TAG,"initializeRequestListener =>"+data.getKey());
                    if(r.Status.equals(REQUEST_STATUS_WAIT))
                        showRequest(data.getKey(),r);
                    else if(r.Status.equals(REQUEST_STATUS_DECLINE))
                        closeRequest();
                    else if(r.Status.equals(REQUEST_STATUS_ACCEPT))
                        startGameRequest(data.getKey(),r);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void initializeResponseListener(){
        DatabaseReference mRef = mDatabase.getReference();

        responseListener = mRef.child(REQUESTS).orderByChild(FROM).equalTo(user.userUID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot data : dataSnapshot.getChildren()) {
                    Requests r = data.getValue(Requests.class);
                    Log.d(TAG,"initializeResponseListener =>"+data.getKey());
                    if(r.Status.equals(REQUEST_STATUS_DECLINE))
                        removeRequest(data.getKey());
                    else if(r.Status.equals(REQUEST_STATUS_ACCEPT))
                        startGameResponse(data.getKey(),r);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showRequest(final String key,final Requests requests){
        requestAlert = new AlertDialog.Builder(this);
        requestAlert.setMessage(requests.FromName+" Challenged you!");


        requestAlert.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(checkLastStatus.equals(REQUEST_STATUS_WAIT) || !checkLastStatus.equals(REQUEST_STATUS_DECLINE))
                    acceptChallenge(key,requests);
            }
        });

        requestAlert.setNegativeButton("Decline", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                declineChallenge(key,requests);
            }
        });
        requestAlert.setCancelable(false);
        requestAlert.show();
    }

    private void closeRequest(){
        Toast.makeText(this,"Should be close",Toast.LENGTH_SHORT).show();
    }

    private void acceptChallenge(String key,Requests requests){
        DatabaseReference mRef = mDatabase.getReference();

        requests.Status = REQUEST_STATUS_ACCEPT;
        mRef.child(REQUESTS).child(key).setValue(requests);
    }

    private void declineChallenge(String key,Requests requests){
        DatabaseReference mRef = mDatabase.getReference();

        requests.Status = REQUEST_STATUS_DECLINE;
        mRef.child(REQUESTS).child(key).setValue(requests);
    }

    private void setPlayerList(ArrayList<User> playerList){
        adapter = new playerListAdapter(playerList);
        lPlayerList.setAdapter(adapter);
    }

    private void removeRequest(String key){
        DatabaseReference mRef = mDatabase.getReference();
        mRef.child(REQUESTS).child(key).removeValue();
        closeRequestProgress();
    }

    private void requestPlayGame(User user){
        showProgressDialog(RESPONSEREQUEST);
        checkPlayerFrom(user);
    }

    private void checkPlayerFrom(final User user){
        DatabaseReference mRef = mDatabase.getReference();

        mRef.child(REQUESTS).orderByChild(FROM).equalTo(user.userUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG,"checkPlayerFrom =>"+dataSnapshot.getValue());
                hideProgressDialog();
                if(dataSnapshot.getValue() != null){
                    Toast.makeText(LobbyActivity.this,"The player made a request.",Toast.LENGTH_SHORT).show();
                    return;
                }
                showProgressDialog(RESPONSEREQUEST);
                checkPlayerTo(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkPlayerTo(final User user){
        DatabaseReference mRef = mDatabase.getReference();

        mRef.child(REQUESTS).orderByChild(TO).equalTo(user.userUID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d(TAG,"checkPlayerTo =>"+dataSnapshot.getValue());
                hideProgressDialog();
                if(dataSnapshot.getValue() != null){
                    Toast.makeText(LobbyActivity.this,"A request has been made to the player.",Toast.LENGTH_SHORT).show();
                    return;
                }
                sendRequestPlayer(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendRequestPlayer(User rUser){
        DatabaseReference mRef = mDatabase.getReference();
        final Requests newRequest = new Requests(user.userUID,user.userName,rUser.userUID,rUser.userName,REQUEST_STATUS_WAIT);
        mRef.child(REQUESTS).push().setValue(newRequest,new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError,
                                   DatabaseReference databaseReference) {
                String uniqueKey = databaseReference.getKey();
                Log.d(TAG,"sendRequestPlayer=>"+uniqueKey);
                showRequestProgress(uniqueKey,newRequest);
            }
        });


    }

    private void showRequestProgress(final String key, final Requests requests){
        requestProgress = new ProgressDialog(this);
        requestProgress.setMessage("You challenged to "+requests.ToName);
        requestProgress.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                declineChallenge(key,requests);
                dialog.dismiss();
            }
        });

        requestProgress.setCancelable(false);
        requestProgress.show();
    }

    private void closeRequestProgress(){
        requestProgress.dismiss();
    }

    private void leaveLobby(){
        DatabaseReference mRef = mDatabase.getReference();

        mRef.child(LOBBY).child(sessionKey).removeValue();
        mRef.child(LOBBY).removeEventListener(playerListener);
        mRef.child(REQUESTS).orderByChild(TO).equalTo(user.userUID).removeEventListener(requestListener);
        mRef.child(REQUESTS).orderByChild(FROM).equalTo(user.userUID).removeEventListener(responseListener);

    }

    private void startGameRequest(String key,Requests requests){
        DatabaseReference mRef = mDatabase.getReference();

        GameStatus gameStatus = new GameStatus("EMPTY",0,0,0);
        PlayerStatus playerStatus = new PlayerStatus("NULL","NULL");
        GameRoom newGRoom = new GameRoom(requests.From,requests.FromName,requests.To,requests.ToName,0,0,gameStatus,gameStatus,playerStatus);
        mRef.child(GAMEROOM).child(key).setValue(newGRoom);
        leaveLobby();
        Intent i = new Intent(this,MultiMathGameActivity.class);
        i.putExtra("GameRoom",newGRoom);
        i.putExtra("GameRoomKey",key);
        i.putExtra("User",user);
        i.putExtra("Rank","TO");
        startActivity(i);
    }

    private void startGameResponse(String key,Requests requests){
        DatabaseReference mRef = mDatabase.getReference();
        mRef.child(REQUESTS).child(key).removeValue();

        GameStatus gameStatus = new GameStatus("EMPTY",0,0,0);
        PlayerStatus playerStatus = new PlayerStatus("NULL","NULL");
        GameRoom newGRoom = new GameRoom(requests.From,requests.FromName,requests.To,requests.ToName,0,0,gameStatus,gameStatus,playerStatus);
        leaveLobby();
        Intent i = new Intent(this,MultiMathGameActivity.class);
        i.putExtra("GameRoom",newGRoom);
        i.putExtra("GameRoomKey",key);
        i.putExtra("User",user);
        i.putExtra("Rank","FROM");
        startActivity(i);
    }

    @VisibleForTesting
    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(message);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private class playerListAdapter extends BaseAdapter{

        ArrayList<User> playerList;

        public playerListAdapter(ArrayList<User> playerList){this.playerList = playerList;}

        @Override
        public int getCount() { return playerList.size();}

        @Override
        public Object getItem(int position) {return playerList.get(position);}

        @Override
        public long getItemId(int position) {return position;}

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = getLayoutInflater().inflate(R.layout.player_list_element,null);

            ImageView playerImage = v.findViewById(R.id.playerImage);
            TextView playerName = v.findViewById(R.id.playerUsername);
            final User nUser = this.playerList.get(position);

            playerName.setText(nUser.userName);
            Picasso.with(LobbyActivity.this).load(nUser.photoUrl).into(playerImage);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(nUser.userUID.equals(user.userUID))
                        return;

                    requestPlayGame(nUser);
                }
            });
            return v;
        }
    }
}
