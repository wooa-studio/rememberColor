package com.wooastudio.remember_color;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.LeaderboardsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {

    private static final int RC_LEADERBOARD_UI = 9004;
    private static final int RC_SIGN_IN = 100;

    private TextView mHighestLevel;
    private TextView mLatestLevel;

    private GoogleSignInClient mGoogleSignInClient;
    private LeaderboardsClient mLeaderboardsClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 21) {
            // 21 버전 이상일 때
            getWindow().setStatusBarColor(Color.GRAY);
        }

        RelativeLayout startButtonLayout = (RelativeLayout) findViewById(R.id.startButtonLayout);
        startButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                startActivity(intent);
            }
        });

        RelativeLayout rankingButtonLayout = (RelativeLayout) findViewById(R.id.rankingButtonLayout);
        rankingButtonLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo
                //showLeaderboard();
                signInSilently();
            }
        });

        mHighestLevel = (TextView) findViewById(R.id.highestLevelCount);
        if (Util.getConfigValue(this, "highestLevel") != null) {
            mHighestLevel.setText(Util.getConfigValue(this, "highestLevel"));
        }

        mLatestLevel = (TextView) findViewById(R.id.latestLevelCount);
        if (Util.getConfigValue(this, "latestLevel") != null) {
            mLatestLevel.setText(Util.getConfigValue(this, "latestLevel"));
        }

        // Create the client used to sign in to Google services.
        mGoogleSignInClient = GoogleSignIn.getClient(this,
                new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN).build());

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Util.getConfigValue(this, "highestLevel") != null) {
            mHighestLevel.setText(Util.getConfigValue(this, "highestLevel"));
        }
        if (Util.getConfigValue(this, "latestLevel") != null) {
            mLatestLevel.setText(Util.getConfigValue(this, "latestLevel"));
        }
        //signInSilently();
    }

    private void showLeaderboard(GoogleSignInAccount googleSignInAccount) {

        mLeaderboardsClient = Games.getLeaderboardsClient(this, googleSignInAccount);
        int score = -1;
        if (Util.getConfigValue(this, "highestLevel") != null) {
            score = Integer.parseInt(Util.getConfigValue(this, "highestLevel"));
            if (score > - 1) {
                mLeaderboardsClient.submitScore(getString(R.string.leaderboard_id), score);
            }
        }


        Games.getLeaderboardsClient(this, GoogleSignIn.getLastSignedInAccount(this))
                .getLeaderboardIntent(getString(R.string.leaderboard_id))
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        startActivityForResult(intent, RC_LEADERBOARD_UI);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String message = e.toString();
                        //handleException(e, getString(R.string.leaderboards_exception));
                    }
                });;



    }

    private void signInSilently() {
         //GoogleSignInOptions signInOptions = GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN;
        /*

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (GoogleSignIn.hasPermissions(account, signInOptions.getScopeArray())) {
            // Already signed in.
            // The signed in account is stored in the 'account' variable.
            GoogleSignInAccount signedInAccount = account;
        } else {
            // Haven't been signed-in before. Try the silent sign-in first.
            GoogleSignInClient signInClient = GoogleSignIn.getClient(this, signInOptions);
            signInClient
                    .silentSignIn()
                    .addOnCompleteListener(
                            this,
                            new OnCompleteListener<GoogleSignInAccount>() {
                                @Override
                                public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                                    if (task.isSuccessful()) {
                                        // The signed in account is stored in the task's result.
                                        GoogleSignInAccount signedInAccount = task.getResult();
                                    } else {
                                        // Player will need to sign-in explicitly using via UI.
                                        // See [sign-in best practices](http://developers.google.com/games/services/checklist) for guidance on how and when to implement Interactive Sign-in,
                                        // and [Performing Interactive Sign-in](http://developers.google.com/games/services/android/signin#performing_interactive_sign-in) for details on how to implement
                                        // Interactive Sign-in.
                                        startSignInIntent();
                                    }
                                }
                            });
        }

         */

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            showLeaderboard(task.getResult());
                        } else {
                            //Log.d(TAG, "signInSilently(): failure", task.getException());
                            //onDisconnected();
                            startSignInIntent();
                        }
                    }
                });
    }

    private void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // The signed in account is stored in the result.
                GoogleSignInAccount signedInAccount = result.getSignInAccount();
                //firebaseAuthWithPlayGames(signedInAccount);
            } else {
                String message = result.getStatus().getStatusMessage();
                if (message == null || message.isEmpty()) {
                    //message = getString(R.string.signin_other_error);
                }
                //new AlertDialog.Builder(this).setMessage(message)
                //        .setNeutralButton(android.R.string.ok, null).show();
            }
        }
    }
}
