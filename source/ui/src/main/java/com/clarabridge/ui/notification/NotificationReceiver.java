package com.clarabridge.ui.notification;

import android.content.Context;

import com.clarabridge.core.AbstractNotificationReceiver;
import com.clarabridge.core.Conversation;
import com.clarabridge.core.Message;
import com.clarabridge.core.ClarabridgeChat;
import com.clarabridge.ui.NotificationActivity;
import com.clarabridge.ui.NotificationController;
import com.clarabridge.ui.fragment.ConversationFragment;

public class NotificationReceiver extends AbstractNotificationReceiver {
    @Override
    public void onNotification(Context context, String conversationId, Message message) {
        Conversation conversation = ClarabridgeChat.getConversation();

        if (conversation != null && (NotificationActivity.isRunning(conversationId)
                || (conversation.getId() != null
                && conversation.getId().equals(conversationId)
                && ConversationFragment.isRunning()))) {
            return;
        }

        if (ClarabridgeChat.getMessageModifierDelegate() != null) {
            message = ClarabridgeChat.getMessageModifierDelegate().beforeNotification(conversationId, message);
        }

        if (message != null) {
            NotificationController.triggerNotification(context, conversationId, message);
        }
    }
}

