package com.clarabridge.core.model;

/**
 * A wrapper class for the action id of the postback performed by the user on a conversation.
 */
public class PostbackDto {

    private final String actionId;

    public PostbackDto(String actionId) {
        this.actionId = actionId;
    }

    public String getActionId() {
        return actionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostbackDto that = (PostbackDto) o;

        return actionId != null ? actionId.equals(that.actionId) : that.actionId == null;
    }

    @Override
    public int hashCode() {
        return actionId != null ? actionId.hashCode() : 0;
    }
}
