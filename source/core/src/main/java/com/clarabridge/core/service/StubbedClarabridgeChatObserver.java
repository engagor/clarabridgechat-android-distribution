package com.clarabridge.core.service;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import com.clarabridge.core.InitializationStatus;
import com.clarabridge.core.LoginResult;
import com.clarabridge.core.LogoutResult;
import com.clarabridge.core.Message;
import com.clarabridge.core.PaymentStatus;
import com.clarabridge.core.ClarabridgeChatCallback;
import com.clarabridge.core.ClarabridgeChatConnectionStatus;
import com.clarabridge.core.model.CardSummaryDto;
import com.clarabridge.core.model.ConversationDto;
import com.clarabridge.core.model.ConversationEventDto;
import com.clarabridge.core.model.MessageActionDto;
import com.clarabridge.core.model.MessageDto;

/**
 * Stubbed implementation of {@link ClarabridgeChatObserver} that does nothing when invoked
 */
class StubbedClarabridgeChatObserver implements ClarabridgeChatObserver {

    @Override
    public void onInitializationStatusChanged(InitializationStatus status) {
        // Intentionally empty
    }

    @Override
    public void onLoginComplete(LoginResult result) {
        // Intentionally empty
    }

    @Override
    public void onLogoutComplete(LogoutResult result) {
        // Intentionally empty
    }

    @Override
    public void onConversationEventReceived(ConversationEventDto event) {
        // Intentionally empty
    }

    @Override
    public void onPaymentProcessed(MessageActionDto messageAction, PaymentStatus status) {
        // Intentionally empty
    }

    @Override
    public void onCardSummaryLoaded(CardSummaryDto stripeCard) {
        // Intentionally empty
    }

    @Override
    public void onFileUploadComplete(
            @NonNull ClarabridgeChatCallback.Response<Message> response,
            @Nullable MessageDto uploadedMessage,
            @NonNull ClarabridgeChatCallback<Message> callback) {
        // Intentionally empty
    }

    @Override
    public void onClarabridgeChatConnectionStatusChanged(ClarabridgeChatConnectionStatus status) {
        // Intentionally empty
    }

    @Override
    public void onConversationsListUpdated(@NonNull final List<ConversationDto> conversationEntities) {
        // Intentionally empty
    }
}
