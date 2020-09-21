package com.clarabridge.core.monitor;

/**
 * Model that represents a client received through the WebSocket and processed by the
 * {@link ConversationMonitor}.
 */
class WsClientDto {

    private final String id;

    WsClientDto(String id) {
        this.id = id;
    }

    String getId() {
        return id;
    }
}
