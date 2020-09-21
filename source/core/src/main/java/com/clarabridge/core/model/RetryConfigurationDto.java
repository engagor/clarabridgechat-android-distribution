package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class RetryConfigurationDto implements Serializable {

    @SerializedName("intervals")
    private RetryIntervalDto interval = new RetryIntervalDto();

    @SerializedName("backoffMultiplier")
    private int backoffMultiplier = 2;

    @SerializedName("maxRetries")
    private int maxRetries = 5;

    public RetryIntervalDto getInterval() {
        return interval;
    }

    public void setInterval(RetryIntervalDto interval) {
        this.interval = interval;
    }

    public int getBackoffMultiplier() {
        return backoffMultiplier;
    }

    public void setBackoffMultiplier(int backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getAggressiveInterval() {
        return interval.getAggressive();
    }

    public int getRegularInterval() {
        return interval.getRegular();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RetryConfigurationDto that = (RetryConfigurationDto) o;

        if (backoffMultiplier != that.backoffMultiplier) {
            return false;
        }
        if (maxRetries != that.maxRetries) {
            return false;
        }
        return interval != null ? interval.equals(that.interval) : that.interval == null;
    }

    @Override
    public int hashCode() {
        int result = interval != null ? interval.hashCode() : 0;
        result = 31 * result + backoffMultiplier;
        result = 31 * result + maxRetries;
        return result;
    }
}
