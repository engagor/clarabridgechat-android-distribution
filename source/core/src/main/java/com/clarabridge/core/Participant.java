package com.clarabridge.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Date;

import com.clarabridge.core.model.ParticipantDto;
import com.clarabridge.core.utils.DateUtils;

/**
 * Object representing the participant of a {@link Conversation}.
 */
public final class Participant {

    @NonNull
    private final ParticipantDto entity;

    Participant(@NonNull ParticipantDto entity) {
        this.entity = entity;
    }

    /**
     * The unique ID of this participant. The same user can be a participant of multiple conversations,
     * with a different ID for each conversation, but {@link #getUserId()} will always be the same.
     *
     * @return the participant ID
     *
     * @see #getUserId()
     */
    public String getId() {
        return entity.getId();
    }

    /**
     * The assigned userId for the user that this participant is associated with. The same user
     * can be a participant of multiple conversation.
     * <p>
     * This property is set automatically by ClarabridgeChat, and is not configurable.
     * This is analogous to {@link User#getUserId()}.
     *
     * @return the participant userId
     */
    public String getUserId() {
        return entity.getUserId();
    }

    /**
     * Returns the unread count for this participant.
     *
     * @return the unread count for this participant
     */
    public int getUnreadCount() {
        return entity.getUnreadCount();
    }

    /**
     * Returns the date when this participant last read messages of the conversation.
     *
     * @return the date when this participant last read messages of the conversation
     */
    @Nullable
    public Date getLastRead() {
        return DateUtils.timestampToDate(entity.getLastRead());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Participant that = (Participant) o;

        return entity.equals(that.entity);
    }

    @Override
    public int hashCode() {
        return entity.hashCode();
    }
}
