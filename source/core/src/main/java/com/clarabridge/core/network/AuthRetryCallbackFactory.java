package com.clarabridge.core.network;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import com.clarabridge.core.AuthenticationCallback;
import com.clarabridge.core.AuthenticationDelegate;
import retrofit2.Callback;

/**
 * A factory to create instances of {@link AuthRetryCallback} to be used as {@link Callback} when
 * calling {@link retrofit2.Call#enqueue(Callback)}.
 */
public class AuthRetryCallbackFactory {

    @Nullable
    private final AuthenticationDelegate authenticationDelegate;

    @Inject
    public AuthRetryCallbackFactory(@Nullable AuthenticationDelegate authenticationDelegate) {
        this.authenticationDelegate = authenticationDelegate;
    }

    /**
     * Creates a new instance of {@link AuthRetryCallback}.
     *
     * @param callback               the {@link ClarabridgeChatApiClientCallback} to be called when the request
     *                               is finished
     * @param authenticationCallback the {@link AuthenticationCallback} to be called if an authentication
     *                               error happens
     * @param <T>                    the type of the object being returned in the response
     * @return a new instance of {@link AuthRetryCallback}
     */
    public <T> Callback<T> createCallback(ClarabridgeChatApiClientCallback<T> callback,
                                          @Nullable AuthenticationCallback authenticationCallback) {

        return new AuthRetryCallback<>(callback, authenticationDelegate, authenticationCallback);
    }
}
