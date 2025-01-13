package com.example.smartattendancesystem.Student;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.smartattendancesystem.R;

public class Inventors extends AppCompatActivity {
    private MediaPlayer mediaPlayer;
    private static final String PREF_NAME = "SongPrefs";
    private static final String LAST_SONG_KEY = "LastPlayedSong";

    // Array of song resources (replace these R.raw values with your actual song resources)
    private final int[] songs = {
            R.raw.song1,
            R.raw.song2,
            R.raw.song3,
            R.raw.song4
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_inventors);

        // UI fullscreen handling
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                            View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        // Play a different song when activity starts
        playNextSong();
    }

    private void playNextSong() {
        // Get the last played song index
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        int lastSongIndex = prefs.getInt(LAST_SONG_KEY, -1);

        // Calculate next song index
        int nextSongIndex = (lastSongIndex + 1) % songs.length;

        // Save the current song index
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(LAST_SONG_KEY, nextSongIndex);
        editor.apply();

        // Stop any currently playing song
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        // Create and start new MediaPlayer
        mediaPlayer = MediaPlayer.create(this, songs[nextSongIndex]);
        mediaPlayer.setOnCompletionListener(mp -> {
            // Optional: what to do when song ends
            // mp.start(); // Uncomment to loop the song
        });
        mediaPlayer.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}