package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.clarabridge.core.utils.JavaUtils;

/**
 * A model representing the user intent when creating a conversation. An example is the
 * "conversation:start" intent sent when creating a conversation in the backend.
 */

public class PostCreateConversationDto implements Serializable {

    @SerializedName("displayName")
    private final String displayName;

    @SerializedName("description")
    private final String description;

    @SerializedName("iconUrl")
    private final String iconUrl;

    @SerializedName("type")
    private final String type;

    @SerializedName("metadata")
    private final Map<String, Object> metadata;

    @SerializedName("client")
    private ClientDto client;

    @SerializedName("messages")
    private final List<PostConversationMessageDto> messages;

    public PostCreateConversationDto(String displayName, String description, String iconUrl,
                                     String type, List<PostConversationMessageDto> messages,
                                     Map<String, Object> metadata) {
        this.displayName = displayName;
        this.description = description;
        this.iconUrl = iconUrl;
        this.type = type;
        this.metadata = metadata;
        this.messages = messages;
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

    public String getType() {
        return type;
    }

    public List<PostConversationMessageDto> getMessages() {
        return messages;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public ClientDto getClient() {
        return client;
    }

    public void setClient(ClientDto client) {
        this.client = client;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PostCreateConversationDto that = (PostCreateConversationDto) o;
        return JavaUtils.equals(displayName, that.displayName)
                && JavaUtils.equals(description, that.description)
                && JavaUtils.equals(iconUrl, that.iconUrl)
                && JavaUtils.equals(type, that.type)
                && JavaUtils.equals(messages, that.messages)
                && JavaUtils.equals(metadata, that.metadata)
                && JavaUtils.equals(client, that.client);
    }

    @Override
    public int hashCode() {
        return JavaUtils.hash(displayName, description, iconUrl, type, messages, metadata, client);
    }
}
