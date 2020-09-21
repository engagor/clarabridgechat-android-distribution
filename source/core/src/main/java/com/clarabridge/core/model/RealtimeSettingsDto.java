package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RealtimeSettingsDto implements Serializable {

    @SerializedName("enabled")
    private boolean enabled = false;
    @SerializedName("baseUrl")
    private String baseUrl;
    @SerializedName("retryInterval")
    private int retryInterval = 0;
    @SerializedName("maxConnectionAttempts")
    private int maxConnectionAttempts = 0;
    @SerializedName("connectionDelay")
    private int connectionDelay = 0;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public int getMaxConnectionAttempts() {
        return maxConnectionAttempts;
    }

    public void setMaxConnectionAttempts(int maxConnectionAttempts) {
        this.maxConnectionAttempts = maxConnectionAttempts;
    }

    public int getConnectionDelay() {
        return connectionDelay;
    }

    public void setConnectionDelay(int connectionDelay) {
        this.connectionDelay = connectionDelay;
    }
}
