package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class StripeCustomerDto implements Serializable {
    @SerializedName("card")
    private CardSummaryDto card;

    public CardSummaryDto getCard() {
        return card;
    }

    public void setCard(final CardSummaryDto card) {
        this.card = card;
    }
}
