package com.example.myradio;

import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v7.app.AppCompatActivity;
//import android.core.content.ContextCompat;

import android.content.ComponentName;
import android.content.Intent;
import android.media.session.MediaController;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.example.myradio.MediaStyleHelper.COMMAND_SET_REPEAT;
import static com.example.myradio.MediaStyleHelper.COMMAND_SET_RESOURCE;


public class MainActivity extends AppCompatActivity {

    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;

    private int mCurrentState;

    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat mMediaControllerCompat;

    private TextView mTitle;
    private TextView mContent;
    private Button mPreButton;
    private Button mForwardButton;
    private Button mPlayPauseButton;
    private Button mRewindButton;
    private Button mNextButton;
    private Button mRepeatButton;
    private SeekBar mSeekBar;
    private List<MediaMetadata> mMediaMetadataList = new ArrayList<>();
    private List<Integer> mLargeIcon = new ArrayList<>();

    /**
     * SourceType 0: AM/FM
     * 1: device path music
     * 2: assets
     */
    private int SourceType = 0;
    private String FM = "rtsp://flv.ccdntech.com/live/_definst_/vod256_Live/fm";
    private String Video = "rtsp://wowzaec2demo.streamlock.net/vod/mp4:BigBuckBunny_115k.mov";
    //am broken
    //private String AM = "rtsp://flv.ccdntech.com/live/_definst_/vod256_Live/am";

    private ArrayList<String> mRadio = new ArrayList<>();
    private String mMode;
    private boolean mRepeat = false;


    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnectionSuspended() {
            super.onConnectionSuspended();
        }

        @Override
        public void onConnected() {
            super.onConnected();
            try {
                LogUtil.e("jimmy", "mMediaBrowserCompatConnectionCallback  onConnected()");
                mMediaControllerCompat = new MediaControllerCompat(MainActivity.this, mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
                MediaControllerCompat.setMediaController(MainActivity.this, mMediaControllerCompat);

                /*Bundle b = new Bundle();
                b.putString("type","radio");
                b.putStringArrayList("song",mRadio);
                b.putBoolean("repeat",mRepeat);
                mMediaControllerCompat.getTransportControls().sendCustomAction( COMMAND_SET_RESOURCE, b);
                mCurrentState = PlaybackStateCompat.STATE_PLAYING;
                mPlayPauseButton.setBackground(getResources().getDrawable(R.drawable.ic_pause_black_24dp));*/

            } catch (RemoteException e) {
                LogUtil.e("jimmy", "browser connected error :" + e.toString());
            }
        }
    };

    //receive playback status, update UI
    private MediaControllerCompat.Callback mMediaControllerCompatCallback = new MediaControllerCompat.Callback() {
        /*@Override
        public void onSessionReady() {
            Log.e("jimmy","onPlaybackStateChanged: onSessionReady ");
            super.onSessionReady();

        }*/

        @Override
        public void onMetadataChanged(MediaMetadataCompat metadata) {
            super.onMetadataChanged(metadata);
            LogUtil.e("jimmy","onMetadataChanged");
        }

        @Override
        public void onExtrasChanged(Bundle extras) {
            super.onExtrasChanged(extras);
            LogUtil.e("jimmy","onExtrasChanged");
            //String tmp = extras.getString("title");
            mTitle.setText(extras.getString("title"));
            mContent.setText(extras.getString("content"));

        }

        @Override
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            LogUtil.e("jimmy", "onPlaybackStateChanged : "+state);
            super.onPlaybackStateChanged(state);
            if (state == null) {
                return;
            }

            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    LogUtil.e("jimmy", "onPlaybackStateChanged : playing" + PlaybackStateCompat.STATE_PLAYING);
                    mCurrentState = PlaybackStateCompat.STATE_PLAYING;
                    //mPlayPauseButton.setPressed(true);
                    mPlayPauseButton.setBackground(getResources().getDrawable(R.drawable.ic_pause_black_24dp));
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    LogUtil.e("jimmy", "onPlaybackStateChanged  pause :" + PlaybackStateCompat.STATE_PAUSED);
                    mCurrentState = PlaybackStateCompat.STATE_PAUSED;
                    //mPlayPauseButton.setPressed(false);
                    mPlayPauseButton.setBackground(getResources().getDrawable(R.drawable.ic_play_black_24dp));
                    break;
                }
                case PlaybackStateCompat.STATE_STOPPED :
                    LogUtil.e("jimmy", "onPlaybackStateChanged  stopped :" + PlaybackStateCompat.STATE_STOPPED);
                    mCurrentState = PlaybackStateCompat.STATE_STOPPED;
                    mPlayPauseButton.setBackground(getResources().getDrawable(R.drawable.ic_play_black_24dp));
                    mContent.setText("播放停止");
                    break;
            }

            switch(state.getErrorCode()){

                case PlaybackStateCompat.ERROR_CODE_APP_ERROR:
                    LogUtil.e("jimmy", "onPlaybackStateChanged  error :" + PlaybackStateCompat.ERROR_CODE_APP_ERROR);
                    mContent.setText("內部錯誤，請檢查網路or source正確");
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("jimmy","onCreate");
        setContentView(R.layout.activity_main);
        mTitle = findViewById(R.id.title);
        mContent = findViewById(R.id.content);

        //init large icon
        initLargeIcon();

        mMode = getIntent().getStringExtra("mode");
        if(mMode.equals("local_music")){
            mTitle.setText(getString(R.string.bt_local_music));
            String path = Environment.getExternalStorageDirectory().toString()+"/Music/";
            LogUtil.e("jimmy","path :" +path);
            File directory = new File(path);
            File[] files = directory.listFiles();
            LogUtil.d("Files", "Size: "+ files.length);
            for (int i = 0; i < files.length; i++) {
                mRadio.add(path+files[i].getName());
                LogUtil.e("jimmy", "FileName:" + files[i].getName());
                getMediaInfo(path+files[i].getName());
            }

        }else if(mMode.equals("remote_rtmp")){
            mTitle.setText(getString(R.string.bt_remote_rtmp));
            //mRadio.add(FM);
            //mRadio.add(Video);
            mMediaMetadataList.add(new MediaMetadata("音樂", "廣播1 title" ,"副標題", FM , R.drawable.ic_stat_image_audiotrack, mLargeIcon.get(0)));
            mMediaMetadataList.add(new MediaMetadata("音樂", "廣播2 title" ,"副標題", Video , R.drawable.ic_stat_image_audiotrack, mLargeIcon.get(1)));
        }else if(mMode.equals("local_movie")){
            //ToDo : find some movie
            mTitle.setText(getString(R.string.bt_local_movie));
        }



        if(mMediaBrowserCompat == null) {
            mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, MediaPlayerService.class),
                    mMediaBrowserCompatConnectionCallback, getIntent().getExtras());

            mMediaBrowserCompat.connect();
        }
        mCurrentState = PlaybackStateCompat.STATE_NONE;

        mPlayPauseButton = (Button) findViewById(R.id.play_bt);
        mPreButton = (Button) findViewById(R.id.pre_bt);
        mNextButton = (Button) findViewById(R.id.next_bt);
        mRewindButton = (Button) findViewById(R.id.rewind_bt);
        mForwardButton = (Button) findViewById(R.id.froward_bt);
        mRepeatButton = findViewById(R.id.repeat_bt);


        mRewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        //rewind
        mPreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaControllerCompat.getTransportControls().skipToPrevious();
            }
        });

        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogUtil.e("jimmy", "play/pause click : " + mCurrentState);
                if (mCurrentState == PlaybackStateCompat.STATE_NONE || mCurrentState == PlaybackStateCompat.STATE_STOPPED) {
                    setDataSourceToService();
                    mPlayPauseButton.setBackground(getResources().getDrawable(R.drawable.ic_pause_black_24dp));
                } else if (mCurrentState == PlaybackStateCompat.STATE_PAUSED) {
                    LogUtil.e("jimmy","pause -> play");
                    //mMediaControllerCompat.getTransportControls().playFromSearch(FM, null);
                    mMediaControllerCompat.getTransportControls().play();
                    //mCurrentState = PlaybackStateCompat.STATE_PLAYING;
                    mPlayPauseButton.setBackground(getResources().getDrawable(R.drawable.ic_pause_black_24dp));
                } else if (mCurrentState == PlaybackStateCompat.STATE_PLAYING) {
                    LogUtil.e("jimmy","play -> pause");
                    mMediaControllerCompat.getTransportControls().pause();
                    mCurrentState = PlaybackStateCompat.STATE_PAUSED;
                    mPlayPauseButton.setBackground(getResources().getDrawable(R.drawable.ic_play_black_24dp));
                }
            }
        });

        mForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        //forward
        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMediaControllerCompat.getTransportControls().skipToNext();
            }
        });

        /*mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });*/

        mRepeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRepeat = !mRepeat;
                if(mRepeat) {
                    mRepeatButton.setBackground(getResources().getDrawable(R.drawable.ic_repeat_black_24dp));
                }else {
                    mRepeatButton.setBackground(getResources().getDrawable(R.drawable.ic_not_repeat_black_24dp));
                }
                Bundle b = new Bundle();
                b.putBoolean("repeat", mRepeat);
                mMediaControllerCompat.getTransportControls().sendCustomAction(COMMAND_SET_REPEAT, b);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaControllerCompat = MediaControllerCompat.getMediaController(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Log.i("jimmy","onStop");

    }

    @Override
    protected void onDestroy() {
        LogUtil.e("jimmy", "Main onDestroy");
        super.onDestroy();

        if(mMediaControllerCompat != null) {
            mMediaControllerCompat.unregisterCallback(mMediaControllerCompatCallback);
            //service will take care player destroy
            /*if (mMediaControllerCompat.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                Log.e("jimmy","onDestroy pause()");
                mMediaControllerCompat.getTransportControls().pause();
            }*/
        }
        if(mMediaBrowserCompat != null  && mMediaBrowserCompat.isConnected()) {
            mMediaBrowserCompat.disconnect();
        }
    }

    void initLargeIcon(){
        if(mLargeIcon == null) mLargeIcon = new ArrayList<>();
        mLargeIcon.add(R.drawable.album_jazz_blues);
        mLargeIcon.add(R.drawable.album_youtube_audio_library_rock_2);
        mLargeIcon.add(R.drawable.sun_piano);
        mLargeIcon.add(R.drawable.sea_piano);
    }

    void getMediaInfo(String path){
        Uri uri = Uri.parse(path);
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        mmr.setDataSource(this,uri);
        String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
        int millSecond = Integer.parseInt(durationStr);
        LogUtil.e("jimmy","song title : "+ title + ", duration :" +millSecond);
        if(mMediaMetadataList == null) mMediaMetadataList = new ArrayList<>();

            /** Notification style
             *             this.mTitle = mTitle;
             *             this.mMediaContent = mMediaContent;
             *             this.mSubTitle = mSubTitle;
             *             this.mSmallIcon = mSmallIcon;
             *             this.mLargerIcon = mLargerIcon;
             *      ---------------------------------------------------------------------------------
             *     | Small icon   \   AppName  \  subText                                           |
             *     | Title                                                  Larger icon                         |
             *     | MediaContent                                                                              |
             *     ----------------------------------------------------------------------------------
             * */
       int icon = -1;
       icon = mLargeIcon.get(mMediaMetadataList.size());

       mMediaMetadataList.add(new MediaMetadata("音樂", title ,"副標題", path , R.drawable.ic_stat_image_audiotrack, icon));

    }

    void setDataSourceToService(){
        Bundle b = new Bundle();
        b.putString("type","radio");
        b.putSerializable("song", (Serializable) mMediaMetadataList);
        b.putBoolean("repeat",mRepeat);
        mMediaControllerCompat.getTransportControls().sendCustomAction( COMMAND_SET_RESOURCE, b);
        mCurrentState = PlaybackStateCompat.STATE_PLAYING;
    }
}
