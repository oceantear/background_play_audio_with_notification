package com.example.myradio;

import android.os.Environment;
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
import java.util.ArrayList;
import java.util.List;

import static com.example.myradio.MediaStyleHelper.COMMAND_SET_RESOURCE;


public class MainActivity extends AppCompatActivity {

    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;

    private int mCurrentState;

    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat mMediaControllerCompat;

    private TextView mTitle;
    private Button mPreButton;
    private Button mForwardButton;
    private Button mPlayPauseButton;
    private Button mRewindButton;
    private Button mNextButton;
    private SeekBar mSeekBar;

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

    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();
            try {
                Log.e("jimmy", "mMediaBrowserCompatConnectionCallback  onConnected()");
                mMediaControllerCompat = new MediaControllerCompat(MainActivity.this, mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
                MediaControllerCompat.setMediaController(MainActivity.this, mMediaControllerCompat);
                //TODO: set media resource
                Bundle b = new Bundle();
                b.putString("type","radio");
                b.putStringArrayList("song",mRadio);
                mMediaControllerCompat.getTransportControls().sendCustomAction( COMMAND_SET_RESOURCE, b);
                mCurrentState = PlaybackStateCompat.STATE_PLAYING;
                mPlayPauseButton.setBackground(getResources().getDrawable(R.drawable.ic_pause_black_24dp));

            } catch (RemoteException e) {
                Log.e("jimmy", "browser connected error :" + e.toString());
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
        public void onSessionDestroyed() {
            super.onSessionDestroyed();
        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.e("jimmy", "onPlaybackStateChanged ");
            super.onPlaybackStateChanged(state);
            if (state == null) {
                return;
            }

            switch (state.getState()) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    Log.e("jimmy", "onPlaybackStateChanged : playing" + PlaybackStateCompat.STATE_PLAYING);
                    mCurrentState = PlaybackStateCompat.STATE_PLAYING;
                    //mPlayPauseButton.setPressed(true);
                    mPlayPauseButton.setBackground(getResources().getDrawable(R.drawable.ic_pause_black_24dp));
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    Log.e("jimmy", "onPlaybackStateChanged  pause :" + PlaybackStateCompat.STATE_PAUSED);
                    mCurrentState = PlaybackStateCompat.STATE_PAUSED;
                    //mPlayPauseButton.setPressed(false);
                    mPlayPauseButton.setBackground(getResources().getDrawable(R.drawable.ic_play_black_24dp));
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTitle = findViewById(R.id.title);

        mMode = getIntent().getStringExtra("mode");
        if(mMode.equals("local_music")){
            mTitle.setText(getString(R.string.bt_local_music));
            String path = Environment.getExternalStorageDirectory().toString()+"/Music/";
            Log.e("jimmy","path :" +path);
            File directory = new File(path);
            File[] files = directory.listFiles();
            Log.d("Files", "Size: "+ files.length);
            for (int i = 0; i < files.length; i++)
            {
                mRadio.add(path+files[i].getName());
                Log.d("Files", "FileName:" + files[i].getName());
            }
        }else if(mMode.equals("remote_rtmp")){
            mTitle.setText(getString(R.string.bt_remote_rtmp));
            mRadio.add(FM);
            mRadio.add(Video);
        }else if(mMode.equals("local_movie")){
            //ToDo : find some movie
            mTitle.setText(getString(R.string.bt_local_movie));
        }

        mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, MediaPlayerService.class),
                mMediaBrowserCompatConnectionCallback, getIntent().getExtras());

        mMediaBrowserCompat.connect();
        mCurrentState = PlaybackStateCompat.STATE_NONE;

        mPlayPauseButton = (Button) findViewById(R.id.play_bt);
        mPreButton = (Button) findViewById(R.id.pre_bt);
        mNextButton = (Button) findViewById(R.id.next_bt);
        mRewindButton = (Button) findViewById(R.id.rewind_bt);
        mForwardButton = (Button) findViewById(R.id.froward_bt);


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
                Log.e("jimmy", "play/pause click : " + mCurrentState);
                if (mCurrentState == PlaybackStateCompat.STATE_NONE) {

                } else if (mCurrentState == PlaybackStateCompat.STATE_PAUSED) {
                    Log.e("jimmy","pause -> play");
                    //mMediaControllerCompat.getTransportControls().playFromSearch(FM, null);
                    mMediaControllerCompat.getTransportControls().play();
                    //mCurrentState = PlaybackStateCompat.STATE_PLAYING;
                    mPlayPauseButton.setBackground(getResources().getDrawable(R.drawable.ic_pause_black_24dp));
                } else if (mCurrentState == PlaybackStateCompat.STATE_PLAYING) {
                    Log.e("jimmy","play -> pause");
                    mMediaControllerCompat.getTransportControls().pause();
                    //mCurrentState = PlaybackStateCompat.STATE_PAUSED;
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMediaControllerCompat = MediaControllerCompat.getMediaController(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mMediaControllerCompat != null)
            mMediaControllerCompat.unregisterCallback(mMediaControllerCompatCallback);

        if(mMediaBrowserCompat != null  && mMediaBrowserCompat.isConnected()) {
            mMediaBrowserCompat.disconnect();
        }
    }

    @Override
    protected void onDestroy() {
        Log.e("jimmy", "onDestroy");
        super.onDestroy();

        if(mMediaControllerCompat != null) {
            mMediaControllerCompat.unregisterCallback(mMediaControllerCompatCallback);
            /*if (mMediaControllerCompat.getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING) {
                Log.e("jimmy","onDestroy pause()");
                mMediaControllerCompat.getTransportControls().pause();
            }*/
        }
        if(mMediaBrowserCompat != null  && mMediaBrowserCompat.isConnected()) {
            mMediaBrowserCompat.disconnect();
        }
    }
}
