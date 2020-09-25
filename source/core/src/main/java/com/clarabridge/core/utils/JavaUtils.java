package com.clarabridge.core.utils;

import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.Callable;

public final class JavaUtils {

    private JavaUtils() {
    }

    /**
     * Replacement for
     * @see Objects#equals(Object, Object)
     */
    @SuppressWarnings("EqualsReplaceableByObjectsCall")
    public static boolean equals(@Nullable Object a, @Nullable Object b) {
        return (a == b) || (a != null && a.equals(b));
    }

    /**
     * Replacement for
     * @see Objects#hash(Object...)
     */
    public static int hash(@Nullable Object... a) {
        if (a == null) {
            return 0;
        }

        int result = 1;

        for (Object element : a) {
            result = 31 * result + (element == null ? 0 : element.hashCode());
        }

        return result;
    }

    /**
     * Checks if all of the provided objects are null,
     */
    public static boolean allNull(Object... args) {
        if (args == null) {
            return true;
        }

        int nullCount = 0;
        boolean containsNull = false;
        for (Object s : args) {
            nullCount += s == null ? 1 : 0;
        }
        return nullCount == args.length;
    }

    /**
     * @return `true` if all elements match the given [predicate].
     */
    public static <T> boolean all(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null || collection.isEmpty()) {
            return true;
        }
        for (T item : collection) {
            if (!predicate.apply(item)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @return `true` if all elements match the given [predicate].
     */
    public static <T> boolean map(Collection<T> collection, Predicate<T> predicate) {
        if (collection == null || collection.isEmpty()) {
            return true;
        }
        for (T item : collection) {
            if (!predicate.apply(item)) {
                return false;
            }
        }
        return true;
    }
}
