package com.clarabridge.core;

import java.io.Serializable;

/**
 * Object representing a user's credit card
 */
public final class CreditCard implements Serializable {
    private static final String TAG = "CreditCard";
    private String cardNumber;
    private int expYear;
    private int expMonth;
    private String securityCode;

    /**
     * Create a credit card with the given details.
     *
     * @param cardNumber   The card number
     * @param expYear      The expiration year
     * @param expMonth     The expiration month
     * @param securityCode The security code
     */
    public CreditCard(final String cardNumber, final int expYear, final int expMonth, final String securityCode) {
        this.cardNumber = cardNumber;
        this.expYear = expYear;
        this.expMonth = expMonth;
        this.securityCode = securityCode;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public int getExpYear() {
        return expYear;
    }

    public int getExpMonth() {
        return expMonth;
    }

    public String getSecurityCode() {
        return securityCode;
    }

    public void setCardNumber(final String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public void setExpYear(final int expYear) {
        this.expYear = expYear;
    }

    public void setExpMonth(final int expMonth) {
        this.expMonth = expMonth;
    }

    public void setSecurityCode(final String securityCode) {
        this.securityCode = securityCode;
    }
}

