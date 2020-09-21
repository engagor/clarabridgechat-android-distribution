package com.clarabridge.core.utils;

import android.support.annotation.Nullable;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class DateUtils {
    private static final String TAG = "DateUtils";

    private DateUtils() {
    }

    public static String toIso(final Date date) {
        return getIsoFormat().format(date);
    }

    public static Date fromIso(final String iso) {
        if (!StringUtils.isEmpty(iso)) {
            try {
                return getIsoFormat().parse(iso);
            } catch (final ParseException ex) {
                Log.e(TAG, String.format("Error parsing date: %s", iso), ex);
            }
        }

        return null;
    }

    private static SimpleDateFormat getIsoFormat() {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy'-'MM'-'dd'T'HH':'mm':'ss'.'SSS'Z'");

        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        return format;
    }

    /**
     * Converts a timestamp returned by the API to a {@link Date} object, given that the input
     * is not null or 0.
     *
     * @param timestamp the timestamp to convert
     * @return a {@link Date} if the input is not null and greater than 0, null otherwise
     */
    @Nullable
    public static Date timestampToDate(@Nullable Double timestamp) {
        if (timestamp != null && timestamp > 0.0) {
            return new Date((long) (timestamp * 1000));
        }

        return null;
    }
}

