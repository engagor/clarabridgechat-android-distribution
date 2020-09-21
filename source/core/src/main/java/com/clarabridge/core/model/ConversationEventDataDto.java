package com.clarabridge.core.model;

import java.io.Serializable;

/**
 * The data of the user related to an event.
 */
public class ConversationEventDataDto implements Serializable {

    private String name;
    private String avatarUrl;
    private Double lastRead;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    /**
     * Returns the timestamp of the message that was last read by the participant sending the event.
     *
     * @return the timestamp of the message that was last read by the participant sending the event
     */
    public Double getLastRead() {
        return lastRead;
    }

    public void setLastRead(Double lastRead) {
        this.lastRead = lastRead;
    }
}
