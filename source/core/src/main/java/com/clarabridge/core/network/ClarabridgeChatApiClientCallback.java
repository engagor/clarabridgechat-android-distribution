package com.clarabridge.core.network;

import android.support.annotation.Nullable;

import okhttp3.Response;

/**
 * A callback interface to be notified of the response of network requests or if an error occurred,
 * {@link #onResult} is always called.
 *
 * @param <T> the type of the Object being returned in the response body in case of a successful
 *            response
 */
public interface ClarabridgeChatApiClientCallback<T> {

    /**
     * Invoked when a network request is finished.
     *
     * @param isSuccessful true if the HTTP Status-Code is in the range [200..300), false otherwise
     * @param statusCode   the HTTP Status-Code of the response
     * @param responseBody the {@link Response#body()} if the request was successful
     */
    void onResult(boolean isSuccessful, int statusCode, @Nullable T responseBody);
}

