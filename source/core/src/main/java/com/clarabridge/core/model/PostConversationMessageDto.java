package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

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
public class PostConversationMessageDto implements Serializable {

    @SerializedName("type")
    private final String type;

    @SerializedName("text")
    private final String text;

    public PostConversationMessageDto(String type, String text) {
        this.type = type;
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public String getText() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PostConversationMessageDto that = (PostConversationMessageDto) o;
        return JavaUtils.equals(type, that.type)
                && JavaUtils.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return JavaUtils.hash(type, text);
    }
}
