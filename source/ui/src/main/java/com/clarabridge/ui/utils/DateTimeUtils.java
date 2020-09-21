package com.clarabridge.ui.utils;

import java.util.Date;

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
}

