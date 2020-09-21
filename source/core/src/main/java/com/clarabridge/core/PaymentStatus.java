package com.clarabridge.core;

/**
 * Stripe payment status.
 */
public enum PaymentStatus {

    /**
     * Payment status unknown.
     */
    UNKNOWN,

    /**
     * Payment processed successfully.
     */
    SUCCESS,

    /**
     * Payment failed for some reason.
     */
    ERROR,

    ;
}
