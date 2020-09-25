package com.clarabridge.core;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.clarabridge.core.model.ConfigDto;
import com.clarabridge.core.model.IntegrationDto;

public class Config implements Serializable {

    private ConfigDto entity;
    private final List<Integration> integrations = new ArrayList<>();

    Config(final ConfigDto entity) {
        this.entity = entity;

        for (final IntegrationDto it : entity.getIntegrations()) {
            integrations.add(new Integration(it));
        }
    }

    /**
     * Flag determining whether stripe is enabled for the app
     *
     * @return True if stripe is enabled, false otherwise
     */
    public boolean isStripeEnabled() {
        for (Integration integration : integrations) {
            if (integration.getType().equals("stripeConnect")) {
                return true;
            }
        }

        return false;
    }

    /**
     * @return the icon url
     */
    @Nullable
    public String getIconUrl() {
        if (entity != null) {
            if (entity.getApp() != null) {
                return entity.getApp().getIconUrl();
            }
            return null;
        }
        return null;
    }

    /**
     * String representing the name of the app
     *
     * @return the app name
     */
    public String getAppName() {
        if (entity != null) {
            if (entity.getApp() != null) {
                return entity.getApp().getName();
            }
            return null;
        }
        return null;
    }

    /**
     * @return the `canUserCreateMoreConversations` that is setting from the sdk server side.
     */
    public boolean canUserCreateMoreConversations() {
        return entity.getConfigIntegrationDto().isCanUserCreateMoreConversations();
    }

    ConfigDto getEntity() {
        return entity;
    }
}
