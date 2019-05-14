package com.xgames.mathgame;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.util.VisibleForTesting;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "MainMenuActivity";
    private static final String LOBBY = "Lobby";

    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private FirebaseDatabase mDatabase;

    private ProgressDialog mProgressDialog;
    private ConstraintLayout background;
    private TextView tUsername;
    private Button bSignIn,bSignOut;
    private ImageView userImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        initializeElements();

        initializeFirebase();
    }

    @Override
    public void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();

        updateUI(currentUser);

        setColor();
    }

    @Override
    public void onBackPressed(){
        AlertDialog.Builder alert =  new AlertDialog.Builder(this);
        alert.setMessage("Do you want to exit game?");
        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });
        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alert.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
            }
        }
    }

    private void initializeElements(){
        background = findViewById(R.id.background);
        tUsername = findViewById(R.id.username);
        bSignIn =findViewById(R.id.signInButton);
        bSignOut = findViewById(R.id.signOutButton);
        userImage = findViewById(R.id.userImage);

        bSignIn.setOnClickListener(this);
        bSignOut.setOnClickListener(this);
        findViewById(R.id.singleButton).setOnClickListener(this);
        findViewById(R.id.multiplayerButton).setOnClickListener(this);
        findViewById(R.id.settingsButton).setOnClickListener(this);
        findViewById(R.id.highScoresButton).setOnClickListener(this);
    }

    private void initializeFirebase(){
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        showProgressDialog();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            currentUser = mAuth.getCurrentUser();
                            updateUI(currentUser);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.background), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                        }
                        hideProgressDialog();
                    }
                });
    }

    @VisibleForTesting
    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
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

    public void setColor(){
        ColorDrawable[] color = {new ColorDrawable(Color.WHITE), new ColorDrawable(0xFF227CE2)};
        TransitionDrawable trans = new TransitionDrawable(color);
        background.setBackgroundDrawable(trans);
        trans.startTransition(7000);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        currentUser = null;
                        updateUI(null);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.signInButton) {
            signIn();
        } else if (i == R.id.signOutButton) {
            signOut();
        } else if (i == R.id.singleButton) {
            startSingleGame();
        }else if(i == R.id.multiplayerButton){
            startMultiplayerGame();
        }else if(i == R.id.highScoresButton){
            startHighScores();
        }else if(i == R.id.settingsButton){
            startSettings();
        }
        else{
            return;
        }
    }

    private void updateUI(FirebaseUser user){
        if(isUserLogin()){
            tUsername.setVisibility(View.VISIBLE);
            bSignOut.setVisibility(View.VISIBLE);
            bSignIn.setVisibility(View.INVISIBLE);

            tUsername.setText(user.getDisplayName());
            Picasso.with(this).load(user.getPhotoUrl()).into(userImage);
        }
        else{
            tUsername.setVisibility(View.INVISIBLE);
            bSignOut.setVisibility(View.INVISIBLE);
            userImage.setVisibility(View.INVISIBLE);
            bSignIn.setVisibility(View.VISIBLE);
        }
    }

    private void startSingleGame(){
        startActivity(new Intent(this,SingleMathGameActivity.class));
    }

    private void startMultiplayerGame(){
        if(!isUserLogin()){
            signIn();
            return;
        }

        View v = getLayoutInflater().inflate(R.layout.username_input,null);
        final EditText input = v.findViewById(R.id.userName);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage("Enter a username");
        alertDialog.setView(v);
        alertDialog.setPositiveButton("Enter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userName = input.getText().toString();
                showProgressDialog();
                enterLobby(userName.trim());
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

    private void startSettings(){
        startActivity(new Intent(this,SettingsActivity.class));
    }

    private void startHighScores(){ startActivity(new Intent(this,HighScoresActivity.class));
    }

    private void enterLobby(String userName){
        if(userName.isEmpty() || userName.length() <= 0) {
            Toast.makeText(this,"UserName is empty",Toast.LENGTH_SHORT).show();
            hideProgressDialog();
            return;
        }
        String uid = currentUser.getUid();
        String photoUrl = currentUser.getPhotoUrl().toString();

        DatabaseReference mRef = mDatabase.getReference();
        final User newUser = new User(uid,userName,photoUrl);

        mRef.child(LOBBY).push().setValue(newUser,new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        String uniqueKey = databaseReference.getKey();
                        Log.d(TAG,"enterLobby:OnComplete");
                        hideProgressDialog();
                        Intent i = new Intent(MainMenuActivity.this,LobbyActivity.class);
                        i.putExtra("User",newUser);
                        i.putExtra("SessionKey",uniqueKey);
                        startActivity(i);

                    }
                });
    }

    private Boolean isUserLogin(){
        return currentUser != null;
    }
}
