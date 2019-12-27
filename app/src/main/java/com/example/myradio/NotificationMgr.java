package com.example.myradio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

/** Notification style
 *      ---------------------------------------------------------------------------------
 *     | Small icon   \   AppName  \  subText                                           |
 *     | Title                                                  Larger icon                         |
 *     | MediaContent                                                                              |
 *     ----------------------------------------------------------------------------------
 * */


public class NotificationMgr {

    private MediaPlayerService mService;
    private NotificationManager mNotificationManager;

    private NotificationCompat.Action mPlayAction;
    private NotificationCompat.Action mPauseAction;
    private NotificationCompat.Action mNextAction;
    private NotificationCompat.Action mPrevAction;

    private String mMediaTitle = "Album";
    private String mMediaContent = "Artist";
    private String mSubText = "Song Name";
    private int mDefaultSmallIcon = R.drawable.ic_stat_image_audiotrack;
    private int mDefaultLargeIcon = R.drawable.album_jazz_blues;
    private int mSmallIcon ;
    private int mLargeIcon ;
    public static final int REQUEST_CODE = 501;
    public static final int NOTIFICATION_ID = 412;

    public NotificationMgr(MediaPlayerService service) {
        this.mService = service;

        mNotificationManager =
                (NotificationManager) mService.getSystemService(Context.NOTIFICATION_SERVICE);

        mPlayAction =
                new NotificationCompat.Action(
                        R.drawable.ic_play_arrow_white_24dp,
                        mService.getString(R.string.label_play),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mService,
                                PlaybackStateCompat.ACTION_PLAY));

        mPauseAction = new NotificationCompat.Action(
                R.drawable.ic_pause_white_24dp,
                mService.getString(R.string.label_pause),
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mService,
                        PlaybackStateCompat.ACTION_PAUSE));

        mNextAction =
                new NotificationCompat.Action(
                        R.drawable.ic_skip_next_white_24dp,
                        mService.getString(R.string.label_next),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mService,
                                PlaybackStateCompat.ACTION_SKIP_TO_NEXT));
        mPrevAction =
                new NotificationCompat.Action(
                        R.drawable.ic_skip_previous_white_24dp,
                        mService.getString(R.string.label_previous),
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                mService,
                                PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS));
        //remove notification when service killed and restart by system
        mNotificationManager.cancelAll();

    }

    public NotificationManager getNotificationManager(){return mNotificationManager;}

    public Notification getNotification(int status ,MediaSessionCompat.Token token, String title, String mediaContent, int largerIcon, int smallIcon){

        if(title != null)
            mMediaTitle = title;
        if(mediaContent != null)
            mMediaContent = mediaContent;
        if(largerIcon > 0)
            mLargeIcon = largerIcon;
        else
            mLargeIcon = mDefaultLargeIcon;
        if(smallIcon > 0)
            mSmallIcon = smallIcon;
        else
            mSmallIcon = mDefaultSmallIcon;

        CreateChannel creatCh = new CreateChannel(mService);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            creatCh.createChannel();
        }


        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(mService, creatCh.getChannelId());
        notificationBuilder
                .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                        .setMediaSession(token)
                        .setShowActionsInCompactView(0, 1, 2) //up to 5 action
                        .setShowCancelButton(true)
                        //.setCancelButtonIntent(
                        //        MediaButtonReceiver.buildMediaButtonPendingIntent(
                        //                mService, PlaybackStateCompat.ACTION_STOP)))
                )
                .setColor(ContextCompat.getColor(mService, R.color.notification_bg))
                .setSmallIcon(mSmallIcon)
                .setLargeIcon(BitmapFactory.decodeResource(mService.getResources(), mLargeIcon))
                .setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                        mService, PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setOnlyAlertOnce(true)
                .setContentIntent(createContentIntent())
                .setContentTitle(mMediaTitle)
                .setContentText(mMediaContent)
                .setSubText(mSubText);



        notificationBuilder.addAction(mPrevAction);

        if(status == PlaybackStateCompat.STATE_PLAYING) {
            notificationBuilder.addAction(mPlayAction);
        }

        if(status == PlaybackStateCompat.STATE_PAUSED) {
            notificationBuilder.addAction(mPauseAction);
        }
        notificationBuilder.addAction(mNextAction);

        return notificationBuilder.build();
    }



    private PendingIntent createContentIntent() {
        Intent openUI = new Intent(mService, MainActivity.class);
        openUI.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(
                mService, REQUEST_CODE, openUI, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public void clearNotification(){

    }

}
