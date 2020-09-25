package com.clarabridge.features.conversationlist;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

import com.clarabridge.core.Config;
import com.clarabridge.core.Conversation;
import com.clarabridge.core.ConversationDelegate;
import com.clarabridge.core.ConversationViewDelegate;
import com.clarabridge.core.InitializationStatus;
import com.clarabridge.core.Message;
import com.clarabridge.core.ClarabridgeChat;
import com.clarabridge.core.ClarabridgeChatCallback;

/**
 * wrapper around that static clarabridgeChat class so we can inject it and
 * mock its methods in tests
 */
final class ClarabridgeChatProxy {

    /**
     * @see ClarabridgeChat#getConversationsList
     */
    void getConversationsList(@Nullable ClarabridgeChatCallback<List<Conversation>> callback) {
        ClarabridgeChat.getConversationsList(callback);
    }

    /**
     * @see ClarabridgeChat#getMoreConversationsList
     */
    void getMoreConversationsList(@Nullable ClarabridgeChatCallback<List<Conversation>> callback) {
        ClarabridgeChat.getMoreConversationsList(callback);
    }

    /**
     * @see ClarabridgeChat#hasMoreConversations
     */
    boolean hasMoreConversations() {
        return ClarabridgeChat.hasMoreConversations();
    }

    /**
     * @see ClarabridgeChat#getInitializationStatus
     */
    InitializationStatus getInitializationStatus() {
        return ClarabridgeChat.getInitializationStatus();
    }

    /**
     * @see ClarabridgeChat#addConversationUiDelegate
     */
    void addConversationUiDelegate(int key, @Nullable ConversationDelegate delegate) {
        ClarabridgeChat.addConversationUiDelegate(key, delegate);
    }

    /**
     * @see ClarabridgeChat#addConversationUiDelegate
     */
    @Nullable
    ConversationViewDelegate getConversationViewDelegate() {
        return ClarabridgeChat.getConversationViewDelegate();
    }

    /**
     * @see ClarabridgeChat#getConfig
     */
    @Nullable
    Config getConfig() {
        return ClarabridgeChat.getConfig();
    }

    /**
     * @see ClarabridgeChat#getConversation
     */
    @Nullable
    Conversation getConversation() {
        return ClarabridgeChat.getConversation();
    }

    /**
     * @see ClarabridgeChat#createConversation
     */
    void createConversation(@Nullable String name,
                            @Nullable String description,
                            @Nullable String iconUrl,
                            @Nullable List<Message> messages,
                            @Nullable Map<String, Object> metadata,
                            @Nullable ClarabridgeChatCallback<Void> callback) {
        ClarabridgeChat.createConversation(name, description, iconUrl, messages, metadata, callback);
    }

    /**
     * @see ClarabridgeChat#loadConversation
     */
    void loadConversation(@NonNull String conversationId,
                          @Nullable ClarabridgeChatCallback<Conversation> callback) {
        ClarabridgeChat.loadConversation(conversationId, callback);
    }

}
