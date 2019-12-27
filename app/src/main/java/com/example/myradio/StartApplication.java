package com.example.myradio;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

public class StartApplication extends AppCompatActivity {

    Button mLocalMusic;
    Button mRemoteRtmp;
    Button mLocalMovie;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);
        mLocalMusic = findViewById(R.id.local_music);
        mRemoteRtmp = findViewById(R.id.remote_rtmp);
        mLocalMovie = findViewById(R.id.local_movie);

        mLocalMusic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(StartApplication.this , MainActivity.class);
                i.putExtra("mode","local_music");
                startActivity(i);
            }
        });

        mRemoteRtmp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(StartApplication.this , MainActivity.class);
                i.putExtra("mode","remote_rtmp");
                startActivity(i);
            }
        });

        mLocalMovie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(StartApplication.this , MainActivity.class);
                i.putExtra("mode","local_movie");
                startActivity(i);
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();
    }


}
