package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

/**
 * A request model to login a user in the backend.
 */
public class PostLoginDto {

    @SerializedName("appUserId")
    private final String userId;
    private final String sessionToken;
    @SerializedName("userId")
    private final String externalId;
    private final ClientDto client;

    public PostLoginDto(
            String userId,
            String sessionToken,
            String externalId,
            ClientDto client) {

        this.userId = userId;
        this.sessionToken = sessionToken;
        this.externalId = externalId;
        this.client = client;
    }

    public String getUserId() {
        return userId;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public String getExternalId() {
        return externalId;
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

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) {
            return false;
        }
        if (sessionToken != null ? !sessionToken.equals(that.sessionToken) : that.sessionToken != null) {
            return false;
        }
        if (externalId != null ? !externalId.equals(that.externalId) : that.externalId != null) {
            return false;
        }
        return client != null ? client.equals(that.client) : that.client == null;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (sessionToken != null ? sessionToken.hashCode() : 0);
        result = 31 * result + (externalId != null ? externalId.hashCode() : 0);
        result = 31 * result + (client != null ? client.hashCode() : 0);
        return result;
    }
}
