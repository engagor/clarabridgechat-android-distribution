package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import com.clarabridge.core.utils.JavaUtils;

/**
 * "conversation": {
 *         "displayName": "Conversation With Gordon",
 *         "iconUrl": "https://avatars.com/freeman.jpg",
 *         "description": "How I Freed Black Mesa Scientists In 10 Easy Steps",
 *         "type": "personal",
 *         "messages": [{
 *            "type": "text”,
 *            "text": "yo"
 *         }]
 */
public class PostAppUserConversationDto implements Serializable {

    @SerializedName("displayName")
    private final String displayName;

    @SerializedName("iconUrl")
    private final String iconUrl;

    @SerializedName("description")
    private final String description;

    @SerializedName("type")
    private final String type;

    @SerializedName("messages")
    private final List<PostConversationMessageDto> messages;

    public PostAppUserConversationDto(String displayName, String iconUrl, String description,
                                      String type, List<PostConversationMessageDto> messages) {
        this.displayName = displayName;
        this.iconUrl = iconUrl;
        this.description = description;
        this.type = type;
        this.messages = messages;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public List<PostConversationMessageDto> getMessages() {
        return messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PostAppUserConversationDto that = (PostAppUserConversationDto) o;
        return JavaUtils.equals(displayName, that.displayName)
                && JavaUtils.equals(iconUrl, that.iconUrl)
                && JavaUtils.equals(description, that.description)
                && JavaUtils.equals(type, that.type)
                && JavaUtils.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return JavaUtils.hash(displayName, iconUrl, description, type, messages);
    }
}
