package com.clarabridge.ui.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

public final class ApplicationInfo {
    private ApplicationInfo() {
    }

    public static String getName(final Context context) {
        final CharSequence name = context.getPackageManager().getApplicationLabel(context.getApplicationInfo());

        if (name != null) {
            return name.toString();
        }

        return "";
    }

    public static Bitmap getPackageIcon(final Context context) {
        Drawable drawable = null;

        try {
            drawable = context.getPackageManager().getApplicationIcon(context.getPackageName());
        } catch (final PackageManager.NameNotFoundException ignored) {
            // Intentionally empty
        }

        Resources res = context.getResources();
        int height = (int) res.getDimension(android.R.dimen.notification_large_icon_height);
        int width = (int) res.getDimension(android.R.dimen.notification_large_icon_width);

        Bitmap mutableBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return mutableBitmap;
    }
}

