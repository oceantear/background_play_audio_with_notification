package com.example.myradio;

import java.io.Serializable;

public class MediaMetadata implements Serializable {

    /**
     * Notification style
     * ---------------------------------------------------------------------------------
     * | Small icon   \   AppName  \  subText                                           |
     * | Title                                                  Larger icon                         |
     * | MediaContent                                                                              |
     * ----------------------------------------------------------------------------------
     */

    private String mTitle;
    private String mMediaContent;
    private String mSubTitle;
    private String mSourcePath;
    private int mSmallIcon;
    private int mLargerIcon;

    public MediaMetadata(String title, String mediaContent, String subTitle, String sourcePath, int smallIcon, int largerIcon) {
        this.mTitle = title;
        this.mMediaContent = mediaContent;
        this.mSubTitle = subTitle;
        this.mSourcePath = sourcePath;
        this.mSmallIcon = smallIcon;
        this.mLargerIcon = largerIcon;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getMediaContent() {
        return mMediaContent;
    }

    public void setMediaContent(String mMediaContent) {
        this.mMediaContent = mMediaContent;
    }

    public String getSubTitle() {
        return mSubTitle;
    }

    public void setSubTitle(String mSubTitle) {
        this.mSubTitle = mSubTitle;
    }

    public String getSourcePath() {
        return mSourcePath;
    }

    public void setSourcePath(String mPath) {
        this.mSourcePath = mPath;
    }

    public int getSmallIcon() {
        return mSmallIcon;
    }

    public void setSmallIcon(int mSmallIcon) {
        this.mSmallIcon = mSmallIcon;
    }

    public int getLargerIcon() {
        return mLargerIcon;
    }

    public void setLargerIcon(int mLargerIcon) {
        this.mLargerIcon = mLargerIcon;
    }
}
