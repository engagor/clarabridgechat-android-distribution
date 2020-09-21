package com.clarabridge.core.model;

import java.io.Serializable;
import java.util.List;

/**
 * Describes the contents of the response body from sending a message
 */
public class PostMessageDto implements Serializable {

    private List<MessageDto> messages;
    private ConversationDto conversation;

    /**
     * @return the list of {@link MessageDto}
     */
    public List<MessageDto> getMessages() {
        return messages;
    }

    /**
     * Sets the list of {@link MessageDto}
     *
     * @param messages the list of messages
     */
    public void setMessages(List<MessageDto> messages) {
        this.messages = messages;
    }

    /**
     * The {@link ConversationDto} entity returned from a POST message request will only
     * contain the conversation ID
     *
     * @return an instance of {@link ConversationDto}
     */
    public ConversationDto getConversation() {
        return conversation;
    }

    /**
     * Sets the {@link ConversationDto}
     *
     * @param conversation the conversation entity
     */
    public void setConversation(ConversationDto conversation) {
        this.conversation = conversation;
    }
}
