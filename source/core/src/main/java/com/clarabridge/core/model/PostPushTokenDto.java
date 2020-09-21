package com.clarabridge.core.model;

/**
 * A request model to update the push notification token in the backend.
 */
public class PostPushTokenDto {

    private final String pushNotificationToken;

    public PostPushTokenDto(String pushNotificationToken) {
        this.pushNotificationToken = pushNotificationToken;
    }

    public String getPushNotificationToken() {
        return pushNotificationToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PostPushTokenDto that = (PostPushTokenDto) o;

        return pushNotificationToken != null
                ? pushNotificationToken.equals(that.pushNotificationToken)
                : that.pushNotificationToken == null;
    }

    @Override
    public int hashCode() {
        return pushNotificationToken != null ? pushNotificationToken.hashCode() : 0;
    }
}
