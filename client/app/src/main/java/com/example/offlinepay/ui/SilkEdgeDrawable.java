package com.example.offlinepay.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import eightbitlab.com.blurview.BlurView;

/**
 * VibrantBlurCardView
 *
 * A FrameLayout subclass that wraps the card content and renders:
 *  1. The blurred + vibrant (ColorMatrix) background via BlurView
 *  2. A LinearGradient "silk edge" border (white 30% → transparent)
 *  3. Squircle geometry via SquircleOutlineProvider
 *
 * Usage in XML: replace the FrameLayout card wrapper with this view.
 */
public class SilkEdgeDrawable extends View {

    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint   = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF bounds      = new RectF();
    private final float STROKE_WIDTH_DP;

    public SilkEdgeDrawable(Context ctx) {
        super(ctx);
        STROKE_WIDTH_DP = 1.5f * ctx.getResources().getDisplayMetrics().density;
        init();
    }

    public SilkEdgeDrawable(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        STROKE_WIDTH_DP = 1.5f * ctx.getResources().getDisplayMetrics().density;
        init();
    }

    private void init() {
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(STROKE_WIDTH_DP);
        fillPaint.setStyle(Paint.Style.FILL);
        setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w == 0 || h == 0) return;

        bounds.set(STROKE_WIDTH_DP / 2, STROKE_WIDTH_DP / 2,
                   w - STROKE_WIDTH_DP / 2, h - STROKE_WIDTH_DP / 2);

        // Silk edge: LinearGradient from #FFFFFF4D (top-left) → #FFFFFF00 (bottom-right)
        strokePaint.setShader(new LinearGradient(
                0, 0, w, h,
                0x4DFFFFFF,   // 30% white
                0x00FFFFFF,   // fully transparent
                Shader.TileMode.CLAMP
        ));

        // Subtle fill: very slight white tint so the card is distinguishable
        fillPaint.setColor(0x12FFFFFF); // ~7% white
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float r = 80f * getResources().getDisplayMetrics().density / getResources().getDisplayMetrics().density;
        // Use a fixed 28dp corner radius in pixels
        float cornerPx = 28f * getResources().getDisplayMetrics().density;

        // Fill
        canvas.drawRoundRect(bounds, cornerPx, cornerPx, fillPaint);
        // Silk edge stroke
        canvas.drawRoundRect(bounds, cornerPx, cornerPx, strokePaint);
    }
}
