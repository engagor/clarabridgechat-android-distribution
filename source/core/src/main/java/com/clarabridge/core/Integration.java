package com.clarabridge.core;

import java.io.Serializable;

import com.clarabridge.core.model.IntegrationDto;

public class Integration implements Serializable {
    private IntegrationDto entity;

    Integration(final IntegrationDto entity) {
        this.entity = entity;
    }

    /**
     * The integration type
     *
     * @return Integration type
     */
    public String getType() {
        return this.entity.getType();
    }

    IntegrationDto getEntity() {
        return this.entity;
    }
}
