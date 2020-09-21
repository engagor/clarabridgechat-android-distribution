package com.clarabridge.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Delegate for events related to sending and displaying of specific messages.
 * <p>
 * Creating a delegate is optional, and can be used to implement callbacks when messages are sent
 * or received. Setting this delegate can have large consequences on the way messages are
 * displayed to the user. When setting <code>MessageModifierDelegate</code>, all delegate methods
 * must be implemented as per their documentation.
 */
public interface MessageModifierDelegate {

    /**
     * Allows a delegate to modify the contents of a message before sending to ClarabridgeChat servers.
     * <p>
     * You should always return a message here. Returning null will result in a {@link NullPointerException}.
     * <p>
     * When the message type is <code>file</code> or <code>image</code>, only the message
     * <code>metadata</code> may be updated. Other message properties such as <code>type</code>
     * or <code>text</code> won't be considered.
     *
     * @param conversationDetails details of the conversation containing the message being sent.
     * @param message             the message being sent.
     * @return the message, modified as required.
     * @see Message
     */
    @NonNull
    Message beforeSend(@NonNull ConversationDetails conversationDetails, @NonNull Message message);

    /**
     * Allows a delegate to modify the contents of a message before it's displayed in the conversation view.
     * <p>
     *
     * @param conversationDetails details of the conversation containing the message being displayed.
     * @param message             the message being displayed.
     * @return the message, modified as required or <code>null</code> to hide the message.
     * @see Message
     */
    @Nullable
    Message beforeDisplay(@NonNull ConversationDetails conversationDetails, @NonNull Message message);

    /**
     * Allows a delegate to modify the contents of a message before it's displayed in a notification.
     * <p>
     *
     * @param conversationId the ID of the conversation the notification belongs to.
     * @param message        the message being displayed in the notification.
     * @return the message, modified as required or <code>null</code> prevent the notification.
     * @see Message
     */
    @Nullable
    Message beforeNotification(@NonNull String conversationId, @NonNull Message message);
}
