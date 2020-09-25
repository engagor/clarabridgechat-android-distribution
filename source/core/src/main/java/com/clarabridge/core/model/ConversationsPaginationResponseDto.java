package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

/**
 * Describes the pagination of the conversation list
 */
public class ConversationsPaginationResponseDto {

    @SerializedName("hasMore")
    private boolean hasMore;

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
}
