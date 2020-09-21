package com.clarabridge.core.monitor;

import android.support.annotation.Nullable;

import com.clarabridge.core.utils.ValueEnum;
import com.clarabridge.core.utils.ValueIndex;

/**
 * Enum class that define the types of messages received through the WebSocket connection.
 */
enum ConversationMonitorEventType implements ValueEnum {

    /**
     * A type that indicates that a {@link com.clarabridge.core.model.MessageDto} was received through the
     * WebSocket.
     */
    MESSAGE("message"),

    /**
     * A type that indicates that a file upload to the server has failed.
     */
    FAILED_UPLOAD("upload:failed"),

    /**
     * A type that indicates a conversation activity, like typing or read receipts.
     */
    ACTIVITY("activity"),

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

    private final String value;

    private static ValueIndex<ConversationMonitorEventType> valueIndex =
            new ValueIndex<>(ConversationMonitorEventType.values());

    ConversationMonitorEventType(String value) {
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
     * @return a value of {@link ConversationMonitorEventType} if the provided value was valid,
     * null otherwise
     */
    @Nullable
    public static ConversationMonitorEventType findByValue(String value) {
        return valueIndex.get(value);
    }
}
