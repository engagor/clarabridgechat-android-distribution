package com.clarabridge.core;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * Abstract class that implements {@link ConversationDelegate} so individual methods can be overridden.
 */
public abstract class ConversationDelegateAdapter implements ConversationDelegate {

    /**
     * @see ConversationDelegate#onMessagesReceived(Conversation, List)
     */
    @Override
    public void onMessagesReceived(@NonNull Conversation conversation, @NonNull List<Message> messages) {
        //no-op
    }

    /**
     * @see ConversationDelegate#onMessagesReset(Conversation, List)
     */
    @Override
    public void onMessagesReset(@NonNull Conversation conversation, @NonNull List<Message> messages) {
        //no-op
    }

    /**
     * @see ConversationDelegate#onUnreadCountChanged(Conversation, int)
     */
    @Override
    public void onUnreadCountChanged(@NonNull Conversation conversation, int unreadCount) {
        //no-op
    }

    /**
     * @see ConversationDelegate#onMessageSent(Message, MessageUploadStatus)
     */
    @Override
    public void onMessageSent(@NonNull Message message, @NonNull MessageUploadStatus status) {
        //no-op
    }

    /**
     * @see ConversationDelegate#onConversationEventReceived(ConversationEvent)
     */
    @Override
    public void onConversationEventReceived(@NonNull ConversationEvent conversationActivity) {
        //no-op
    }

    /**
     * @see ConversationDelegate#onInitializationStatusChanged(InitializationStatus)
     */
    @Override
    public void onInitializationStatusChanged(@NonNull InitializationStatus status) {
        //no-op
    }

    /**
     * @see ConversationDelegate#onLoginComplete(LoginResult)
     */
    @Override
    public void onLoginComplete(@NonNull LoginResult result) {
        //no-op
    }

    /**
     * @see ConversationDelegate#onLogoutComplete(LogoutResult)
     */
    @Override
    public void onLogoutComplete(@NonNull LogoutResult result) {
        //no-op
    }

    /**
     * @see ConversationDelegate#onPaymentProcessed(MessageAction, PaymentStatus)
     */
    @Override
    public void onPaymentProcessed(@NonNull MessageAction messageAction, @NonNull PaymentStatus status) {
        //no-op
    }

    /**
     * @see ConversationDelegate#shouldTriggerAction(MessageAction)
     */
    @Override
    public boolean shouldTriggerAction(@NonNull MessageAction messageAction) {
        return false;
    }

    /**
     * @see ConversationDelegate#onCardSummaryLoaded(CardSummary)
     */
    @Override
    public void onCardSummaryLoaded(@NonNull CardSummary cardSummary) {
        //no-op
    }

    /**
     * @see ConversationDelegate#onClarabridgeChatConnectionStatusChanged(ClarabridgeChatConnectionStatus)
     */
    @Override
    public void onClarabridgeChatConnectionStatusChanged(@NonNull ClarabridgeChatConnectionStatus status) {
        //no-op
    }

    /**
     * @see ConversationDelegate#onClarabridgeChatShown()
     */
    @Override
    public void onClarabridgeChatShown() {
        //no-op
    }

    /**
     * @see ConversationDelegate#onClarabridgeChatHidden()
     */
    @Override
    public void onClarabridgeChatHidden() {
        //no-op
    }

    /**
     * @see ConversationDelegate#onConversationsListUpdated(List)
     */
    @Override
    public void onConversationsListUpdated(@NonNull List<Conversation> conversationsList) {
        //no-op
    }
}
