package com.storeyfilms.zenith;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.storeyfilms.zenith.doc.LinkInfo;
import com.storeyfilms.zenith.doc.VideoInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private VideoView               m_vvMain;
    private ProgressBar             m_progressBar;

    private MediaController         m_mediaController;

    private GestureDetector         mDetector;

    private ArrayList<VideoInfo>    m_videoList;

    private int                     m_iVideoIndex;

    private VideoInfo               m_curVideoInfo;
    private boolean                 m_blIsFirst;
    private int                     m_iStopPos;
    private String                  m_strPackageName;

    private int                     m_iScreenWidth;
    private int                     m_iScreenHeight;

    private String                  m_strContribute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Bundle extras = getIntent().getExtras();
        m_strContribute = extras.getString("contribute");

        initVideoList();

        m_vvMain = (VideoView)findViewById(R.id.vv_main);
        m_progressBar = (ProgressBar) findViewById(R.id.pb_spinner);
        m_blIsFirst = true;
        m_iVideoIndex = -1;
        m_strPackageName = getPackageName();

        int res_id = getResources().getIdentifier("after_login_video_to_twitter_prize_480",
                "raw", m_strPackageName);
        Uri video = Uri.parse("android.resource://com.storeyfilms.zenith/" + Integer.toString(res_id));
        m_vvMain.setVideoURI(video);

        m_progressBar.setVisibility(View.VISIBLE);

        m_vvMain.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (m_blIsFirst == true && m_iVideoIndex == -1) {
                    startMainVideo();
                } else {
                   
                }
            }
        });


        m_vvMain.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                m_progressBar.setVisibility(View.GONE);
            }
        });

        Point pt = new Point();
        this.getWindow().getWindowManager().getDefaultDisplay().getRealSize(pt);
        m_iScreenWidth = pt.x;
        m_iScreenHeight = pt.y;

        // get the gesture detector
        mDetector = new GestureDetector(this, new MyGestureListener());

        // Add a touch listener to the view
        // The touch listener passes all its events on to the gesture detector
        m_vvMain.setOnTouchListener(touchListener);
    }

    private void startMainVideo() {
        m_blIsFirst = false;
        m_iVideoIndex = 0;

        playLoopVideo();
    }

    private void playLoopVideo() {
        if (m_iVideoIndex >= m_videoList.size() || m_iVideoIndex < 0) {
            return;
        }

        m_curVideoInfo = m_videoList.get(m_iVideoIndex);
        if (m_curVideoInfo == null) return;

        m_progressBar.setVisibility(View.VISIBLE);
    }

    private void playSwipeVideo() {
        if (m_curVideoInfo == null) return;

        m_progressBar.setVisibility(View.VISIBLE);
    }

    private void playLeftVideo() {
        if (m_curVideoInfo == null) return;

        if (m_curVideoInfo.locked == true || m_curVideoInfo.leftLink == null) return;

        m_progressBar.setVisibility(View.VISIBLE);
    }

    private void playRightVideo() {
        if (m_curVideoInfo == null) return;

        if (m_curVideoInfo.rightLink == null) return;

        m_progressBar.setVisibility(View.VISIBLE);
    }

    private void startRightLink() {
        if (m_curVideoInfo == null) return;
        if (m_curVideoInfo.rightLink == null) return;

        Intent i = new Intent(Intent.ACTION_VIEW);
        startActivity(i);
    }

    private void startLeftLink() {
        if (m_curVideoInfo == null) return;
        if (m_curVideoInfo.leftLink == null) return;


    }

    private void replayMainVideo() {
        m_blIsFirst = true;
        m_iVideoIndex = -1;

        m_progressBar.setVisibility(View.VISIBLE);
    }

    private void initVideoList() {
        m_videoList = new ArrayList<VideoInfo>();

        if (!loadVideoData(m_strContribute))  return;
        //if (!loadVideoData("50"))  return;
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getAssets().open("videos.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private boolean loadVideoData(String contribution) {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (m_blIsFirst) {
            m_vvMain.seekTo(m_iStopPos);
            m_vvMain.start();
        } else {
            playLoopVideo();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (m_blIsFirst) {
            m_iStopPos = m_vvMain.getCurrentPosition();
            m_vvMain.pause();
        } else {
            m_vvMain.stopPlayback();
        }
    }


    // This touch listener passes everything on to the gesture detector.
    // That saves us the trouble of interpreting the raw touch events
    // ourselves.
    View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            // pass the events to the gesture detector
            // a return value of true means the detector is handling it
            // a return value of false means the detector didn't
            // recognize the event

            return mDetector.onTouchEvent(event);

        }
    };

    // In the SimpleOnGestureListener subclass you should override
    // onDown and any other gesture that you want to detect.
    class MyGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onDown(MotionEvent event) {
            Log.d("TAG","onDown: ");

            // don't return false here or else none of the other
            // gestures will work
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Log.i("TAG", "onSingleTapConfirmed: ");

            Log.i("TAG_Orientation", Float.toString(e.getOrientation()));
            Log.i("TAG_X", Float.toString(e.getX()));

            if (m_blIsFirst == true && m_iVideoIndex == -1) {
               // startMainVideo();
            } else {
                if (m_curVideoInfo != null) {
                }
            }

            return true;
        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.i("TAG", "onFling: ");
            Log.i("TAGX", Float.toString(velocityX));
            Log.i("TAGY", Float.toString(velocityY));

            if (m_blIsFirst == true && m_iVideoIndex == -1) {
                startMainVideo();
            } else {
                if (m_curVideoInfo != null) {
                }
            }

            return true;
        }
    }
}
