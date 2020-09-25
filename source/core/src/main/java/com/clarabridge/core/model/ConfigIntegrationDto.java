package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

public class ConfigIntegrationDto {

    @SerializedName("canUserCreateMoreConversations")
    private boolean canUserCreateMoreConversations;

    public boolean isCanUserCreateMoreConversations() {
        return canUserCreateMoreConversations;
    }

    public void setCanUserCreateMoreConversations(boolean canUserCreateMoreConversations) {
        this.canUserCreateMoreConversations = canUserCreateMoreConversations;
    }
}
