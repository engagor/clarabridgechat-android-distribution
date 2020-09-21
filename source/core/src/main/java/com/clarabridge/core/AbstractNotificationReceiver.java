package com.clarabridge.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Abstract class that can be registered to receive Message notifications from FCM.
 */
public abstract class AbstractNotificationReceiver extends BroadcastReceiver {
    @Override
    public final void onReceive(final Context context, final Intent intent) {
        final Message message = (Message) intent.getSerializableExtra("message");
        final String conversationId = intent.getStringExtra("conversationId");

        if (message != null) {
            onNotification(context, conversationId, message);
        }
    }

    /**
     * Method called when a new message is received.
     *
     * @param context The BroadcastReceiver context
     * @param conversationId The conversationId of the received message
     * @param message The received message
     */
    public abstract void onNotification(Context context, String conversationId, Message message);
}

