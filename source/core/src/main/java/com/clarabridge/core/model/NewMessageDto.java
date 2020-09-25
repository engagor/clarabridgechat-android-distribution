package com.clarabridge.core.model;

import androidx.annotation.Nullable;

import java.util.Map;

/**
 * Model used when the user is sending a new message to the backend through the API.
 */
public class NewMessageDto {

    private final String text;
    private final String role;
    private final String type;
    @Nullable
    private final CoordinatesDto coordinates;
    private final String payload;
    private final Map<String, Object> metadata;

    public NewMessageDto(
            String text,
            String role,
            String type,
            @Nullable CoordinatesDto coordinates,
            String payload,
            Map<String, Object> metadata) {

        this.text = text;
        this.role = role;
        this.type = type;
        this.coordinates = coordinates;
        this.payload = payload;
        this.metadata = metadata;
    }

    public String getText() {
        return text;
    }

    public String getRole() {
        return role;
    }

    public String getType() {
        return type;
    }

    @Nullable
    public CoordinatesDto getCoordinates() {
        return coordinates;
    }

    public String getPayload() {
        return payload;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NewMessageDto that = (NewMessageDto) o;

        if (text != null ? !text.equals(that.text) : that.text != null) {
            return false;
        }
        if (role != null ? !role.equals(that.role) : that.role != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (coordinates != null ? !coordinates.equals(that.coordinates) : that.coordinates != null) {
            return false;
        }
        if (payload != null ? !payload.equals(that.payload) : that.payload != null) {
            return false;
        }
        return metadata != null ? metadata.equals(that.metadata) : that.metadata == null;
    }

    @Override
    public int hashCode() {
        int result = text != null ? text.hashCode() : 0;
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (coordinates != null ? coordinates.hashCode() : 0);
        result = 31 * result + (payload != null ? payload.hashCode() : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        return result;
    }
}
