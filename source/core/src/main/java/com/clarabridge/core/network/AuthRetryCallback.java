package com.clarabridge.core.network;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.clarabridge.core.AuthenticationCallback;
import com.clarabridge.core.AuthenticationDelegate;
import com.clarabridge.core.AuthenticationError;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static java.net.HttpURLConnection.HTTP_INTERNAL_ERROR;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

/**
 * A {@link Callback} implementation to be used for all network requests. It has an automatic retry
 * strategy for authenticated requests.
 * <p>
 * A retry can happen if the response code is {@link java.net.HttpURLConnection#HTTP_UNAUTHORIZED},
 * the current number of retry attempts is less then {@link #MAX_RETRY_ATTEMPTS} and
 * {@link #authenticationDelegate} is not null.
 *
 * @param <T> the response type of the {@link Call}
 */
public class AuthRetryCallback<T> implements Callback<T> {

    /**
     * The max number of retry attempts this request is allowed to perform
     */
    @VisibleForTesting
    static final int MAX_RETRY_ATTEMPTS = 5;
    /**
     * The current number of retry attempts this request has performed
     */
    @VisibleForTesting
    int retryAttempts = 0;

    @Nullable
    private final ClarabridgeChatApiClientCallback<T> callback;
    @Nullable
    private final AuthenticationDelegate authenticationDelegate;
    @Nullable
    private final AuthenticationCallback authenticationCallback;

    public AuthRetryCallback(@Nullable ClarabridgeChatApiClientCallback<T> callback,
                             @Nullable AuthenticationDelegate authenticationDelegate,
                             @Nullable AuthenticationCallback authenticationCallback) {

        this.callback = callback;
        this.authenticationDelegate = authenticationDelegate;
        this.authenticationCallback = authenticationCallback;
    }

    @Override
    public void onResponse(@NonNull final Call<T> call, @NonNull Response<T> response) {
        boolean shouldRetry = response.code() == HTTP_UNAUTHORIZED
                && retryAttempts < MAX_RETRY_ATTEMPTS
                && authenticationDelegate != null;

        if (shouldRetry) {
            authenticationDelegate.onInvalidAuth(createAuthError(response), createAuthCallback(call));
        } else if (callback != null) {
            callback.onResult(response.isSuccessful(), response.code(), response.body());
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        if (callback != null) {
            callback.onResult(false, HTTP_INTERNAL_ERROR, null);
        }
    }

    /**
     * Creates a new instance of {@link AuthenticationError} with the details of the failed request.
     *
     * @param response the {@link Response} of the failed request
     * @return a new instance of {@link AuthenticationError}
     */
    @VisibleForTesting
    AuthenticationError createAuthError(@NonNull Response<T> response) {
        String responseBodyString = response.body() != null ? response.body().toString() : "";

        return new AuthenticationError(response.code(), responseBodyString);
    }

    /**
     * Creates a new instance of {@link AuthenticationCallback} that wraps the {@link #authenticationCallback}
     * and calls it internally when {@link AuthenticationCallback#updateToken(String)} is called.
     * <p>
     * The wrapper will update the {@link AuthRetryCallback#retryAttempts} and enqueue the received
     * {@link Call} again when {@link AuthenticationCallback#updateToken(String)} is called.
     *
     * @param call the original {@link Call} to be cloned and enqueued again
     * @return the instance of {@link AuthenticationCallback}
     */
    @VisibleForTesting
    AuthenticationCallback createAuthCallback(@NonNull final Call<T> call) {
        return new AuthenticationCallback() {
            @Override
            public void updateToken(@NonNull String jwt) {
                if (authenticationCallback != null) {
                    authenticationCallback.updateToken(jwt);
                }
                retryAttempts++;

                call.clone().enqueue(AuthRetryCallback.this);
            }
        };
    }

}
