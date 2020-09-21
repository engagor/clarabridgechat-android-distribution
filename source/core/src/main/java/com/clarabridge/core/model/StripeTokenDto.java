package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class StripeTokenDto implements Serializable {
    @SerializedName("id")
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }
}
