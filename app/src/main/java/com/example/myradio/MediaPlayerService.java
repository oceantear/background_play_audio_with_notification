package com.example.myradio;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetFileDescriptor;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
//import android.support.v4.media.MediaBrowserServiceCompat;

//import android.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.core.app.NotificationCompat;
//import androidx.core.app.NotificationManagerCompat;
//import androidx.core.content.ContextCompat;
//import androidx.media.MediaBrowserServiceCompat;
//import androidx.media.session.MediaButtonReceiver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;
import static android.view.KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD;
import static com.example.myradio.MediaStyleHelper.COMMAND_EXAMPLE;
import static com.example.myradio.MediaStyleHelper.COMMAND_SET_RESOURCE;
import static com.example.myradio.NotificationMgr.NOTIFICATION_ID;
import static com.example.myradio.NotificationMgr.REQUEST_CODE;

public class MediaPlayerService extends MediaBrowserServiceCompat implements MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mMediaPlayer;
    private MediaSessionCompat mMediaSessionCompat;
    private NotificationCompat.Action mPlayAction;
    private NotificationCompat.Action mPauseAction;
    private NotificationCompat.Action mNextAction;
    private NotificationCompat.Action mPrevAction;
    private NotificationMgr mNotificationMgr;
    //private static final int REQUEST_CODE = 501;
    //public static final int NOTIFICATION_ID = 412;
    private String mMediaTitle;
    private String mMediaContent;
    private String mSubText;
    private int mSmallIcon;
    private int mLargeIcon;
    private int mQueueIndex = -1;


    private List<String> DataSource = new ArrayList<>();
    private String mPlayingSource = null;

    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if( mMediaPlayer != null && mMediaPlayer.isPlaying() ) {
                mMediaPlayer.pause();
            }
        }
    };

    //MediaSession receive media controller command
    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            Log.e("jimmy","onMediaButtonEvent intent():"+mediaButtonEvent);
            KeyEvent event =   (KeyEvent)mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            Log.e("jimmy","event :"+event);
            if(event.getKeyCode() == KEYCODE_MEDIA_PLAY){
                Log.e("jimmy","service  key event : play");
                //if(mMediaPlayer.isPlaying()){
                //    mMediaPlayer.pause();
                //    showPausedNotification();
                //}
            }else if(event.getKeyCode() == KEYCODE_MEDIA_PAUSE){
                Log.e("jimmy","service  key event : pause");
                //onPause();
                //showPlayingNotification();
            }else if( event.getKeyCode() == KEYCODE_MEDIA_SKIP_FORWARD){
                Log.e("jimmy","service  key event : skip forward");

            }else if ( event.getKeyCode() == KEYCODE_MEDIA_SKIP_BACKWARD){
                Log.e("jimmy","service  key event : skip backward");
            }else if (event.getKeyCode() == KEYCODE_MEDIA_NEXT){
                Log.e("jimmy","service  key event : next");
                onSkipToNext();
                //showPlayingNotification();
            }else if(event.getKeyCode() == KEYCODE_MEDIA_PREVIOUS){
                Log.e("jimmy","service  key event : previous");
                onSkipToPrevious();
                //showPlayingNotification();
            }


            return super.onMediaButtonEvent(mediaButtonEvent);

        }

        @Override
        public void onPrepare() {
            super.onPrepare();
            Log.e("jimmy","onPrepare: ");
            try {
                mPlayingSource = DataSource.get(mQueueIndex);
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(mPlayingSource);
                mMediaPlayer.prepare();
            } catch (IOException e) {
                Log.e("jimmy", "onPrepare error :" + e.toString());
            }
        }

        @Override
        public void onPlay() {
            Log.e("jimmy","MediaPlayerService onPlay()");
            super.onPlay();

            try {
                if (!successfullyRetrievedAudioFocus()) {
                    Log.e("jimmy","retrieve audio focus fail");
                    return;
                }

                if(mPlayingSource == null) {
                    if (mQueueIndex < 0 || (mQueueIndex > DataSource.size() - 1)) {
                        Log.e("jimmy", "mQueueIndex out of bound");
                        return;
                    } else
                        onPrepare();
                }

                if (!mMediaPlayer.isPlaying()) {
                    mMediaPlayer.start();
                    mMediaSessionCompat.setActive(true);
                    setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);

                    showPausedNotification();
                }else
                    Log.e("jimmy","play not playing");
            }catch (Exception e) {
                Log.e("jimmy", "onPlay error :" + e.toString());
            }
            //mMediaPlayer.start();
        }

        @Override
        public void onPause() {
            Log.e("jimmy","MediaPlayerService onPause()");
            super.onPause();

            if( mMediaPlayer.isPlaying() ) {
                mMediaPlayer.pause();
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                stopForeground(false);
                showPlayingNotification();
            }
        }


        @Override
        public void onSeekTo(long pos) {
            Log.e("jimmy","onSeekTo");
            super.onSeekTo(pos);
        }

        @Override
        public void onFastForward() {
            Log.e("jimmy","onFastForward");
            //super.onFastForward();
        }

        @Override
        public void onRewind() {
            Log.e("jimmy","onRewind");
            //super.onRewind();
        }

        @Override
        public void onSkipToNext() {
            Log.e("jimmy","onSkipToNext :"+mQueueIndex);
            //super.onSkipToNext();
            mPlayingSource = null;
            if(mMediaPlayer.isPlaying())
                mMediaPlayer.pause();

            if(mQueueIndex >= (DataSource.size() - 1))
                Log.e("jimmy","end of play list");
            else
                Log.e("jimmy","not end of play list");
            mQueueIndex = (mQueueIndex >= (DataSource.size() - 1)) ? 0 :  (++mQueueIndex);
            //mPlayingSource = DataSource.get(mQueueIndex);
            Log.e("jimmy","mQueueIndex : "+mQueueIndex);
            onPlay();
        }

        @Override
        public void onSkipToPrevious() {
            Log.e("jimmy","onSkipToPrevious");
            //super.onSkipToPrevious();
            mPlayingSource = null;
            if(mMediaPlayer.isPlaying())
                mMediaPlayer.pause();

            mQueueIndex = (mQueueIndex == 0) ? DataSource.size() -1 :  (--mQueueIndex);
            //mPlayingSource = DataSource.get(mQueueIndex);
            Log.e("jimmy","mQueueIndex : "+mQueueIndex);
            onPlay();
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {
            Log.e("jimmy","onCustomAction");
            String t = extras.getString("type");

            super.onCustomAction(action, extras);
            if(COMMAND_SET_RESOURCE.equals(action)){
                if(t.equals("radio")) {
                    DataSource.addAll(extras.getStringArrayList("song"));
                    mQueueIndex = 0;
                    try {
                        mMediaPlayer.setDataSource(DataSource.get(mQueueIndex));
                        mMediaPlayer.prepare();
                        mMediaSessionCompat.setActive(true);
                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e("jimmy","onPlayFromSearch.error : "+e.toString());
                    }
                }
            }

        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("jimmy","Service onCreate");
        //default string
        mMediaTitle = "Album";
        mMediaContent = "Artist";
        mSubText = "Song Name";
        //default Icons
        mSmallIcon = R.drawable.ic_stat_image_audiotrack;
        mLargeIcon = R.drawable.album_jazz_blues;
        mNotificationMgr = new NotificationMgr(this);
        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();
    }

    private void initNoisyReceiver() {
        //Handles headphones coming unplugged. cannot be done through a manifest receiver
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Log.e("jimmy","onTaskRemoved");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.e("jimmy","service destroy()");
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.abandonAudioFocus(this);
        unregisterReceiver(mNoisyReceiver);
        mMediaSessionCompat.release();
        mMediaPlayer.stop();
        NotificationManagerCompat.from(this).cancel(1);
    }

    private void initMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setVolume(1.0f, 1.0f);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                Log.e("MediaPlayService","MediaPlayer onPrepared()");
                mMediaPlayer.start();
                setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                showPausedNotification();
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {

                return false;
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Log.e("MediaPlayService","MediaPlayer onCompletion()");
            }
        });
    }

    private void showPausedNotification() {

        ContextCompat.startForegroundService(
                MediaPlayerService.this,
                new Intent(MediaPlayerService.this, MediaPlayerService.class));

        Notification notification = mNotificationMgr.getNotification(PlaybackStateCompat.STATE_PAUSED , getSessionToken());
        mNotificationMgr.getNotificationManager().notify(NOTIFICATION_ID , notification);
        startForeground(NOTIFICATION_ID, notification);
    }

    private void showPlayingNotification() {

        stopForeground(false);
        Notification notification = mNotificationMgr.getNotification(PlaybackStateCompat.STATE_PLAYING , getSessionToken());
        mNotificationMgr.getNotificationManager().notify(NOTIFICATION_ID,notification);
    }




    private void initMediaSession() {
        Log.e("jimmy","initMediaSession");

        ComponentName mediaButtonReceiver = new ComponentName(getApplicationContext(), MediaButtonReceiver.class);
        mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "MediaPlayerService", mediaButtonReceiver, null);
        //mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "MediaPlayerService");

        mMediaSessionCompat.setFlags( MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS );

        mMediaSessionCompat.setCallback(mMediaSessionCallback);


        /*Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(this, MediaButtonReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
        mMediaSessionCompat.setMediaButtonReceiver(pendingIntent);*/

        setSessionToken(mMediaSessionCompat.getSessionToken());
        mMediaSessionCompat.setActive(true);
    }

    private void setMediaPlaybackState(int state) {
        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        if( state == PlaybackStateCompat.STATE_PLAYING ) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mMediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }

    private void initMediaSessionMetadata() {
        MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
        //Notification icon in card
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));

        //lock screen icon for pre lollipop
        metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ART, BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_TITLE, "Display Title");
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_DISPLAY_SUBTITLE, "Display Subtitle");
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, 1);
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, 1);

        mMediaSessionCompat.setMetadata(metadataBuilder.build());
    }

    private boolean successfullyRetrievedAudioFocus() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        int result = audioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }


    //Not important for general audio service, required for class
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if(TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }

        return null;
    }

    //Not important for general audio service, required for class
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch( focusChange ) {
            case AudioManager.AUDIOFOCUS_LOSS: {
                if( mMediaPlayer.isPlaying() ) {
                    mMediaPlayer.stop();
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                mMediaPlayer.pause();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if( mMediaPlayer != null ) {
                    mMediaPlayer.setVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                if( mMediaPlayer != null ) {
                    if( !mMediaPlayer.isPlaying() ) {
                        mMediaPlayer.start();
                    }
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if( mMediaPlayer != null ) {
            mMediaPlayer.release();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Log.e("MediaPlayerService","onStartCommand() Action :"+intent.getAction());
        //Log.e("MediaPlayerService","onStartCommand() flags :"+flags);
        //Log.e("MediaPlayerService","onStartCommand() startId :"+startId);
        MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
        return super.onStartCommand(intent, flags, startId);
    }

}
