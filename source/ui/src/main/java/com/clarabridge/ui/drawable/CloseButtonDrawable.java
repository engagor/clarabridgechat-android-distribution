package com.clarabridge.ui.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.clarabridge.ui.R;
import com.clarabridge.ui.utils.DpVisitor;

public class CloseButtonDrawable extends Drawable {
    private final Paint paint;
    private final Context context;

    public CloseButtonDrawable(Context context) {
        this.context = context;
        paint = new Paint();
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeJoin(Paint.Join.BEVEL);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(context.getResources().getColor(R.color.ClarabridgeChat_accent));
    }

    @Override
    public void draw(Canvas canvas) {
        final float[] points;
        final Rect b = getBounds();
        final DpVisitor dpVisitor = new DpVisitor(context);
        final float strokeWidth = dpVisitor.toPixels(3);
        final float size = dpVisitor.toPixels(7);

        paint.setStrokeWidth(strokeWidth);
        points = new float[]{size, b.height() - size,
                b.width() - size, size,
                size, size,
                b.width() - size, b.height() - size};

        paint.setColor(context.getResources().getColor(R.color.ClarabridgeChat_accent));
        canvas.drawLines(points, paint);
    }

    @Override
    protected boolean onLevelChange(int level) {
        return super.onLevelChange(level);
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }
}
