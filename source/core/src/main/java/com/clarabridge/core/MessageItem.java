package com.clarabridge.core;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.clarabridge.core.model.MessageActionDto;
import com.clarabridge.core.model.MessageItemDto;

public class MessageItem implements Serializable {
    private MessageItemDto entity;
    private List<MessageAction> messageActions = new LinkedList<>();

    MessageItem(MessageItemDto entity) {
        this.entity = entity;

        for (MessageActionDto it : entity.getMessageActions()) {
            messageActions.add(new MessageAction(it));
        }
    }

    /**
     * Creates an empty message item.
     */
    public MessageItem() {
        this(new MessageItemDto());
    }

    /**
     * The description of the item
     *
     * @return The description
     */
    public String getDescription() {
        return entity.getDescription();
    }


    /**
     * Sets the description of the item
     */
    public void setDescription(String description) {
        entity.setDescription(description);
    }

    /**
     * The media type of the item
     *
     * @return The media type
     */
    public String getMediaType() {
        return entity.getMediaType();
    }

    /**
     * Sets the media type of the item
     */
    public void setMediaType(String mediaType) {
        entity.setMediaType(mediaType);
    }

    /**
     * The media url of the item
     *
     * @return The media url
     */
    public String getMediaUrl() {
        return entity.getMediaUrl();
    }

    /**
     * Sets the media url of the item
     */
    public void setMediaUrl(String mediaUrl) {
        entity.setMediaUrl(mediaUrl);
    }

    /**
     * The size of the item
     *
     * @return The size
     */
    public String getSize() {
        return entity.getSize();
    }

    /**
     * Sets the size of the item
     */
    public void setSize(String size) {
        entity.setSize(size);
    }

    /**
     * The title of the item
     *
     * @return The title
     */
    public String getTitle() {
        return entity.getTitle();
    }

    /**
     * Sets the title of the item
     */
    public void setTitle(String title) {
        entity.setTitle(title);
    }

    /**
     * The list of Message Action objects contained in the message item.
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
     * The metadata of the item
     *
     * @return The metadata
     */
    public Map<String, Object> getMetadata() {
        return entity.getMetadata();
    }

    /**
     * Sets the metadata of the item
     */
    public void setMetadata(Map<String, Object> metadata) {
        entity.setMetadata(metadata);
    }

    MessageItemDto getEntity() {
        return entity;
    }

    @Override
    public boolean equals(Object obj) {
        MessageItem lhs = this;
        MessageItem rhs;

        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        rhs = (MessageItem) obj;
        return lhs.entity == rhs.entity || lhs.entity.equals(rhs.entity);
    }
}
