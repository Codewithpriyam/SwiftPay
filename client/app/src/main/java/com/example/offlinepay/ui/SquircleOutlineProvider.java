package com.example.offlinepay.ui;

import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.annotation.RequiresApi;

/**
 * SquircleOutlineProvider
 *
 * Provides a "continuous curvature" squircle path at a given corner radius.
 * Unlike a standard rounded rect (circular arcs), a squircle transitions the
 * straight edge into the curve smoothly — matching the Apple Liquid Glass geometry.
 *
 * Implementation: approximates the squircle using 8 cubic Bezier segments.
 * The Bezier control point ratio (0.552284749831) mimics a true circle arc;
 * we use a slightly higher value (0.63) to give it the characteristic "squircle"
 * flattening at the corners.
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class SquircleOutlineProvider extends ViewOutlineProvider {

    private final float cornerRadiusPx;

    public SquircleOutlineProvider(float cornerRadiusPx) {
        this.cornerRadiusPx = cornerRadiusPx;
    }

    @Override
    public void getOutline(View view, Outline outline) {
        float w = view.getWidth();
        float h = view.getHeight();
        if (w <= 0 || h <= 0) return;

        // Clamp corner radius
        float r = Math.min(cornerRadiusPx, Math.min(w, h) / 2f);

        // Build the squircle path
        Path path = buildSquirclePath(w, h, r);

        // For API 30+ we can set path directly; for lower APIs use clipPath rect
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            outline.setPath(path);
        } else {
            // Fallback: use standard round rect (visually very close)
            outline.setRoundRect(0, 0, (int) w, (int) h, r);
        }
        outline.setAlpha(1.0f);
    }

    /**
     * Builds a squircle path using cubic bezier segments.
     * k = 0.63 is the squircle control factor (vs 0.552 for a true circle).
     */
    private Path buildSquirclePath(float w, float h, float r) {
        final float k = 0.63f * r;  // control point distance
        Path path = new Path();

        // Start: top edge, left of top-right corner
        path.moveTo(r, 0);
        // Top edge → top-right squircle corner
        path.lineTo(w - r, 0);
        path.cubicTo(w - r + k, 0, w, r - k, w, r);
        // Right edge → bottom-right squircle corner
        path.lineTo(w, h - r);
        path.cubicTo(w, h - r + k, w - r + k, h, w - r, h);
        // Bottom edge → bottom-left squircle corner
        path.lineTo(r, h);
        path.cubicTo(r - k, h, 0, h - r + k, 0, h - r);
        // Left edge → top-left squircle corner
        path.lineTo(0, r);
        path.cubicTo(0, r - k, r - k, 0, r, 0);
        path.close();
        return path;
    }
}
