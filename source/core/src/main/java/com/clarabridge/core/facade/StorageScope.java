package com.clarabridge.core.facade;

import android.support.annotation.NonNull;

/**
 * Used for determining which IDs should be used to generate file names for storage
 */
public enum StorageScope {
    UNSCOPED("general"),
    APP_ID("app_id"),
    USER_ID("user_id"),
    ;

    private final String directoryName;

    StorageScope(@NonNull String directoryName) {
        this.directoryName = directoryName;
    }

    /**
     * @return the directory name to be used by this scoped storage
     */
    @NonNull
    public String getDirectoryName() {
        return directoryName;
    }
}
