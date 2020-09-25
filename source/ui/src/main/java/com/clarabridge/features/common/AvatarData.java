package com.clarabridge.features.common;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;

import com.clarabridge.core.Config;
import com.clarabridge.core.Conversation;
import com.clarabridge.core.utils.JavaUtils;
import com.clarabridge.ui.R;

/**
 * Holds the data needed for the avatar view.
 */
public final class AvatarData {

    /**
     * Creates an avatar data instance from a conversation
     */
    public static AvatarData from(@Nullable Config config, @Nullable Conversation conversation) {

        String configIconUrl = null;
        if (config != null) {
            configIconUrl = config.getIconUrl();
        }


        String conversationIconUrl = null;
        if (conversation != null) {
            conversationIconUrl = conversation.getIconUrl();
        }

        return new AvatarData(conversationIconUrl, configIconUrl, R.drawable.clarabridgechat_single_user_image);
    }

    /**
     * the fallback drawable resource if all else fails
     */
    @DrawableRes
    public final int fallbackResource;

    /**
     * The conversation url,
     */
    public final String conversationUrl;

    /**
     * User urls avatars to be shown
     */
    @Nullable
    public final String brandUrl;

    /**
     * @param conversationUrl  User avatar url to be shown, if empty/null the brand will be shown
     * @param brandUrl         The brand avatar url, if null the fallbackResource will be shown
     * @param fallbackResource the fallback drawable resource if all else fails
     */
    private AvatarData(@Nullable String conversationUrl,
                       @Nullable String brandUrl,
                       @DrawableRes int fallbackResource) {
        this.fallbackResource = fallbackResource;
        this.brandUrl = brandUrl;
        this.conversationUrl = conversationUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AvatarData that = (AvatarData) o;
        return fallbackResource == that.fallbackResource
                && JavaUtils.equals(brandUrl, that.brandUrl)
                && JavaUtils.equals(conversationUrl, that.conversationUrl);
    }

    @Override
    public int hashCode() {
        return JavaUtils.hash(fallbackResource, brandUrl, conversationUrl);
    }

    @Override
    public String toString() {
        return "AvatarData{" +
                "fallbackResource=" + fallbackResource + '\'' +
                ", brandUrl='" + brandUrl + '\'' +
                ", conversationUrl=" + conversationUrl +
                '}';
    }
}
