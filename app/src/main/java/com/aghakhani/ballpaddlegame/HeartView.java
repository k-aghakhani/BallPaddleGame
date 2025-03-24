package com.aghakhani.ballpaddlegame;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class HeartView extends View {
    private float heartX, heartY; // Heart position
    private float heartSpeedY = 5; // Speed of heart moving downward
    private final float heartSize = 40; // Size of the heart
    private Paint paint; // Paint object for drawing
    private boolean isVisible = false; // Visibility flag for heart

    public HeartView(Context context) {
        super(context);
        heartX = 500; // Initial X position
        heartY = -heartSize; // Start above the screen
        paint = new Paint();
        paint.setColor(Color.GREEN); // Set heart color to green
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isVisible) {
            // Draw a simple circle as a heart (for simplicity)
            canvas.drawCircle(heartX, heartY, heartSize / 2, paint);
        }
    }

    public void update() {
        if (isVisible) {
            heartY += heartSpeedY; // Move heart downward
            if (heartY - heartSize > getHeight()) {
                isVisible = false; // Hide heart if it goes off-screen
            }
            invalidate(); // Request redraw
        }
    }

    public boolean isCollidingWith(PaddleView paddle) {
        if (!isVisible) return false; // No collision if heart is not visible
        float paddleY = paddle.getYPosition();
        // Check collision with paddle
        return heartY + heartSize / 2 >= paddleY &&
                heartY - heartSize / 2 <= paddleY + paddle.getPaddleHeight() &&
                heartX >= paddle.getPaddleLeft() && heartX <= paddle.getPaddleRight();
    }

    public void reset() {
        // Reset heart to top with random X position
        heartX = (float) (Math.random() * (getWidth() - heartSize));
        heartY = -heartSize;
        isVisible = true; // Make heart visible
    }
}