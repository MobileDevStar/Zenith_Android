package com.storeyfilms.zenith;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Point;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.storeyfilms.zenith.doc.LinkInfo;
import com.storeyfilms.zenith.doc.VideoInfo;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private VideoView               m_vvSwipe;
    private VideoView               m_vvPush;
    private Button                  m_butLogin;

    private MediaController         m_mediaController;

    private GestureDetector         mDetector;

    private ArrayList<VideoInfo>    m_videoList;

    private boolean                 m_blSplash;
    private int                     m_iVideoIndex;
    private VideoInfo               m_curVideoInfo;
    private boolean                 m_blIsPush;
    private int                     m_iStopPos;

    private int                     m_iScreenWidth;
    private int                     m_iScreenHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVideoList();
        m_blSplash = true;

//        m_vvSwipe = (VideoView)findViewById(R.id.vv_swipe);
        m_vvPush = (VideoView)findViewById(R.id.vv_push);
        m_butLogin = (Button)findViewById(R.id.but_login);

        m_iVideoIndex = 0;
        startPushVideo();

        m_vvPush.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (m_blIsPush) {
                    mediaPlayer.start();
                } else {
                    m_iVideoIndex++;
                    startPushVideo();
                }
            }
        });

/*
        Uri video = Uri.parse("android.resource://com.storeyfilms.zenith/" +R.raw.title_login);
        m_vvSwipe.setVideoURI(video);
        m_vvSwipe.start();

        m_vvSwipe.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (m_blSplash) {
                    m_butLogin.setVisibility(View.VISIBLE);
                } else {
                    m_iVideoIndex++;
                    startPushVideo();
                }
            }
        });



        m_butLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m_blSplash = false;
                m_butLogin.setVisibility(View.INVISIBLE);
                m_vvSwipe.setVisibility(View.INVISIBLE);
                m_vvPush.setVisibility(View.VISIBLE);

                m_iVideoIndex = 0;
                startPushVideo();
            }
        });
*/
        m_vvPush.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                //mediaPlayer.setLooping(true);
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
        m_vvPush.setOnTouchListener(touchListener);
    }

    private void startPushVideo() {
        if (m_iVideoIndex >= m_videoList.size()) {
            return;
        }

        m_curVideoInfo = m_videoList.get(m_iVideoIndex);
        if (m_curVideoInfo == null) return;

        m_blIsPush = true;

       /// m_vvSwipe.setVisibility(View.INVISIBLE);
       /// m_vvPush.setVisibility(View.VISIBLE);

        Uri video = Uri.parse("https://zenithzero.s3.us-east-2.amazonaws.com/" + m_curVideoInfo.curVideo);
        //Uri video = Uri.parse("android.resource://com.storeyfilms.zenith/" + Integer.parseInt(m_curVideoInfo.curVideo));
        m_vvPush.setVideoURI(video);
        m_vvPush.start();
    }

    private void startSwipeVideo() {
        if (m_curVideoInfo == null) return;

        m_blIsPush = false;

        //m_vvSwipe.setVisibility(View.VISIBLE);
       // m_vvPush.setVisibility(View.INVISIBLE);

        Uri video = Uri.parse("https://zenithzero.s3.us-east-2.amazonaws.com/" + m_curVideoInfo.swipeVideo);
//        m_vvSwipe.setVideoURI(video);
//        m_vvSwipe.start();
        //Uri video = Uri.parse("android.resource://com.storeyfilms.zenith/" + Integer.parseInt(m_curVideoInfo.swipeVideo));
        m_vvPush.setVideoURI(video);
        m_vvPush.start();
    }

    private void startRightLink() {
        if (m_curVideoInfo == null) return;
        if (m_curVideoInfo.rightLink == null) return;

        String url = m_curVideoInfo.rightLink.link;
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        startActivity(i);
    }

    private void startLeftLink() {
        if (m_curVideoInfo == null) return;
        if (m_curVideoInfo.leftLink == null) return;

        if (m_curVideoInfo.leftLink.name.equalsIgnoreCase("twitter")) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/" + m_curVideoInfo.leftLink.link)));
//            try {
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=" + m_curVideoInfo.leftLink.link)));
//            }catch (Exception e) {
//                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/#!/" + m_curVideoInfo.leftLink.link)));
//            }
        } else if (m_curVideoInfo.leftLink.name.equalsIgnoreCase("snapchat")) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("*/*");
            intent.setPackage("com.snapchat.android");
            startActivity(Intent.createChooser(intent, "Open Snapchat"));
        }

    }

    private void initVideoList() {
        m_videoList = new ArrayList<VideoInfo>();

        String rightLinkName = "Indiegogo";
        String rightLinkPath = "https://www.indiegogo.com/project/preview/031584c9";
        LinkInfo rightLink = new LinkInfo(rightLinkName, rightLinkPath);

        String leftLinkName = "Twitter";
        String leftLinkPath = "@zenithzeromovie";
        LinkInfo leftLink = new LinkInfo(leftLinkName, leftLinkPath);

        String curVideo = "push.mov";
        String swipeVideo = "swipe.mov";
        String nextVideo = "push.mp4";

//        String curVideo = Integer.toString(R.raw.push);
//        String swipeVideo = Integer.toString(R.raw.swipe);
//        String nextVideo = Integer.toString(R.raw.push3);
        String prevVideo = null;

        VideoInfo videoInfo = new VideoInfo(curVideo, swipeVideo, nextVideo, prevVideo, rightLink, leftLink);
        m_videoList.add(videoInfo);

        curVideo = "push.mp4";
        swipeVideo = "swipe.mov";
        nextVideo = "push3.mp4";
        prevVideo = "push.mov";

//        curVideo = Integer.toString(R.raw.push3);
//        swipeVideo = Integer.toString(R.raw.swipe);
//        nextVideo = null;
//        prevVideo = Integer.toString(R.raw.push);

        leftLinkName = "Snapchat";
        leftLinkPath = "zenithzeromovie";
        leftLink = new LinkInfo(leftLinkName, leftLinkPath);

        videoInfo = new VideoInfo(curVideo, swipeVideo, nextVideo, prevVideo, rightLink, leftLink);
        m_videoList.add(videoInfo);
/*
        curVideo = "push3.mp4";
        swipeVideo = "swipe.mov";
        nextVideo = null;
        prevVideo = "push.mp4";

        leftLinkName = "Instagram";
        leftLinkPath = "zenith0archivealpha";
        leftLink = new LinkInfo(leftLinkName, leftLinkPath);

        videoInfo = new VideoInfo(curVideo, swipeVideo, nextVideo, prevVideo, rightLink, leftLink);
        m_videoList.add(videoInfo);
*/
    }

    @Override
    public void onResume() {

        super.onResume();

        if (m_curVideoInfo == null) return;

        m_vvPush.seekTo(m_iStopPos);
        m_vvPush.start();
//        if (m_blIsPush) {
//            startPushVideo();
//        } else {
//            startSwipeVideo();
//        }
    }

    @Override
    public void onPause() {

        super.onPause();
        //m_vvPush.stopPlayback();

        m_iStopPos = m_vvPush.getCurrentPosition();
        m_vvPush.pause();
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

            if (m_blIsPush) {
                float orientation = e.getOrientation();
                if (orientation == 0) {
                    if (e.getX() < m_iScreenWidth / 2) {
                        startLeftLink();
                    } else {
                        startRightLink();
                    }
                } else {
                    if (e.getX() < m_iScreenHeight / 2) {
                        startLeftLink();
                    } else {
                        startRightLink();
                    }
                }
            }

            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("TAG", "onLongPress: ");
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Log.i("TAG", "onDoubleTap: ");
            return true;
        }
//
//        @Override
//        public boolean onScroll(MotionEvent e1, MotionEvent e2,
//                                float distanceX, float distanceY) {
//            Log.i("TAG", "onScroll: ");
//            Log.i("TAG", Float.toString(distanceX));
//            return false;
//        }

        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Log.i("TAG", "onFling: ");
            Log.i("TAGX", Float.toString(velocityX));
            Log.i("TAGY", Float.toString(velocityY));

            if (m_blIsPush) {
                float orientation = event1.getOrientation();
                if (orientation == 0) {
                    if (event1.getX() < m_iScreenWidth / 2) {
                        startSwipeVideo();
                        Log.i("TAG", "000000000000000000000000000");
                    }
                } else {
                    if (event1.getX() < m_iScreenHeight / 2) {
                        startSwipeVideo();
                        Log.i("TAG", "ppppppppppppppppppppp");
                    }
                }
            }

            return true;
        }
    }
}
