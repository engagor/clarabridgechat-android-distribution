package com.clarabridge.core.monitor;

import com.google.gson.annotations.SerializedName;

/**
 * Model that represents a conversation received through the WebSocket and processed by the
 * {@link ConversationMonitor}.
 */
class WsConversationDto {

    @SerializedName("_id")
    private final String id;
    private final Double appMakerLastRead;

    WsConversationDto(
            String id,
            Double appMakerLastRead) {
        this.id = id;
        this.appMakerLastRead = appMakerLastRead;
    }

    String getId() {
        return id;
    }

    Double getAppMakerLastRead() {
        return appMakerLastRead;
    }
}
