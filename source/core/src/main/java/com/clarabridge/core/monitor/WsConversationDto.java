package com.clarabridge.core.monitor;

import com.google.gson.annotations.SerializedName;

/**
 * Model that represents a conversation received through the WebSocket and processed by the
 * {@link ConversationMonitor}.
 */
class WsConversationDto {

    @SerializedName("_id")
    private final String id;
    @SerializedName("appMakerLastRead")
    private final Double businessLastRead;

    WsConversationDto(
            String id,
            Double businessLastRead) {
        this.id = id;
        this.businessLastRead = businessLastRead;
    }

    String getId() {
        return id;
    }

    Double getBusinessLastRead() {
        return businessLastRead;
    }
}
