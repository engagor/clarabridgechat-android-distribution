package com.clarabridge.core;

import androidx.annotation.NonNull;

public interface AuthenticationCallback {

    /**
     * Updates the JWT used to authenticate the user
     *
     * @param jwt the new token
     */
    void updateToken(@NonNull String jwt);

}
