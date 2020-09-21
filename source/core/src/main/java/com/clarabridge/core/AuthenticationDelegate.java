package com.clarabridge.core;

public interface AuthenticationDelegate {

    /**
     * Notifies the delegate of a failed request due to invalid credentials
     *
     * @param error    detail about the authentication error. See {@link AuthenticationError}
     * @param callback callback to invoke with a new token. See {@link AuthenticationCallback}
     */
    void onInvalidAuth(AuthenticationError error, AuthenticationCallback callback);
}
