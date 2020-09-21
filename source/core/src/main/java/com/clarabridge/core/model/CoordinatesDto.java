package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CoordinatesDto implements Serializable {
    @SerializedName("lat")
    private Double latitude;
    @SerializedName("long")
    private Double longitude;

    public CoordinatesDto() {
    }

    public CoordinatesDto(Double latitude, Double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Double getLat() {
        return latitude;
    }

    public Double getLong() {
        return longitude;
    }

    public void setLat(Double latitude) {
        this.latitude = latitude;
    }

    public void setLong(Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public boolean equals(Object o) {
        if (o.getClass() != getClass()) {
            return false;
        }

        CoordinatesDto other = (CoordinatesDto) o;

        return latitude != null
                && latitude.equals(other.latitude)
                && longitude != null
                && longitude.equals(other.longitude);
    }
}
