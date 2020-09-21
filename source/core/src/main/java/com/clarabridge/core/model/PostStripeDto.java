package com.clarabridge.core.model;

import android.support.annotation.Nullable;

/**
 * A request model to be sent when making Stripe related requests, like charging or storing a token.
 */
public class PostStripeDto {

    @Nullable
    private final String actionId;
    @Nullable
    private final String token;

    public PostStripeDto(
            @Nullable String actionId,
            @Nullable String token) {
        this.actionId = actionId;
        this.token = token;
    }

    @Nullable
    public String getActionId() {
        return actionId;
    }

    @Nullable
    public String getToken() {
        return token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostStripeDto that = (PostStripeDto) o;

        if (actionId != null ? !actionId.equals(that.actionId) : that.actionId != null) {
            return false;
        }
        return token != null ? token.equals(that.token) : that.token == null;
    }

    @Override
    public int hashCode() {
        int result = actionId != null ? actionId.hashCode() : 0;
        result = 31 * result + (token != null ? token.hashCode() : 0);
        return result;
    }
}
