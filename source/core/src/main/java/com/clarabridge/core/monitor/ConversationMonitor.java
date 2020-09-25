package com.clarabridge.core.monitor;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.saulpower.fayeclient.FayeClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;

import com.clarabridge.core.AuthenticationCallback;
import com.clarabridge.core.AuthenticationDelegate;
import com.clarabridge.core.AuthenticationError;
import com.clarabridge.core.ConversationEventType;
import com.clarabridge.core.Logger;
import com.clarabridge.core.MessageType;
import com.clarabridge.core.facade.PersistenceFacade;
import com.clarabridge.core.facade.Serialization;
import com.clarabridge.core.model.ConversationEventDto;
import com.clarabridge.core.model.MessageDto;
import com.clarabridge.core.model.SourceDto;
import com.clarabridge.core.utils.StringUtils;

/**
 * An implementation of {@link FayeClient.FayeListener} that manages the connection with the Faye
 * client and listen to its messages, pre-processing them before calling delegating the calls to
 * the {@link Delegate}.
 *
 * @see Delegate
 * @see WsMessageDto
 */
public class ConversationMonitor implements FayeClient.FayeListener {

    private static final String LOG_TAG = "ConversationMonitor";

    public interface Delegate {

        /**
         * Invoked when the {@link ConversationMonitor} connects to the server.
         */
        void onMonitorConnected();

        /**
         * Invoked when the {@link ConversationMonitor} disconnects from the server.
         */
        void onMonitorDisconnected();

        /**
         * Invoked when a {@link MessageDto} is received through the WebSocket connection.
         *
         * @param conversationId the ID of the parent conversation of the message
         * @param message        the {@link MessageDto} received
         */
        void onMessageReceived(String conversationId, MessageDto message);

        /**
         * Invoked when a file upload was rejected by the server for any reason.
         *
         * @param messageId the ID of the rejected message
         * @param status    the status code of why the message was rejected
         * @param errorCode the error code
         */
        void onMessageRejected(String messageId, int status, String errorCode);

        /**
         * Invoked when an activity happens in a conversation.
         *
         * @param event the pre-processed activity
         * @see ConversationMonitorEventType
         * @see ConversationEventType
         * @see ConversationEventDto
         */
        void onConversationActivityReceived(ConversationEventDto event);

        /**
         * Invoked when the upload of a file sent in a {@link MessageDto} is finished.
         *
         * @param message the {@link MessageDto} with the file that was uploaded
         */
        void onUploadComplete(MessageDto message);
    }

    private final Serialization serializer;
    private final String appId;
    private final String userId;
    private final String clientId;
    private final String sessionToken;
    private final FayeClient fayeClient;
    private final Delegate delegate;
    private final AuthenticationDelegate authenticationDelegate;
    private final PersistenceFacade persistenceFacade;

    private boolean connected;

    ConversationMonitor(
            Serialization serializer,
            String appId,
            String userId,
            String clientId,
            PersistenceFacade persistenceFacade,
            String sessionToken,
            String host,
            FayeClient fayeClient,
            Delegate delegate,
            AuthenticationDelegate authenticationDelegate,
            int maxConnectionAttempts,
            long retryInterval) {

        this.serializer = serializer;
        this.appId = appId;
        this.userId = userId;
        this.clientId = clientId;
        this.sessionToken = sessionToken;
        this.delegate = delegate;
        this.authenticationDelegate = authenticationDelegate;
        this.persistenceFacade = persistenceFacade;

        if (fayeClient == null) {
            // FayeClient internally expects the scheme to be "wss" to establish a secure connection
            String fayeUrl = host.startsWith("https://")
                    ? host.replaceFirst("https://", "wss://")
                    : host;

            this.fayeClient = new FayeClient(
                    new Handler(Looper.getMainLooper()),
                    URI.create(fayeUrl),
                    String.format("/sdk/apps/%s/appusers/%s", appId, userId),
                    maxConnectionAttempts,
                    retryInterval);
        } else {
            this.fayeClient = fayeClient;
        }

        this.fayeClient.setFayeListener(this);
    }

    @Override
    public void connectedToServer() {
        Logger.d(LOG_TAG, "Connected to server");

        connected = true;

        if (delegate != null) {
            delegate.onMonitorConnected();
        }
    }

    @Override
    public void disconnectedFromServer() {
        Logger.d(LOG_TAG, "Disconnected from server");

        connected = false;

        if (delegate != null) {
            delegate.onMonitorDisconnected();
        }
    }

    @Override
    public void onAuthenticationError(AuthenticationError authenticationError) {
        Logger.d(LOG_TAG, "onAuthenticationError: " + authenticationError);
        connected = false;
        authenticationDelegate.onInvalidAuth(authenticationError, new AuthenticationCallback() {
            @Override
            public void updateToken(@NonNull String jwt) {
                persistenceFacade.saveJwt(jwt);
                fayeClient.resetWebSocketConnection();
            }
        });
    }

    @Override
    public void subscribedToChannel(String s) {
        Logger.d(LOG_TAG, "Subscribed to channel: " + s);
    }

    @Override
    public void subscriptionFailedWithError(String s) {
        Logger.d(LOG_TAG, "Subscription failed with error: " + s);
    }

    @Override
    public void messageReceived(JSONObject jsonObject) {
        Logger.d(LOG_TAG, "Message received: " + jsonObject.toString());

        JSONArray events;
        try {
            events = jsonObject.getJSONArray("events");

            for (int i = 0; i < events.length(); i++) {
                JSONObject event;
                try {
                    event = events.getJSONObject(i);
                    processEvent(event);
                } catch (JSONException exception) {
                    Logger.e(LOG_TAG, "Unable to process event.", exception);
                }
            }
        } catch (JSONException exception) {
            Logger.e(LOG_TAG, "Unable to process events: " + jsonObject.toString(), exception);
        }
    }

    /**
     * Entry point to process any event received from Faye.
     *
     * @param event the {@link JSONObject} representation of the event received
     */
    private void processEvent(JSONObject event) {
        WsMessageDto message = serializer.deserialize(event.toString(), WsMessageDto.class);

        if (message == null) {
            return;
        }

        ConversationMonitorEventType type = ConversationMonitorEventType.findByValue(message.getType());

        if (type == null) {
            return;
        }

        String conversationId = message.getConversation().getId();

        switch (type) {
            case MESSAGE:
                processMessageEvent(conversationId, message.getMessage());
                break;
            case ACTIVITY:
                processActivityEvent(
                        conversationId,
                        message.getActivity(),
                        message.getConversation().getBusinessLastRead());
                break;
            case FAILED_UPLOAD:
                processRejectionEvent(message.getClient(), message.getData(), message.getError());
                break;
            case CONVERSATION_ADDED:
            case CONVERSATION_REMOVED:
                processConversationEvent(conversationId, message.getType());
            case PARTICIPANT_ADDED:
            case PARTICIPANT_REMOVED:
                processParticipantEvent(conversationId, message.getType(), message.getParticipant());
                break;
            default:
                // Intentionally empty
        }
    }

    /**
     * Pre-processes {@link ConversationMonitorEventType#MESSAGE} events before invoking the
     * {@link Delegate}.
     *
     * @param conversationId the ID of the parent conversation
     * @param message        the {@link MessageDto} received through the WebSocket
     */
    private void processMessageEvent(String conversationId, MessageDto message) {
        if (delegate != null && message != null) {
            SourceDto source = message.getSource();
            String messageType = message.getType();

            if (source != null && clientId.equals(source.getId())
                    && (MessageType.FILE.getValue().equals(messageType)
                    || MessageType.IMAGE.getValue().equals(messageType))) {
                delegate.onUploadComplete(message);
            } else {
                delegate.onMessageReceived(conversationId, message);
            }
        }
    }

    /**
     * Pre-processes {@link ConversationMonitorEventType#FAILED_UPLOAD} events before invoking the
     * {@link Delegate}.
     *
     * @param client the {@link WsClientDto} related to the event
     * @param data   the {@link WsErrorDataDto} received
     * @param error  the {@link WsErrorDto} received
     */
    private void processRejectionEvent(
            @Nullable WsClientDto client,
            @Nullable WsErrorDataDto data,
            @Nullable WsErrorDto error) {

        if (delegate == null || client == null || data == null || error == null) {
            return;
        }

        if (client.getId().equals(clientId)) {
            delegate.onMessageRejected(data.getMessageId(), error.getStatus(), error.getCode());
        }
    }

    /**
     * Pre-processes {@link ConversationMonitorEventType#ACTIVITY} events before invoking the
     * {@link Delegate}.
     *
     * @param conversationId   the ID of the parent conversation
     * @param activity         the {@link WsActivityDto} received through the WebSocket
     * @param businessLastRead the timestamp received if the activity is a
     *                         {@link ConversationEventType#CONVERSATION_READ}
     */
    private void processActivityEvent(
            String conversationId,
            WsActivityDto activity,
            Double businessLastRead) {

        if (delegate != null && activity != null) {
            ConversationEventType type = ConversationEventType.findByValue(activity.getType());

            if (type == null) {
                return;
            }

            ConversationEventDto conversationActivity = new ConversationEventDto(conversationId, type);

            conversationActivity.setRole(activity.getRole());
            conversationActivity.setUserId(activity.getUserId());

            if (activity.getData() != null) {
                conversationActivity.setName(activity.getData().getName());
                conversationActivity.setAvatarUrl(activity.getData().getAvatarUrl());
                conversationActivity.setLastRead(activity.getData().getLastRead());
            }

            if (ConversationEventRole.BUSINESS.getValue().equals(activity.getRole())
                    && businessLastRead != null) {
                conversationActivity.setLastRead(businessLastRead);
            }

            delegate.onConversationActivityReceived(conversationActivity);
        }
    }

    /**
     * Pre-processes {@link ConversationMonitorEventType#CONVERSATION_ADDED} and
     * {@link ConversationMonitorEventType#CONVERSATION_REMOVED} events before invoking the
     * {@link Delegate}.
     *
     * @param conversationId the ID of the parent conversation
     * @param activityType   the string value of the {@link ConversationMonitorEventType}
     */
    private void processConversationEvent(
            String conversationId,
            String activityType) {

        if (delegate != null) {
            ConversationEventType type = ConversationEventType.findByValue(activityType);

            if (type == null) {
                return;
            }

            ConversationEventDto activity = new ConversationEventDto(conversationId, type);

            delegate.onConversationActivityReceived(activity);
        }
    }

    /**
     * Pre-processes {@link ConversationMonitorEventType#PARTICIPANT_ADDED} and
     * {@link ConversationMonitorEventType#PARTICIPANT_REMOVED} events before invoking the
     * {@link Delegate}.
     *
     * @param conversationId the ID of the parent conversation
     * @param activityType   the string value of the {@link ConversationMonitorEventType}
     * @param participant    the {@link WsParticipantDto} related to the event
     */
    private void processParticipantEvent(
            String conversationId,
            String activityType,
            WsParticipantDto participant) {

        if (delegate != null && participant != null) {
            ConversationEventType type = ConversationEventType.findByValue(activityType);

            if (type == null) {
                return;
            }

            ConversationEventDto activity = new ConversationEventDto(conversationId, type);
            activity.setUserId(participant.getUserId());

            delegate.onConversationActivityReceived(activity);
        }
    }

    /**
     * @return a boolean value that indicates the connection status of the monitor
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * @return the app ID used to create this instance of {@link ConversationMonitor}
     */
    public String getAppId() {
        return appId;
    }

    /**
     * Create a connection with the WebSocket server if this monitor was not already connected.
     */
    public void resume() {
        if (!connected) {
            JSONObject args = new JSONObject();

            try {
                args.put("appId", appId);
                args.put("appUserId", userId);
                String jwt = persistenceFacade.getJwt();
                if (!StringUtils.isEmpty(jwt)) {
                    args.put("jwt", jwt);
                } else if (!StringUtils.isEmpty(sessionToken)) {
                    args.put("sessionToken", sessionToken);
                }
            } catch (JSONException ex) {
                Logger.e(LOG_TAG, "Error while resuming the ConversationMonitor", ex);
            }

            fayeClient.connectToServer(args);
        }
    }

    /**
     * Disconnects from the WebSocket server.
     */
    public void pause() {
        fayeClient.disconnectFromServer();
        connected = false;
    }

    /**
     * Disconnects from the WebSocket server and closes the connection.
     */
    public void close() {
        fayeClient.disconnectFromServer();
        fayeClient.closeWebSocketConnection();
        connected = false;
    }

    /**
     * Reset the connection with the WebSocket server.
     */
    public void reset() {
        if (!connected) {
            return;
        }

        fayeClient.resetWebSocketConnection();
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
