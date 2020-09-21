package com.clarabridge.core.model;

import java.util.List;

/**
 * Describes a list of conversations belonging to a user. May not include all of the messages
 * for that conversation and will instead contain the most recently added message.
 */
public class ConversationsListResponseDto {

    private List<ConversationDto> conversations;

    public ConversationsListResponseDto(List<ConversationDto> conversations) {
        this.conversations = conversations;
    }

    /**
     * @return the list of {@link ConversationDto}
     */
    public List<ConversationDto> getConversations() {
        return conversations;
    }

}
