package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Describes a list of conversations belonging to a user. May not include all of the messages
 * for that conversation and will instead contain the most recently added message.
 */
public class ConversationsListResponseDto {

    @SerializedName("conversations")
    private List<ConversationDto> conversations;

    @SerializedName("conversationsPagination")
    private ConversationsPaginationResponseDto conversationsPagination;

    public ConversationsListResponseDto(List<ConversationDto> conversations) {
        this.conversations = conversations;
    }

    /**
     * @return the list of {@link ConversationDto}
     */
    public List<ConversationDto> getConversations() {
        return conversations;
    }

    /**
     * @return the has more conversations flag for the next page
     */
    public ConversationsPaginationResponseDto getConversationsPagination() {
        return conversationsPagination;
    }

    public void setConversationsPagination(ConversationsPaginationResponseDto conversationsPagination) {
        this.conversationsPagination = conversationsPagination;
    }
}
