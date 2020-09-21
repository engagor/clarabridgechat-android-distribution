package com.clarabridge.core;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import com.clarabridge.core.model.MessageActionDto;

public class MessageAction implements Serializable {

    private MessageActionDto entity;

    MessageAction(@NonNull final MessageActionDto entity) {
        this.entity = entity;
    }

    /**
     * Creates an empty message action.
     */
    public MessageAction() {
        this(new MessageActionDto());
    }

    /**
     * The text of the action
     *
     * @return The text
     */
    public String getText() {
        return entity.getText();
    }

    /**
     * Sets the text of the action
     */
    public void setText(String text) {
        entity.setText(text);
    }

    /**
     * The icon URL of the action
     *
     * @return The icon URL
     */
    public String getIconUrl() {
        return entity.getIconUrl();
    }

    /**
     * Sets the icon URL of the action
     */
    public void setIconUrl(String iconUrl) {
        entity.setIconUrl(iconUrl);
    }

    /**
     * The URI of the action
     *
     * @return The URI
     */
    public String getUri() {
        return entity.getUri();
    }

    /**
     * Sets the URI of the action
     */
    public void setUri(String uri) {
        entity.setUri(uri);
    }

    /**
     * Fallback URI for action types not supported by the SDK
     *
     * @return The fallback Uri
     */
    public String getFallback() {
        return entity.getFallback();
    }

    /**
     * Sets the fallback URI for action types not supported by the SDK
     */
    public void setFallback(String uri) {
        entity.setFallback(uri);
    }

    /**
     * The size of a webview
     *
     * @return The size
     */
    public String getSize() {
        return entity.getSize();
    }

    /**
     * Sets the size for a webview
     */
    public void setSize(String size) {
        entity.setSize(size);
    }

    /**
     * The price of the action
     *
     * @return The price (in cents)
     */
    public long getAmount() {
        return entity.getAmount();
    }

    /**
     * Sets the price of the action
     */
    public void setAmount(long amount) {
        entity.setAmount(amount);
    }

    /**
     * The state of the action
     *
     * @return The state of the action
     */
    public String getState() {
        return entity.getState();
    }

    /**
     * The type of action
     *
     * @return The type
     */
    public String getType() {
        return entity.getType();
    }

    /**
     * Sets the type of action
     */
    public void setType(String type) {
        entity.setType(type);
    }

    /**
     * The action payload
     *
     * @return The payload
     */
    public String getPayload() {
        return entity.getPayload();
    }

    /**
     * Sets the action payload
     */
    public void setPayload(String payload) {
        entity.setPayload(payload);
    }

    /**
     * The action metadata
     *
     * @return The metadata
     */
    public Map<String, Object> getMetadata() {
        if (entity.getMetadata() != null) {
            return Collections.unmodifiableMap(entity.getMetadata());
        }

        return null;
    }

    /**
     * Sets the action metadata
     */
    public void setMetadata(Map<String, Object> metadata) {
        entity.setMetadata(metadata);
    }

    /**
     * The type of currency
     *
     * @return The currency type
     */
    public String getCurrency() {
        return entity.getCurrency();
    }

    /**
     * Sets the type of currency
     */
    public void setCurrency(String currency) {
        entity.setCurrency(currency);
    }

    /**
     * Flag indicating if the message action should be the default for a given MessageItem
     *
     * @return True or false
     */
    public boolean isDefault() {
        return entity.isDefault();
    }

    /**
     * Sets the default flag
     */
    public void setDefault(boolean isDefault) {
        entity.setDefault(isDefault);
    }

    MessageActionDto getEntity() {
        return entity;
    }

    @Override
    public boolean equals(Object obj) {
        MessageAction lhs = this;
        MessageAction rhs;

        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        rhs = (MessageAction) obj;
        return lhs.entity == rhs.entity || lhs.entity.equals(rhs.entity);
    }
}
