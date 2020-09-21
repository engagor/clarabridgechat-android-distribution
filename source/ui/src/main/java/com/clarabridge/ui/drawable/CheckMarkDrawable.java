package com.clarabridge.ui.drawable;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;

import com.clarabridge.ui.R;
import com.clarabridge.ui.utils.DpVisitor;

public class CheckMarkDrawable extends Drawable {
    private final Paint paint;
    private final Context context;

    public CheckMarkDrawable(Context context) {
        this.context = context;

        paint = new Paint();
        paint.setStrokeCap(Paint.Cap.SQUARE);
        paint.setFlags(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeJoin(Paint.Join.BEVEL);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    @Override
    public void draw(Canvas canvas) {
        final Rect b = getBounds();
        final DpVisitor dpVisitor = new DpVisitor(context);
        final float strokeWidth = dpVisitor.toPixels(3);
        final float middleX = b.width() / 2;
        final float middleY = b.height() / 2;

        paint.setStrokeWidth(strokeWidth);

        paint.setColor(context.getResources().getColor(R.color.ClarabridgeChat_accent));
        canvas.drawRect(b, paint);

        Path path = new Path();
        path.moveTo(middleX - dpVisitor.toPixels(10), middleY + dpVisitor.toPixels(5));
        path.lineTo(middleX, middleY + dpVisitor.toPixels(15));
        path.moveTo(middleX, middleY + dpVisitor.toPixels(15));
        path.lineTo(middleX + dpVisitor.toPixels(25), middleY - dpVisitor.toPixels(10));

        paint.setColor(Color.WHITE);
        canvas.drawPath(path, paint);
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
