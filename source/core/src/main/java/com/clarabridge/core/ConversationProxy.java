package com.clarabridge.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.clarabridge.core.model.ConversationDto;
import com.clarabridge.core.utils.StringUtils;

/**
 * Proxy implementation of {@link Conversation} that will call through to the real implementation
 * if the entity is loaded by the SDK, or else log a warning and no-op calls to perform actions
 */
final class ConversationProxy extends ConversationBase {

    ConversationProxy(@NonNull ConversationDto entity) {
        super(entity);
    }

    /**
     * Check if the entity represented by this {@link ConversationProxy} matches the entity loaded
     * by the SDK
     *
     * @return true if the entity matches, false otherwise
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isCurrentConversation() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null) {
            Logger.e(LOG_TAG, "ClarabridgeChat has not been initialized");
            return false;
        }

        if (clarabridgeChatInternal.getConversation() == null
                || StringUtils.isNotNullAndEqual(clarabridgeChatInternal.getConversationId(), entity.getId())) {
            return true;
        }
        Logger.w(LOG_TAG, "This is not the currently active conversation. Use ClarabridgeChat#loadConversation first");
        return false;
    }

    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#markAllAsRead()
     */
    @Override
    public final void markAllAsRead() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().markAllAsRead();
    }

    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#sendMessage(Message)
     */
    @Override
    public void sendMessage(@NonNull Message message) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().sendMessage(message);
    }


    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#retryMessage(Message)
     */
    @Nullable
    @Override
    public Message retryMessage(@NonNull Message message) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return null;
        }
        return clarabridgeChatInternal.getConversation().retryMessage(message);
    }


    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#addMessage(Message)
     */
    @Override
    public void addMessage(Message message) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().addMessage(message);
    }


    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#removeMessage(Message)
     */
    @Override
    public void removeMessage(Message message) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().removeMessage(message);
    }


    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#uploadImage(Message, ClarabridgeChatCallback)
     */
    @Override
    public void uploadImage(@NonNull Message imageMessage, @Nullable ClarabridgeChatCallback<Message> callback) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().uploadImage(imageMessage, callback);
    }


    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#uploadFile(Message, ClarabridgeChatCallback)
     */
    @Override
    public void uploadFile(@NonNull Message fileMessage, @Nullable ClarabridgeChatCallback<Message> callback) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().uploadFile(fileMessage, callback);
    }


    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#startTyping()
     */
    @Override
    public void startTyping() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().startTyping();
    }

    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#stopTyping()
     */
    @Override
    public void stopTyping() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().stopTyping();
    }

    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#processPayment(CreditCard, MessageAction)
     */
    @Override
    public void processPayment(CreditCard creditCard, MessageAction action) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().processPayment(creditCard, action);
    }

    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#triggerAction(MessageAction)
     */
    @Override
    public void triggerAction(MessageAction action) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().triggerAction(action);
    }

    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#loadCardSummary()
     */
    @Override
    public void loadCardSummary() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().loadCardSummary();
    }

    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#postback(MessageAction, ClarabridgeChatCallback)
     */
    @Override
    public void postback(@NonNull MessageAction action, @Nullable ClarabridgeChatCallback<Void> callback) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().postback(action, callback);
    }

    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#clarabridgeChatShown()
     */
    @Override
    public void clarabridgeChatShown() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().clarabridgeChatShown();
    }

    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#clarabridgeChatHidden()
     */
    @Override
    public void clarabridgeChatHidden() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return;
        }
        clarabridgeChatInternal.getConversation().clarabridgeChatHidden();
    }

    /**
     * Invokes the real conversation if this proxy represents the current conversation
     *
     * @see ConversationReal#isClarabridgeChatShown()
     */
    @Override
    public boolean isClarabridgeChatShown() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal == null || !isCurrentConversation()) {
            return false;
        }
        return clarabridgeChatInternal.getConversation().isClarabridgeChatShown();
    }
}
