package com.clarabridge.core.model;

/**
 * A wrapper class to send a legacy client id to the backend when upgrading a user.
 */
public class PostClientIdDto {

    private final String clientId;

    public PostClientIdDto(String clientId) {
        this.clientId = clientId;
    }

    public String getClientId() {
        return clientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostClientIdDto that = (PostClientIdDto) o;

        return clientId != null ? clientId.equals(that.clientId) : that.clientId == null;
    }

    @Override
    public int hashCode() {
        return clientId != null ? clientId.hashCode() : 0;
    }
}
