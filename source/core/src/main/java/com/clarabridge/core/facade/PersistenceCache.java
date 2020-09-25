package com.clarabridge.core.facade;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import com.clarabridge.core.model.ConversationDto;
import com.clarabridge.core.utils.StringUtils;

/**
 * Simple in-memory caching layer for the {@link PersistenceFacade} to help alleviate the amount
 * of calls we would be making to read from storage.
 */
class PersistenceCache {

    private Set<ConversationDto> conversationsLinkedHashSet;
    private AtomicBoolean hasMoreConversations = new AtomicBoolean(true);

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
        conversationsLinkedHashSet = new LinkedHashSet<>();
    }

    /**
     * @return a copy of the conversations list, or null if nothing was cached
     */
    @NonNull
    List<ConversationDto> getConversationsList() {
        return new ArrayList<>(conversationsLinkedHashSet);
    }

    /**
     * @param toIndex The index in which we want to get the list of conversations according to it.
     * @return a copy of the conversations list by given offset, or null if nothing was cached
     */
    @NonNull
    List<ConversationDto> getConversationsList(int toIndex) {
        if (conversationsLinkedHashSet != null && !conversationsLinkedHashSet.isEmpty()) {
            if (conversationsLinkedHashSet.size() >= toIndex) {
                return new LinkedList<>(conversationsLinkedHashSet).subList(Math.max(toIndex - 10, 0), toIndex);
            } else {
                return new ArrayList<>(conversationsLinkedHashSet);
            }
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Saves a new conversations list to the cache
     *
     * @param conversationsList the new list to be cached
     */
    void saveConversationsList(@Nullable List<ConversationDto> conversationsList) {
        if (conversationsList != null && !conversationsList.isEmpty()) {
            conversationsLinkedHashSet.addAll(conversationsList);
            TreeSet<ConversationDto> conversationsTreeSet = new TreeSet<>(Collections.reverseOrder());
            conversationsTreeSet.addAll(conversationsLinkedHashSet);
            conversationsLinkedHashSet.clear();
            conversationsLinkedHashSet.addAll(conversationsTreeSet);
        }
    }

    /**
     * Deletes a conversation from the cached conversation list
     *
     * @param conversation the conversation object that we need to delete.
     */
    void removeConversationFromConversationList(@NonNull ConversationDto conversation) {
        List<ConversationDto> cachedList = new ArrayList<>(conversationsLinkedHashSet);
        for (Iterator<ConversationDto> iterator = cachedList.iterator(); iterator.hasNext();) {
            ConversationDto conversationDto = iterator.next();
            if (StringUtils.isEqual(conversationDto.getId(), conversation.getId())) {
                iterator.remove();
            }
        }
        conversationsLinkedHashSet.clear();
        conversationsLinkedHashSet.addAll(cachedList);
    }

    /**
     * Adds a conversation from the cached conversation list
     *
     * @param conversation the conversation object that we need to add.
     */
    void addConversationToConversationList(@NonNull ConversationDto conversation) {
        conversationsLinkedHashSet.add(conversation);
        TreeSet<ConversationDto> conversationsTreeSet = new TreeSet<>(Collections.reverseOrder());
        conversationsTreeSet.addAll(conversationsLinkedHashSet);
        conversationsLinkedHashSet.clear();
        conversationsLinkedHashSet.addAll(conversationsTreeSet);
    }

    /**
     * @return if there is more conversations to be fetched or not.
     */
    boolean isHasMoreConversations() {
        return hasMoreConversations.get();
    }

    /**
     * Saves the hasMore we got from {@link com.clarabridge.core.model.ConversationsPaginationResponseDto} once
     *
     * @param hasMore is the boolean that indicates if there is more conversations to be fetched or not.
     */
    void setHasMoreConversations(boolean hasMore) {
        hasMoreConversations.set(hasMore);
    }

    /**
     * Clear the conversation list that is cached on-memory
     */
    void clearConversationList() {
        conversationsLinkedHashSet.clear();
    }

}
