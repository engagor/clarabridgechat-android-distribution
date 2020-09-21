package com.clarabridge.core.facade;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.clarabridge.core.model.ConversationDto;

/**
 * Simple in-memory caching layer for the {@link PersistenceFacade} to help alleviate the amount
 * of calls we would be making to read from storage.
 */
class PersistenceCache {

    private List<ConversationDto> conversationsList;
    private Map<String, ConversationDto> conversations;

    /**
     * @return a new empty instance of {@link PersistenceCache}
     */
    static PersistenceCache create() {
        return new PersistenceCache();
    }

    /**
     * Create a new instance of the {@link PersistenceCache} which would be cold and hold no values
     */
    PersistenceCache() {
        this.conversations = new HashMap<>();
    }

    /**
     * @return a copy of the conversations list, or null if nothing was cached
     */
    @Nullable
    List<ConversationDto> getConversationsList() {
        return conversationsList != null
                ? new ArrayList<>(conversationsList)
                : null;
    }

    /**
     * Saves a new conversations list to the cache
     *
     * @param conversationsList the new list to be cached
     */
    void saveConversationsList(@Nullable List<ConversationDto> conversationsList) {
        this.conversationsList = conversationsList != null
                ? new ArrayList<>(conversationsList)
                : null;
    }

    /**
     * Gets the conversation entity cached by the given conversation ID
     *
     * @param conversationId the identifier for conversation entity
     * @return a {@link ConversationDto} cached by the given ID, or null if nothing was cached
     */
    @Nullable
    ConversationDto getConversationById(String conversationId) {
        return conversations.get(conversationId);
    }

    /**
     * Caches a {@link ConversationDto} by the given conversation ID
     *
     * @param conversationId the identifier for the conversation entity
     * @param conversation the {@link ConversationDto} to be cached
     */
    void saveConversationById(String conversationId, @Nullable ConversationDto conversation) {
        conversations.put(conversationId, conversation);
    }

}
