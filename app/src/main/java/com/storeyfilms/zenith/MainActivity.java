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
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.storeyfilms.zenith.doc.LinkInfo;
import com.storeyfilms.zenith.doc.VideoInfo;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private VideoView               m_vvMain;
    private Button                  m_butLogin;
    private ProgressBar             m_progressBar;

    private MediaController         m_mediaController;

    private GestureDetector         mDetector;

    private ArrayList<VideoInfo>    m_videoList;

    private int                     m_iVideoIndex;

    private VideoInfo               m_curVideoInfo;
    private boolean                 m_blIsFirst;
    private int                     m_iStopPos;

    private int                     m_iScreenWidth;
    private int                     m_iScreenHeight;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVideoList();

        m_vvMain = (VideoView)findViewById(R.id.vv_main);
        m_butLogin = (Button)findViewById(R.id.but_login);
        m_progressBar = (ProgressBar) findViewById(R.id.pb_spinner);
        m_blIsFirst = true;
        m_iVideoIndex = -1;

        Uri video = Uri.parse("android.resource://com.storeyfilms.zenith/" +R.raw.after_login_video_to_twitter_prize_480_sound);
       // Uri video = Uri.parse("https://zenithzero.s3.us-east-2.amazonaws.com/480/Twitter_prize_LOOP_480_SOUND.mov");
        m_vvMain.setVideoURI(video);

        m_progressBar.setVisibility(View.VISIBLE);

        m_vvMain.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                if (m_blIsFirst == true && m_iVideoIndex == -1) {
                    startMainVideo();
                } else {
                    if (m_curVideoInfo.state == VideoInfo.LOOP_STATE) {
                        mediaPlayer.start();
                    } else if (m_curVideoInfo.state == VideoInfo.LEFT_STATE) {
                        startLeftLink();
                    } else if (m_curVideoInfo.state == VideoInfo.RIGHT_STATE) {
                        startRightLink();
                    } else if (m_curVideoInfo.state == VideoInfo.SWIPE_STATE) {
                        m_iVideoIndex++;
                        playLoopVideo();
                    }
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
        m_vvMain.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                m_progressBar.setVisibility(View.GONE);
                m_vvMain.start();
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

        m_curVideoInfo.state = VideoInfo.LOOP_STATE;
        //Uri video = Uri.parse("https://zenithzero.s3.us-east-2.amazonaws.com/480/" + m_curVideoInfo.loopVideo);
        Uri video = Uri.parse("android.resource://com.storeyfilms.zenith/" + Integer.parseInt(m_curVideoInfo.loopVideo));
        m_vvMain.setVideoURI(video);
        m_progressBar.setVisibility(View.VISIBLE);
    }

    private void playSwipeVideo() {
        if (m_curVideoInfo == null) return;

        m_curVideoInfo.state = VideoInfo.SWIPE_STATE;
        //Uri video = Uri.parse("https://zenithzero.s3.us-east-2.amazonaws.com/480/" + m_curVideoInfo.swipeVideo);
        Uri video = Uri.parse("android.resource://com.storeyfilms.zenith/" + Integer.parseInt(m_curVideoInfo.swipeVideo));
        m_vvMain.setVideoURI(video);
        m_progressBar.setVisibility(View.VISIBLE);
    }

    private void playLeftVideo() {
        if (m_curVideoInfo == null) return;

        if (m_curVideoInfo.locked == true || m_curVideoInfo.leftLink == null) return;

        m_curVideoInfo.state = VideoInfo.LEFT_STATE;
        //Uri video = Uri.parse("https://zenithzero.s3.us-east-2.amazonaws.com/480/" + m_curVideoInfo.leftLink.video);
        Uri video = Uri.parse("android.resource://com.storeyfilms.zenith/" + Integer.parseInt(m_curVideoInfo.leftLink.video));
        m_vvMain.setVideoURI(video);
        m_progressBar.setVisibility(View.VISIBLE);
    }

    private void playRightVideo() {
        if (m_curVideoInfo == null) return;

        if (m_curVideoInfo.rightLink == null) return;

        m_curVideoInfo.state = VideoInfo.RIGHT_STATE;
        //Uri video = Uri.parse("https://zenithzero.s3.us-east-2.amazonaws.com/480/" + m_curVideoInfo.rightLink.video);
        Uri video = Uri.parse("android.resource://com.storeyfilms.zenith/" + Integer.parseInt(m_curVideoInfo.rightLink.video));
        m_vvMain.setVideoURI(video);
        m_progressBar.setVisibility(View.VISIBLE);
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

        String subject = "Twitter";
        boolean locked = false;
        String loopVideo = Integer.toString(R.raw.twitter_prize_loop_480_sound);//"Twitter_prize_LOOP_480_SOUND.mov";
        String swipeVideo = Integer.toString(R.raw.twitter_prize_swipe_to_snapchat_480_sound);//"Twitter_prize_swipe_to_Snapchat_480_SOUND.mov";
        int state = VideoInfo.LOOP_STATE;

        String rightLinkName = "Indiegogo";
        String rightVideo = Integer.toString(R.raw.twitter_prize_gaia_indiegogo_480_sound);//"Twitter_prize_Gaia_Indiegogo_480_SOUND.mov";
        String rightLinkPath = "https://www.indiegogo.com/project/preview/031584c9";
        LinkInfo rightLink = new LinkInfo(rightLinkName, rightVideo, rightLinkPath);

        String leftLinkName = "Twitter";
        String leftVideo = Integer.toString(R.raw.twitter_prize_push_480_sound);//"Twitter_prize_push_480_SOUND.mov";
        String leftLinkPath = "@zenithzeromovie";
        LinkInfo leftLink = new LinkInfo(leftLinkName, leftVideo, leftLinkPath);

        VideoInfo videoInfo = new VideoInfo(subject, locked, loopVideo, swipeVideo, rightLink, leftLink, state);
        m_videoList.add(videoInfo);

        subject = "Snapchat";
        locked = false;
        loopVideo = Integer.toString(R.raw.snapchat_prize_loop_480_sound);//"Snapchat_prize_loop_480_SOUND.mov";
        swipeVideo = Integer.toString(R.raw.snapchat_prize_swipe_to_lock_z_button_480_sound);//"Snapchat_prize_swipe_to_LOCK_Z_button_480_SOUND.mov";
        state = VideoInfo.LOOP_STATE;

        rightLinkName = "Indiegogo";
        rightVideo = Integer.toString(R.raw.snapchat_prize_gaia_indiegogo_480_sound);//"Snapchat_prize_Gaia_Indiegogo_480_SOUND.mov";
        rightLinkPath = "https://www.indiegogo.com/project/preview/031584c9";
        rightLink = new LinkInfo(rightLinkName, rightVideo, rightLinkPath);

        leftLinkName = "Snapchat";
        leftVideo = Integer.toString(R.raw.snapchat_prize_push_480_sound);//"Snapchat_prize_push_480_SOUND.mov";
        leftLinkPath = "zenithzeromovie";
        leftLink = new LinkInfo(leftLinkName, leftVideo, leftLinkPath);

        videoInfo = new VideoInfo(subject, locked, loopVideo, swipeVideo, rightLink, leftLink, state);
        m_videoList.add(videoInfo);


        subject = "ZButton";
        locked = true;
        loopVideo = Integer.toString(R.raw.lock_z_button_prize_loop_480_sound);//"LOCK_Z_button_prize_loop_480_SOUND.mov";
        swipeVideo = Integer.toString(R.raw.lock_z_button_prize_swipe_to_lock_instagram_480_sound);//"LOCK_Z_button_prize_swipe_to_LOCK_Instagram_480_SOUND.mov";
        state = VideoInfo.LOOP_STATE;

        rightLinkName = "Indiegogo";
        rightVideo = Integer.toString(R.raw.lock_z_button_prize_gaia_indiegogo_480);//"Snapchat_prize_Gaia_Indiegogo_480_SOUND.mov";
        rightLinkPath = "https://www.indiegogo.com/project/preview/031584c9";
        rightLink = new LinkInfo(rightLinkName, rightVideo, rightLinkPath);

        leftLink = null;

        videoInfo = new VideoInfo(subject, locked, loopVideo, swipeVideo, rightLink, leftLink, state);
        m_videoList.add(videoInfo);
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
                startMainVideo();
            } else {
                if (m_curVideoInfo != null) {
                    float orientation = e.getOrientation();
                    if (m_curVideoInfo.state == VideoInfo.LOOP_STATE) {
                        if (orientation == 0) {
                            if (e.getX() < m_iScreenWidth / 2) {
                                playLeftVideo();
                            } else {
                                playRightVideo();
                            }
                        } else {
                            if (e.getX() < m_iScreenHeight / 2) {
                                playLeftVideo();
                            } else {
                                playRightVideo();
                            }
                        }
                    } else if (m_curVideoInfo.state == VideoInfo.SWIPE_STATE) {
                        m_iVideoIndex++;
                        if (m_iVideoIndex < m_videoList.size() || m_iVideoIndex >= 0) {
                            m_curVideoInfo = m_videoList.get(m_iVideoIndex);
                            if (orientation == 0) {
                                if (e.getX() < m_iScreenWidth / 2) {
                                    playLeftVideo();
                                } else {
                                    playRightVideo();
                                }
                            } else {
                                if (e.getX() < m_iScreenHeight / 2) {
                                    playLeftVideo();
                                } else {
                                    playRightVideo();
                                }
                            }
                        }
                    }
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
                    if (m_curVideoInfo.state == VideoInfo.LOOP_STATE) {
                        float orientation = event1.getOrientation();
                        if (orientation == 0) {
                            if (event1.getX() < m_iScreenWidth / 2) {
                                if (velocityX > 0) {
                                    playSwipeVideo();
                                } else {
                                    m_iVideoIndex--;
                                    playLoopVideo();
                                }
                                Log.i("TAG", "000000000000000000000000000");
                            }
                        } else {
                            if (event1.getX() < m_iScreenHeight / 2) {
                                if (velocityX > 0) {
                                    playSwipeVideo();
                                } else {
                                    m_iVideoIndex--;
                                    playLoopVideo();
                                }

                                Log.i("TAG", "ppppppppppppppppppppp");
                            }
                        }
                    }
                }
            }

            return true;
        }
    }
}
