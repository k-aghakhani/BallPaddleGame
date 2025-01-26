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
        ballX = 500;
        ballY = 500;
        paint = new Paint();
        paint.setColor(Color.RED);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(ballX, ballY, ballRadius, paint);
    }

    public void update() {
        ballX += ballSpeedX;
        ballY += ballSpeedY;

        if (ballX <= ballRadius || ballX >= getWidth() - ballRadius) {
            ballSpeedX = -ballSpeedX;
        }

        if (ballY <= ballRadius) {
            ballSpeedY = -ballSpeedY;
        }

        invalidate();
    }

    public boolean isCollidingWith(PaddleView paddle) {
        float paddleY = paddle.getYPosition();
        return ballY + ballRadius >= paddleY &&
                ballY - ballRadius <= paddleY + paddle.getPaddleHeight() &&
                ballX >= paddle.getPaddleLeft() && ballX <= paddle.getPaddleRight();
    }

    public boolean isOutOfBounds() {
        return ballY - ballRadius > getHeight();
    }

    public void reset() {
        ballX = 500;
        ballY = 500;
        ballSpeedX = 10;
        ballSpeedY = 10;
    }

    public void increaseSpeed() {
        ballSpeedX *= 1.1;
        ballSpeedY *= 1.1;
    }

    public void bounceOffPaddle() {
        ballSpeedY = -Math.abs(ballSpeedY);
    }
}