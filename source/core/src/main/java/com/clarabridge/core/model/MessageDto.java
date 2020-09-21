package com.clarabridge.core.model;

import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clarabridge.core.annotation.LocalField;

public class MessageDto implements Serializable, Comparable<MessageDto> {
    public enum Status {
        UNSENT,
        SENDING_FAILED,
        SENT,
        UNREAD,
        NOTIFICATION_SHOWN,
        READ,
        ;
    }

    @SerializedName("_id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("role")
    private String role;
    @SerializedName("text")
    private String text;
    @SerializedName("textFallback")
    private String textFallback;
    @SerializedName("type")
    private String type;
    @SerializedName("ruleId")
    private String ruleId;
    @SerializedName("payload")
    private String payload;
    @SerializedName("metadata")
    private Map<String, Object> metadata;
    @SerializedName("received")
    private Double received;
    @SerializedName("authorId")
    private String authorId;
    @SerializedName("mediaUrl")
    private String mediaUrl;
    @SerializedName("mediaType")
    private String mediaType;
    @SerializedName("mediaSize")
    private long mediaSize;
    @SerializedName("avatarUrl")
    private String avatarUrl;
    @SerializedName("coordinates")
    private CoordinatesDto coordinates;
    @SerializedName("actions")
    private List<MessageActionDto> messageActions;
    @SerializedName("items")
    private List<MessageItemDto> messageItems;
    @SerializedName("displaySettings")
    private DisplaySettingsDto displaySettings;
    @SerializedName("source")
    private SourceDto source;

    @LocalField
    @SerializedName("status")
    private Status status;
    @LocalField
    @SerializedName("created")
    private Double created;
    @LocalField
    private boolean isFromCurrentUser;

    public MessageDto() {

    }

    public MessageDto(final MessageDto message) {
        id = message.id;
        name = message.name;
        role = message.role;
        text = message.text;
        textFallback = message.textFallback;
        type = message.type;
        source = message.source;
        ruleId = message.ruleId;
        payload = message.payload;

        if (message.metadata != null) {
            metadata = new HashMap<>(message.metadata);
        }

        received = message.received;
        authorId = message.authorId;
        mediaUrl = message.mediaUrl;
        mediaType = message.mediaType;
        mediaSize = message.mediaSize;
        avatarUrl = message.avatarUrl;

        if (message.coordinates != null) {
            coordinates = new CoordinatesDto(message.coordinates.getLat(), message.coordinates.getLong());
        }

        if (message.displaySettings != null) {
            displaySettings = new DisplaySettingsDto(message.displaySettings.getImageAspectRatio());
        }

        messageActions = new ArrayList<>();

        if (message.messageActions != null) {
            messageActions.addAll(message.messageActions);
        }

        messageItems = new ArrayList<>();

        if (message.messageItems != null) {
            messageItems.addAll(message.messageItems);
        }

        status = message.status;
        created = message.created;
        isFromCurrentUser = message.isFromCurrentUser;
    }

    public void update(final MessageDto rhs) {
        id = rhs.id;
        name = rhs.name;
        role = rhs.role;
        text = rhs.text;
        textFallback = rhs.textFallback;
        type = rhs.type;
        ruleId = rhs.ruleId;
        payload = rhs.payload;
        metadata = rhs.metadata;
        received = rhs.received;
        authorId = rhs.authorId;
        mediaUrl = rhs.mediaUrl;
        mediaType = rhs.mediaType;
        mediaSize = rhs.mediaSize;
        avatarUrl = rhs.avatarUrl;
        coordinates = rhs.coordinates;
        messageActions = rhs.messageActions;
        source = rhs.source;
        isFromCurrentUser = rhs.isFromCurrentUser;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    @NonNull
    public List<MessageActionDto> getMessageActions() {
        if (messageActions == null) {
            messageActions = new ArrayList<>();
        }

        return messageActions;
    }

    public void setMessageActions(List<MessageActionDto> actions) {
        this.messageActions = actions;
    }

    @NonNull
    public List<MessageItemDto> getMessageItems() {
        if (messageItems == null) {
            messageItems = new ArrayList<>();
        }

        return messageItems;
    }

    public void setMessageItems(List<MessageItemDto> items) {
        this.messageItems = items;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(final String authorId) {
        this.authorId = authorId;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(final String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(final String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(final String mediaType) {
        this.mediaType = mediaType;
    }

    public long getMediaSize() {
        return mediaSize;
    }

    public void setMediaSize(final long mediaSize) {
        this.mediaSize = mediaSize;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(final String text) {
        this.text = text;
    }

    public String getTextFallback() {
        return textFallback;
    }

    public void setTextFallback(String textFallback) {
        this.textFallback = textFallback;
    }

    public Double getReceived() {
        return received;
    }

    public void setReceived(final Double received) {
        this.received = received;
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(final String ruleId) {
        this.ruleId = ruleId;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @NonNull
    public Status getStatus() {
        if (status == null) {
            status = Status.UNREAD;
        }

        return status;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @NonNull
    public CoordinatesDto getCoordinates() {
        if (coordinates == null) {
            coordinates = new CoordinatesDto();
        }

        return coordinates;
    }

    public void setCoordinates(CoordinatesDto coordinates) {
        this.coordinates = coordinates;
    }

    public Double getCreated() {
        return created;
    }

    public void setCreated(Double created) {
        this.created = created;
    }

    @NonNull
    public DisplaySettingsDto getDisplaySettings() {
        if (displaySettings == null) {
            displaySettings = new DisplaySettingsDto();
        }

        return displaySettings;
    }

    public void setDisplaySettings(DisplaySettingsDto displaySettings) {
        this.displaySettings = displaySettings;
    }

    public SourceDto getSource() {
        return source;
    }

    public void setSource(SourceDto source) {
        this.source = source;
    }

    public boolean isFromCurrentUser() {
        return isFromCurrentUser;
    }

    public void setIsFromCurrentUser(boolean fromCurrentUser) {
        isFromCurrentUser = fromCurrentUser;
    }

    /***
     * This is a modified version of the generated equals to satisfy ClarabridgeChat business logic. Any two
     * {@link MessageDto} are considered equal if either their {@link #id} or {@link #created} are
     * not null and equal.
     *
     * @param o the {@link Object} to compare
     * @return true if this is equal to the provided object, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MessageDto that = (MessageDto) o;

        if (id != null && that.id != null && !id.equals(that.id)) {
            return false;
        }

        if (created != null && that.created != null && created.equals(that.created)) {
            return true;
        }

        return id != null && that.id != null;
    }

    /**
     * The rules required for {@link #equals(Object)} make it impossible to honor the contract between
     * it and hashCode, so take special care if you have to rely on this method.
     *
     * @return the hash code of this instance, calculated based on {@link #id} and {@link #created}
     */
    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (created != null ? created.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(@NonNull MessageDto messageDto) {
        if (messageDto.getReceived() == null && received == null) {
            return 0;
        }

        if (messageDto.getReceived() == null) {
            return 1;
        }

        if (received == null) {
            return -1;
        }

        return received.compareTo(messageDto.getReceived());
    }
}
