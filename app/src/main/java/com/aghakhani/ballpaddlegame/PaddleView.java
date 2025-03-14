package com.aghakhani.ballpaddlegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class PaddleView extends View {
    private float paddleX;
    private final float paddleWidth = 200;
    private final float paddleHeight = 30;
    private Paint paint;

    public PaddleView(Context context) {
        super(context);
        // Initialize paddle position and appearance
        paddleX = 400;
        paint = new Paint();
        paint.setColor(Color.BLUE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // Draw the paddle near the bottom of the screen
        float paddleY = getHeight() - 100;
        canvas.drawRect(paddleX, paddleY, paddleX + paddleWidth, paddleY + paddleHeight, paint);
    }

    public void setPaddlePosition(float x) {
        // Update paddle position based on touch input
        paddleX = x - paddleWidth / 2;
        if (paddleX < 0) paddleX = 0;
        if (paddleX + paddleWidth > getWidth()) paddleX = getWidth() - paddleWidth;
        invalidate();
    }

    public float getPaddleLeft() {
        // Return left edge of the paddle
        return paddleX;
    }

    public float getPaddleRight() {
        // Return right edge of the paddle
        return paddleX + paddleWidth;
    }

    public float getYPosition() {
        // Return vertical position of the paddle
        return getHeight() - 100;
    }

    public float getPaddleHeight() {
        // Return paddle height
        return paddleHeight;
    }
}