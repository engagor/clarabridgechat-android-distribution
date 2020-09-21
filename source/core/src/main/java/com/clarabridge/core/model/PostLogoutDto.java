package com.clarabridge.core.model;

/**
 * A request model to logout a user in the backend.
 */
public class PostLogoutDto {

    private final ClientDto client;

    public PostLogoutDto(ClientDto client) {

        this.client = client;
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

        PostLogoutDto that = (PostLogoutDto) o;

        return client != null ? client.equals(that.client) : that.client == null;
    }

    @Override
    public int hashCode() {
        return client != null ? client.hashCode() : 0;
    }
}
