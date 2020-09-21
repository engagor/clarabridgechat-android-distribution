package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class ConversationResponseDto implements Serializable {
    @SerializedName("conversation")
    private ConversationDto conversation;
    @SerializedName("messages")
    private List<MessageDto> messages;
    @SerializedName("hasPrevious")
    private boolean hasPrevious = false;

    public ConversationDto getConversation() {
        return conversation;
    }

    public void setConversation(final ConversationDto conversation) {
        this.conversation = conversation;
    }

    public List<MessageDto> getMessages() {
        return messages;
    }

    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }

    public boolean getHasPrevious() {
        return hasPrevious;
    }

    public void setHasPrevious(boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
}

