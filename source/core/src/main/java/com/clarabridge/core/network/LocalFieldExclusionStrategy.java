package com.clarabridge.core.network;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import com.clarabridge.core.annotation.LocalField;

/**
 * An {@link ExclusionStrategy} that skips fields annotated with {@link LocalField}.
 */
class LocalFieldExclusionStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(LocalField.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}
