package com.clarabridge.core.service;

/**
 * User connection status.
 */
enum ConnectionStatus {

    /**
     * User has internet connectivity.
     */
    CONNECTED,

    /**
     * User is disconnected from the internet.
     */
    DISCONNECTED,

    /**
     * Internet status unknown.
     */
    UNKNOWN,

    ;
}
