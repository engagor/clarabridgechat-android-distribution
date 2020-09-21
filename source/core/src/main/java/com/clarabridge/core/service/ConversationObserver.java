package com.clarabridge.core.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.clarabridge.core.model.MessageDto;

public interface ConversationObserver {

    /**
     * Invoked when the SDK has received an update that affects a {@link com.clarabridge.core.model.ConversationDto}
     * such as a new message via the web socket connection or a user logging in/out.
     *
     * @param id the ID of the conversation that was updated
     */
    void onConversationUpdated(@Nullable final String id);

    /**
     * Invoked when an attempt to send a message has completed regardless of success state. The status of
     * the message can be retrieved by {@link MessageDto#getStatus()} to determine the state of the
     * message.
     *
     * @param messageEntity the {@link MessageDto} that was being sent
     */
    void onMessageSent(@NonNull final MessageDto messageEntity);

}
