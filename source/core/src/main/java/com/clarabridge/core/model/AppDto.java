package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * A model representing a ClarabridgeChat App. An app is managed through the ClarabridgeChat Dashboard and is returned
 * as part of the {@link ConfigDto} of an integration.
 */
public class AppDto implements Serializable {

    @SerializedName("_id")
    private String id;
    @SerializedName("status")
    private String status;
    @SerializedName("name")
    private String name;
    @SerializedName("settings")
    private AppSettingsDto settings;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AppSettingsDto getSettings() {
        return settings;
    }

    public void setSettings(AppSettingsDto settings) {
        this.settings = settings;
    }
}

