package com.example.myradio;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;
import android.view.KeyEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.view.KeyEvent.KEYCODE_MEDIA_FAST_FORWARD;
import static android.view.KeyEvent.KEYCODE_MEDIA_NEXT;
import static android.view.KeyEvent.KEYCODE_MEDIA_PAUSE;
import static android.view.KeyEvent.KEYCODE_MEDIA_PLAY;
import static android.view.KeyEvent.KEYCODE_MEDIA_PREVIOUS;
import static android.view.KeyEvent.KEYCODE_MEDIA_REWIND;
import static android.view.KeyEvent.KEYCODE_MEDIA_STOP;
import static com.example.myradio.MediaStyleHelper.COMMAND_SET_REPEAT;
import static com.example.myradio.MediaStyleHelper.COMMAND_SET_RESOURCE;
import static com.example.myradio.NotificationMgr.NOTIFICATION_ID;

public class MediaPlayerService extends MediaBrowserServiceCompat implements AudioManager.OnAudioFocusChangeListener {

    private MediaPlayer mMediaPlayer;
    private MediaSessionCompat mMediaSessionCompat;
    private NotificationMgr mNotificationMgr;
    private int mQueueIndex = -1;
    private List<MediaMetadata> mDataSource = new ArrayList<>();
    private String mPlayingSource = null;
    private boolean isRepeat = false;
    private AudioManager mAudioManager;

    private BroadcastReceiver mNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.e("jimmy", "become noisy");
            //detect headphone unplugged , pause music
            if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                showPlayingNotification(mDataSource.get(mQueueIndex).getTitle(), mDataSource.get(mQueueIndex).getSubTitle(), mDataSource.get(mQueueIndex).getMediaContent(),
                        mDataSource.get(mQueueIndex).getLargerIcon(), mDataSource.get(mQueueIndex).getSmallIcon());
            }
        }
    };

    //MediaSession receive media controller command
    private MediaSessionCompat.Callback mMediaSessionCallback = new MediaSessionCompat.Callback() {

        @Override
        public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
            LogUtil.e("jimmy", "onMediaButtonEvent intent():" + mediaButtonEvent);
            KeyEvent event = (KeyEvent) mediaButtonEvent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
            LogUtil.e("jimmy", "event :" + event);
            if (event.getKeyCode() == KEYCODE_MEDIA_PLAY) {
                LogUtil.e("jimmy", "service  key event : play");
                if (mMediaPlayer == null)
                    initMediaPlayer();
                onMPlay();
            } else if (event.getKeyCode() == KEYCODE_MEDIA_PAUSE) {
                LogUtil.e("jimmy", "service  key event : pause");
                onMPause();
            } else if (event.getKeyCode() == KEYCODE_MEDIA_FAST_FORWARD) {
                LogUtil.e("jimmy", "service  key event : fast forward");
                onFastForward();
            } else if (event.getKeyCode() == KEYCODE_MEDIA_REWIND) {
                LogUtil.e("jimmy", "service  key event : rewind");
                onRewind();
            } else if (event.getKeyCode() == KEYCODE_MEDIA_NEXT) {
                LogUtil.e("jimmy", "service  key event : next");
                onMSkipToNext();
            } else if (event.getKeyCode() == KEYCODE_MEDIA_PREVIOUS) {
                LogUtil.e("jimmy", "service  key event : previous");
                onMSkipToPrevious();
            } else if (event.getKeyCode() == KEYCODE_MEDIA_STOP) {
                onMStop();
            }

            return true;
        }

        @Override
        public void onPrepare() {
            onMPause();
        }

        @Override
        public void onPlay() {
            LogUtil.e("jimmy", "MediaPlayerService onPlay()");
            onMPlay();
        }

        @Override
        public void onPause() {
            LogUtil.e("jimmy", "MediaPlayerService onPause()");
            onMPause();
        }

        @Override
        public void onSeekTo(long pos) {
            LogUtil.e("jimmy", "onSeekTo");
        }

        @Override
        public void onFastForward() {
            LogUtil.e("jimmy", "onFastForward");
        }

        @Override
        public void onRewind() {
            LogUtil.e("jimmy", "onRewind");
        }

        @Override
        public void onSkipToNext() {
            LogUtil.e("jimmy", "onSkipToNext :" + mQueueIndex);
            onMSkipToNext();
        }

        @Override
        public void onSkipToPrevious() {
            LogUtil.e("jimmy", "onSkipToPrevious");
            onMSkipToPrevious();
        }

        @Override
        public void onStop() {
            LogUtil.e("jimmy", "onStop");
            onMStop();
        }

        @Override
        public void onCustomAction(String action, Bundle extras) {

            LogUtil.e("jimmy", "onCustomAction");
            String t = extras.getString("type");
            List<MediaMetadata> tmp = (List<MediaMetadata>) extras.getSerializable("song");
            super.onCustomAction(action, extras);
            if (COMMAND_SET_RESOURCE.equals(action)) {
                if (t.equals("radio")) {
                    mDataSource.addAll(tmp);
                    mQueueIndex = 0;
                    mPlayingSource = null;
                    try {
                        if (mMediaPlayer == null)
                            initMediaPlayer();
                        onMPlay();
                        mMediaSessionCompat.setActive(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                        LogUtil.e("jimmy", "onPlayFromSearch.error : " + e.toString());
                        setMediaPlaybackErrorState(PlaybackStateCompat.ERROR_CODE_APP_ERROR);
                    }
                }
            } else if (COMMAND_SET_REPEAT.equals(action)) {
                isRepeat = extras.getBoolean("repeat");
            }

        }
    };

    private void onMPrepare() {
        LogUtil.e("jimmy", "onPrepare: ");
        try {
            mPlayingSource = mDataSource.get(mQueueIndex).getSourcePath();
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mPlayingSource);
            mMediaPlayer.prepare();
        } catch (IOException e) {
            LogUtil.e("jimmy", "onPrepare error :" + e.toString());
        }
    }

    private void onMPlay() {

        LogUtil.e("jimmy", "MediaPlayerService onPlay()");
        try {
            if (!successfullyRetrievedAudioFocus()) {
                LogUtil.e("jimmy", "retrieve audio focus fail");
                return;
            }

            if (mPlayingSource == null) {
                if (mQueueIndex < 0 || (mQueueIndex > mDataSource.size() - 1)) {
                    LogUtil.e("jimmy", "mQueueIndex out of bound");
                    return;
                } else
                    onMPrepare();
            }

            if (!mMediaPlayer.isPlaying()) {
                mMediaPlayer.start();

                mMediaSessionCompat.setActive(true);
                setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);

                showPausedNotification(mDataSource.get(mQueueIndex).getTitle(), mDataSource.get(mQueueIndex).getSubTitle(), mDataSource.get(mQueueIndex).getMediaContent(),
                        mDataSource.get(mQueueIndex).getLargerIcon(), mDataSource.get(mQueueIndex).getSmallIcon());
            } else
                LogUtil.e("jimmy", "play not playing");
        } catch (Exception e) {
            LogUtil.e("jimmy", "onPlay error :" + e.toString());
        }
    }

    private void onMPause() {
        LogUtil.e("jimmy", "MediaPlayerService onPause()");

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
            showPlayingNotification(mDataSource.get(mQueueIndex).getTitle(), mDataSource.get(mQueueIndex).getSubTitle(), mDataSource.get(mQueueIndex).getMediaContent(),
                    mDataSource.get(mQueueIndex).getLargerIcon(), mDataSource.get(mQueueIndex).getSmallIcon());
        }
    }

    private void onMSkipToPrevious() {
        LogUtil.e("jimmy", "onSkipToPrevious");

        mPlayingSource = null;
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.pause();
        //first of list, seek to the last one media
        mQueueIndex = (mQueueIndex == 0) ? mDataSource.size() - 1 : (--mQueueIndex);

        LogUtil.e("jimmy", "mQueueIndex : " + mQueueIndex);
        onMPlay();
    }

    private void onMSkipToNext() {
        LogUtil.e("jimmy", "onSkipToNext :" + mQueueIndex);

        mPlayingSource = null;
        if (mMediaPlayer.isPlaying())
            mMediaPlayer.pause();

        //end ot list, seek to the first one media
        mQueueIndex = (mQueueIndex >= (mDataSource.size() - 1)) ? 0 : (++mQueueIndex);

        LogUtil.e("jimmy", "mQueueIndex : " + mQueueIndex);
        onMPlay();
    }

    private void onMStop() {

        LogUtil.e("jimmy", "onStop");
        mMediaPlayer.stop();
        //setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
        setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
        //unregisterReceiver(mNoisyReceiver);
        mMediaPlayer.reset();
        stopForeground(true);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.e("jimmy", "Service onCreate");
        mNotificationMgr = new NotificationMgr(this);
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        initMediaPlayer();
        initMediaSession();
        initNoisyReceiver();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        LogUtil.e("jimmy", "onTaskRemoved");
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.e("jimmy", "service destroy()");

        if (mAudioManager != null) mAudioManager.abandonAudioFocus(this);
        unregisterReceiver(mNoisyReceiver);
        mMediaSessionCompat.release();
        if (mMediaPlayer != null && mMediaPlayer.isPlaying())
            mMediaPlayer.stop();
        if (mMediaPlayer != null) mMediaPlayer.release();

    }

    private void initMediaPlayer() {

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setVolume(1.0f, 1.0f);

        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                LogUtil.e("MediaPlayService", "init MediaPlayer ");
                mMediaPlayer.start();
                setMediaPlaybackState(PlaybackStateCompat.STATE_PLAYING);
                showPausedNotification(mDataSource.get(mQueueIndex).getTitle(), mDataSource.get(mQueueIndex).getSubTitle(), mDataSource.get(mQueueIndex).getMediaContent(),
                        mDataSource.get(mQueueIndex).getLargerIcon(), mDataSource.get(mQueueIndex).getSmallIcon());
            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                LogUtil.e("jimmy", "media onError");
                return false;
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                LogUtil.e("MediaPlayService", "MediaPlayer onCompletion()");
                if (isRepeat) {
                    LogUtil.i("jimmy", " repeat, play next song");
                    //tricky, simulate media key event
                    if (mAudioManager != null)
                        mAudioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_MEDIA_NEXT));
                } else {
                    showPlayingNotification(mDataSource.get(mQueueIndex).getTitle() + "(播放完畢)", mDataSource.get(mQueueIndex).getSubTitle() + "(播放完畢)",
                            mDataSource.get(mQueueIndex).getMediaContent() + "播放完畢", -1, -1);
                    if (mAudioManager != null)
                        mAudioManager.dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KEYCODE_MEDIA_PAUSE));
                    setMediaPlaybackState(PlaybackStateCompat.STATE_PAUSED);
                }
            }
        });
    }

    private void initMediaSession() {
        LogUtil.e("jimmy", "initMediaSession");

        mMediaSessionCompat = new MediaSessionCompat(getApplicationContext(), "MediaPlayerService");

        mMediaSessionCompat.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_QUEUE_COMMANDS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        mMediaSessionCompat.setCallback(mMediaSessionCallback);

        setSessionToken(mMediaSessionCompat.getSessionToken());
        mMediaSessionCompat.setActive(true);
    }

    private void initNoisyReceiver() {
        //Handles headphones unplugged.
        IntentFilter filter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        registerReceiver(mNoisyReceiver, filter);
    }

    private void showPausedNotification(String title, String subTitle, String mediaContent, int largerIcon, int smallIcon) {

        ContextCompat.startForegroundService(
                MediaPlayerService.this,
                new Intent(MediaPlayerService.this, MediaPlayerService.class));

        Notification notification = mNotificationMgr.getNotification(PlaybackStateCompat.STATE_PAUSED, getSessionToken(), title, subTitle, mediaContent, largerIcon, smallIcon);
        mNotificationMgr.getNotificationManager().notify(NOTIFICATION_ID, notification);
        startForeground(NOTIFICATION_ID, notification);
    }

    private void showPlayingNotification(String title, String subTitle, String mediaContent, int largerIcon, int smallIcon) {

        stopForeground(false);
        Notification notification = mNotificationMgr.getNotification(PlaybackStateCompat.STATE_PLAYING, getSessionToken(), title, subTitle, mediaContent, largerIcon, smallIcon);
        mNotificationMgr.getNotificationManager().notify(NOTIFICATION_ID, notification);
    }

    private void setMediaPlaybackState(int state) {

        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        if (state == PlaybackStateCompat.STATE_PLAYING) {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PAUSE);
        } else {
            playbackstateBuilder.setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_PLAY);
        }
        playbackstateBuilder.setState(state, PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 0);
        mMediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }

    private void setMediaPlaybackErrorState(int state) {

        PlaybackStateCompat.Builder playbackstateBuilder = new PlaybackStateCompat.Builder();
        playbackstateBuilder.setErrorMessage(state, "");
        mMediaSessionCompat.setPlaybackState(playbackstateBuilder.build());
    }

    private boolean successfullyRetrievedAudioFocus() {

        int result = mAudioManager.requestAudioFocus(this,
                AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);

        return result == AudioManager.AUDIOFOCUS_GAIN;
    }

    //is called to gain the authority to access the Media that the MediaBrowserService provides
    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        if (TextUtils.equals(clientPackageName, getPackageName())) {
            return new BrowserRoot(getString(R.string.app_name), null);
        }

        return null;
    }

    //is called by the subscribe method of the MediaBrowser and will return all child MediaItems
    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        LogUtil.e("jimmy", "audio focus change: " + focusChange);
        switch (focusChange) {
            //stop when another app get audio focus
            case AudioManager.AUDIOFOCUS_LOSS: {
                if (mMediaPlayer.isPlaying()) {
                    mAudioManager.abandonAudioFocus(this);
                    mMediaPlayer.stop();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                    mPlayingSource = null;
                    setMediaPlaybackState(PlaybackStateCompat.STATE_STOPPED);
                    showPlayingNotification(mDataSource.get(mQueueIndex).getTitle(), mDataSource.get(mQueueIndex).getSubTitle(), mDataSource.get(mQueueIndex).getMediaContent(),
                            mDataSource.get(mQueueIndex).getLargerIcon(), mDataSource.get(mQueueIndex).getSmallIcon());
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: {
                mMediaPlayer.pause();
                break;
            }
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: {
                if (mMediaPlayer != null) {
                    mMediaPlayer.setVolume(0.3f, 0.3f);
                }
                break;
            }
            case AudioManager.AUDIOFOCUS_GAIN: {
                if (mMediaPlayer != null) {
                    if (!mMediaPlayer.isPlaying()) {
                        mMediaPlayer.start();
                    }
                    mMediaPlayer.setVolume(1.0f, 1.0f);
                }
                break;
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mMediaSessionCompat, intent);
        return super.onStartCommand(intent, flags, startId);
    }

}
