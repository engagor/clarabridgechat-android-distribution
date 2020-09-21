package com.clarabridge.ui.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;

public final class BitmapVisitor {
    private BitmapVisitor() {
    }

    public static Bitmap createRoundedBitmap(final Bitmap image, final int diameter) {
        return createRoundedBitmap(image, diameter, diameter);
    }

    public static Bitmap createRoundedBitmap(final Bitmap image, final int width, final int height) {
        final Bitmap targetBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(targetBitmap);
        final Path path = new Path();
        final Paint paint = new Paint();

        path.addCircle(
                ((float) width) / 2,
                ((float) height) / 2,
                (float) (Math.min(width, height)) / 2,
                Path.Direction.CW);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setFilterBitmap(true);
        canvas.clipPath(path);
        canvas.drawBitmap(image,
                new Rect(0, 0, image.getWidth(), image.getHeight()),
                new RectF(0, 0, width, height),
                paint);

        return targetBitmap;
    }
}

