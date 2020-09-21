package com.clarabridge.core.model;

import java.io.Serializable;

/**
 * The settings of a ClarabridgeChat App. An app is managed through the ClarabridgeChat Dashboard and is returned
 * as part of the {@link ConfigDto} of an integration.
 */
public class AppSettingsDto implements Serializable {

    private final boolean multiConvoEnabled;

    public AppSettingsDto(boolean multiConvoEnabled) {
        this.multiConvoEnabled = multiConvoEnabled;
    }

    public boolean isMultiConvoEnabled() {
        return multiConvoEnabled;
    }
}
