package com.clarabridge.core.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class StringUtils {
    private StringUtils() {
    }

    public static boolean isNotNullAndEqual(final String lhs, final String rhs) {
        if (lhs == null || rhs == null) {
            return false;
        }

        return isEqual(lhs, rhs);
    }


    public static boolean isNotNullAndNotEqual(String lhs, String rhs) {
        if (lhs == null || rhs == null) {
            return false;
        }

        return !isEqual(lhs, rhs);
    }

    public static boolean isEqual(final String lhs, final String rhs) {
        if (lhs == null && rhs == null) {
            return true;
        }

        if (lhs == null || rhs == null) {
            return false;
        }

        return lhs.equals(rhs);
    }

    public static boolean isEmpty(final String s) {
        if (s == null) {
            return true;
        }

        return s.isEmpty();
    }

    /**
     * Checks if any of the provided strings are empty, returning true if at least one is either
     * null or empty.
     *
     * @param args the Strings to be checked
     * @return true if at least one argument is null or empty, false otherwise
     */
    public static boolean anyEmpty(String... args) {
        if (args == null) {
            return true;
        }
        boolean result = false;
        for (String s : args) {
            result |= isEmpty(s);
        }
        return result;
    }

    public static String emptyIfNull(final String s) {
        if (s == null) {
            return "";
        }

        return s;
    }

    public static String emptyIfNull(CharSequence s) {
        if (s == null) {
            return "";
        }

        return s.toString();
    }

    public static String encode(final String s) {
        String encodedString;

        try {
            encodedString = URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 is an unknown encoding!?");
        }

        return encodedString;
    }
}

