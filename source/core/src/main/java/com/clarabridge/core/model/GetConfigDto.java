package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GetConfigDto implements Serializable {

    @SerializedName("config")
    private ConfigDto config;

    public ConfigDto getConfig() {
        return config;
    }

    public void setConfig(ConfigDto config) {
        this.config = config;
    }
}
