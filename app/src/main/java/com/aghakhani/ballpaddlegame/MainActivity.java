package com.aghakhani.ballpaddlegame;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private BallView ballView;
    private PaddleView paddleView;
    private RelativeLayout gameLayout;
    private TextView scoreTextView;
    private int score = 0;
    private boolean isGameRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create the game layout
        gameLayout = new RelativeLayout(this);
        gameLayout.setBackgroundColor(Color.BLACK);

        // Create and add the ball view
        ballView = new BallView(this);
        gameLayout.addView(ballView);

        // Create and add the paddle view
        paddleView = new PaddleView(this);
        gameLayout.addView(paddleView);

        // Create and add the score text view
        scoreTextView = new TextView(this);
        scoreTextView.setTextColor(Color.WHITE);
        scoreTextView.setTextSize(24);
        scoreTextView.setText("Score: 0");
        RelativeLayout.LayoutParams scoreParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        scoreParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        scoreParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        gameLayout.addView(scoreTextView, scoreParams);

        setContentView(gameLayout);

        // Start the game loop
        startGameLoop();
    }

    private void startGameLoop() {
        new Thread(() -> {
            while (isGameRunning) {
                try {
                    Thread.sleep(16); // Approximately 60 FPS
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                runOnUiThread(() -> {
                    ballView.update();

                    if (ballView.isCollidingWith(paddleView)) {
                        score++;
                        ballView.bounceOffPaddle();
                        ballView.increaseSpeed();
                        scoreTextView.setText("Score: " + score);
                    } else if (ballView.isOutOfBounds()) {
                        showGameOverDialog();
                    }
                });
            }
        }).start();
    }

    private void showGameOverDialog() {
        if (isFinishing() || isDestroyed()) return;

        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Game Over");
            builder.setMessage("Your final score: " + score);
            builder.setCancelable(false);
            builder.setPositiveButton("Restart", (dialog, which) -> resetGame());
            builder.setNegativeButton("Exit", (dialog, which) -> finish());
            builder.show();
        });
    }

    private void resetGame() {
        score = 0;
        scoreTextView.setText("Score: 0");
        ballView.reset();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isGameRunning = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                paddleView.setPaddlePosition(event.getX());
                break;
        }
        return true;
    }
}