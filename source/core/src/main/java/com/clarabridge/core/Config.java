package com.clarabridge.core;

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

    ConfigDto getEntity() {
        return entity;
    }
}
