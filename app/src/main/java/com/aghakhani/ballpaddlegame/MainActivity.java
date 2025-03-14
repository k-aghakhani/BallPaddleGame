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
    private MediaPlayer mediaPlayer;
    private BallView ballView;
    private PaddleView paddleView;
    private FrameLayout gameContainer;
    private TextView scoreTextView;
    private int score = 0;
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

        // Initialize sound for game over
        mediaPlayer = MediaPlayer.create(this, R.raw.lose_sound);

        // Start the game loop
        startGameLoop();
    }

    private void startGameLoop() {
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
                            // Increase speed significantly every 10 points
                            if (score % 10 == 0) {
                                ballView.increaseSpeedForMilestone();
                            }
                            ballView.increaseSpeed(); // Regular speed increase after each hit
                            scoreTextView.setText("Score: " + score);
                        }
                        // Check if ball is out of bounds
                        else if (ballView.isOutOfBounds()) {
                            isGameRunning = false;
                            showGameOverDialog();
                        }
                    }
                });
            }
        });

        gameThread.start();
    }

    private void showGameOverDialog() {
        if (isFinishing() || isDestroyed()) return;

        // Show game over dialog on the main thread
        mainHandler.post(() -> {
            mediaPlayer.start();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Game Over");
            builder.setMessage("Your final score: " + score);
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
        scoreTextView.setText("Score: 0");
        ballView.reset();
        startGameLoop();
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