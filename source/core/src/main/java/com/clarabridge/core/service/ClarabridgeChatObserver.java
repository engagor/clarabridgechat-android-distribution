package com.clarabridge.core.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

public interface ClarabridgeChatObserver {

    /**
     * Invoked when the SDK initialization state has changed from one {@link InitializationStatus} to another
     *
     * @param status the new {@link InitializationStatus}
     */
    void onInitializationStatusChanged(@NonNull final InitializationStatus status);

    /**
     * Invoked when a user login has completed with one of {@link LoginResult#SUCCESS} or {@link LoginResult#ERROR}
     * when the login API has been explicitly called by the integrator. Will not be invoked if the SDK
     * is logging in a user based on credentials stored on-device.
     *
     * @param result the {@link LoginResult} outcome of the login task
     */
    void onLoginComplete(@NonNull final LoginResult result);

    /**
     * Invoked when a user logout has completed with one of {@link LogoutResult#SUCCESS} or {@link LogoutResult#ERROR}
     *
     * @param result the {@link LogoutResult} outcome of the logout task
     */
    void onLogoutComplete(@NonNull final LogoutResult result);

    /**
     * Invoked when one of {@link com.clarabridge.core.ConversationEventType} has been received over the web socket
     * to let the SDK know that activity has taken place on a conversation.
     *
     * @param event the {@link ConversationEventDto} that occurred
     */
    void onConversationEventReceived(@NonNull final ConversationEventDto event);

    /**
     * Invoked when a Stripe payment has completed with one of {@link PaymentStatus#SUCCESS} or
     * {@link PaymentStatus#ERROR}.
     * <p>
     * Can also be invoked with {@link PaymentStatus#ERROR} when a request to retrieve or store a
     * stripe token has failed.
     *
     * @param messageAction the payment {@link MessageActionDto} being performed
     * @param status        the {@link PaymentStatus} of the request
     */
    void onPaymentProcessed(@NonNull final MessageActionDto messageAction,
                            @NonNull final PaymentStatus status);

    /**
     * Invoked when the user card details have been retrieved from the backend or we have the details
     * stored on device.
     *
     * @param stripeCard the users {@link CardSummaryDto}
     */
    void onCardSummaryLoaded(@NonNull final CardSummaryDto stripeCard);

    /**
     * Invoked when a file upload request has completed regardless of success state.
     * <p>
     * A successful state could mean that the request completed successfully, or we retrieved a response
     * via the web socket before the request response.
     * <p>
     * A failure state could mean that the request was rejected, or the file size was too large
     *
     * @param response        a {@link ClarabridgeChatCallback.Response} describing the state of the upload
     * @param uploadedMessage the {@link MessageDto} that was being sent
     * @param callback        a {@link ClarabridgeChatCallback} provided by the integrator
     */
    void onFileUploadComplete(@NonNull final ClarabridgeChatCallback.Response<Message> response,
                              @Nullable final MessageDto uploadedMessage,
                              @NonNull final ClarabridgeChatCallback<Message> callback);

    /**
     * Invoked when the {@link com.clarabridge.core.monitor.ConversationMonitor} listening to the web socket
     * has changed connection state
     *
     * @param status the {@link ClarabridgeChatConnectionStatus} of the monitor
     */
    void onClarabridgeChatConnectionStatusChanged(@NonNull final ClarabridgeChatConnectionStatus status);

    /**
     * Invoked when the persisted conversation list has been updated
     *
     * @param conversationEntities the updated list of {@link ConversationDto}
     */
    void onConversationsListUpdated(@NonNull final List<ConversationDto> conversationEntities);

}
