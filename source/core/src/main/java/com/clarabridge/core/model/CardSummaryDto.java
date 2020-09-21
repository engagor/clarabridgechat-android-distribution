package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CardSummaryDto implements Serializable {
    @SerializedName("last4")
    private String last4;
    @SerializedName("brand")
    private String brand;

    public String getLast4() {
        return last4;
    }

    public void setLast4(final String last4) {
        this.last4 = last4;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(final String brand) {
        this.brand = brand;
    }
}
