package com.clarabridge.core.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.clarabridge.core.utils.StringUtils;

public class ConversationDto implements Serializable, Comparable<ConversationDto> {

    @Nullable
    @SerializedName("_id")
    private String id;
    @Nullable
    private String displayName;
    @Nullable
    private String description;
    @Nullable
    private String iconUrl;
    @Nullable
    private Double businessLastRead;
    private boolean isDefault;
    private Double lastUpdatedAt;
    @Nullable
    private Map<String, Object> metadata;
    private String type;
    @Nullable
    private List<ParticipantDto> participants;
    @Nullable
    private List<MessageDto> messages;

    public void update(@NonNull final ConversationDto rhs) {
        this.id = rhs.id;
        this.displayName = rhs.displayName;
        this.description = rhs.description;
        this.iconUrl = rhs.iconUrl;
        this.businessLastRead = rhs.businessLastRead;
        this.isDefault = rhs.isDefault;
        this.lastUpdatedAt = rhs.lastUpdatedAt;
        this.metadata = rhs.metadata;
        this.type = rhs.type;
        this.participants = rhs.participants;

        if (this.messages == null || this.messages.isEmpty() || rhs.messages == null || rhs.messages.isEmpty()) {
            this.messages = rhs.messages != null ? Collections.synchronizedList(rhs.messages) : null;
        } else {
            boolean shouldReplaceMessages = true;

            for (MessageDto message : rhs.messages) {
                if (messages.contains(message)) {
                    shouldReplaceMessages = false;
                    break;
                }
            }

            if (shouldReplaceMessages) {
                messages = Collections.synchronizedList(rhs.messages);
                return;
            }

            synchronized (rhs.getMessages()) {
                for (final MessageDto it : rhs.getMessages()) {
                    int messageIndex = messages.indexOf(it);
                    if (messageIndex >= 0) {
                        messages.get(messageIndex).update(it);
                    } else {
                        messages.add(it);
                    }
                }
            }
        }
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable final String id) {
        this.id = id;
    }

    @Nullable
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@Nullable String displayName) {
        this.displayName = displayName;
    }

    @Nullable
    public String getDescription() {
        return description;
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    @Nullable
    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(@Nullable String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Nullable
    public Double getBusinessLastRead() {
        return businessLastRead;
    }

    public void setBusinessLastRead(@Nullable Double businessLastRead) {
        this.businessLastRead = businessLastRead;
    }

    public boolean getDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    public Double getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(Double lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    @Nullable
    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(@Nullable Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @NonNull
    public List<ParticipantDto> getParticipants() {
        return participants != null ? participants : new ArrayList<ParticipantDto>();
    }

    public void setParticipants(@Nullable List<ParticipantDto> participants) {
        this.participants = participants;
    }

    @NonNull
    public List<MessageDto> getMessages() {
        if (messages == null) {
            messages = Collections.synchronizedList(new ArrayList<MessageDto>());
        }

        return messages;
    }

    @NonNull
    public List<MessageDto> getUnsentMessages() {
        List<MessageDto> unsentMessages = Collections.synchronizedList(new ArrayList<MessageDto>());

        if (messages == null) {
            return unsentMessages;
        }

        for (MessageDto messageDto : messages) {
            if (messageDto.getStatus() == MessageDto.Status.UNSENT) {
                unsentMessages.add(messageDto);
            }
        }

        return unsentMessages;
    }

    public void addMessages(List<MessageDto> messagesToAdd) {
        if (messages == null) {
            messages = Collections.synchronizedList(new ArrayList<MessageDto>());
        }

        if (messagesToAdd != null) {
            messages.addAll(messagesToAdd);
        }
    }

    public void setMessages(final List<MessageDto> messages) {
        if (messages != null) {
            this.messages = Collections.synchronizedList(new ArrayList<>(messages));
        }
    }

    /**
     * Returns the unread count of the {@link ParticipantDto} with the same userId.
     *
     * @param userId the ID of the user
     * @return the unread count if found, 0 otherwise
     */
    public int getUnreadCount(String userId) {
        if (userId == null || participants == null) {
            return 0;
        }

        for (ParticipantDto participant : participants) {
            if (StringUtils.isNotNullAndEqual(participant.getUserId(), userId)) {
                return participant.getUnreadCount();
            }
        }

        return 0;
    }

    /**
     * Returns the timestamp of the most recent message that was read by either the app maker or
     * any other participant participant of the conversation. The provided app user id is used
     * to identify the current user.
     *
     * @param userId the current user ID
     * @return the timestamp if one exists, null otherwise
     */
    @NonNull
    public Double getLastRead(String userId) {
        Double maxLastRead = businessLastRead != null ? businessLastRead : 0;

        if (participants != null) {
            for (ParticipantDto participant : participants) {
                if (StringUtils.isNotNullAndNotEqual(participant.getUserId(), userId)) {
                    Double participantLastRead = participant.getLastRead() != null
                            ? participant.getLastRead()
                            : 0;

                    if (participantLastRead > maxLastRead) {
                        maxLastRead = participantLastRead;
                    }
                }
            }
        }

        return maxLastRead;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ConversationDto that = (ConversationDto) o;

        if (isDefault != that.isDefault) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (displayName != null ? !displayName.equals(that.displayName) : that.displayName != null) {
            return false;
        }
        if (businessLastRead != null
                ? !businessLastRead.equals(that.businessLastRead)
                : that.businessLastRead != null) {
            return false;
        }
        if (lastUpdatedAt != null ? !lastUpdatedAt.equals(that.lastUpdatedAt) : that.lastUpdatedAt != null) {
            return false;
        }
        if (metadata != null ? !metadata.equals(that.metadata) : that.metadata != null) {
            return false;
        }
        if (type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }
        if (participants != null ? !participants.equals(that.participants) : that.participants != null) {
            return false;
        }
        return messages != null ? messages.equals(that.messages) : that.messages == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (displayName != null ? displayName.hashCode() : 0);
        result = 31 * result + (businessLastRead != null ? businessLastRead.hashCode() : 0);
        result = 31 * result + (isDefault ? 1 : 0);
        result = 31 * result + (lastUpdatedAt != null ? lastUpdatedAt.hashCode() : 0);
        result = 31 * result + (metadata != null ? metadata.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (participants != null ? participants.hashCode() : 0);
        result = 31 * result + (messages != null ? messages.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(@NonNull ConversationDto conversationDto) {
        if (conversationDto.getLastUpdatedAt() == null && lastUpdatedAt == null) {
            return 0;
        }

        if (conversationDto.getLastUpdatedAt() == null) {
            return 1;
        }

        if (lastUpdatedAt == null) {
            return -1;
        }

        return lastUpdatedAt.compareTo(conversationDto.getLastUpdatedAt());
    }
}

