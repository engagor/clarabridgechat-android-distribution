package com.clarabridge.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;
import java.util.Date;

import com.clarabridge.core.model.ConversationEventDto;
import com.clarabridge.core.utils.DateUtils;

/**
 * Object representing a conversation event.
 *
 * @see ConversationEventType
 */
public final class ConversationEvent implements Serializable {

    @NonNull
    private final ConversationEventDto entity;

    ConversationEvent(@NonNull ConversationEventDto entity) {
        this.entity = entity;
    }

    /**
     * Returns the ID of the parent conversation of this event.
     *
     * @return the ID of the parent conversation of this event
     */
    @NonNull
    public String getConversationId() {
        return entity.getConversationId();
    }

    /**
     * The type of event that is being triggered. See {@link ConversationEventType} for possible types
     *
     * @return The activity type
     */
    @NonNull
    public ConversationEventType getType() {
        return entity.getType();
    }

    /**
     * Always available for {@link ConversationEventType#PARTICIPANT_ADDED} and
     * {@link ConversationEventType#PARTICIPANT_REMOVED}.
     * <p>
     * Available for {@link ConversationEventType#CONVERSATION_READ}, {@link ConversationEventType#TYPING_START}
     * and {@link ConversationEventType#TYPING_STOP} if they were triggered by another app user.
     *
     * @return the app user ID if available, null otherwise
     */
    @Nullable
    public String getAppUserId() {
        return entity.getAppUserId();
    }

    /**
     * Returns the role of the participant related to this event (typically "appUser" or "appMaker") if this
     * event type is {@link ConversationEventType#CONVERSATION_READ}, {@link ConversationEventType#TYPING_START}
     * or {@link ConversationEventType#TYPING_STOP}.
     *
     * @return the role of the participant related to this event, or null if this event has no related participants
     */
    @Nullable
    public String getRole() {
        return entity.getRole();
    }

    /**
     * Returns the name of the participant related to this event if this event type is
     * {@link ConversationEventType#CONVERSATION_READ}, {@link ConversationEventType#TYPING_START}
     * or {@link ConversationEventType#TYPING_STOP}.
     *
     * @return the name of the participant related to this event, or null if this event has no related participants
     */
    @Nullable
    public String getName() {
        return entity.getName();
    }

    /**
     * Returns the avatar URL of the participant related to this event if this event type is
     * {@link ConversationEventType#CONVERSATION_READ}, {@link ConversationEventType#TYPING_START}
     * or {@link ConversationEventType#TYPING_STOP}.
     *
     * @return the avatar URL of the participant related to this event, or null if this event has
     * no related participants
     */
    @Nullable
    public String getAvatarUrl() {
        return entity.getAvatarUrl();
    }

    /**
     * Returns the date of the last read message if this event type is
     * {@link ConversationEventType#CONVERSATION_READ}.
     *
     * @return the date of the last read message if applicable to this event, null otherwise
     */
    @Nullable
    public Date getLastRead() {
        return DateUtils.timestampToDate(entity.getLastRead());
    }
}
