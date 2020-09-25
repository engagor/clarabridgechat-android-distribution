package com.clarabridge.core;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.clarabridge.core.model.CoordinatesDto;
import com.clarabridge.core.model.MessageActionDto;
import com.clarabridge.core.model.MessageDto;
import com.clarabridge.core.model.MessageItemDto;
import com.clarabridge.core.utils.DateUtils;

/**
 * Object representing a received or sent message
 */
public final class Message implements Serializable, Comparable<Message> {
    private static String TAG = "Message";
    private MessageDto entity;
    private List<MessageAction> messageActions = new LinkedList<>();
    private List<MessageItem> messageItems = new LinkedList<>();
    private Bitmap image;
    private File file;

    Message(@NonNull MessageDto entity) {
        this.entity = entity;

        for (MessageActionDto it : entity.getMessageActions()) {
            messageActions.add(new MessageAction(it));
        }

        for (MessageItemDto it : entity.getMessageItems()) {
            messageItems.add(new MessageItem(it));
        }

        if (entity.getCreated() == null) {
            entity.setCreated(((double) System.currentTimeMillis()) / 1000.0);
        }

        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (entity.getUserId() != null && clarabridgeChatInternal != null) {
            entity.setIsFromCurrentUser(entity.getUserId().equals(clarabridgeChatInternal.getUserId()));
        }
    }

    Message() {
        this(new MessageDto());
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();

        if (clarabridgeChatInternal != null) {
            entity.setUserId(clarabridgeChatInternal.getUserId());
        }

        entity.setRole("appUser");
        entity.setStatus(MessageDto.Status.UNSENT);
        entity.setCreated(((double) System.currentTimeMillis()) / 1000.0);
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public Message copy() {
        return new Message(new MessageDto(entity));
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public boolean isCarousel() {
        return MessageType.CAROUSEL.getValue().equals(getType()) || MessageType.LIST.getValue().equals(getType());
    }

    /**
     * Create a message with the given text. The message will be owned by the current user.
     *
     * @param text The text
     */
    public Message(String text) {
        this();
        entity.setType("text");
        entity.setText(text);
    }

    /**
     * Create a message with the given text, payload, and metadata. The message will be owned by the
     * current user.
     *
     * @param text     The text
     * @param payload  The message payload
     * @param metadata The message metadata (JSON Object)
     */
    public Message(String text, String payload, Map<String, Object> metadata) {
        this(text);
        entity.setPayload(payload);
        entity.setMetadata(metadata);
    }

    /**
     * Create a message with the given coordinates.
     *
     * @param coordinates The coordinates
     * @param metadata    The message metadata (JSON Object)
     */
    public Message(Coordinates coordinates, Map<String, Object> metadata) {
        this();
        entity.setType(MessageType.LOCATION.getValue());
        entity.setCoordinates(coordinates.getEntity());
        entity.setMetadata(metadata);
    }

    /**
     * Create a message with the given bitmap.
     *
     * @param bitmap The bitmap
     */
    public Message(Bitmap bitmap) {
        this();
        entity.setType(MessageType.IMAGE.getValue());
        setImage(bitmap);
    }

    /**
     * Create a message with the given file.
     *
     * @param file The file
     */
    public Message(File file) {
        this();
        entity.setMediaSize(file.length());
        entity.setType(MessageType.FILE.getValue());
        setFile(file);
    }

    /**
     * The url for the user's avatar image.
     *
     * @return The url
     */
    public String getAvatarUrl() {
        if (!isFromCurrentUser()) {
            return entity.getAvatarUrl();
        }

        return null;
    }

    /**
     * The id for the user.
     *
     * @return The id
     */
    @Nullable
    public String getUserId() {
        if (!isFromCurrentUser()) {
            return entity.getUserId();
        }

        return null;
    }

    /**
     * The Role for the user.
     *
     * @return The Role
     */
    @Nullable
    public String getUserRole() {
        if (!isFromCurrentUser()) {
            return entity.getRole();
        }

        return null;
    }

    /**
     * Sets the url for the user's avatar image.
     *
     * @param avatarUrl the url for the user's avatar image
     */
    public void setAvatarUrl(String avatarUrl) {
        if (!isFromCurrentUser()) {
            entity.setAvatarUrl(avatarUrl);
        }
    }

    /**
     * The date and time the message was sent.
     *
     * @return The date
     */
    @Nullable
    public Date getDate() {
        Date dateReceived = DateUtils.timestampToDate(entity.getReceived());

        return dateReceived != null ? dateReceived : DateUtils.timestampToDate(entity.getCreated());
    }

    /**
     * Returns true if the message originated from the user, or false if the message comes from another
     * participant.
     *
     * @return Is the message from the current user
     */
    public boolean isFromCurrentUser() {
        return entity.isFromCurrentUser();
    }

    /**
     * The list of Message Action objects contained in the message.
     *
     * @return The message actions
     */
    public List<MessageAction> getMessageActions() {
        return Collections.unmodifiableList(messageActions);
    }

    /**
     * Adds a message action to the list of actions
     */
    public void addMessageAction(MessageAction messageAction) {
        List<MessageActionDto> currentActions = entity.getMessageActions();
        currentActions.add(messageAction.getEntity());
        messageActions.add(messageAction);
    }

    /**
     * Removes a message action from the list of actions
     */
    public void removeMessageAction(MessageAction messageAction) {
        List<MessageActionDto> currentActions = entity.getMessageActions();
        currentActions.remove(messageAction.getEntity());
        messageActions.remove(messageAction);
    }

    /**
     * The list of Message Item objects contained in the message.
     *
     * @return The message items
     */
    public List<MessageItem> getMessageItems() {
        return Collections.unmodifiableList(messageItems);
    }

    /**
     * Adds a message item to the list of items
     */
    public void addMessageItem(MessageItem messageItem) {
        List<MessageItemDto> currentItems = entity.getMessageItems();
        currentItems.add(messageItem.getEntity());
        messageItems.add(messageItem);
    }

    /**
     * Removes a message item from the list of items
     */
    public void removeMessageItem(MessageItem messageItem) {
        List<MessageItemDto> currentItems = entity.getMessageItems();
        currentItems.remove(messageItem.getEntity());
        messageItems.remove(messageItem);
    }

    /**
     * The display name of the user. This property may be null if no display name could be determined.
     *
     * @return The displayName
     */
    public String getName() {
        return entity.getDisplayName();
    }

    /**
     * Sets the user's displayName for the message.
     */
    public void setName(String displayName) {
        entity.setDisplayName(displayName);
    }

    /**
     * The text content of the message.
     *
     * @return The text
     */
    public String getText() {
        String trimmedMediaUrl = entity.getMediaUrl() == null ? "" : entity.getMediaUrl().trim();
        String trimmedText = entity.getText() == null ? "" : entity.getText().trim();
        if (!TextUtils.isEmpty(trimmedMediaUrl) && trimmedMediaUrl.equals(trimmedText)) {
            return "";
        }
        return trimmedText;
    }

    /**
     * Sets the text for the message.
     */
    public void setText(String text) {
        entity.setText(text);
    }

    /**
     * The upload status of the message.
     *
     * @return The status
     */
    @NonNull
    public MessageUploadStatus getUploadStatus() {
        switch (entity.getStatus()) {
            case SENDING_FAILED:
                return MessageUploadStatus.FAILED;
            case UNSENT:
                return MessageUploadStatus.UNSENT;
            case SENT:
            case UNREAD:
            case NOTIFICATION_SHOWN:
            case READ:
            default:
                return isFromCurrentUser()
                        ? MessageUploadStatus.SENT
                        : MessageUploadStatus.NOT_USER_MESSAGE;
        }
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public void setUploadStatus(MessageUploadStatus uploadStatus) {
        switch (uploadStatus) {
            case UNSENT:
                entity.setStatus(MessageDto.Status.UNSENT);
                break;
            case FAILED:
                entity.setStatus(MessageDto.Status.SENDING_FAILED);
                break;
            case SENT:
                entity.setStatus(MessageDto.Status.SENT);
                break;
            default:
                Log.w(TAG, "Invalid message status");
        }
    }

    /**
     * Return true if the message has been read
     *
     * @return Flag indicating that the message has been read
     */
    public boolean isRead() {
        MessageDto.Status status = entity.getStatus();

        return status != MessageDto.Status.UNREAD && status != MessageDto.Status.NOTIFICATION_SHOWN;
    }

    /**
     * The metadata of the message
     *
     * @return The metadata (JSON Object)
     */
    public Map<String, Object> getMetadata() {
        return entity.getMetadata();
    }

    /**
     * Sets the metadata for the message
     */
    public void setMetadata(Map<String, Object> metadata) {
        entity.setMetadata(metadata);
    }

    /**
     * The message payload from an action
     *
     * @return The message payload
     */
    public String getPayload() {
        return entity.getPayload();
    }

    /**
     * Sets the payload for the message
     */
    public void setPayload(String payload) {
        entity.setPayload(payload);
    }

    /**
     * The media URL of the message.
     *
     * @return The media URL
     */
    public String getMediaUrl() {
        return entity.getMediaUrl();
    }

    /**
     * Sets the media URL for the message
     */
    public void setMediaUrl(String mediaUrl) {
        entity.setMediaUrl(mediaUrl);
    }

    /**
     * The media type of the message.
     *
     * @return The media type
     */
    public String getMediaType() {
        return entity.getMediaType();
    }

    /**
     * Sets the media type for the message
     */
    public void setMediaType(String mediaType) {
        entity.setMediaType(mediaType);
    }

    /**
     * The media size of the message.
     *
     * @return The media size
     */
    public long getMediaSize() {
        return entity.getMediaSize();
    }

    /**
     * Sets the media size for the message
     */
    public void setMediaSize(long mediaSize) {
        entity.setMediaSize(mediaSize);
    }

    /**
     * The ID of the message.
     *
     * @return The ID
     */
    public String getId() {
        return entity.getId();
    }

    /**
     * Sets the image for the message.
     */
    public void setImage(Bitmap bitmap) {
        image = bitmap;
    }

    /**
     * Sets the file for the message.
     */
    public void setFile(File file) {
        this.file = file;
    }

    /**
     * The image of the message.
     *
     * @return The image
     */
    public Bitmap getImage() {
        return image;
    }

    /**
     * The file of the message.
     *
     * @return The file
     */
    public File getFile() {
        return file;
    }

    /**
     * Text to display for unsupported message types
     *
     * @return The text to display
     */
    public String getTextFallback() {
        return entity.getTextFallback();
    }

    /**
     * Sets the text to display for unsupported message types
     */
    public void setTextFallback(String textFallback) {
        entity.setTextFallback(textFallback);
    }

    @Override
    public boolean equals(Object o) {
        Message lhs = this;
        Message rhs;

        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        rhs = (Message) o;

        return lhs.entity == rhs.entity || lhs.entity.equals(rhs.entity);
    }

    @Override
    public int hashCode() {
        return entity != null ? entity.hashCode() : 0;
    }

    @androidx.annotation.RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public MessageDto getEntity() {
        return entity;
    }

    /**
     * The message type
     *
     * @return The message type
     * @see com.clarabridge.core.MessageType
     */
    public String getType() {
        if (entity.getType() == null) {
            if (image != null) {
                return MessageType.IMAGE.getValue();
            }

            if (file != null) {
                return MessageType.FILE.getValue();
            }

            if (entity.getMediaUrl() != null && !entity.getMediaUrl().isEmpty()) {
                if (entity.getMediaType() != null && entity.getMediaType().startsWith("image")) {
                    return MessageType.IMAGE.getValue();
                } else {
                    return MessageType.FILE.getValue();
                }
            }

            return MessageType.TEXT.getValue();
        }

        return entity.getType();
    }

    /**
     * Sets the type of the message
     */
    public void setType(String type) {
        entity.setType(type);
    }

    /**
     * Returns <code>true</code> if the message has reply actions
     *
     * @return <code>true</code> if the message has reply actions, <code>false</code> otherwise
     */
    public boolean hasReplies() {
        if (!messageActions.isEmpty()) {
            for (MessageAction action : messageActions) {
                if (action.getType().equals("reply")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns <code>true</code> if the message has a location request
     *
     * @return <code>true</code> if the message has a location request, <code>false</code> otherwise
     */
    public boolean hasLocationRequest() {
        if (!messageActions.isEmpty()) {
            for (MessageAction action : messageActions) {
                if (action.getType().equals("locationRequest")) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Returns Coordinates
     *
     * @return Coordinates
     */
    public Coordinates getCoordinates() {
        return new Coordinates(entity.getCoordinates());
    }

    /**
     * Sets the coordinates for the message
     */
    public void setCoordinates(Coordinates coordinates) {
        entity.setCoordinates(coordinates.getEntity());
    }

    /**
     * Returns DisplaySettings for carousel messages
     *
     * @return DisplaySettings
     */
    public DisplaySettings getDisplaySettings() {
        return new DisplaySettings(entity.getDisplaySettings());
    }

    /**
     * Sets the display settings for the message
     */
    public void setDisplaySettings(DisplaySettings displaySettings) {
        entity.setDisplaySettings(displaySettings.getEntity());
    }

    /**
     * Returns <code>true</code> if the message has valid coordinates
     *
     * @return <code>true</code> if the message has valid coordinates, <code>false</code> otherwise
     */
    public boolean hasValidCoordinates() {
        CoordinatesDto coordinates = entity.getCoordinates();
        return coordinates.getLat() != null && coordinates.getLong() != null;
    }

    @Override
    public int compareTo(@NonNull Message o) {
        if (entity == null && o.entity == null) {
            return 0;
        }

        if (entity == null) {
            return -1;
        }

        if (o.entity == null) {
            return 1;
        }

        return entity.compareTo(o.entity);
    }
}

