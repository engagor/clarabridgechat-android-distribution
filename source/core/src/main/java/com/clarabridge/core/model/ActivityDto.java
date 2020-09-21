package com.clarabridge.core.model;

/**
 * A wrapper class for an activity performed by the user on a conversation. Currently, the type
 * value can be "conversation:read", "typing:start" or "typing:stop".
 */
public class ActivityDto {

    private final String type;

    public ActivityDto(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ActivityDto that = (ActivityDto) o;

        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        return type != null ? type.hashCode() : 0;
    }

}
