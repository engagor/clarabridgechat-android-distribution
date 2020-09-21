package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ProfileSettingsDto implements Serializable {

    @SerializedName("enabled")
    private boolean enabled = false;
    @SerializedName("uploadInterval")
    private int uploadInterval = 60;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getUploadInterval() {
        return uploadInterval;
    }

    public void setUploadInterval(int uploadInterval) {
        this.uploadInterval = uploadInterval;
    }
}
