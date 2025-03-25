package com.aghakhani.ballpaddlegame;

import android.app.AlertDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private MediaPlayer mediaPlayer; // Media player for game over sound
    private MediaPlayer lifeLostSound; // Media player for life lost sound
    private MediaPlayer lifeGainedSound; // Media player for life gained sound
    private BallView ballView; // Ball view instance
    private HeartView heartView; // Heart view instance for bonus life
    private PaddleView paddleView; // Paddle view instance
    private FrameLayout gameContainer; // Container for game views
    private TextView scoreTextView; // TextView to display score, lives, and level
    private int score = 0; // Player's current score
    private int lives = 3; // Player's current lives
    private int level = 1; // Player's current level (starts at 1)
    private boolean isGameRunning = false; // Flag to control game loop
    private Thread gameThread; // Thread for game loop
    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // Handler for UI updates
    private long lastHeartSpawnTime = 0; // Timestamp of last heart spawn
    private static final long HEART_SPAWN_INTERVAL = 15000; // Interval for heart spawn (15 seconds)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        scoreTextView = findViewById(R.id.scoreTextView);
        gameContainer = findViewById(R.id.gameContainer);

        // Initialize and add ball, paddle, and heart views to the game container
        ballView = new BallView(this);
        gameContainer.addView(ballView);

        paddleView = new PaddleView(this);
        gameContainer.addView(paddleView);

        heartView = new HeartView(this);
        gameContainer.addView(heartView);

        // Initialize sound effects
        mediaPlayer = MediaPlayer.create(this, R.raw.lose_sound);
        lifeLostSound = MediaPlayer.create(this, R.raw.life_lost_sound);
        lifeGainedSound = MediaPlayer.create(this, R.raw.life_gained_sound); // Sound for gaining a life

        // Update initial score, lives, and level display
        updateScoreLivesLevelText();

        // Start the game loop
        startGameLoop();
    }

    private void startGameLoop() {
        // Stop any existing game thread
        if (gameThread != null && gameThread.isAlive()) {
            isGameRunning = false;
            try {
                gameThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        isGameRunning = true;

        // Game loop in a separate thread
        gameThread = new Thread(() -> {
            while (isGameRunning) {
                try {
                    Thread.sleep(16); // Target 60 FPS
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                // Update game state on the main thread
                mainHandler.post(() -> {
                    if (isGameRunning) {
                        ballView.update(); // Update ball position
                        heartView.update(); // Update heart position

                        // Check collision between ball and paddle
                        if (ballView.isCollidingWith(paddleView)) {
                            score++;
                            ballView.bounceOffPaddle();
                            if (score % 10 == 0) {
                                level++; // Increase level every 10 points
                                ballView.increaseSpeedForMilestone();
                                showLevelUpDialog();
                            }
                            ballView.increaseSpeed();
                            updateScoreLivesLevelText();
                        }
                        // Check if ball is out of bounds
                        else if (ballView.isOutOfBounds()) {
                            lives--;
                            if (lives > 0) {
                                lifeLostSound.start();
                                ballView.reset();
                                updateScoreLivesLevelText();
                            } else {
                                isGameRunning = false;
                                showGameOverDialog();
                            }
                        }

                        // Check collision between heart and paddle
                        if (heartView.isCollidingWith(paddleView)) {
                            lives++; // Increase lives by 1
                            lifeGainedSound.start(); // Play life gained sound
                            heartView.reset(); // Reset heart position
                            updateScoreLivesLevelText();
                        }

                        // Spawn heart periodically every 15 seconds
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastHeartSpawnTime >= HEART_SPAWN_INTERVAL) {
                            heartView.reset();
                            lastHeartSpawnTime = currentTime;
                        }
                    }
                });
            }
        });

        gameThread.start();
    }

    private void showLevelUpDialog() {
        if (isFinishing() || isDestroyed()) return;

        // Pause the game while showing dialog
        isGameRunning = false;

        // Show level-up dialog on the main thread
        mainHandler.post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Level Up!");
            builder.setMessage("You have successfully completed Level " + level + "!");
            builder.setCancelable(false);
            builder.setPositiveButton("Continue", (dialog, which) -> startGameLoop());
            builder.show();
        });
    }

    private void showGameOverDialog() {
        if (isFinishing() || isDestroyed()) return;

        // Show game over dialog on the main thread
        mainHandler.post(() -> {
            mediaPlayer.start();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Game Over");
            builder.setMessage("Your final score: " + score + "\nLives remaining: 0\nFinal Level: " + level);
            builder.setCancelable(false);
            builder.setPositiveButton("Restart", (dialog, which) -> resetGame());
            builder.setNegativeButton("Exit", (dialog, which) -> finish());
            builder.show();
        });
    }

    private void resetGame() {
        // Reset game state and restart loop
        score = 0;
        lives = 3;
        level = 1; // Reset level to 1
        scoreTextView.setText("Score: 0 | Lives: 3 | Level: 1");
        ballView.reset();
        heartView.reset();
        startGameLoop();
    }

    private void updateScoreLivesLevelText() {
        // Update the score, lives, and level display
        scoreTextView.setText("Score: " + score + " | Lives: " + lives + " | Level: " + level);
    }

    @Override
    protected void onPause() {
        super.onPause();
        isGameRunning = false; // Pause the game loop
        if (gameThread != null && gameThread.isAlive()) {
            gameThread.interrupt();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isGameRunning) {
            startGameLoop(); // Resume the game loop
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isGameRunning = false;
        if (gameThread != null && gameThread.isAlive()) {
            gameThread.interrupt();
        }
        // Release media player resources
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (lifeLostSound != null) {
            lifeLostSound.release();
            lifeLostSound = null;
        }
        if (lifeGainedSound != null) {
            lifeGainedSound.release();
            lifeGainedSound = null;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Handle paddle movement based on touch input
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                paddleView.setPaddlePosition(event.getX());
                break;
        }
        return true;
    }
}