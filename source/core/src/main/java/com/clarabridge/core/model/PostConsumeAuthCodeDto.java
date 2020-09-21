package com.clarabridge.core.model;

/**
 * A wrapper class to send an auth code to the backend when authenticating an user.
 */
public class PostConsumeAuthCodeDto {

    private final String authCode;
    private final ClientDto client;

    public PostConsumeAuthCodeDto(
            String authCode,
            ClientDto client) {
        this.authCode = authCode;
        this.client = client;
    }

    public String getAuthCode() {
        return authCode;
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

        PostConsumeAuthCodeDto that = (PostConsumeAuthCodeDto) o;

        if (authCode != null ? !authCode.equals(that.authCode) : that.authCode != null) {
            return false;
        }
        return client != null ? client.equals(that.client) : that.client == null;
    }

    @Override
    public int hashCode() {
        int result = authCode != null ? authCode.hashCode() : 0;
        result = 31 * result + (client != null ? client.hashCode() : 0);
        return result;
    }
}
