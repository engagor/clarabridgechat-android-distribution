package com.clarabridge.core;

import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.util.Locale;

import com.clarabridge.core.utils.StringUtils;

/**
 * Logger class that can be enabled/disable to assist with debugging SDK integrations without
 * always printing information to the app output log.
 */
@SuppressWarnings("checkstyle:MethodName")
public class Logger {

    private static final String DEFAULT_LOG_TAG = "ClarabridgeChat";
    private static final int MAX_LOG_TAG_LENGTH = 23;

    private static boolean isEnabled;

    private Logger() {
        // No instance, much singleton, wow
    }

    /**
     * Sets the enabled/disabled state of the {@link Logger}
     *
     * @param isEnabled true if logging should be enabled, false otherwise
     */
    public static void setEnabled(boolean isEnabled) {
        Logger.isEnabled = isEnabled;
    }

    /**
     * Returns the enabled state of the Logger.
     *
     * @return true if logging is enabled, false otherwise
     */
    public static boolean isEnabled() {
        return Logger.isEnabled;
    }

    /**
     * Logs a {@link Log#VERBOSE} log message
     *
     * @param logTag  The log tag for the log message
     * @param message The body text for the log message
     * @param args    Arguments used for formatted string messages
     */
    public static void v(String logTag, String message, Object... args) {
        v(logTag, message, null, args);
    }

    /**
     * Logs a {@link Log#VERBOSE} level log message
     *
     * @param logTag    The log tag for the log message
     * @param message   The body text for the log message
     * @param throwable A throwable exception to be written to the log message
     * @param args      Arguments used for formatted string messages
     */
    public static void v(String logTag, String message, Throwable throwable, Object... args) {
        if (isEnabled) {
            Log.v(sanitiseLogTag(logTag), formatMessage(message, args), throwable);
        }
    }

    /**
     * Logs an {@link Log#INFO} level log message
     *
     * @param logTag  The log tag for the log message
     * @param message The body text for the log message
     * @param args    Arguments used for formatted string messages
     */
    public static void i(String logTag, String message, Object... args) {
        i(logTag, message, null, args);
    }

    /**
     * Logs a {@link Log#INFO} level log message
     *
     * @param logTag    The log tag for the log message
     * @param message   The body text for the log message
     * @param throwable A throwable exception to be written to the log message
     * @param args      Arguments used for formatted string messages
     */
    public static void i(String logTag, String message, Throwable throwable, Object... args) {
        if (isEnabled) {
            Log.i(sanitiseLogTag(logTag), formatMessage(message, args), throwable);
        }
    }

    /**
     * Logs an {@link Log#DEBUG} level log message
     *
     * @param logTag  The log tag for the log message
     * @param message The body text for the log message
     * @param args    Arguments used for formatted string messages
     */
    public static void d(String logTag, String message, Object... args) {
        d(logTag, message, null, args);
    }

    /**
     * Logs a {@link Log#DEBUG} level log message
     *
     * @param logTag    The log tag for the log message
     * @param message   The body text for the log message
     * @param throwable A throwable exception to be written to the log message
     * @param args      Arguments used for formatted string messages
     */
    public static void d(String logTag, String message, Throwable throwable, Object... args) {
        if (isEnabled) {
            Log.d(sanitiseLogTag(logTag), formatMessage(message, args), throwable);
        }
    }

    /**
     * Logs an {@link Log#WARN} level log message
     *
     * @param logTag  The log tag for the log message
     * @param message The body text for the log message
     * @param args    Arguments used for formatted string messages
     */
    public static void w(String logTag, String message, Object... args) {
        w(logTag, message, null, args);
    }

    /**
     * Logs a {@link Log#WARN} level log message
     *
     * @param logTag    The log tag for the log message
     * @param message   The body text for the log message
     * @param throwable A throwable exception to be written to the log message
     * @param args      Arguments used for formatted string messages
     */
    public static void w(String logTag, String message, Throwable throwable, Object... args) {
        if (isEnabled) {
            Log.w(sanitiseLogTag(logTag), formatMessage(message, args), throwable);
        }
    }

    /**
     * Logs an {@link Log#ERROR} level log message
     *
     * @param logTag  The log tag for the log message
     * @param message The body text for the log message
     * @param args    Arguments used for formatted string messages
     */
    public static void e(String logTag, String message, Object... args) {
        e(logTag, message, null, args);
    }

    /**
     * Logs a {@link Log#ERROR} level log message
     *
     * @param logTag    The log tag for the log message
     * @param message   The body text for the log message
     * @param throwable A throwable exception to be written to the log message
     * @param args      Arguments used for formatted string messages
     */
    public static void e(String logTag, String message, Throwable throwable, Object... args) {
        if (isEnabled) {
            Log.e(sanitiseLogTag(logTag), formatMessage(message, args), throwable);

        }
    }

    /**
     * Examines and sanitises the given log tag to ensure it is not empty or longer than the max
     * allowed log tag length.
     * <p>
     * If the given tag was empty, then logs will use the {@link #DEFAULT_LOG_TAG}
     * <p>
     * If the given tag was too long, then it will be truncated
     *
     * @param logTag the log tag to be examined
     * @return the sanitised log tag
     */
    @VisibleForTesting
    static String sanitiseLogTag(String logTag) {
        if (StringUtils.isEmpty(logTag)) {
            return DEFAULT_LOG_TAG;
        }
        return logTag.length() > MAX_LOG_TAG_LENGTH
                ? logTag.substring(0, MAX_LOG_TAG_LENGTH)
                : logTag;
    }

    /**
     * Formats the given string to contain the given args
     *
     * @param message the message format
     * @param args    the arguments used for formatting
     * @return the formatted string, or the original message if there were no args provided
     */
    @VisibleForTesting
    static String formatMessage(String message, Object... args) {
        if (message == null) {
            return "";
        }
        if (args == null || args.length == 0) {
            return message;
        }
        return String.format(Locale.US, message, args);
    }

}
