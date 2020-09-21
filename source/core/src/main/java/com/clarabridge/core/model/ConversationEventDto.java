package com.clarabridge.core.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.Serializable;

import com.clarabridge.core.ConversationEventType;

/**
 * Model that represents a conversation activity received through the WebSocket. What fields are
 * available depend on this event {@link #getType()}, please refer to the getters for more information.
 */
public class ConversationEventDto implements Serializable {

    @NonNull
    private final String conversationId;
    @NonNull
    private final ConversationEventType type;
    @Nullable
    private String appUserId;
    @Nullable
    private String role;
    @Nullable
    private String name;
    @Nullable
    private String avatarUrl;
    @Nullable
    private Double lastRead;

    public ConversationEventDto(
            @NonNull String conversationId,
            @NonNull ConversationEventType type) {
        this.conversationId = conversationId;
        this.type = type;
    }

    /**
     * Returns the ID of the parent conversation of this event.
     *
     * @return the ID of the parent conversation of this event, always available
     */
    @NonNull
    public String getConversationId() {
        return conversationId;
    }

    /**
     * Returns the type of this event.
     *
     * @return the {@link ConversationEventType} of this event
     * @see ConversationEventType
     */
    @NonNull
    public ConversationEventType getType() {
        return type;
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
        return appUserId;
    }

    public void setAppUserId(@Nullable String appUserId) {
        this.appUserId = appUserId;
    }

    /**
     * Returns the role of the user related to this event (typically "appUser" or "appMaker") if this
     * event type is {@link ConversationEventType#CONVERSATION_READ}, {@link ConversationEventType#TYPING_START}
     * or {@link ConversationEventType#TYPING_STOP}.
     *
     * @return the role of the user related to this event, or null if no user is related
     */
    @Nullable
    public String getRole() {
        return role;
    }

    public void setRole(@Nullable String role) {
        this.role = role;
    }

    /**
     * Returns the name of the participant related to this event if this event type is
     * {@link ConversationEventType#CONVERSATION_READ}, {@link ConversationEventType#TYPING_START}
     * or {@link ConversationEventType#TYPING_STOP}.
     *
     * @return the name of the participant related to this event, or null if no user is related
     */
    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    /**
     * Returns the avatar URL of the participant related to this event if this event type is
     * {@link ConversationEventType#CONVERSATION_READ}, {@link ConversationEventType#TYPING_START}
     * or {@link ConversationEventType#TYPING_STOP}.
     *
     * @return the avatar URL of the participant related to this event, or null if no user is related
     */
    @Nullable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(@Nullable String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * Returns the date of the last read message if this event type is
     * {@link ConversationEventType#CONVERSATION_READ}.
     *
     * @return the date of the last read message if applicable to this event, null otherwise
     */
    @Nullable
    public Double getLastRead() {
        return lastRead;
    }

    public void setLastRead(@Nullable Double lastRead) {
        this.lastRead = lastRead;
    }
}
