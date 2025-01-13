// GameView.java
package com.example.smartattendancesystem.Student.ScheduleClass;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.view.MotionEvent;
import android.view.View;

import com.example.smartattendancesystem.R;

import java.util.ArrayList;
import java.util.List;

public class GameView extends View {
    private Bird bird;
    private List<Pipe> pipes;
    private Paint paint;
    private boolean isPlaying;
    private int score;
    private float screenWidth;
    private static float screenHeight;
    private static final int PIPE_SPACING = 600;
    private static final int PIPE_WIDTH = 100;
    private static final int PIPE_GAP = 400;

    // Audio variables
    private MediaPlayer bgMusic;
    private SoundPool soundPool;
    private int jumpSound;
    private int deathSound;
    private boolean isSoundLoaded;

    public GameView(Context context) {
        super(context);
        paint = new Paint();
        isPlaying = false;
        score = 0;
        pipes = new ArrayList<>();

        // Initialize audio
        initAudio();
    }

    private void initAudio() {
        // Initialize background music
        bgMusic = MediaPlayer.create(getContext(), R.raw.song4);
        bgMusic.setLooping(true);

        // Initialize sound effects
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(2)
                .setAudioAttributes(audioAttributes)
                .build();

        // Load sound effects
        deathSound = soundPool.load(getContext(), R.raw.death, 1);

        soundPool.setOnLoadCompleteListener((soundPool, sampleId, status) -> {
            isSoundLoaded = true;
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
        initGame();
    }
    private void gameOver() {
        isPlaying = false;
        // Play death sound
        if (isSoundLoaded) {
            soundPool.play(deathSound, 1.0f, 1.0f, 1, 0, 1.0f);
        }
        // Pause background music
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }

    private void update() {
        if (!isPlaying || bird == null) return;

        bird.update();

        // Update pipes
        for (Pipe pipe : pipes) {
            pipe.update();

            // Check collision
            if (checkCollision(bird, pipe)) {
                gameOver();
                return;
            }

            // Update score
            if (!pipe.passed && pipe.x + PIPE_WIDTH < bird.x) {
                score++;
                pipe.passed = true;
            }
        }

        // Recycle pipes
        for (Pipe pipe : pipes) {
            if (pipe.x + PIPE_WIDTH < 0) {
                pipe.reset(findLastPipeX() + PIPE_SPACING);
            }
        }

        // Check if bird hits ground or ceiling
        if (bird.y > screenHeight || bird.y < 0) {
            gameOver();
        }
    }

    // Clean up resources
    public void onDestroy() {
        if (bgMusic != null) {
            bgMusic.release();
            bgMusic = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
    }

    // Pause game and audio
    public void onPause() {
        if (bgMusic != null && bgMusic.isPlaying()) {
            bgMusic.pause();
        }
    }

    // Resume game and audio
    public void onResume() {
        if (bgMusic != null && !bgMusic.isPlaying() && isPlaying) {
            bgMusic.start();
        }
    }

    private void initGame() {
        // Reset bird position and velocity
        bird = new Bird(screenWidth / 3, screenHeight / 2);

        // Clear and reinitialize pipes
        pipes.clear();
        for (int i = 0; i < 3; i++) {
            pipes.add(new Pipe(screenWidth + i * PIPE_SPACING));
        }

        // Reset score
        score = 0;
        isPlaying = true;

        // Start background music
        if (bgMusic != null && !bgMusic.isPlaying()) {
            bgMusic.start();
        }

        // Force a redraw
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (isPlaying) {
                bird.jump();
                // Play jump sound
                if (isSoundLoaded) {
                    soundPool.play(jumpSound, 1.0f, 1.0f, 1, 0, 1.0f);
                }
            } else {
                // Complete reset of game state
                bird = null;
                pipes.clear();
                initGame();
            }
            invalidate();
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw background
        canvas.drawColor(Color.rgb(135, 206, 235));

        // Draw bird
        if (bird != null) {
            paint.setColor(Color.YELLOW);
            canvas.drawCircle(bird.x, bird.y, bird.radius, paint);
        }

        // Draw pipes
        paint.setColor(Color.GREEN);
        for (Pipe pipe : pipes) {
            canvas.drawRect(pipe.x, 0, pipe.x + PIPE_WIDTH, pipe.topHeight, paint);
            canvas.drawRect(pipe.x, pipe.bottomY, pipe.x + PIPE_WIDTH, screenHeight, paint);
        }

        // Draw score - Set alignment to LEFT before drawing score
        paint.setColor(Color.BLACK);
        paint.setTextSize(50);
        paint.setTextAlign(Paint.Align.LEFT);  // Reset alignment for score
        canvas.drawText("Score: " + score, 50, 100, paint);

        if (isPlaying) {
            update();
            invalidate();
        } else {
            // Game over text - Change alignment for centered text
            paint.setTextSize(80);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText("Game Over!", screenWidth / 2, screenHeight / 2, paint);
            paint.setTextSize(40);
            canvas.drawText("Tap to restart", screenWidth / 2, screenHeight / 2 + 80, paint);
        }
    }

    private float findLastPipeX() {
        float maxX = 0;
        for (Pipe pipe : pipes) {
            maxX = Math.max(maxX, pipe.x);
        }
        return maxX;
    }

    private boolean checkCollision(Bird bird, Pipe pipe) {
        return bird.x + bird.radius > pipe.x &&
                bird.x - bird.radius < pipe.x + PIPE_WIDTH &&
                (bird.y - bird.radius < pipe.topHeight ||
                        bird.y + bird.radius > pipe.bottomY);
    }

    private static class Bird {
        float x, y;
        float velocity;
        float gravity;
        float jumpForce;
        float radius;

        Bird(float x, float y) {
            this.x = x;
            this.y = y;
            velocity = 0;
            gravity = 0.8f;
            jumpForce = -15;
            radius = 30;
        }

        void update() {
            velocity += gravity;
            y += velocity;
        }

        void jump() {
            velocity = jumpForce;
        }
    }

    private static class Pipe {
        float x;
        float topHeight;
        float bottomY;
        boolean passed;
        private static final float SPEED = 5;

        Pipe(float x) {
            this.x = x;
            reset(x);
        }

        void update() {
            x -= SPEED;
        }

        void reset(float x) {
            this.x = x;
            topHeight = (float) (Math.random() * (screenHeight - PIPE_GAP - 200) + 100);
            bottomY = topHeight + PIPE_GAP;
            passed = false;
        }
    }
}