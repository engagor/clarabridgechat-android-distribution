package com.clarabridge.core.monitor;

import com.google.gson.annotations.SerializedName;

/**
 * A model that represents a conversation participant received through the WebSocket and processed
 * by the {@link ConversationMonitor}.
 */
class WsParticipantDto {

    @SerializedName("appUserId")
    private final String userId;

    WsParticipantDto(String userId) {
        this.userId = userId;
    }

    String getUserId() {
        return userId;
    }
}
