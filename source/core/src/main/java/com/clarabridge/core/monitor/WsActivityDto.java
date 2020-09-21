package com.clarabridge.core.monitor;

import android.support.annotation.Nullable;

import com.clarabridge.core.model.ConversationEventDataDto;

/**
 * Model that represents an activity received through the WebSocket and processed by the
 * {@link ConversationMonitor}.
 */
class WsActivityDto {

    private final String role;
    private final String type;
    private final ConversationEventDataDto data;
    @Nullable
    private final String appUserId;

    WsActivityDto(
            String role,
            String type,
            ConversationEventDataDto data,
            @Nullable String appUserId) {
        this.role = role;
        this.type = type;
        this.data = data;
        this.appUserId = appUserId;
    }

    String getRole() {
        return role;
    }

    String getType() {
        return type;
    }

    ConversationEventDataDto getData() {
        return data;
    }

    /***
     * @return the app user ID if this {@link WsActivityDto} was triggered by another app user,
     * null if it was triggered by the app maker
     */
    @Nullable
    public String getAppUserId() {
        return appUserId;
    }
}
