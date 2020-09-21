package com.clarabridge.core.model;

/**
 * A model that describes the user as an Author of an action related to a conversation
 * (subscribing, send messages, uploading files, etc.) when sending requests to the backend.
 * In these cases the role should be defined as "appUser".
 */
public class PostAuthorDto {

    private final String role;
    private final ClientDto client;
    private final String appUserId;

    public PostAuthorDto(
            String role,
            ClientDto client,
            String appUserId) {
        this.role = role;
        this.client = client;
        this.appUserId = appUserId;
    }

    public String getRole() {
        return role;
    }

    public ClientDto getClient() {
        return client;
    }

    public String getAppUserId() {
        return appUserId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostAuthorDto that = (PostAuthorDto) o;

        if (role != null ? !role.equals(that.role) : that.role != null) {
            return false;
        }
        if (client != null ? !client.equals(that.client) : that.client != null) {
            return false;
        }
        return appUserId != null ? appUserId.equals(that.appUserId) : that.appUserId == null;
    }

    @Override
    public int hashCode() {
        int result = role != null ? role.hashCode() : 0;
        result = 31 * result + (client != null ? client.hashCode() : 0);
        result = 31 * result + (appUserId != null ? appUserId.hashCode() : 0);
        return result;
    }
}
