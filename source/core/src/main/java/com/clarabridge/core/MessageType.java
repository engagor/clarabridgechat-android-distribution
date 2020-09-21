package com.clarabridge.core;

/**
 * The {@link Message} type
 */
public enum MessageType {

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

    ;

    private String value;

    MessageType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
