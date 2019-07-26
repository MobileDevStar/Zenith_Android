package com.storeyfilms.zenith;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.storeyfilms.zenith.doc.User;

import java.net.InetAddress;

public class SplashActivity extends AppCompatActivity {

    private final String        TAG = "Authentication";
    private final String        USERID_KEY = "userid";
    private final String        EMAIL_KEY = "email";
    private final String        PASSWORD_KEY = "password";
    private final String        CONTRIBUTE_KEY = "contribute";

    private VideoView           m_videoView;
    private ImageView           m_ivButLogin;
    private ImageView           m_ivButSignup;
    private ImageView           m_ivRegister;

    private EditText            m_etLoginEmail;
    private EditText            m_etLoginPassword;
    private EditText            m_etSignupEmail;
    private EditText            m_etSignupPassword;

    private View                m_vLogin;
    private View                m_vSignup;

    private boolean             m_blSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        m_blSignup = false;

        m_videoView = (VideoView)findViewById(R.id.vv_splash);
        m_vLogin = (View)findViewById(R.id.v_login);
        m_vSignup = (View)findViewById(R.id.v_signup);

        m_etLoginEmail = (EditText)findViewById(R.id.et_email);
        m_etLoginPassword = (EditText)findViewById(R.id.et_password);
        m_etSignupEmail = (EditText) findViewById(R.id.et_signup_email);
        m_etSignupPassword = (EditText) findViewById(R.id.et_signup_password);

        m_ivButLogin = (ImageView) findViewById(R.id.iv_but_login);
        m_ivButSignup = (ImageView) findViewById(R.id.iv_but_signup);
        m_ivRegister = (ImageView) findViewById(R.id.iv_register);

        int res_id = getResources().getIdentifier("title_login_480_sound",
                "raw", getPackageName());
        Uri video = Uri.parse("android.resource://com.storeyfilms.zenith/" + Integer.toString(res_id));

        m_videoView.setVideoURI(video);

        m_videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                m_videoView.start();
            }
        });

        m_videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                m_vLogin.setVisibility(View.VISIBLE);
            }
        });

        m_ivButLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logIn();
            }
        });

        m_ivButSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signUp();
            }
        });

        m_ivRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_blSignup = true;
                m_vLogin.setVisibility(View.INVISIBLE);
                m_vSignup.setVisibility(View.VISIBLE);
            }
        });
    }

    private void logIn() {
        final String email = m_etLoginEmail.getText().toString();
        final String password = m_etLoginPassword.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(this, R.string.empty_email, Toast.LENGTH_LONG).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, R.string.empty_password, Toast.LENGTH_LONG).show();
            return;
        }

        if (checkNetwork()) {
            final FirebaseAuth auth =  FirebaseAuth.getInstance();
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                FirebaseUser user = auth.getCurrentUser();
                                firebaseLoginSuccess(user);
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "signInWithEmail:failure", task.getException());
                                Toast.makeText(SplashActivity.this, R.string.auth_failed,
                                        Toast.LENGTH_SHORT).show();
                            }
                            // ...
                        }
                    });
        } else {
            loginLocal(email, password);
        }
    }

    private void loginLocal(String inEmail, String inPassword) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String contribute = sharedPreferences.getString(CONTRIBUTE_KEY, "1");
        String email = sharedPreferences.getString(EMAIL_KEY, "");
        String password = sharedPreferences.getString(PASSWORD_KEY, "");

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.not_signup, Toast.LENGTH_SHORT).show();
        } else {
            if (inEmail.equals(email) && inPassword.equals(password)) {
                updateUI(contribute);
            }
        }
    }

    private boolean checkNetwork() {
        try {
            InetAddress ipAddr = InetAddress.getByName("google.com");
            //You can replace it with your name
            return !ipAddr.equals("");

        } catch (Exception e) {
            return false;
        }
    }

    private void firebaseLoginSuccess(FirebaseUser user) {
        String userID = user.getUid();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String contribute = sharedPreferences.getString(CONTRIBUTE_KEY, "1");

        updateUI(contribute);
    }

    private void signUp() {
        final String email = m_etSignupEmail.getText().toString();
        final String password = m_etSignupPassword.getText().toString();

        if (email.isEmpty()) {
            Toast.makeText(this, R.string.empty_email, Toast.LENGTH_LONG).show();
            return;
        }
        if (password.isEmpty()) {
            Toast.makeText(this, R.string.empty_password, Toast.LENGTH_LONG).show();
            return;
        }

        final FirebaseAuth auth =  FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();

                            signupSuccess(email, password, user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(SplashActivity.this, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }

    private void signupSuccess(String email, String password, FirebaseUser user) {
        String userID = user.getUid();
        String contribute = "1";
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(USERID_KEY, userID);
        editor.putString(EMAIL_KEY, email);
        editor.putString(PASSWORD_KEY, password);
        editor.putString(CONTRIBUTE_KEY, contribute);
        editor.commit();

        User userInfo = new User("zenith", email, contribute, "");

        DatabaseReference   database = FirebaseDatabase.getInstance().getReference();
        database.child("users").child(userID).setValue(userInfo);

        updateUI(contribute);
    }

    private void updateUI(String contribute) {
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra("contribute", contribute);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (m_blSignup) {
            m_blSignup = false;
            m_vLogin.setVisibility(View.VISIBLE);
            m_vSignup.setVisibility(View.INVISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}
