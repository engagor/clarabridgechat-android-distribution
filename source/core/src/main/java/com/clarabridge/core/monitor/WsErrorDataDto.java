package com.clarabridge.core.monitor;

/**
 * Model that represents an error data received through the WebSocket and processed by the
 * {@link ConversationMonitor}.
 */
class WsErrorDataDto {

    private final String messageId;

    WsErrorDataDto(String messageId) {
        this.messageId = messageId;
    }

    String getMessageId() {
        return messageId;
    }
}
