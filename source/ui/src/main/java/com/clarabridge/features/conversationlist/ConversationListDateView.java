package com.clarabridge.features.conversationlist;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import android.util.AttributeSet;

import java.util.concurrent.TimeUnit;

import static com.clarabridge.ui.utils.DateTimeUtils.getRelativeTimestampLongText;

public class ConversationListDateView extends androidx.appcompat.widget.AppCompatTextView {

    private static long MINUTE_IN_MILLIS = TimeUnit.MINUTES.toMillis(1);
    private static long NO_VALUE = -1L;
    private long date = NO_VALUE;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            setText(getRelativeTimestampLongText(date, getResources()));
            handler.postDelayed(this, MINUTE_IN_MILLIS);
        }
    };

    public ConversationListDateView(Context context) {
        super(context);
    }

    public ConversationListDateView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ConversationListDateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (date != NO_VALUE) {
            startTimer();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopTimer();
    }

    public void setDate(Long date) {
        if (date != null && date != NO_VALUE) {
            this.date = date;
            startTimer();
            setText(getRelativeTimestampLongText(date, getResources()));
        } else {
            this.date = NO_VALUE;
            stopTimer();
        }
    }

    private void startTimer() {
        handler.postDelayed(runnable, MINUTE_IN_MILLIS);
    }

    private void stopTimer() {
        handler.removeCallbacks(runnable);
    }
}
