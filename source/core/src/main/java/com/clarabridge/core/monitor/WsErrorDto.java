package com.clarabridge.core.monitor;

/**
 * Model that represents an error received through the WebSocket and processed by the
 * {@link ConversationMonitor}.
 */
class WsErrorDto {

    private final int status;
    private final String code;

    WsErrorDto(
            int status,
            String code) {
        this.status = status;
        this.code = code;
    }

    int getStatus() {
        return status;
    }

    String getCode() {
        return code;
    }
}
