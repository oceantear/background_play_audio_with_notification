package com.example.myradio;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.RequiresApi;
//import android.support.annotation.RequiresApi;

//import android.annotation.RequiresApi;

public class CreateChannel {

    private Context mCtx;
    private CreateChannel mCreCh;
    private static final String CHANNEL_ID = "media_playback_channel";

    public CreateChannel(Context ctx) {
        mCtx = ctx;

    }

    public String getChannelId(){
        return CHANNEL_ID;
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public void createChannel() {
        NotificationManager
                mNotificationManager =
                (NotificationManager) mCtx
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = CHANNEL_ID;
        // The user-visible name of the channel.
        CharSequence name = "Media playback";
        // The user-visible description of the channel.
        String description = "Media playback controls";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mNotificationManager.createNotificationChannel(mChannel);
    }
}
