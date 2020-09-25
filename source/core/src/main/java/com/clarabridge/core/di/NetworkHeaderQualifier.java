package com.clarabridge.core.di;

import androidx.annotation.RestrictTo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Qualifier;

/**
 * A {@link Qualifier} annotation to be used by interceptors that should be used when making requests
 * to the ClarabridgeChat API.
 */
@Qualifier
@Retention(RetentionPolicy.RUNTIME)
@RestrictTo(RestrictTo.Scope.LIBRARY)
public @interface NetworkHeaderQualifier {
    String value();
}

