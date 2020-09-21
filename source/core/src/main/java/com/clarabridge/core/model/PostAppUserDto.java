package com.clarabridge.core.model;

import java.io.Serializable;

public class PostAppUserDto extends AppUserDto implements Serializable {

    private ClientDto client;
    private String intent;

    public PostAppUserDto(final AppUserDto appUser) {
        if (appUser != null) {
            update(appUser);
            setUserId(appUser.getUserId());
        }
    }

    public ClientDto getClient() {
        return client;
    }

    public void setClient(ClientDto client) {
        this.client = client;
    }

    public String getIntent() {
        return intent;
    }

    public void setIntent(String intent) {
        this.intent = intent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostAppUserDto that = (PostAppUserDto) o;

        if (client != null ? !client.equals(that.client) : that.client != null) {
            return false;
        }
        return intent != null ? intent.equals(that.intent) : that.intent == null;
    }

    @Override
    public int hashCode() {
        int result = client != null ? client.hashCode() : 0;
        result = 31 * result + (intent != null ? intent.hashCode() : 0);
        return result;
    }
}
