package com.clarabridge.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.clarabridge.core.Conversation;
import com.clarabridge.core.ClarabridgeChat;
import com.clarabridge.core.ClarabridgeChatCallback;
import com.clarabridge.core.utils.StringUtils;

import static com.clarabridge.ui.NotificationController.loadActivityIntent;

public class NotificationActivity extends AppCompatActivity {
    private static String loadingConversationId;
    private Handler handler = new Handler(Looper.getMainLooper());

    public static boolean isRunning(@NonNull String conversationId) {
        return StringUtils.isNotNullAndEqual(conversationId, loadingConversationId);
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clarabridgechat_activity_notification);

        final String conversationId = getIntent().getStringExtra(getString(R.string.ClarabridgeChat_settings_conversationIdKey));
        Conversation conversation = ClarabridgeChat.getConversation();
        String currentConversationId = conversation != null ? conversation.getId() : null;

        loadingConversationId = conversationId;

        if (conversationId != null && !conversationId.equals(currentConversationId)) {
            ConversationActivity.close();
        }

        ClarabridgeChat.loadConversation(conversationId, new ClarabridgeChatCallback<Conversation>() {
            @Override
            public void run(@NonNull Response<Conversation> response) {
                showConversationActivity(conversationId);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loadingConversationId = null;
    }

    private void showConversationActivity(final String conversationId) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Intent intent = loadActivityIntent(
                        NotificationActivity.this,
                        conversationId,
                        getString(R.string.ClarabridgeChat_settings_notificationIntent),
                        ConversationActivity.class
                );
                startActivity(intent);

                finish();
            }
        });
    }
}
