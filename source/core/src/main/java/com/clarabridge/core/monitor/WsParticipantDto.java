package com.clarabridge.core.monitor;

import com.google.gson.annotations.SerializedName;

/**
 * A model that represents a conversation participant received through the WebSocket and processed
 * by the {@link ConversationMonitor}.
 */
class WsParticipantDto {

    @SerializedName("appUserId")
    private final String appUserId;

    WsParticipantDto(String appUserId) {
        this.appUserId = appUserId;
    }

    String getAppUserId() {
        return appUserId;
    }
}
