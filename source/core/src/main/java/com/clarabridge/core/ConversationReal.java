package com.clarabridge.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.clarabridge.core.model.ConversationDto;
import com.clarabridge.core.model.MessageDto;
import com.clarabridge.core.model.ParticipantDto;
import com.clarabridge.core.service.ConversationObserver;
import com.clarabridge.core.utils.StringUtils;

/**
 * Real implementation of {@link Conversation} that will perform actions for the {@link ConversationDto}
 * currently loaded by the SDK
 */
class ConversationReal extends ConversationBase implements ConversationObserver {

    private final Object countChangedGuard = new Object();
    private boolean isShown;

    /***
     * The ID of the conversation that this model currently represents.
     *
     * Used to identify if the {@link #entity} was updated and the conversation changed.
     */
    private String currentConversationId;

    ConversationReal(@NonNull ConversationDto entity) {
        super(entity);
    }

    // region Delegate getters

    /**
     * The delegate set via {@link ClarabridgeChat#setMessageModifierDelegate(MessageModifierDelegate)}.
     *
     * @return an instance of {@link MessageModifierDelegate}
     */
    private MessageModifierDelegate getMessageModifierDelegate() {
        return ClarabridgeChat.getMessageModifierDelegate();
    }
    // endregion

    /**
     * {@inheritDoc}
     */
    @Override
    public void markAllAsRead() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();

        synchronized (entity.getMessages()) {
            for (MessageDto it : entity.getMessages()) {
                MessageDto.Status status = it.getStatus();

                if (status == MessageDto.Status.UNREAD
                        || status == MessageDto.Status.NOTIFICATION_SHOWN) {
                    it.setStatus(MessageDto.Status.READ);
                }
            }
        }

        if (clarabridgeChatInternal != null) {
            clarabridgeChatInternal.sync();
            clarabridgeChatInternal.updateReadState();
        }

        synchronized (countChangedGuard) {
            for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
                delegate.onUnreadCountChanged(this, 0);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendMessage(@NonNull Message message) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        MessageModifierDelegate messageModifierDelegate = getMessageModifierDelegate();

        if (message.getUploadStatus() != MessageUploadStatus.UNSENT) {
            Log.w(LOG_TAG, "Ignoring a message with upload status different from MessageUploadStatus.UNSENT");
            return;
        }

        if (messageModifierDelegate != null) {
            message = messageModifierDelegate.beforeSend(this, message);
        }

        if (clarabridgeChatInternal != null) {
            message.getEntity().setIsFromCurrentUser(true);
            entity.getMessages().add(message.getEntity());
            messages.add(message);
            clarabridgeChatInternal.sendMessage(message.getEntity());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Nullable
    @Override
    public Message retryMessage(@NonNull Message message) {
        if (message.getUploadStatus() != MessageUploadStatus.FAILED) {
            Log.w(LOG_TAG, "Tried to retry a message that did not fail.");
            return null;
        }

        removeMessage(message);

        message.getEntity().setStatus(MessageDto.Status.UNSENT);
        sendMessage(message);

        return message;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMessage(Message message) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();

        if (message.getUploadStatus() != MessageUploadStatus.UNSENT) {
            Log.w(LOG_TAG, "Ignoring a message with upload status different from MessageUploadStatus.UNSENT");
            return;
        }

        if (clarabridgeChatInternal != null) {
            message.getEntity().setIsFromCurrentUser(true);
            entity.getMessages().add(message.getEntity());
            messages.add(message);
            clarabridgeChatInternal.sync();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeMessage(Message message) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        MessageUploadStatus uploadStatus = message.getUploadStatus();

        if (uploadStatus != MessageUploadStatus.UNSENT && uploadStatus != MessageUploadStatus.FAILED) {
            Log.w(LOG_TAG, "Tried to remove a message which is already sent");
            return;
        }

        if (clarabridgeChatInternal != null) {
            entity.getMessages().remove(message.getEntity());
            messages.remove(message);
            clarabridgeChatInternal.sync();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uploadImage(@NonNull Message imageMessage, @Nullable ClarabridgeChatCallback<Message> callback) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        MessageModifierDelegate messageModifierDelegate = getMessageModifierDelegate();

        if (messageModifierDelegate != null) {
            imageMessage = messageModifierDelegate.beforeSend(this, imageMessage);
        }

        imageMessage.getEntity().setIsFromCurrentUser(true);
        imageMessage.getEntity().setStatus(MessageDto.Status.UNSENT);

        if (clarabridgeChatInternal != null) {
            clarabridgeChatInternal.uploadImage(imageMessage, ClarabridgeChat.ensureNonNull(callback));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void uploadFile(@NonNull Message fileMessage, @Nullable ClarabridgeChatCallback<Message> callback) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        MessageModifierDelegate messageModifierDelegate = getMessageModifierDelegate();

        if (messageModifierDelegate != null) {
            fileMessage = messageModifierDelegate.beforeSend(this, fileMessage);
        }

        fileMessage.getEntity().setIsFromCurrentUser(true);
        fileMessage.getEntity().setStatus(MessageDto.Status.UNSENT);

        if (clarabridgeChatInternal != null) {
            clarabridgeChatInternal.uploadFile(fileMessage, ClarabridgeChat.ensureNonNull(callback));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void startTyping() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();

        if (clarabridgeChatInternal != null) {
            clarabridgeChatInternal.startTyping();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stopTyping() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();

        if (clarabridgeChatInternal != null) {
            clarabridgeChatInternal.stopTyping();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processPayment(CreditCard creditCard, MessageAction action) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();

        if (clarabridgeChatInternal != null) {
            clarabridgeChatInternal.processPayment(creditCard, action.getEntity());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void triggerAction(MessageAction action) {
        //get the integrator delegate
        ConversationDelegate conversationDelegate = ClarabridgeChat.getConversationDelegate();

        //if the integrator delegate returns true for shouldTriggerAction we need to call all the
        //ui delegates
        if (conversationDelegate == null || conversationDelegate.shouldTriggerAction(action)) {

            //remove the integrator delegate from the list of all delegates
            Collection<ConversationDelegate> conversationDelegates = new ArrayList<>(ClarabridgeChat.getConversationDelegates());
            conversationDelegates.remove(conversationDelegate);

            for (ConversationDelegate delegate : conversationDelegates) {
                if (delegate != null) {
                    delegate.shouldTriggerAction(action);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void loadCardSummary() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();

        if (clarabridgeChatInternal != null) {
            clarabridgeChatInternal.loadCardSummary();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void postback(@NonNull MessageAction action, @Nullable ClarabridgeChatCallback<Void> callback) {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();

        if (clarabridgeChatInternal != null) {
            clarabridgeChatInternal.postback(action.getEntity(), ClarabridgeChat.ensureNonNull(callback));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clarabridgeChatShown() {
        isShown = true;
        for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
            delegate.onClarabridgeChatShown();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clarabridgeChatHidden() {
        isShown = false;
        for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
            delegate.onClarabridgeChatHidden();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isClarabridgeChatShown() {
        return isShown;
    }

    //region ConversationObserver
    @Override
    public void onMessageSent(@NonNull MessageDto messageEntity) {
        Message message = null;
        for (Message it : messages) {
            if (it.getEntity() == messageEntity) {
                message = it;
                break;
            }
        }

        if (message == null) {
            return;
        }

        for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
            delegate.onMessageSent(message, message.getUploadStatus());
        }
    }

    @Override
    public void onConversationUpdated(@Nullable String id) {
        if (!StringUtils.isEqual(entity.getId(), id)) {
            return;
        }

        List<Message> receivedMessages = new LinkedList<>();
        synchronized (entity.getMessages()) {
            for (MessageDto messageEntity : entity.getMessages()) {
                Message message = new Message(new MessageDto(messageEntity));
                receivedMessages.add(message);
            }
        }

        boolean shouldResetMessages = currentConversationId != null
                && !currentConversationId.equals(id)
                || !receivedMessages.containsAll(messages);

        currentConversationId = id;

        participants.clear();
        for (ParticipantDto it : entity.getParticipants()) {
            participants.add(new Participant(it));
        }

        if (shouldResetMessages) {
            resetMessages(receivedMessages);
        } else {
            addNewMessages(receivedMessages);
        }
    }
    //endregion

    /**
     * Adds any new messages from the entity to the {@link #messages} list held by this class
     *
     * @see ConversationDto#getMessages()
     */
    @VisibleForTesting
    void addNewMessages(List<Message> receivedMessages) {
        final LinkedList<Message> newMessages = new LinkedList<>();

        for (Message message : receivedMessages) {
            if (messages.contains(message)) {
                continue;
            }

            newMessages.add(message);
        }
        if (newMessages.isEmpty()) {
            return;
        }

        messages.addAll(newMessages);
        Collections.sort(messages);

        synchronized (countChangedGuard) {
            int unreadCount = getUnreadCount();

            for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
                delegate.onMessagesReceived(this, newMessages);
                delegate.onUnreadCountChanged(this, unreadCount);
            }
        }
    }

    /**
     * Replaces the {@link #messages} list with the messages from the entity
     *
     * @see ConversationDto#getMessages()
     */
    @VisibleForTesting
    void resetMessages(List<Message> receivedMessages) {
        messages.clear();
        messages.addAll(receivedMessages);

        synchronized (countChangedGuard) {
            int unreadCount = getUnreadCount();

            for (ConversationDelegate delegate : ClarabridgeChat.getConversationDelegates()) {
                delegate.onMessagesReset(this, getMessages());
                delegate.onUnreadCountChanged(this, unreadCount);
            }
        }
    }

    /**
     * Updates the entity that this {@link Conversation} instance represents
     *
     * @param entity the new {@link ConversationDto} entity to be represented
     */
    void updateEntity(@NonNull ConversationDto entity) {
        this.entity = entity;
        onConversationUpdated(entity.getId());
    }
}

