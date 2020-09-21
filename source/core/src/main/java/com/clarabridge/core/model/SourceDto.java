package com.clarabridge.core.model;

import java.io.Serializable;
import com.google.gson.annotations.SerializedName;

public class SourceDto implements Serializable {
    @SerializedName("type")
    private String type;
    @SerializedName("id")
    private String id;
    @SerializedName("group")
    private String group;
    @SerializedName("intent")
    private String intent;
    @SerializedName("originalMessageId")
    private String originalMessageId;
    @SerializedName("originalMessageTimestamp")
    private Double originalMessageTimestamp;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    public String getOriginalMessageId() {
        return originalMessageId;
    }

    public void setOriginalMessageId(String originalMessageId) {
        this.originalMessageId = originalMessageId;
    }

    public Double getOriginalMessageTimestamp() {
        return originalMessageTimestamp;
    }

    public void setOriginalMessageTimestamp(Double originalMessageTimestamp) {
        this.originalMessageTimestamp = originalMessageTimestamp;
    }
}
