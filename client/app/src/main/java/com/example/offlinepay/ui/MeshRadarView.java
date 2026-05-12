package com.example.offlinepay.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import java.util.ArrayList;
import java.util.List;

public class MeshRadarView extends View {
    private Paint circlePaint;
    private Paint glowPaint;
    private Paint peerPaint;
    private float pulseRadius = 0;
    private ValueAnimator pulseAnimator;
    private int peerCount = 0;

    public MeshRadarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(2f);
        circlePaint.setColor(Color.parseColor("#33FFFFFF"));

        glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        glowPaint.setStyle(Paint.Style.FILL);
        glowPaint.setColor(Color.parseColor("#1AFFFFFF"));

        peerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        peerPaint.setStyle(Paint.Style.FILL);
        peerPaint.setColor(Color.parseColor("#00E676")); // Vibrant Green
        peerPaint.setShadowLayer(10, 0, 0, Color.parseColor("#00E676"));

        pulseAnimator = ValueAnimator.ofFloat(0, 1);
        pulseAnimator.setDuration(3000);
        pulseAnimator.setRepeatCount(ValueAnimator.INFINITE);
        pulseAnimator.setInterpolator(new LinearInterpolator());
        pulseAnimator.addUpdateListener(animation -> {
            pulseRadius = (float) animation.getAnimatedValue();
            invalidate();
        });
        pulseAnimator.start();
    }

    public void setPeerCount(int count) {
        this.peerCount = count;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float centerX = getWidth() / 2f;
        float centerY = getHeight() / 2f;
        float maxRadius = Math.min(centerX, centerY) * 0.8f;

        // Draw pulses
        for (int i = 0; i < 3; i++) {
            float r = ((pulseRadius + (i * 0.33f)) % 1.0f) * maxRadius;
            circlePaint.setAlpha((int) ((1.0f - (r / maxRadius)) * 50));
            canvas.drawCircle(centerX, centerY, r, circlePaint);
        }

        // Draw core glow
        canvas.drawCircle(centerX, centerY, 20, glowPaint);

        // Draw Peers as glowing dots on the rings
        if (peerCount > 0) {
            for (int i = 0; i < peerCount; i++) {
                double angle = Math.toRadians((360.0 / peerCount) * i);
                float px = centerX + (float) (Math.cos(angle) * maxRadius * 0.6);
                float py = centerY + (float) (Math.sin(angle) * maxRadius * 0.6);
                canvas.drawCircle(px, py, 8, peerPaint);
            }
        }
    }
}
