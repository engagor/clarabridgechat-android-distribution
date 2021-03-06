package com.clarabridge.core;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * Stubbed implementation of {@link ClarabridgeChatCallback} that does nothing when invoked
 *
 * @param <T> the type parameter for this callback
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public class StubbedCallback<T> implements ClarabridgeChatCallback<T> {

    @Override
    public void run(@NonNull Response<T> response) {
        // Intentionally empty
    }

}
