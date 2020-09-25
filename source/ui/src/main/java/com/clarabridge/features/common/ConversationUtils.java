package com.clarabridge.features.common;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import androidx.annotation.Nullable;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.clarabridge.core.Config;
import com.clarabridge.core.Conversation;
import com.clarabridge.core.Message;
import com.clarabridge.core.MessageType;
import com.clarabridge.core.utils.StringUtils;
import com.clarabridge.ui.R;

/**
 * Helper class for handling some logic related to {@link Conversation}
 * - Getting the conversation title
 * - Getting the conversation subtitle
 * - Getting the last message of the conversation
 */
public final class ConversationUtils {

    private ConversationUtils() {
    }

    public static String getTitle(@Nullable Conversation conversation, @Nullable Config config) {
        String appName = getAppName(config);
        if (conversation == null) {
            return appName;
        }
        return conversation.getDisplayName() != null ? conversation.getDisplayName() : appName;
    }

    private static String getAppName(@Nullable Config config) {
        return config != null ? config.getAppName() : StringUtils.emptyIfNull(null);
    }

    public static String getSubTitle(Conversation conversation) {
        // return conversation name if set
        if (conversation.getDescription() != null) {
            return conversation.getDescription();
        }
        return null;
    }

    public static RequestBuilder<Drawable> addAvatarDataToGlide(AvatarData avatarData, RequestManager requestManager) {
        if (avatarData.conversationUrl != null) {
            //show avatar urls
            return requestManager
                    .asDrawable()
                    .load(avatarData.conversationUrl)
                    .placeholder(avatarData.fallbackResource)
                    .error(avatarData.fallbackResource)
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade());
        } else if (avatarData.brandUrl != null) {
            //show brand url
            return requestManager
                    .asDrawable()
                    .load(avatarData.brandUrl)
                    .placeholder(avatarData.fallbackResource)
                    .error(avatarData.fallbackResource)
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade());
        } else {
            //show default resource
            return requestManager
                    .asDrawable()
                    .load(avatarData.fallbackResource)
                    .placeholder(avatarData.fallbackResource)
                    .circleCrop()
                    .transition(DrawableTransitionOptions.withCrossFade());
        }
    }

    public static String getLastMessage(Conversation conversation, Resources resources) {
        // the string that holds even "You" in case of the last message creator is the current user
        // or the display name of the participant that creates the last message.
        String author = "";

        List<Message> messages = new ArrayList<>(conversation.getMessages());
        Collections.sort(messages, new Comparator<Message>() {
            @Override
            public int compare(Message lhs, Message rhs) {
                if (lhs == null) {
                    return -1;
                }
                return lhs.compareTo(rhs) * -1;
            }
        });

        // check if there is no messages in the conversation, then ... will be returned
        if (messages.isEmpty()) {
            return resources.getString(R.string.ClarabridgeChat_conversationListLastNoMessages);
        }

        Message message = messages.get(0);
        // check if the received message is sent from the current user or from a participant
        if (message.isFromCurrentUser()) {
            author = resources.getString(R.string.ClarabridgeChat_conversationListLastSentByCurrentUser);
        } else if (message.getName() != null && !StringUtils.isEmpty(message.getName())) {
            author = message.getName();
        }

        String lastMessage;
        MessageType messageType = MessageType.findByValue(message.getType());

        //noinspection ConstantConditions
        switch (messageType) {
            // Example for current user -> You: Merrow Team Message
            // Example for participant  -> Participant Name: Merrow Team Message
            case TEXT:
                if (!StringUtils.isEmpty(author)) {
                    author += ": ";
                }
                lastMessage = author + message.getText();
                break;
            // Example for current user -> You sent an image
            // Example for participant  -> Participant sent an image
            case IMAGE:
                lastMessage = resources.getString(R.string.ClarabridgeChat_conversationListLastMessageImage, author);
                break;
            // Example for current user -> You sent a file
            // Example for participant  -> Participant sent file
            case FILE:
                lastMessage = resources.getString(R.string.ClarabridgeChat_conversationListLastMessageFile, author);
                break;
            // Example for current user -> You sent a form
            // Example for participant  -> Participant sent a form
            case FORM:
                lastMessage = resources.getString(R.string.ClarabridgeChat_conversationListLastMessageForm, author);
                break;
            default:
                // For unhandled MessageType
                // For LOCATION, LIST and CAROUSEL Message Types
                // Example for current user -> You sent a message
                // Example for participant  -> Participant sent a message
                lastMessage = resources.getString(R.string.ClarabridgeChat_conversationListLastMessageDefault, author);
        }

        return lastMessage;

    }

}
