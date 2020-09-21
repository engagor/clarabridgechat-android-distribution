package com.clarabridge.core.utils;

import android.support.annotation.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Composition class to create a cached map of enum values to their constant, so that the constant
 * can be found based on a matching string value.
 *
 * @param <T> the Enum
 */
public class ValueIndex<T extends Enum & ValueEnum> {

    private final Map<String, T> valueIndex;

    public ValueIndex(T[] values) {
        valueIndex = new HashMap<>(values.length);
        for (T value : values) {
            valueIndex.put(value.getValue(), value);
        }
    }

    /**
     * Utility method to find the enum value of the provided string.
     *
     * @param value the input string
     * @return a value of {@link T} if the provided value was valid, null otherwise
     */
    @Nullable
    public T get(String value) {
        if (value == null) {
            return null;
        }

        return valueIndex.get(value);
    }

}
