package com.clarabridge.core;

import androidx.annotation.NonNull;

import java.io.Serializable;

import com.clarabridge.core.model.CardSummaryDto;

public class CardSummary implements Serializable {

    private final CardSummaryDto entity;

    CardSummary(@NonNull final CardSummaryDto entity) {
        this.entity = entity;
    }

    /**
     * The last 4 digits of the card
     *
     * @return The last 4 digits
     */
    public String getLast4() {
        return entity.getLast4();
    }

    /**
     * The brand of the card
     *
     * @return The brand
     */
    public String getBrand() {
        return entity.getBrand();
    }

    /**
     * The backing entity of this {@link CardSummary}
     *
     * @return the backing {@link CardSummaryDto} entity
     */
    CardSummaryDto getEntity() {
        return entity;
    }
}
