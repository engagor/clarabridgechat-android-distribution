package com.clarabridge.core.model;

import java.util.Map;

public class PostMetadataDto {

    private final Map<String, Object> metadata;

    public PostMetadataDto(Map<String, Object> metadata) {
        this.metadata = metadata;
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

        PostMetadataDto that = (PostMetadataDto) o;

        return metadata != null ? metadata.equals(that.metadata) : that.metadata == null;
    }

    @Override
    public int hashCode() {
        return metadata != null ? metadata.hashCode() : 0;
    }
}
