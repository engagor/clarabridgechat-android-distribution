package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class DisplaySettingsDto implements Serializable {
    @SerializedName("imageAspectRatio")
    private String imageAspectRatio;

    public DisplaySettingsDto() {
        imageAspectRatio = "horizontal";
    }

    public DisplaySettingsDto(String imageAspectRatio) {
        this.imageAspectRatio = imageAspectRatio;
    }

    public String getImageAspectRatio() {
        return imageAspectRatio;
    }

    public void setImageAspectRatio(String imageAspectRatio) {
        this.imageAspectRatio = imageAspectRatio;
    }
}
