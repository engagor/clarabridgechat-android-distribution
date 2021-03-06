package com.clarabridge.core;

import androidx.annotation.NonNull;

public class AuthenticationError {

    private final int status;
    private final String data;

    public AuthenticationError(final int status, final String data) {
        this.status = status;
        this.data = data;
    }

    /**
     * @return the HTTP Status-Code of the response
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return the string representation of the response body
     */
    public String getData() {
        return data;
    }

    @NonNull
    @Override
    public String toString() {
        return "AuthenticationError{" +
                "status=" + status +
                ", data='" + data + '\'' +
                '}';
    }
}
