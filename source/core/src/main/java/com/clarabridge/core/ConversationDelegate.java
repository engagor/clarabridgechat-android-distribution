package com.clarabridge.core;

import android.support.annotation.NonNull;

import java.util.List;

/**
 * Delegate for events related to the conversation.
 * <p>
 * Creating a delegate is optional, and may be used to receive callbacks when important
 * changes happen in the conversation.
 * <p>
 * Callbacks are guaranteed to be called from the main thread.
 */
public interface ConversationDelegate {

    /*
     * Identifies a delegate as belonging to the integrator
     */
    int INTEGRATOR_DELEGATE = 0;

    /*
     * Identifies a delegate as belonging to the SDK
     */
    int SDK_UI_DELEGATE = 1;

    /**
     * Notifies the delegate of new incoming messages.
     * <p>
     * Called when new messages are received from the server.
     *
     * @param conversation The conversation object.
     * @param messages     A list of Message objects representing the new messages.
     */
    void onMessagesReceived(@NonNull Conversation conversation, @NonNull List<Message> messages);

    /**
     * Notifies the delegate the messages in a conversation have been reset.
     * <p>
     * Called when new messages are received from the server that do not match locally stored cache
     * or if a different conversation was loaded.
     *
     * @param conversation The conversation object.
     * @param messages     A list of Message objects representing the new messages.
     */
    void onMessagesReset(@NonNull Conversation conversation, @NonNull List<Message> messages);

    /**
     * Notifies the delegate of a change in unread message count.
     * <p>
     * Called when conversation data is fetched from the server, or when the user enters the conversation activity.
     *
     * @param conversation The conversation object
     * @param unreadCount  The count of unread messages
     */
    void onUnreadCountChanged(@NonNull Conversation conversation, int unreadCount);

    /**
     * Notifies the delegate of a message being sent.
     * <p>
     * Called once the message has been sent successfully or not.
     *
     * @param message The message that was sent
     * @param status  The status of the message that was sent
     * @see MessageUploadStatus
     */
    void onMessageSent(@NonNull Message message, @NonNull MessageUploadStatus status);

    /**
     * Notifies the delegate of a new activity.
     * <p>
     * Called when a new activity is received from the server
     *
     * @param conversationActivity The conversationActivity that was received
     * @see ConversationEventType
     */
    void onConversationEventReceived(@NonNull ConversationEvent conversationActivity);

    /**
     * Notifies the delegate of a change in Initialization status.
     * <p>
     *
     * @param status The new status
     * @see InitializationStatus
     */
    void onInitializationStatusChanged(@NonNull InitializationStatus status);

    /**
     * Notifies the delegate when a call to {@link ClarabridgeChat#login(String, String, ClarabridgeChatCallback)} is complete
     *
     * @param result The login result
     * @see LoginResult
     */
    void onLoginComplete(@NonNull LoginResult result);

    /**
     * Notifies the delegate when a call to {@link ClarabridgeChat#logout(ClarabridgeChatCallback)} is complete
     *
     * @param result The logout result
     * @see LogoutResult
     */
    void onLogoutComplete(@NonNull LogoutResult result);

    /**
     * Notifies the delegate when payment is processed.
     * <p>
     *
     * @param messageAction The purchased message action
     * @param status        The status of the payment
     * @see MessageAction
     * @see PaymentStatus
     */
    void onPaymentProcessed(@NonNull MessageAction messageAction, @NonNull PaymentStatus status);

    /**
     * Allows a delegate to override default action behavior.
     * <p>
     * Called when the user taps a message action.
     *
     * @param messageAction The message action that was tapped.
     * @return false to prevent default behaviour of message action click, true otherwise
     * @see MessageAction
     */
    boolean shouldTriggerAction(@NonNull MessageAction messageAction);

    /**
     * Notifies the delegate when a saved stripe card is loaded.
     * <p>
     * Called when the stripe card info is loaded.
     *
     * @param cardSummary The stripe card information.
     * @see CardSummary
     */
    void onCardSummaryLoaded(@NonNull CardSummary cardSummary);

    /**
     * Notifies the delegate when the user's connection to ClarabridgeChat has changed.
     * <p>
     * Called when the user's connection to ClarabridgeChat changes.
     *
     * @param status The connection status.
     * @see ClarabridgeChatConnectionStatus
     */
    void onClarabridgeChatConnectionStatusChanged(@NonNull ClarabridgeChatConnectionStatus status);

    /**
     * Notifies the delegate when the ClarabridgeChat Conversation is shown.
     */
    void onClarabridgeChatShown();

    /**
     * Notifies the delegate when the ClarabridgeChat Conversation is hidden.
     */
    void onClarabridgeChatHidden();

    /**
     * Notifies the delegate when the conversations list was updated
     *
     * @param conversationsList the updated list of {@link Conversation}
     */
    void onConversationsListUpdated(@NonNull final List<Conversation> conversationsList);
}
