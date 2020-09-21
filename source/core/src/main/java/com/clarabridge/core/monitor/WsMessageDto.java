package com.clarabridge.core.monitor;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import com.clarabridge.core.model.MessageDto;

/**
 * Model that represents a message received through the WebSocket and processed by the
 * {@link ConversationMonitor}.
 * <p>
 * This model contains properties to encapsulate any type of message received, their availability
 * is subject to the message type.
 *
 * @see ConversationMonitorEventType
 */
class WsMessageDto {

    @NonNull
    private final String type;
    @NonNull
    private final WsConversationDto conversation;
    @Nullable
    private final MessageDto message;
    @Nullable
    private final WsActivityDto activity;
    @Nullable
    private final WsParticipantDto participant;
    @Nullable
    private final WsClientDto client;
    @Nullable
    @SerializedName("err")
    private final WsErrorDto error;
    @Nullable
    private final WsErrorDataDto data;

    WsMessageDto(
            @NonNull String type,
            @NonNull WsConversationDto conversation,
            @Nullable MessageDto message,
            @Nullable WsActivityDto activity,
            @Nullable WsParticipantDto participant,
            @Nullable WsClientDto client,
            @Nullable WsErrorDto error,
            @Nullable WsErrorDataDto data) {
        this.type = type;
        this.conversation = conversation;
        this.message = message;
        this.activity = activity;
        this.client = client;
        this.participant = participant;
        this.error = error;
        this.data = data;
    }

    /**
     * @return this message type. See {@link ConversationMonitorEventType}
     */
    @NonNull
    String getType() {
        return type;
    }

    /**
     * @return a model that contain the conversation id of this {@link WsMessageDto}
     */
    @NonNull
    WsConversationDto getConversation() {
        return conversation;
    }

    /**
     * @return a {@link MessageDto} if this {@link WsMessageDto} type is
     * {@link ConversationMonitorEventType#MESSAGE}, null otherwise
     */
    @Nullable
    MessageDto getMessage() {
        return message;
    }

    /**
     * @return a {@link WsActivityDto} if this {@link WsMessageDto} type is
     * {@link ConversationMonitorEventType#ACTIVITY}, null otherwise
     */
    @Nullable
    WsActivityDto getActivity() {
        return activity;
    }

    /**
     * @return a {@link WsParticipantDto} if this {@link WsMessageDto} type is either
     * {@link ConversationMonitorEventType#PARTICIPANT_ADDED} or
     * {@link ConversationMonitorEventType#PARTICIPANT_REMOVED}, null otherwise
     */
    @Nullable
    WsParticipantDto getParticipant() {
        return participant;
    }

    /**
     * @return a {@link WsClientDto} if this {@link WsMessageDto} type is
     * {@link ConversationMonitorEventType#FAILED_UPLOAD}, null otherwise
     */
    @Nullable
    WsClientDto getClient() {
        return client;
    }

    /**
     * @return a {@link WsErrorDto} if this {@link WsMessageDto} type is
     * {@link ConversationMonitorEventType#FAILED_UPLOAD}, null otherwise
     */
    @Nullable
    WsErrorDto getError() {
        return error;
    }

    /**
     * @return a {@link WsErrorDataDto} if this {@link WsMessageDto} type is
     * {@link ConversationMonitorEventType#FAILED_UPLOAD}, null otherwise
     */
    @Nullable
    WsErrorDataDto getData() {
        return data;
    }
}
