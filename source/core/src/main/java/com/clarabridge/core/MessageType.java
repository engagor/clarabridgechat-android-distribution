package com.clarabridge.core;

import androidx.annotation.Nullable;

import com.clarabridge.core.utils.ValueEnum;
import com.clarabridge.core.utils.ValueIndex;

/**
 * The {@link Message} type
 */
public enum MessageType implements ValueEnum {

    /**
     * A text message. It could also contain action buttons
     */
    TEXT("text"),

    /**
     * A message that contains an image. It may also contain text and action buttons
     */
    IMAGE("image"),

    /**
     * A message that contains a file. It may also contain text
     */
    FILE("file"),

    /**
     * A message that contains a horizontal scrollable set of items. It may also contain text
     */
    CAROUSEL("carousel"),

    /**
     * A message that contains a vertically scrollable set of items. It may also contain text
     */
    LIST("list"),

    /**
     * A message that contains a location.
     */
    LOCATION("location"),

    /**
     * A message that contains a form.
     */
    FORM("form"),

    ;

    private String value;

    private static ValueIndex<MessageType> valueIndex =
            new ValueIndex<>(MessageType.values());

    MessageType(String value) {
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
     * @return a value of {@link MessageType} if the provided value was valid,
     * null otherwise
     */
    @Nullable
    public static MessageType findByValue(String value) {
        return valueIndex.get(value);
    }

}
