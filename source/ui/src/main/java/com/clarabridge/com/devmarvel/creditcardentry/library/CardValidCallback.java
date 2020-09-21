package com.clarabridge.com.devmarvel.creditcardentry.library;

public interface CardValidCallback {
    /**
     * called when data entry is complete and the card is valid
     *
     * @param card the validated card
     */
    void cardValid(CreditCard card);

    /**
     * called when data entry changes and card is invalid
     *
     * @param card the invalid card
     */
    void cardInvalid(CreditCard card);
}
