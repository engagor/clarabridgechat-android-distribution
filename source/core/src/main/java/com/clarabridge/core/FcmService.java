package com.clarabridge.core;

import android.content.Context;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import com.clarabridge.core.facade.Serialization;
import com.clarabridge.core.facade.impl.LocalGsonSerializer;
import com.clarabridge.core.model.MessageDto;
import com.clarabridge.core.utils.StringUtils;

/**
 * Firebase Cloud Messaging Service
 * <p>
 * Service used to receive the push notification message from FCM.
 * <p>
 * This service is automatically registered when using the AAR library.
 */
public class FcmService extends FirebaseMessagingService {

    private static final String LOG_TAG = "FcmService";
    private static final Serialization serializer = new LocalGsonSerializer();

    public FcmService() {
    }

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        final Settings settings = ClarabridgeChat.getSettings();

        if (settings != null && settings.isFirebaseCloudMessagingAutoRegistrationEnabled()) {
            String projectId = ClarabridgeChat.getFirebaseCloudMessagingProjectId();

            if (!StringUtils.isEmpty(projectId)) {
                ClarabridgeChat.setFirebaseCloudMessagingToken(token);
            }
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage message) {
        triggerClarabridgeChatNotification(message.getData(), this);
    }

    /**
     * Helper function that handles a push notification received from ClarabridgeChat.
     *
     * @param data    the data received in a RemoteMessage
     * @param context the context where the message was received
     */
    public static void triggerClarabridgeChatNotification(final Map<String, String> data, final Context context) {
        final String clarabridgeChatNotification = data.get("smoochNotification");
        if (!StringUtils.isEqual(clarabridgeChatNotification, "true")) {
            return;
        }

        final String payload = data.get("message");

        if (BuildConfig.DEBUG) {
            Log.d(LOG_TAG, String.format("Message received: %s", payload));
        }

        final ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();
        if (clarabridgeChatInternal != null) {
            clarabridgeChatInternal.fcmMessageReceived();
        }

        final MessageDto entity = serializer.deserialize(payload, MessageDto.class);
        Notifier.notify(context, data.get("conversationId"), entity);
    }
}
