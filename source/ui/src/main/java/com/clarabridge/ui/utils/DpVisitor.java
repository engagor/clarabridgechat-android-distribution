package com.clarabridge.ui.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.TypedValue;

public final class DpVisitor {
    private final Resources resources;

    public DpVisitor(final Context context) {
        this.resources = context.getResources();
    }

    public float toPixels(final float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public static float toPixels(final Context context, final float dp) {
        final Resources r = context.getResources();

        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
    }
}

