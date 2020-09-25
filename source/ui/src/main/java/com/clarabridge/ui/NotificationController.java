package com.clarabridge.ui;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import androidx.core.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import com.clarabridge.core.Message;
import com.clarabridge.core.MessageAction;
import com.clarabridge.ui.fragment.ConversationFragment;
import com.clarabridge.ui.utils.ApplicationInfo;
import com.clarabridge.ui.utils.BitmapVisitor;

public class NotificationController {
    private static String TAG = "NotificationController";

    private static final Handler handler;
    private static final HandlerThread handlerThread = new HandlerThread("NotificationControllerThread");

    static {
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    static Intent loadActivityIntent(Context context,
                                     String conversationId,
                                     String intentString,
                                     Class fallbackIntent) {
        Class<?> intentToLaunch;

        try {
            intentToLaunch = Class.forName(intentString);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, String.format(
                    "Unable to find specified intent: %s\nDefaulting to %s",
                    intentString,
                    fallbackIntent
            ));

            intentToLaunch = fallbackIntent;
        }

        if (!Activity.class.isAssignableFrom(intentToLaunch)) {
            Log.e(TAG, String.format(
                    "Specified intent is not an activity: %s\nDefaulting to %s",
                    intentString,
                    fallbackIntent
            ));

            intentToLaunch = fallbackIntent;
        }

        Intent intent = new Intent(context, intentToLaunch);

        intent.putExtra(
                context.getString(R.string.ClarabridgeChat_settings_notificationTriggerKey),
                context.getString(R.string.ClarabridgeChat_settings_notificationTrigger)
        );
        intent.putExtra(context.getString(R.string.ClarabridgeChat_settings_conversationIdKey), conversationId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return intent;
    }

    /**
     * Triggers a ClarabridgeChat notification, which will open intent from resource ClarabridgeChat_settings_notificationIntent on tap.
     * The notification will use provided message's avatarUrl as source for the icon.
     *
     * @param context The context from which to trigger the notification
     * @param message The message from which to trigger the notification
     */
    public static void triggerNotification(final Context context, final String conversationId, final Message message) {
        if (context == null || message == null) {
            Log.e(TAG, "Tried to trigger notification with null context or message. Ignoring.");
            return;
        }

        handler.post(new Runnable() {
            @Override
            public void run() {
                Message copy = message.copy();
                Resources resources = context.getResources();
                int avatarSize = resources.getDimensionPixelSize(R.dimen.ClarabridgeChat_messageAvatar);

                Bitmap image = BitmapVisitor.createRoundedBitmap(
                        BitmapFactory.decodeResource(resources, R.drawable.clarabridgechat_img_avatar),
                        avatarSize
                );

                try {
                    image = Glide.with(context.getApplicationContext())
                            .asBitmap()
                            .apply(RequestOptions.circleCropTransform())
                            .load(message.getAvatarUrl())
                            .submit(avatarSize, avatarSize)
                            .get();
                } catch (Exception error) {
                    Log.e(TAG, "Failed to load avatar for push notification.\nAvatar URL: " +
                            message.getAvatarUrl() + "\nError: " + error.getMessage());
                }

                NotificationManager notificationManager =
                        (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

                String channelId = context.getString(R.string.ClarabridgeChat_settings_notificationChannelId);
                String channelName = context.getString(R.string.ClarabridgeChat_settings_notificationChannelName);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Create channel to show notifications.
                    // Attempting to create an existing notification channel with its original values results in a no-op
                    notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                            channelName, NotificationManager.IMPORTANCE_HIGH));
                }

                NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId);
                String notificationIntentString = context.getString(R.string.ClarabridgeChat_settings_beforeNotificationIntent);

                Intent intent = loadActivityIntent(
                        context,
                        conversationId,
                        notificationIntentString,
                        NotificationActivity.class
                );

                int id = context.getResources().getInteger(R.integer.ClarabridgeChat_settings_notificationId);
                String tag = context.getString(R.string.ClarabridgeChat_settings_notificationTag) + "." + conversationId;
                int count = ConversationFragment.incrementPendingNotifications();
                String contentText = copy.getText();

                if (copy.getText().trim().isEmpty() && copy.getMessageActions().size() > 0) {
                    MessageAction messageAction = copy.getMessageActions().get(0);
                    contentText = messageAction.getText();
                }

                if (TextUtils.isEmpty(contentText)) {
                    contentText = context.getString(R.string.ClarabridgeChat_notificationFallbackText);
                }

                builder.setAutoCancel(true);
                builder.setContentIntent(PendingIntent.getActivity(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                ));
                builder.setContentTitle(
                        copy.getName() == null || copy.getName().isEmpty()
                                ? ApplicationInfo.getName(context) : copy.getName()
                );
                builder.setContentText(contentText);
                builder.setDefaults(NotificationCompat.DEFAULT_ALL);
                builder.setLargeIcon(image);
                builder.setSmallIcon(R.drawable.clarabridgechat_ic_notification);

                if (count > 1) {
                    builder.setNumber(count);
                }

                if (Build.VERSION.SDK_INT >= 16) {
                    builder.setPriority(Notification.PRIORITY_HIGH);
                }

                if (copy.getDate() != null) {
                    builder.setWhen(copy.getDate().getTime());
                }

                notificationManager.notify(tag, id, builder.build());
            }
        });
    }
}
