package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MessageItemDto implements Serializable {
    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("mediaUrl")
    private String mediaUrl;

    @SerializedName("mediaType")
    private String mediaType;

    @SerializedName("size")
    private String size;

    @SerializedName("actions")
    private List<MessageActionDto> messageActions;

    @SerializedName("metadata")
    private Map<String, Object> metadata;

    public MessageItemDto() {
    }

    public MessageItemDto(MessageItemDto messageItem) {
        title = messageItem.title;
        description = messageItem.description;
        mediaUrl = messageItem.mediaUrl;
        mediaType = messageItem.mediaType;
        size = messageItem.size;

        messageActions = new ArrayList<>();

        if (messageItem.messageActions != null) {
            messageActions.addAll(messageItem.messageActions);
        }

        if (messageItem.metadata != null) {
            metadata = new HashMap<>(messageItem.metadata);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public List<MessageActionDto> getMessageActions() {
        if (messageActions == null) {
            messageActions = new LinkedList<>();
        }

        return messageActions;
    }

    public void setMessageActions(List<MessageActionDto> messageActions) {
        this.messageActions = messageActions;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
