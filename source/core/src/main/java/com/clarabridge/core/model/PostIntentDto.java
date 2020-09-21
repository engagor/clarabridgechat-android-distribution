package com.clarabridge.core.model;

/**
 * A wrapper class for the user intent when creating a conversation. An example is the
 * "conversation:start" intent sent when creating a conversation in the backend.
 */
public class PostIntentDto {

    private final String intent;
    private final ClientDto client;

    public PostIntentDto(String intent, ClientDto client) {
        this.intent = intent;
        this.client = client;
    }

    public String getIntent() {
        return intent;
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

        PostIntentDto that = (PostIntentDto) o;

        if (intent != null ? !intent.equals(that.intent) : that.intent != null) {
            return false;
        }
        return client != null ? client.equals(that.client) : that.client == null;
    }

    @Override
    public int hashCode() {
        int result = intent != null ? intent.hashCode() : 0;
        result = 31 * result + (client != null ? client.hashCode() : 0);
        return result;
    }
}
