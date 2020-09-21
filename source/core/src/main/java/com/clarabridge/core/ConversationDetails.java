package com.clarabridge.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Provides an interface to view the information of a {@link Conversation}.
 */
public interface ConversationDetails {

    /**
     * Returns the ID of this conversation.
     *
     * @return the ID of this conversation, or null if a conversation wasn't started yet
     * @see Conversation#sendMessage(Message)
     * @see ClarabridgeChat#startConversation(ClarabridgeChatCallback)
     */
    @Nullable
    String getId();

    /**
     * Returns the display name of this conversation.
     *
     * @return the display name of this conversation
     */
    @Nullable
    String getDisplayName();

    /**
     * Returns the date when the business last read the user's messages.
     *
     * @return the {@link Date} when the business last read the user's messages
     */
    @Nullable
    Date getAppMakerLastRead();

    /**
     * Returns the date when this conversation was last updated.
     *
     * @return the {@link Date} when this conversation was last updated
     */
    @Nullable
    Date getLastUpdatedAt();

    /**
     * Data representing an optional flat object containing additional properties associated with the conversation.
     *
     * @return the metadata of the conversation
     */
    @Nullable
    Map<String, Object> getMetadata();

    /**
     * Returns the list of participants of this conversation.
     *
     * @return the list of {@link Participant}s of this conversation.
     */
    @NonNull
    List<Participant> getParticipants();

    /**
     * Returns the list of messages of this conversation.
     *
     * @return the list of {@link Message}s of this conversation
     */
    @NonNull
    List<Message> getMessages();

    /**
     * Count of unread messages in the conversation.
     * <p>
     * The primary use of this property is to be able to display an indicator when the conversation has unread messages.
     *
     * @return The unread message count
     */
    int getUnreadCount();

    /**
     * Returns the date when any other participant of the conversation last read the user's messages.
     *
     * @return {@link Date} when any other participant of the conversation last read the user's messages
     */
    @Nullable
    Date getLastRead();
}
