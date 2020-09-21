package com.clarabridge.core;

import android.support.annotation.Nullable;

import com.clarabridge.core.utils.ValueEnum;
import com.clarabridge.core.utils.ValueIndex;

public enum ConversationEventType implements ValueEnum {

    /**
     * An event type indicating that another member of the conversation (business or participant)
     * started typing a response.
     */
    TYPING_START("typing:start"),

    /**
     * An event type indicating that another member of the conversation (business or participant)
     * stopped typing a response.
     */
    TYPING_STOP("typing:stop"),

    /**
     * An event type indicating that a member of the conversation (business, another participant or
     * the current user) recently read the conversation.
     * <p>
     * This event type is triggered for the current user when the conversation is read on a different
     * device.
     */
    CONVERSATION_READ("conversation:read"),

    /**
     * An event type indicating that the current user is now a participant of a new conversation.
     */
    CONVERSATION_ADDED("conversation:added"),

    /**
     * An event type indicating that the current user is no longer a participant of a conversation.
     */
    CONVERSATION_REMOVED("conversation:removed"),

    /**
     * An event type indicating that another participant has joined a conversation that the current
     * user is a part of.
     */
    PARTICIPANT_ADDED("participant:added"),

    /**
     * An event type indicating that another participant has left a conversation that the current
     * user is a part of.
     */
    PARTICIPANT_REMOVED("participant:removed"),

    ;

    private String value;

    private static ValueIndex<ConversationEventType> valueIndex =
            new ValueIndex<>(ConversationEventType.values());

    ConversationEventType(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    /**
     * Utility method to find the enum value of the provided string.
     *
     * @param value the input string
     * @return a value of {@link ConversationEventType} if the provided value was valid,
     * null otherwise
     */
    @Nullable
    public static ConversationEventType findByValue(String value) {
        return valueIndex.get(value);
    }
}
