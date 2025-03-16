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
    private MediaPlayer mediaPlayer; // For game over sound
    private MediaPlayer lifeLostSound; // For life lost sound
    private BallView ballView;
    private PaddleView paddleView;
    private FrameLayout gameContainer;
    private TextView scoreTextView;
    private int score = 0;
    private int lives = 3; // Number of lives the player starts with
    private boolean isGameRunning = false;
    private Thread gameThread;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI components
        scoreTextView = findViewById(R.id.scoreTextView);
        gameContainer = findViewById(R.id.gameContainer);

        // Initialize and add ball and paddle views to the game container
        ballView = new BallView(this);
        gameContainer.addView(ballView);

        paddleView = new PaddleView(this);
        gameContainer.addView(paddleView);

        // Initialize sounds
        mediaPlayer = MediaPlayer.create(this, R.raw.lose_sound);
        lifeLostSound = MediaPlayer.create(this, R.raw.life_lost_sound); // Initialize life lost sound

        // Update score text to include lives
        updateScoreAndLivesText();

        // Start the game loop
        startGameLoop();
    }

    private void startGameLoop() {
        // Ensure any previous game thread is stopped
        if (gameThread != null && gameThread.isAlive()) {
            isGameRunning = false;
            try {
                gameThread.join(); // Wait for the thread to finish
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        isGameRunning = true;

        // Game loop running in a separate thread
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
                        ballView.update();

                        // Check for collision with paddle
                        if (ballView.isCollidingWith(paddleView)) {
                            score++;
                            ballView.bounceOffPaddle();
                            // Increase speed significantly and show level dialog every 10 points
                            if (score % 10 == 0) {
                                ballView.increaseSpeedForMilestone();
                                showLevelUpDialog();
                            }
                            ballView.increaseSpeed(); // Regular speed increase after each hit
                            updateScoreAndLivesText();
                        }
                        // Check if ball is out of bounds
                        else if (ballView.isOutOfBounds()) {
                            lives--;
                            if (lives > 0) {
                                lifeLostSound.start(); // Play life lost sound
                                ballView.reset(); // Reset ball position and speed
                                updateScoreAndLivesText();
                            } else {
                                isGameRunning = false;
                                showGameOverDialog();
                            }
                        }
                    }
                });
            }
        });

        gameThread.start();
    }

    private void showLevelUpDialog() {
        if (isFinishing() || isDestroyed()) return;

        // Pause the game while showing the dialog
        isGameRunning = false;

        // Calculate current level (score / 10)
        int level = score / 10;

        // Show level-up dialog on the main thread
        mainHandler.post(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Level Up!");
            builder.setMessage("You have successfully completed Level " + level + "!");
            builder.setCancelable(false);
            builder.setPositiveButton("Continue", (dialog, which) -> {
                // Restart the game loop after dialog is dismissed
                startGameLoop();
            });
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
            builder.setMessage("Your final score: " + score + "\nLives remaining: 0");
            builder.setCancelable(false);
            builder.setPositiveButton("Restart", (dialog, which) -> resetGame());
            builder.setNegativeButton("Exit", (dialog, which) -> {
                isGameRunning = false;
                finish();
            });
            builder.show();
        });
    }

    private void resetGame() {
        // Reset game state and restart the loop
        score = 0;
        lives = 3; // Reset lives to initial value
        scoreTextView.setText("Score: 0 | Lives: 3");
        ballView.reset();
        startGameLoop();
    }

    private void updateScoreAndLivesText() {
        // Update the score and lives display
        scoreTextView.setText("Score: " + score + " | Lives: " + lives);
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