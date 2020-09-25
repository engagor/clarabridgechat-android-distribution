package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import com.clarabridge.core.utils.JavaUtils;

public class PostAppUserDto extends AppUserDto implements Serializable {

    @SerializedName("client")
    private final ClientDto client;

    @SerializedName("intent")
    private final String intent;

    @SerializedName("conversation")
    private final PostAppUserConversationDto conversation;

    public PostAppUserDto(final AppUserDto appUser, ClientDto client, String intent,
                          PostAppUserConversationDto conversation) {
        this.client = client;
        this.intent = intent;
        this.conversation = conversation;
        if (appUser != null) {
            update(appUser);
            setExternalId(appUser.getExternalId());
        }
    }

    public ClientDto getClient() {
        return client;
    }

    public String getIntent() {
        return intent;
    }

    public PostAppUserConversationDto getConversation() {
        return conversation;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        PostAppUserDto that = (PostAppUserDto) o;
        return JavaUtils.equals(client, that.client)
                && JavaUtils.equals(intent, that.intent)
                && JavaUtils.equals(conversation, that.conversation);
    }

    @Override
    public int hashCode() {
        return JavaUtils.hash(super.hashCode(), client, intent, conversation);
    }
}
