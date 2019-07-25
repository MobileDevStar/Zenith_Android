package com.storeyfilms.zenith;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

public class SplashActivity extends AppCompatActivity {

    private VideoView   m_videoView;
    private Button      m_butLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        m_videoView = (VideoView)findViewById(R.id.vv_splash);
        m_butLogin = (Button)findViewById(R.id.but_login1);

       // AssetFileDescriptor afd = getAssets().openFd("xyz/age.mp4");
        //.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());

        int dd = getResources().getIdentifier("title_login_480_sound",
                "raw", getPackageName());

        Uri video = Uri.parse("android.resource://com.storeyfilms.zenith/" + Integer.toString(dd));
        //Uri video = Uri.parse("file:///android_asset/title_login_480_sound.mp4");
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
                m_butLogin.setVisibility(View.VISIBLE);
            }
        });

        m_butLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
