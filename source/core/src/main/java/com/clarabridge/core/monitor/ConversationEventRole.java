package com.clarabridge.core.monitor;

import android.support.annotation.Nullable;

import com.clarabridge.core.utils.ValueEnum;
import com.clarabridge.core.utils.ValueIndex;

/**
 * Enum class that define the possible roles that authors of messages received through the WebSocket
 * connection can have.
 */
public enum ConversationEventRole implements ValueEnum {

    /**
     * When a WebSocket message is authored by the "appMaker", the business that owns the app.
     */
    APP_MAKER("appMaker"),

    /**
     * When a WebSocket message is authored by an "appUser".
     */
    APP_USER("appUser"),

    ;

    private final String value;

    private static ValueIndex<ConversationEventRole> valueIndex =
            new ValueIndex<>(ConversationEventRole.values());

    ConversationEventRole(String value) {
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
     * @return a value of {@link ConversationEventRole} if the provided value was valid,
     * null otherwise
     */
    @Nullable
    public static ConversationEventRole findByValue(String value) {
        return valueIndex.get(value);
    }
}
