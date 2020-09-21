package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UpgradeAppUserDto extends AppUserDto implements Serializable {

    @SerializedName("sessionToken")
    private String sessionToken;

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
