package com.storeyfilms.zenith.doc;

public class VideoInfo {
     public String       curVideo;
     public String       swipeVideo;
     public String       nextVideo;
     public String       prevVideo;

     public LinkInfo    rightLink;
     public LinkInfo    leftLink;

     public VideoInfo(String curVideo, String swipeVideo, String nextVideo, String prevVideo, LinkInfo rightLink, LinkInfo leftLink) {
          this.curVideo = curVideo;
          this.swipeVideo = swipeVideo;
          this.nextVideo = nextVideo;
          this.prevVideo = prevVideo;

          this.rightLink = rightLink;
          this.leftLink = leftLink;
     }
}
