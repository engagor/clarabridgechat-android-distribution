package com.clarabridge.core;

/**
 * Initialization response status.
 */
public enum InitializationStatus {

    /**
     * Init completed successfully.
     */
    SUCCESS,

    /**
     * Init failed for some reason.
     */
    ERROR,

    /**
     * Invalid app id.
     */
    INVALID_ID,

    /**
     * Init hasn't yet responded.
     */
    UNKNOWN,

    ;
}
