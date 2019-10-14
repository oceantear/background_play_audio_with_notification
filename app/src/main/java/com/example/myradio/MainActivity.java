package com.example.myradio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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


public class MainActivity extends AppCompatActivity {

    private static final int STATE_PAUSED = 0;
    private static final int STATE_PLAYING = 1;

    private int mCurrentState;

    private MediaBrowserCompat mMediaBrowserCompat;
    private MediaControllerCompat mMediaControllerCompat;

    private Button mPlayPauseToggleButton;
    private static final String FM = "rtsp://flv.ccdntech.com/live/_definst_/vod256_Live/fm";
    private static final String AM = "rtsp://flv.ccdntech.com/live/_definst_/vod256_Live/am";

    private MediaBrowserCompat.ConnectionCallback mMediaBrowserCompatConnectionCallback = new MediaBrowserCompat.ConnectionCallback() {

        @Override
        public void onConnected() {
            super.onConnected();
            try {
                Log.e("jimmy","mMediaBrowserCompatConnectionCallback  onConnected()");
                mMediaControllerCompat = new MediaControllerCompat(MainActivity.this, mMediaBrowserCompat.getSessionToken());
                mMediaControllerCompat.registerCallback(mMediaControllerCompatCallback);
                //setSupportMediaController(mMediaControllerCompat);
                mMediaControllerCompat.setMediaController(MainActivity.this, mMediaControllerCompat);
                //getSupportMediaController().getTransportControls().playFromMediaId(String.valueOf(R.raw.warner_tautz_off_broadway), null);
                //TODO: play media
                //mMediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().playFromMediaId();

            } catch( RemoteException e ) {
                Log.e("jimmy","browser connected error :"+e.toString());
            }
        }
    };

    private MediaControllerCompat.Callback mMediaControllerCompatCallback = new MediaControllerCompat.Callback() {
        @Override
        public void onSessionReady() {
            Log.e("jimmy","onPlaybackStateChanged: onSessionReady ");
            super.onSessionReady();

        }

        @Override
        public void onPlaybackStateChanged(PlaybackStateCompat state) {
            Log.e("jimmy","onPlaybackStateChanged ");
            super.onPlaybackStateChanged(state);
            if( state == null ) {
                return;
            }

            switch( state.getState() ) {
                case PlaybackStateCompat.STATE_PLAYING: {
                    Log.e("jimmy","onPlaybackStateChanged :"+STATE_PLAYING);
                    mCurrentState = STATE_PLAYING;
                    break;
                }
                case PlaybackStateCompat.STATE_PAUSED: {
                    Log.e("jimmy","onPlaybackStateChanged :"+STATE_PLAYING);
                    mCurrentState = STATE_PAUSED;
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMediaBrowserCompat = new MediaBrowserCompat(this, new ComponentName(this, MediaPlayerService.class),
                mMediaBrowserCompatConnectionCallback, getIntent().getExtras());

        mMediaBrowserCompat.connect();

        mPlayPauseToggleButton = (Button) findViewById(R.id.button);

        mPlayPauseToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if( mCurrentState == STATE_PAUSED ) {
                    //getSupportMediaController().getTransportControls().play();
                    mMediaControllerCompat.getMediaController();
                    mCurrentState = STATE_PLAYING;
                } else {
                    /*if( getSupportMediaController().getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ) {
                        getSupportMediaController().getTransportControls().pause();
                    }*/
                    if(mCurrentState == PlaybackStateCompat.STATE_PLAYING){
                        mMediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().pause();
                    }else{
                        Log.e("jimmy","play bt click");
                        //mMediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().play();
                        mMediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().playFromSearch(FM,null);
                        //mMediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().prepareFromSearch(FM,null);
                    }
                    mCurrentState = STATE_PAUSED;
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( mMediaControllerCompat.getMediaController(MainActivity.this).getPlaybackState().getState() == PlaybackStateCompat.STATE_PLAYING ) {
            mMediaControllerCompat.getMediaController(MainActivity.this).getTransportControls().pause();
        }

        mMediaBrowserCompat.disconnect();
    }
}
