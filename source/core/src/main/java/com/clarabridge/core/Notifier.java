package com.clarabridge.core;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import androidx.annotation.RestrictTo;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import java.util.LinkedList;

import com.clarabridge.core.model.MessageDto;
import com.clarabridge.core.utils.StringUtils;

/**
 * Static utility class to broadcast notifications to the notification receiver.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
public final class Notifier {
    private static final String TAG = Notifier.class.getName();

    private static final int FILTER_CAPACITY = 25;
    private static final LinkedList<String> filter = new LinkedList<>();
    private static final String INTENT_ACTION = "com.clarabridge.NOTIFICATION";
    private static final String NOTIFICATION_RECEIVER_CLASS_NAME = "com.clarabridge.ui.notification.NotificationReceiver";

    private Notifier() {
    }

    public static void notify(final Context context, final String conversationId, final MessageDto entity) {
        if (entity != null && !StringUtils.isEmpty(entity.getId())) {
            synchronized (filter) {
                final String id = entity.getId();

                if (!filter.contains(id)) {
                    final Message message = new Message(entity);
                    final Intent intent = new Intent(INTENT_ACTION);

                    intent.putExtra("message", message);
                    intent.putExtra("conversationId", conversationId);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        broadcastLocalIntent(context, intent);
                    } else {
                        broadcastGlobalIntent(context, intent);
                    }

                    filter.addFirst(id);

                    if (filter.size() > FILTER_CAPACITY) {
                        filter.removeLast();
                    }
                }
            }
        }
    }

    private static void broadcastLocalIntent(Context context, Intent intent) {
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context);

        AbstractNotificationReceiver internalReceiver = null;

        try {
            internalReceiver =
                    (AbstractNotificationReceiver) Class.forName(NOTIFICATION_RECEIVER_CLASS_NAME).newInstance();
            localBroadcastManager.registerReceiver(internalReceiver, new IntentFilter(INTENT_ACTION));
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Error registering receiver!", e);
            }
        }

        localBroadcastManager.sendBroadcastSync(intent);

        if (internalReceiver != null) {
            localBroadcastManager.unregisterReceiver(internalReceiver);
        }
    }

    private static void broadcastGlobalIntent(Context context, Intent intent) {
        context.sendBroadcast(intent);
    }
}
