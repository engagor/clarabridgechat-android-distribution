package com.clarabridge.core.model;

/**
 * A request model to login a user in the backend.
 */
public class PostLoginDto {

    private final String appUserId;
    private final String sessionToken;
    private final String userId;
    private final ClientDto client;

    public PostLoginDto(
            String appUserId,
            String sessionToken,
            String userId,
            ClientDto client) {

        this.appUserId = appUserId;
        this.sessionToken = sessionToken;
        this.userId = userId;
        this.client = client;
    }

    public String getAppUserId() {
        return appUserId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getUserId() {
        return userId;
    }

    public ClientDto getClient() {
        return client;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostLoginDto that = (PostLoginDto) o;

        if (appUserId != null ? !appUserId.equals(that.appUserId) : that.appUserId != null) {
            return false;
        }
        if (sessionToken != null ? !sessionToken.equals(that.sessionToken) : that.sessionToken != null) {
            return false;
        }
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) {
            return false;
        }
        return client != null ? client.equals(that.client) : that.client == null;
    }

    @Override
    public int hashCode() {
        int result = appUserId != null ? appUserId.hashCode() : 0;
        result = 31 * result + (sessionToken != null ? sessionToken.hashCode() : 0);
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (client != null ? client.hashCode() : 0);
        return result;
    }
}
