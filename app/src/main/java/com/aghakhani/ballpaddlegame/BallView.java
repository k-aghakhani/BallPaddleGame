package com.aghakhani.ballpaddlegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class BallView extends View {
    private float ballX, ballY;
    private float ballSpeedX = 10, ballSpeedY = 10;
    private final float ballRadius = 30;
    private Paint paint;

    public BallView(Context context) {
        super(context);
        // Initialize ball position and appearance
        ballX = 500;
        ballY = 500;
        paint = new Paint();
        paint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw the ball on the canvas
        canvas.drawCircle(ballX, ballY, ballRadius, paint);
    }

    public void update() {
        // Update ball position
        ballX += ballSpeedX;
        ballY += ballSpeedY;

        // Bounce off left and right walls
        if (ballX <= ballRadius || ballX >= getWidth() - ballRadius) {
            ballSpeedX = -ballSpeedX;
        }

        // Bounce off top wall
        if (ballY <= ballRadius) {
            ballSpeedY = -ballSpeedY;
        }

        // Request redraw
        invalidate();
    }

    public boolean isCollidingWith(PaddleView paddle) {
        // Check collision with paddle
        float paddleY = paddle.getYPosition();
        return ballY + ballRadius >= paddleY &&
                ballY - ballRadius <= paddleY + paddle.getPaddleHeight() &&
                ballX >= paddle.getPaddleLeft() && ballX <= paddle.getPaddleRight();
    }

    public boolean isOutOfBounds() {
        // Check if ball is below the screen
        return ballY - ballRadius > getHeight();
    }

    public void reset() {
        // Reset ball to initial state
        ballX = 500;
        ballY = 500;
        ballSpeedX = 10;
        ballSpeedY = 10;
    }

    public void increaseSpeed() {
        // Slightly increase ball speed after each paddle hit
        ballSpeedX *= 1.1;
        ballSpeedY *= 1.1;
    }

    public void increaseSpeedForMilestone() {
        // Significantly increase ball speed every 10 points
        ballSpeedX *= 1.5;
        ballSpeedY *= 1.5;
    }

    public void bounceOffPaddle() {
        // Bounce the ball upward off the paddle
        ballSpeedY = -Math.abs(ballSpeedY);
    }
}