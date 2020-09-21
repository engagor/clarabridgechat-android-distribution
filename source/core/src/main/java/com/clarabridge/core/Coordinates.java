package com.clarabridge.core;

import java.io.Serializable;

import com.clarabridge.core.model.CoordinatesDto;

public class Coordinates implements Serializable {
    private CoordinatesDto entity;

    Coordinates(final CoordinatesDto entity) {
        this.entity = entity;
    }

    /**
     * Create coordinates with the given latitude/longitude.
     *
     * @param latitude The latitude
     * @param longitude The longitude
     */
    public Coordinates(Double latitude, Double longitude) {
        entity = new CoordinatesDto();
        entity.setLat(latitude);
        entity.setLong(longitude);
    }

    /**
     * The latitude
     *
     * @return Latitude
     */
    public Double getLat() {
        return entity.getLat();
    }

    /**
     * The longitude
     *
     * @return Longitude
     */
    public Double getLong() {
        return entity.getLong();
    }

    CoordinatesDto getEntity() {
        return this.entity;
    }
}
