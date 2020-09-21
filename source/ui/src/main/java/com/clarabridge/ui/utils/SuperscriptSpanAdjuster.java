package com.clarabridge.ui.utils;

/**
 * This is a helper class to help adjust the alignment of a section of text, when using SpannableStrings to set text
 * formatting dynamically.
 * From: http://stackoverflow.com/questions/8991606/adjusting-text-alignment-using-spannablestring
 */

import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class SuperscriptSpanAdjuster extends MetricAffectingSpan {
    double ratio = 0.5;

    public SuperscriptSpanAdjuster() {
    }

    public SuperscriptSpanAdjuster(double ratio) {
        this.ratio = ratio;
    }

    @Override
    public void updateDrawState(TextPaint paint) {
        paint.baselineShift += (int) (paint.ascent() * ratio);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        paint.baselineShift += (int) (paint.ascent() * ratio);
    }
}
