package com.clarabridge.core.service;

import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import com.clarabridge.core.di.SdkScope;
import com.clarabridge.core.facade.PersistenceFacade;
import com.clarabridge.core.model.ConversationDto;
import com.clarabridge.core.model.MessageDto;
import com.clarabridge.core.model.ParticipantDto;
import com.clarabridge.core.utils.StringUtils;

/**
 * A helper class to check and update stored {@link ConversationDto}s.
 */
@SdkScope
public class ConversationManager {

    private final PersistenceFacade persistenceFacade;

    @Inject
    ConversationManager(PersistenceFacade persistenceFacade) {
        this.persistenceFacade = persistenceFacade;
    }

    /**
     * Indicates whether or not a saved {@link ConversationDto} is up-to-date. A conversation is
     * considered up-to-date if the last {@link MessageDto} of {@link ConversationDto#getMessages()}
     * is equal to the message stored for that conversation in {@link PersistenceFacade#getConversationsList()}.
     *
     * @param conversationId the id of the conversation to check
     * @return true if the conversation is up-to-date, false otherwise
     */
    boolean isSavedConversationUpToDate(String conversationId) {
        ConversationDto savedConversation = persistenceFacade.getConversationById(conversationId);

        if (savedConversation == null) {
            return false;
        }

        List<ConversationDto> conversations = persistenceFacade.getConversationsList();

        for (ConversationDto conversation : conversations) {
            if (StringUtils.isNotNullAndNotEqual(conversation.getId(), conversationId)) {
                continue;
            }

            boolean sameParticipants = conversation.getParticipants() != null
                    && conversation.getParticipants().equals(savedConversation.getParticipants());

            if (conversation.getMessages().isEmpty()) {
                return sameParticipants;
            }

            if (savedConversation.getMessages().isEmpty()) {
                return false;
            }

            if (!sameParticipants) {
                return false;
            }

            int mostRecentSavedMessageIndex = savedConversation.getMessages().size() - 1;
            MessageDto mostRecentSavedMessage = savedConversation.getMessages().get(mostRecentSavedMessageIndex);
            MessageDto mostRecentListMessage = conversation.getMessages().get(0);

            return mostRecentListMessage.equals(mostRecentSavedMessage);
        }

        return false;
    }

    /**
     * Add the given {@link MessageDto} to the respective conversation persisted in the list of
     * {@link ConversationDto}s if it exists in {@link PersistenceFacade}. It also updates the
     * timestamps and unread count of the {@link ConversationDto} and all of its {@link ParticipantDto}s.
     *
     * @param conversationId        the ID of the {@link ConversationDto} to be updated
     * @param message               the {@link MessageDto} to be added
     * @param currentAppUserId      the ID of the current user
     * @param isConversationVisible if the conversation UI is currently visible
     */
    boolean addMessageToConversationList(
            String conversationId,
            MessageDto message,
            String currentAppUserId,
            boolean isConversationVisible) {

        List<ConversationDto> conversations = persistenceFacade.getConversationsList();

        for (ConversationDto conversation : conversations) {
            if (StringUtils.isNotNullAndNotEqual(conversation.getId(), conversationId)) {
                continue;
            }

            conversation.setMessages(Collections.singletonList(message));

            updateTimestampsAndUnreadCount(
                    conversation,
                    message.getReceived(),
                    message.getUserId(),
                    currentAppUserId,
                    isConversationVisible);

            persistenceFacade.saveConversationsList(conversations);

            return true;
        }

        return false;
    }

    /**
     * Add the given {@link MessageDto} to the persisted {@link ConversationDto} if it exists in
     * {@link PersistenceFacade}. It also updates the timestamps and unread count of the
     * {@link ConversationDto} and all of its {@link ParticipantDto}s.
     *
     * @param conversationId        the ID of the {@link ConversationDto} to be updated
     * @param message               the {@link MessageDto} to be added
     * @param currentAppUserId      the ID of the current user
     * @param isConversationVisible if the conversation UI is currently visible
     */
    void addMessageToConversation(
            String conversationId,
            MessageDto message,
            String currentAppUserId,
            boolean isConversationVisible) {

        ConversationDto conversation = persistenceFacade.getConversationById(conversationId);

        if (conversation == null) {
            return;
        }

        conversation.addMessages(Collections.singletonList(message));

        updateTimestampsAndUnreadCount(
                conversation,
                message.getReceived(),
                message.getUserId(),
                currentAppUserId,
                isConversationVisible);

        persistenceFacade.saveConversationById(conversationId, conversation);
    }

    /**
     * Update the timestamps and unread count of the given {@link ConversationDto} and all of its
     * {@link ParticipantDto}s. This method should be used when the list of messages was modified
     * since it will change the time when the conversation was last updated at and potentially increase
     * the unread count of the participants.
     *
     * @param conversation          the {@link ConversationDto} to be updated
     * @param newTimestamp          the new timestamp to be set
     * @param authorAppUserId       the ID of the user authoring the event
     * @param currentAppUserId      the ID of the current user of the SDK
     * @param isConversationVisible if the conversation UI is currently visible to the user
     */
    void updateTimestampsAndUnreadCount(
            ConversationDto conversation,
            Double newTimestamp,
            @Nullable String authorAppUserId,
            String currentAppUserId,
            boolean isConversationVisible) {

        conversation.setLastUpdatedAt(newTimestamp);

        if (conversation.getParticipants() == null) {
            return;
        }

        for (ParticipantDto participant : conversation.getParticipants()) {
            boolean isAuthor = participant.getUserId().equals(authorAppUserId);
            boolean isCurrentUser = participant.getUserId().equals(currentAppUserId);

            if (isAuthor || (isCurrentUser && isConversationVisible)) {
                participant.setLastRead(newTimestamp);
                participant.setUnreadCount(0);
            } else {
                participant.setUnreadCount(participant.getUnreadCount() + 1);
            }
        }
    }

    /**
     * Updates the persisted {@link ConversationDto} if it exists in {@link PersistenceFacade} to set
     * the unread count and the last read timestamp of the {@link ParticipantDto} whose ID matches
     * the provided userId. This method should be used when a read event is received as it will
     * always set the unread count to 0.
     *
     * @param conversationId the ID of the {@link ConversationDto} to be updated
     * @param newTimestamp   the new timestamp to be set
     * @param userId         the ID of the user to be updated
     */
    void setParticipantLastRead(
            String conversationId,
            Double newTimestamp,
            String userId) {

        List<ConversationDto> conversations = persistenceFacade.getConversationsList();

        for (ConversationDto conversation : conversations) {
            if (StringUtils.isNotNullAndEqual(conversation.getId(), conversationId)) {
                setParticipantLastRead(conversation, newTimestamp, userId);
                persistenceFacade.saveConversationsList(conversations);
                break;
            }
        }

        ConversationDto savedConversation = persistenceFacade.getConversationById(conversationId);

        if (savedConversation != null) {
            setParticipantLastRead(savedConversation, newTimestamp, userId);
            persistenceFacade.saveConversationById(savedConversation.getId(), savedConversation);
        }
    }

    /**
     * Update the unread count and the last read timestamp of the {@link ParticipantDto} whose
     * ID matches the provided userId.
     *
     * @param conversation the {@link ConversationDto} to be updated
     * @param newTimestamp the new timestamp to be set
     * @param userId    the ID of the user to be updated
     */
    private void setParticipantLastRead(
            ConversationDto conversation,
            Double newTimestamp,
            String userId) {

        if (conversation.getParticipants() == null) {
            return;
        }

        for (ParticipantDto participant : conversation.getParticipants()) {
            if (StringUtils.isNotNullAndEqual(participant.getUserId(), userId)) {
                participant.setUnreadCount(0);
                participant.setLastRead(newTimestamp);
                return;
            }
        }
    }

    /**
     * Updates the persisted {@link ConversationDto} if it exists in {@link PersistenceFacade} to set
     * its {@link ConversationDto#setBusinessLastRead(Double)} to the received value.
     * <p>
     * Both the list and the individual conversation storage will be updated.
     *
     * @param conversationId the ID of the {@link ConversationDto} to be updated
     * @param newTimestamp   the new timestamp to be set
     */
    void setBusinessLastRead(
            String conversationId,
            Double newTimestamp) {

        List<ConversationDto> conversations = persistenceFacade.getConversationsList();

        for (ConversationDto conversation : conversations) {
            if (StringUtils.isEqual(conversation.getId(), conversationId)) {
                conversation.setBusinessLastRead(newTimestamp);
                persistenceFacade.saveConversationsList(conversations);
                break;
            }
        }

        ConversationDto savedConversation = persistenceFacade.getConversationById(conversationId);

        if (savedConversation != null) {
            savedConversation.setBusinessLastRead(newTimestamp);
            persistenceFacade.saveConversationById(savedConversation.getId(), savedConversation);
        }
    }
}
