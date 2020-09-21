package com.clarabridge.core.di;

import android.support.annotation.RestrictTo;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import javax.inject.Scope;

/**
 * A {@link Scope} annotation to be used for services that should be scoped to the SDK lifecycle.
 */
@Scope
@Retention(RetentionPolicy.RUNTIME)
@RestrictTo(RestrictTo.Scope.LIBRARY)
public @interface SdkScope {
}

