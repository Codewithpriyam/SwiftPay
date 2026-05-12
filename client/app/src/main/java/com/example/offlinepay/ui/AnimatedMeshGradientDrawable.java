package com.example.offlinepay.ui;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * AnimatedMeshGradientDrawable
 *
 * Renders three overlapping radial gradient "blobs" that slowly drift across
 * the canvas, producing the liquid mesh-gradient "bleeding" effect visible
 * through the frosted glass card above it.
 *
 * Uses a simple Handler-based 16ms tick (≈60fps) instead of Choreographer
 * to avoid threading pitfalls on older API levels.
 */
public class AnimatedMeshGradientDrawable extends Drawable {

    private static final int COLOR_INDIGO    = 0xFF0F172A; // Top Indigo
    private static final int COLOR_DEEP_BLUE = 0xFF0A0F1F; // Bottom Deep Blue
    private static final int COLOR_SOFT_TEAL = 0xFF1E293B; // Mid Blend

    private final Paint paintIndigo  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintBlue    = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintTeal    = new Paint(Paint.ANTI_ALIAS_FLAG);

    // Ultra-slow speed for subtle "liquid" feel
    private static final float SPEED = 0.00008f; 

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean running = false;

    private final Runnable tickRunnable = new Runnable() {
        @Override
        public void run() {
            if (running) {
                invalidateSelf();
                handler.postDelayed(this, 16); // ~60fps
            }
        }
    };

    public void start() {
        if (!running) {
            running = true;
            handler.post(tickRunnable);
        }
    }

    public void stop() {
        running = false;
        handler.removeCallbacks(tickRunnable);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        android.graphics.Rect b = getBounds();
        int w = b.width();
        int h = b.height();
        if (w == 0 || h == 0) return;

        // Draw a deep dark base first so background is never pure white
        canvas.drawColor(0xFF0A0A1A);

        float t = System.currentTimeMillis() * SPEED;
        float r = Math.max(w, h) * 0.80f;

        // Blob 1: Indigo
        float x1 = w * (0.5f + 0.38f * (float) Math.sin(t * 0.8));
        float y1 = h * (0.4f + 0.30f * (float) Math.cos(t * 0.6));
        paintIndigo.setShader(new RadialGradient(x1, y1, r,
                new int[]{COLOR_INDIGO, 0x000F172A}, null, Shader.TileMode.CLAMP));
        canvas.drawPaint(paintIndigo);

        // Blob 2: Deep Blue
        float x2 = w * (0.5f + 0.42f * (float) Math.cos(t * 0.5));
        float y2 = h * (0.55f + 0.32f * (float) Math.sin(t * 0.9));
        paintBlue.setShader(new RadialGradient(x2, y2, r,
                new int[]{COLOR_DEEP_BLUE, 0x000A0F1F}, null, Shader.TileMode.CLAMP));
        canvas.drawPaint(paintBlue);

        // Blob 3: Soft Teal
        float x3 = w * (0.45f + 0.35f * (float) Math.sin(t * 1.1 + 1.2));
        float y3 = h * (0.6f  + 0.38f * (float) Math.cos(t * 0.45 + 0.5));
        paintTeal.setShader(new RadialGradient(x3, y3, r,
                new int[]{COLOR_SOFT_TEAL, 0x001E293B}, null, Shader.TileMode.CLAMP));
        canvas.drawPaint(paintTeal);
    }

    @Override
    public void setAlpha(int alpha) {
        paintIndigo.setAlpha(alpha);
        paintBlue.setAlpha(alpha);
        paintTeal.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        paintIndigo.setColorFilter(colorFilter);
        paintBlue.setColorFilter(colorFilter);
        paintTeal.setColorFilter(colorFilter);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
