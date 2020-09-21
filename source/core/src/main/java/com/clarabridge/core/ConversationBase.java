package com.clarabridge.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.clarabridge.core.model.ConversationDto;
import com.clarabridge.core.model.MessageDto;
import com.clarabridge.core.model.ParticipantDto;
import com.clarabridge.core.utils.DateUtils;

/**
 * Base class for {@link Conversation} implementations that includes getters
 */
abstract class ConversationBase implements Conversation {

    protected static final String LOG_TAG = "Conversation";

    protected List<Message> messages = new LinkedList<>();
    protected List<Participant> participants = new ArrayList<>();
    protected ConversationDto entity;

    ConversationBase(@NonNull ConversationDto entity) {
        this.entity = entity;

        for (MessageDto it : entity.getMessages()) {
            messages.add(new Message(it));
        }

        for (ParticipantDto it : entity.getParticipants()) {
            participants.add(new Participant(it));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public final String getId() {
        return entity.getId();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public final String getDisplayName() {
        return entity.getDisplayName();
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public final Date getAppMakerLastRead() {
        return DateUtils.timestampToDate(entity.getAppMakerLastRead());
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Date getLastUpdatedAt() {
        return DateUtils.timestampToDate(entity.getLastUpdatedAt());
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public final Map<String, Object> getMetadata() {
        return entity.getMetadata();
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    public List<Participant> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public final List<Message> getMessages() {
        List<Message> messagesCopy = new LinkedList<>();

        for (Message message : messages) {
            messagesCopy.add(message.copy());
        }

        return Collections.unmodifiableList(messagesCopy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getUnreadCount() {
        if (ClarabridgeChat.getInstance() == null) {
            return 0;
        }

        return entity.getUnreadCount(ClarabridgeChat.getInstance().getAppUserId());
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public final Date getLastRead() {
        if (ClarabridgeChat.getInstance() == null) {
            return null;
        }

        return DateUtils.timestampToDate(entity.getLastRead(ClarabridgeChat.getInstance().getAppUserId()));
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConversationBase that = (ConversationBase) o;

        if (messages != null ? !messages.equals(that.messages) : that.messages != null) {
            return false;
        }
        if (participants != null ? !participants.equals(that.participants) : that.participants != null) {
            return false;
        }

        return entity != null ? entity.equals(that.entity) : that.entity == null;
    }

    @Override
    public final int hashCode() {
        int result = messages != null ? messages.hashCode() : 0;
        result = 31 * result + (participants != null ? participants.hashCode() : 0);
        result = 31 * result + (entity != null ? entity.hashCode() : 0);
        return result;
    }
}

