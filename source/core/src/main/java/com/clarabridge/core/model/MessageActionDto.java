package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class MessageActionDto implements Serializable {

    @SerializedName("_id")
    private String id;
    @SerializedName("text")
    private String text;
    @SerializedName("iconUrl")
    private String iconUrl;
    @SerializedName("uri")
    private String uri;
    @SerializedName("fallback")
    private String fallback;
    @SerializedName("size")
    private String size;
    @SerializedName("type")
    private String type;
    @SerializedName("payload")
    private String payload;
    @SerializedName("metadata")
    private Map<String, Object> metadata;
    @SerializedName("amount")
    private long amount;
    @SerializedName("state")
    private String state;
    @SerializedName("currency")
    private String currency;
    @SerializedName("default")
    private boolean isDefault;

    public MessageActionDto() {

    }

    public MessageActionDto(MessageActionDto messageAction) {
        id = messageAction.id;
        uri = messageAction.uri;
        text = messageAction.text;
        type = messageAction.type;
        size = messageAction.size;
        iconUrl = messageAction.iconUrl;
        payload = messageAction.payload;
        fallback = messageAction.fallback;
        isDefault = messageAction.isDefault;

        if (messageAction.metadata != null) {
            metadata = new HashMap<>(messageAction.metadata);
        }

        state = messageAction.state;
        amount = messageAction.amount;
        currency = messageAction.currency;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MessageActionDto that = (MessageActionDto) o;

        return id != null ? id.equals(that.id) : that.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
