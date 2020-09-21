package com.clarabridge.core;

import android.app.Application;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;

/**
 * The Conversation provides an interface to interact with a user's conversation.
 * <p>
 * To obtain the current conversation use {@link ClarabridgeChat#getConversation()},
 * or {@link ClarabridgeChat#getConversationById(String, ClarabridgeChatCallback)} to obtain other conversations that are
 * not active.
 * <p>
 * {@link ClarabridgeChat#init(Application, Settings, ClarabridgeChatCallback)} must have been called prior to retrieving
 * any conversation object.
 * <p>
 * To send a message, use {@link #sendMessage(Message)} with a {@link Message Message} object.
 * Example:
 * {@code ClarabridgeChat.getConversation().sendMessage(new Message("Hello World!"));}
 * <p>
 * A conversation must be made active with {@link ClarabridgeChat#loadConversation(String, ClarabridgeChatCallback)}
 * before executing any actions on it.
 *
 * @see ClarabridgeChat#getConversation()
 * @see ClarabridgeChat#getConversationById(String, ClarabridgeChatCallback)
 * @see ClarabridgeChat#getConversationsList(ClarabridgeChatCallback)
 * @see ClarabridgeChat#loadConversation(String, ClarabridgeChatCallback)
 */
public interface Conversation extends ConversationDetails {

    /**
     * Marks all unread messages as read.
     * <p>
     * Marks all unread messages as read, and notifies that the unread count changed.
     */
    void markAllAsRead();

    /**
     * Adds a new message to the conversation.
     * <p>
     * For each message added using sendMessage, a notification will be fired indicating the success
     * or failure status of the upload.
     * <p>
     * A conversation will be started automatically if this is the very first message and a conversation
     * hasn't been started yet.
     *
     * @param message The message to send
     */
    void sendMessage(@NonNull Message message);

    /**
     * Retries a message that failed to send.
     * <p>
     * The failed message will be removed from the message list, and a new message will be constructed
     * with the same text as the failed message. A notification will be fired indicating the success
     * or failure status of the upload.
     *
     * @param message The failed message
     * @return The sent message, or null if the message could not be sent
     */
    @Nullable
    Message retryMessage(@NonNull Message message);

    /**
     * Adds a new message to the conversation without sending it.
     *
     * @param message The message to add
     */
    void addMessage(Message message);

    /**
     * Removes a failed or unsent message from the conversation.
     *
     * @param message The message to remove
     */
    void removeMessage(Message message);

    /**
     * Uploads a user image to the conversation.
     * <p>
     * For each image upload, provided {@link com.clarabridge.core.ClarabridgeChatCallback} will be called with status
     * code, error and the resulting message
     *
     * @param imageMessage The imageMessage to upload
     * @param callback     The callback, to be called with {@link ClarabridgeChatCallback.Response} getData()
     *                     of type {@link Message}, null in case of error
     * @see Message
     * @see ClarabridgeChatCallback
     */
    void uploadImage(@NonNull Message imageMessage, @Nullable ClarabridgeChatCallback<Message> callback);

    /**
     * Uploads a user file to the conversation.
     * <p>
     * For each file upload, provided {@link com.clarabridge.core.ClarabridgeChatCallback} will be called with status
     * code, error and the resulting message
     *
     * @param fileMessage The fileMessage to upload
     * @param callback    The callback, to be called with {@link ClarabridgeChatCallback.Response} getData()
     *                    of type {@link Message}, null in case of error
     * @see Message
     * @see ClarabridgeChatCallback
     */
    void uploadFile(@NonNull Message fileMessage, @Nullable ClarabridgeChatCallback<Message> callback);

    /**
     * Notify the server that the user is typing. This method is called automatically when using the
     * default ConversationFragment. Only call this method if your application implements its own conversation view.
     * <p>
     * Typing updates are automatically throttled, so you may call this method as often as necessary.
     * The typing stop event will automatically fire 10 seconds after the most recent call to this method.
     *
     * @see #stopTyping()
     */
    void startTyping();

    /**
     * Notify the server that the user has finished typing. This method is called automatically
     * when using the default ConversationFragment. Only call this method if your application
     * implements its own conversation view.
     * <p>
     * If the user was not flagged as typing recently, this method will result in a no-op.
     *
     * @see #startTyping()
     */
    void stopTyping();

    /**
     * Processes a credit card payment.
     * <p>
     *
     * @param creditCard The credit card object
     * @param action     The message action
     * @see CreditCard
     * @see MessageAction
     */
    void processPayment(CreditCard creditCard, MessageAction action);

    /**
     * Triggers message action resulting from a click.
     * <p>
     *
     * @param action The message action
     * @see MessageAction
     */
    void triggerAction(MessageAction action);

    /**
     * Loads billing info (if exists) to display last4 in credit form.
     */
    void loadCardSummary();

    /**
     * Triggers MessageAction postback.
     *
     * @see MessageAction
     * @see ClarabridgeChatCallback
     */
    void postback(@NonNull MessageAction action, @Nullable ClarabridgeChatCallback<Void> callback);

    /**
     * Notifies the delegate when ClarabridgeChat Conversation is shown
     * <p>
     * Used by the clarabridgeChat-ui package to notify the Conversation.Delegate when ClarabridgeChat is shown
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    void clarabridgeChatShown();

    /**
     * Notifies the delegate when ClarabridgeChat Conversation is hidden
     * <p>
     * Used by the clarabridgeChat-ui package to notify the Conversation.Delegate when ClarabridgeChat is hidden
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    void clarabridgeChatHidden();

    /**
     * Returns <code>true</code> if the ClarabridgeChat UI is currently shown
     *
     * @return <code>true</code> if the ClarabridgeChat UI is currently shown, <code>false</code> otherwise
     */
    boolean isClarabridgeChatShown();
}

