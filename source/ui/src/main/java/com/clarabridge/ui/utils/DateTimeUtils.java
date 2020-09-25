package com.clarabridge.ui.utils;

import android.content.res.Resources;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public final class DateTimeUtils {
    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;

    private DateTimeUtils() {
    }

    public static long getDeltaHours(final Date lhs, final Date rhs) {
        long lhsTime = 0;
        long rhsTime = 0;

        if (lhs != null) {
            lhsTime = lhs.getTime();
        }

        if (rhs != null) {
            rhsTime = rhs.getTime();
        }

        return (rhsTime - lhsTime) / HOUR;
    }

    public static String getRelativeTimestampLongText(long time, Resources resources) {
        long timeDifference = System.currentTimeMillis() - time;
        final int minute = 60 * 1000;
        final int hour = 60 * minute;
        final int day = 24 * hour;

        if (timeDifference < minute) {
            return resources.getString(com.clarabridge.ui.R.string.ClarabridgeChat_relativeTimeJustNow);
        } else if (timeDifference >= minute && timeDifference < hour) {
            return resources.getQuantityString(com.clarabridge.ui.R.plurals.ClarabridgeChat_conversationLastUpdatedMinutes,
                    Math.round(timeDifference / minute),
                    Math.round(timeDifference / minute));
        } else if (timeDifference < day) {
            return resources.getQuantityString(com.clarabridge.ui.R.plurals.ClarabridgeChat_conversationLastUpdatedHours,
                    Math.round(timeDifference / hour),
                    Math.round(timeDifference / hour));
        } else if (timeDifference > day && timeDifference < (day * 2)) {
            return resources.getString(com.clarabridge.ui.R.string.ClarabridgeChat_conversationLastUpdatedYesterday);
        }

        DateFormat dateStyleFormatter = DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());
        return dateStyleFormatter.format(time);
    }
}

