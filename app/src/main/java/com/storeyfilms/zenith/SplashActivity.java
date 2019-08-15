package com.storeyfilms.zenith;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.Image;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.storeyfilms.zenith.async.HttpAsyncTask;
import com.storeyfilms.zenith.doc.User;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class SplashActivity extends AppCompatActivity {

    private final String        TAG = "Authentication";
    private final String        USERID_KEY = "userid";
    private final String        USERNAME_KEY = "username";
    private final String        EMAIL_KEY = "email";
    private final String        PASSWORD_KEY = "password";
    public static final String  CONTRIBUTE_KEY = "contribute";

    public static final String  API_TOKEN = "1";

    private VideoView           m_videoView;
    private ImageView           m_ivButLogin;
    private ImageView           m_ivButSignup;
    private ImageView           m_ivRegister;
    private ImageView           m_ivForgotPassword;
    private ImageView           m_ivDonate;

    private EditText            m_etLoginEmail;
    private EditText            m_etLoginPassword;
    private EditText            m_etSignupUsername;
    private EditText            m_etSignupEmail;
    private EditText            m_etSignupPassword;

    private View                m_vWaiting;

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
        m_etSignupUsername = (EditText)findViewById(R.id.et_signup_username);
        m_etSignupUsername.setText("David Storey");
        m_etSignupEmail = (EditText) findViewById(R.id.et_signup_email);
        m_etSignupPassword = (EditText) findViewById(R.id.et_signup_password);

        m_ivButLogin = (ImageView) findViewById(R.id.iv_but_login);
        m_ivButSignup = (ImageView) findViewById(R.id.iv_but_signup);
        m_ivRegister = (ImageView) findViewById(R.id.iv_register);
        m_ivForgotPassword = (ImageView) findViewById(R.id.iv_forgot_password);
        m_ivDonate = (ImageView) findViewById(R.id.iv_donate);

        m_vWaiting = (View) findViewById(R.id.v_waiting);

        int res_id = getResources().getIdentifier("title_login_480",
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
                onCompletedSplashVideo();
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

        m_ivDonate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                donate();
            }
        });

        m_vWaiting.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });

        m_ivForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPassword();
            }
        });
    }

    

    private boolean checkNetwork() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void firebaseLoginSuccess(String email, String password, FirebaseUser user) {
        String userID = user.getUid();
        String username = user.getDisplayName();

        ///////////////Integrate with Indiegogo///////////////
        new HttpAsyncTask(this).execute(username, email);
    }

    private void signUp() {
        final String username = m_etSignupUsername.getText().toString().trim();
        final String email = m_etSignupEmail.getText().toString().trim();
        final String password = m_etSignupPassword.getText().toString();

        if (username.isEmpty()) {
            Toast.makeText(this, R.string.empty_username, Toast.LENGTH_SHORT).show();
            return;
        }

        if (email.isEmpty()) {
            Toast.makeText(this, R.string.empty_email, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isValidEmail(email)) {
            Toast.makeText(this, R.string.invalide_email, Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, R.string.empty_password, Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(this, R.string.password_len, Toast.LENGTH_SHORT).show();
            return;
        }

        showWaiting(true);
        final FirebaseAuth auth =  FirebaseAuth.getInstance();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                        }    
                    }
                });
    }

    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void signupSuccess(String username, String email, String password, FirebaseUser user) {
        String userID = user.getUid();

        
        new HttpAsyncTask(this).execute(username, email);
/*
        User userInfo = new User("zenith", email, contribute, "");

        DatabaseReference   database = FirebaseDatabase.getInstance().getReference();
        database.child("users").child(userID).setValue(userInfo);
*/
//        updateUI(contribute);
    }


    private void forgotPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password?");
        builder.setMessage("Enter email address");

        String prevEmail = m_etLoginEmail.getText().toString();

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);
        input.setText(prevEmail);

// Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = input.getText().toString();
                if (email.isEmpty()) {
                    Toast.makeText(SplashActivity.this, R.string.empty_email, Toast.LENGTH_SHORT).show();
                    return;
                }

                
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void showWaiting(boolean isWaiting) {
        if (isWaiting) {
            m_vWaiting.setVisibility(View.VISIBLE);
        } else {
            m_vWaiting.setVisibility(View.INVISIBLE);
        }
    }

    private void donate() {
        Intent i = new Intent(Intent.ACTION_VIEW);
        startActivity(i);
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
