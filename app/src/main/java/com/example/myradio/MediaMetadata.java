package com.example.myradio;

import java.io.Serializable;

public class MediaMetadata implements Serializable {

    /** Notification style
     *      ---------------------------------------------------------------------------------
     *     | Small icon   \   AppName  \  subText                                           |
     *     | Title                                                  Larger icon                         |
     *     | MediaContent                                                                              |
     *     ----------------------------------------------------------------------------------
     * */

    private String mTitle;
    private String mMediaContent;
    private String mSubTitle;
    private int mSmallIcon;
    private int mLargerIcon;

    public MediaMetadata(String mTitle, String mMediaContent, String mSubTitle, int mSmallIcon, int mLargerIcon) {
        this.mTitle = mTitle;
        this.mMediaContent = mMediaContent;
        this.mSubTitle = mSubTitle;
        this.mSmallIcon = mSmallIcon;
        this.mLargerIcon = mLargerIcon;
    }

    public String getmTitle() {
        return mTitle;
    }

    public void setmTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getmMediaContent() {
        return mMediaContent;
    }

    public void setmMediaContent(String mMediaContent) {
        this.mMediaContent = mMediaContent;
    }

    public String getmSubTitle() {
        return mSubTitle;
    }

    public void setmSubTitle(String mSubTitle) {
        this.mSubTitle = mSubTitle;
    }

    public int getmSmallIcon() {
        return mSmallIcon;
    }

    public void setmSmallIcon(int mSmallIcon) {
        this.mSmallIcon = mSmallIcon;
    }

    public int getmLargerIcon() {
        return mLargerIcon;
    }

    public void setmLargerIcon(int mLargerIcon) {
        this.mLargerIcon = mLargerIcon;
    }
}
