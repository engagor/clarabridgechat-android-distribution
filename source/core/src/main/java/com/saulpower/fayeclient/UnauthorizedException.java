package com.saulpower.fayeclient;

import com.clarabridge.core.AuthenticationError;

class UnauthorizedException extends Exception {

    private final AuthenticationError authenticationError;

    UnauthorizedException(AuthenticationError authenticationError) {
        super(authenticationError.toString());
        this.authenticationError = authenticationError;
    }

    AuthenticationError getAuthenticationError() {
        return authenticationError;
    }
}
