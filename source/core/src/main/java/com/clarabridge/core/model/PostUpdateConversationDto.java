package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

import com.clarabridge.core.utils.JavaUtils;

/**
 * A model representing the user intent when creating a conversation. An example is the
 * "conversation:start" intent sent when creating a conversation in the backend.
 */

public class PostUpdateConversationDto implements Serializable {

    @SerializedName("displayName")
    private final String displayName;

    @SerializedName("description")
    private final String description;

    @SerializedName("iconUrl")
    private final String iconUrl;

    @SerializedName("metadata")
    private final Map<String, Object> metadata;

    @SerializedName("client")
    private final ClientDto client;

    public PostUpdateConversationDto(String displayName, String description,
                                     String iconUrl, Map<String, Object> metadata, ClientDto client) {
        this.displayName = displayName;
        this.description = description;
        this.iconUrl = iconUrl;
        this.metadata = metadata;
        this.client = client;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public ClientDto getClient() {
        return client;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PostUpdateConversationDto that = (PostUpdateConversationDto) o;
        return JavaUtils.equals(displayName, that.displayName)
                && JavaUtils.equals(description, that.description)
                && JavaUtils.equals(iconUrl, that.iconUrl)
                && JavaUtils.equals(metadata, that.metadata)
                && JavaUtils.equals(client, that.client);
    }

    @Override
    public int hashCode() {
        return JavaUtils.hash(displayName, description, iconUrl, metadata, client);
    }
}
