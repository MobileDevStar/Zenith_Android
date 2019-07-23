package com.storeyfilms.zenith.doc;

public class VideoInfo {
     public static final int READY_STATE = 0;
     public static final int LOOP_STATE = 1;
     public static final int LEFT_STATE = 2;
     public static final int RIGHT_STATE = 3;
     public static final int SWIPE_STATE = 4;

     public static final int RESERVED1_STATE = 5;
     public static final int RESERVED2_STATE = 6;

     public String       subject;
     public boolean      locked;

     public String       loopVideo;
     public String       swipeVideo;
     public LinkInfo     rightLink;
     public LinkInfo     leftLink;

     public int          state;

     public VideoInfo(String subject, boolean locked, String loopVideo, String swipeVideo, LinkInfo rightLink, LinkInfo leftLink, int state) {
          this.subject = subject;
          this.locked = locked;
          this.loopVideo = loopVideo;
          this.swipeVideo = swipeVideo;
          this.rightLink = rightLink;
          this.leftLink= leftLink;
          this.state = state;
     }

}

