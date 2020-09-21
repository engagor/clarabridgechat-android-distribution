package com.clarabridge.core;

import java.io.Serializable;

import com.clarabridge.core.model.DisplaySettingsDto;

public class DisplaySettings implements Serializable {
    private DisplaySettingsDto entity;

    DisplaySettings(final DisplaySettingsDto entity) {
        this.entity = entity;
    }

    /**
     * Create display settings with the given image aspect ratio.
     *
     * @param imageAspectRatio The image aspect ratio
     */
    public DisplaySettings(String imageAspectRatio) {
        this.entity = new DisplaySettingsDto();
        this.entity.setImageAspectRatio(imageAspectRatio);
    }

    /**
     * The image aspect ratio
     *
     * @return Image aspect ratio
     */
    public String getImageAspectRatio() {
        return entity.getImageAspectRatio();
    }

    DisplaySettingsDto getEntity() {
        return this.entity;
    }
}
