package com.clarabridge.features.conversationlist;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Consumer;

import java.util.List;

import com.clarabridge.core.Config;
import com.clarabridge.core.Conversation;
import com.clarabridge.core.ConversationDelegate;
import com.clarabridge.core.ConversationDelegateAdapter;
import com.clarabridge.core.ConversationViewDelegate;
import com.clarabridge.core.InitializationStatus;
import com.clarabridge.core.ClarabridgeChatCallback;
import com.clarabridge.ui.ConversationActivity;

class ConversationListUseCase {

    private static final int CONVERSATION_DELEGATE = 21;
    private static final int CONVERSATION_INIT_DELEGATE = 22;

    private final Activity activity;
    private final ClarabridgeChatProxy clarabridgeChatProxy;

    private InternetConnectivityReceiver internetConnectivityReceiver = null;

    ConversationListUseCase(Activity activity, ClarabridgeChatProxy clarabridgeChatProxy) {
        this.activity = activity;
        this.clarabridgeChatProxy = clarabridgeChatProxy;
    }

    void getConversationList(final ClarabridgeChatCallback<List<Conversation>> useCaseCallback) {
        InitializationStatus status = clarabridgeChatProxy.getInitializationStatus();
        if (status == InitializationStatus.UNKNOWN) {
            clarabridgeChatProxy.addConversationUiDelegate(CONVERSATION_INIT_DELEGATE, new ConversationDelegateAdapter() {
                @Override
                public void onInitializationStatusChanged(@NonNull InitializationStatus status) {
                    clarabridgeChatProxy.addConversationUiDelegate(CONVERSATION_INIT_DELEGATE, null);
                    if (status == InitializationStatus.SUCCESS) {
                        clarabridgeChatProxy.getConversationsList(useCaseCallback);
                    } else {
                        useCaseCallback.run(new ClarabridgeChatCallback.Response.Builder<List<Conversation>>(400)
                                .withError("SDK is not initialized").build());
                    }
                }
            });
        } else if (status == InitializationStatus.SUCCESS) {
            clarabridgeChatProxy.getConversationsList(useCaseCallback);
        } else {
            useCaseCallback.run(new ClarabridgeChatCallback.Response.Builder<List<Conversation>>(400)
                    .withError("SDK is not initialized").build());
        }
    }

    void getMoreConversationsList(ClarabridgeChatCallback<List<Conversation>> useCaseCallback) {
        clarabridgeChatProxy.getMoreConversationsList(useCaseCallback);
    }

    boolean hasMoreConversations() {
        return clarabridgeChatProxy.hasMoreConversations();
    }

    void addConversationChangeDelegate(@NonNull ConversationDelegate conversationDelegate) {
        clarabridgeChatProxy.addConversationUiDelegate(CONVERSATION_DELEGATE, conversationDelegate);
    }

    void removeConversationChangeDelegate() {
        clarabridgeChatProxy.addConversationUiDelegate(CONVERSATION_DELEGATE, null);
    }

    /**
     * Given the {@link ConversationListPresenter} has called this method `isNewConversationButtonShown()`
     * With     <-   `showNewConversationButton`    -> Integrator Mobile SDK Settings
     * And      <- `canUserCreateMoreConversations` -> API Webservice Backend Settings
     * And      <-        `hasConversation`         -> Indicates if the conversation list is empty or not
     * When the Conversation List Screen is shown
     * Then New Conversation Button will be VISIBILITY
     * <p>
     * |:---:|:------------------------------:|:---------------:|:-------------------------:|:----------:|
     * | No. | canUserCreateMoreConversations | hasConversation | showNewConversationButton | VISIBILITY |
     * |:---:|:------------------------------:|:---------------:|:-------------------------:|:----------:|
     * |  1  |              True              |      True       |           True            |    SHOW    |
     * |  2  |              True              |      True       |           False           |    HIDE    |
     * |  3  |              True              |      False      |           True            |    SHOW    |
     * |  4  |              True              |      False      |           False           |    HIDE    |
     * |  5  |              False             |      True       |           True            |    HIDE    |
     * |  6  |              False             |      True       |           False           |    HIDE    |
     * |  7  |              False             |      False      |           True            |    SHOW    |
     * |  8  |              False             |      False      |           False           |    HIDE    |
     * |:---:|:------------------------------:|:---------------:|:-------------------------:|:----------:|
     */
    boolean isNewConversationButtonShown(boolean showNewConversationButton) {
        if (canUserCreateMoreConversations()) {
            // Only true when the `hasConversation` and `showNewConversationButton` are true (1)
            // OR
            // when the `hasConversation` is false and `showNewConversationButton` is true (3)
            // Otherwise (2) and (4) will be false
            // AND
            // the button won't be shown in the conversation list screen.
            return (hasConversation() && showNewConversationButton)
                    || (!hasConversation() && showNewConversationButton);
        } else {
            // Only true when the `hasConversation` is false and `showNewConversationButton` is true (7)
            // Otherwise (5), (6) and (8) will be false
            // AND
            // the button won't be shown in the conversation list screen.
            return !hasConversation() && showNewConversationButton;
        }
    }

    void notifyViewDelegateOnStartActivity() {
        final ConversationViewDelegate viewDelegate = clarabridgeChatProxy.getConversationViewDelegate();
        if (viewDelegate != null) {
            viewDelegate.onStartActivityCalled(activity.getIntent());
        }
    }

    Config getConfig() {
        return clarabridgeChatProxy.getConfig();
    }

    void navigateToConversationActivity(String conversationId, ClarabridgeChatCallback<Conversation> callback) {
        clarabridgeChatProxy.loadConversation(conversationId, callback);
    }

    void navigateToConversationActivity() {
        ConversationActivity.builder().show(activity);
    }

    void registerForNetworkChanges(Consumer<Boolean> connectionCallback) {
        getNetworkStatus(connectionCallback);
        internetConnectivityReceiver = new InternetConnectivityReceiver(connectionCallback);
        activity.registerReceiver(internetConnectivityReceiver,
                new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    void unRegisterForNetworkChanges() {
        if (internetConnectivityReceiver != null) {
            activity.unregisterReceiver(internetConnectivityReceiver);
            internetConnectivityReceiver = null;
        }
    }

    void createNewConversation(@Nullable ClarabridgeChatCallback<Void> callback) {
        clarabridgeChatProxy.createConversation(null, null, null, null, null, callback);
    }

    /**
     * checks if the delegate exists and "shouldCreateCustomConversationFlow" is true
     * then it will call the interceptor
     *
     * @return true if the interceptor was called, false if we should open the screen
     */
    boolean callIntegratorNewConversationInterceptor() {
        final ConversationViewDelegate delegate = clarabridgeChatProxy.getConversationViewDelegate();
        if (delegate != null && delegate.shouldCreateCustomConversationFlow()) {
            delegate.onCreateConversationClick();
            return true; //Intercepted return true
        } else {
            return false; //Not intercepted return false
        }
    }

    /*
     * private methods
     */
    private boolean canUserCreateMoreConversations() {
        final Config config = getConfig();
        return config != null && config.canUserCreateMoreConversations();
    }

    private boolean hasConversation() {
        final Conversation conversation = clarabridgeChatProxy.getConversation();
        return conversation != null && conversation.getId() != null;
    }

    private void getNetworkStatus(Consumer<Boolean> connectionCallback) {
        final ConnectivityManager connectivityManager =
                (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            final NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetInfo == null || !activeNetInfo.isConnected() || !activeNetInfo.isAvailable()) {
                connectionCallback.accept(false);
            } else {
                connectionCallback.accept(true);
            }
        }
    }

    private class InternetConnectivityReceiver extends BroadcastReceiver {

        private final Consumer<Boolean> connectionCallback;

        public InternetConnectivityReceiver(Consumer<Boolean> connectionCallback) {
            this.connectionCallback = connectionCallback;
        }

        @Override
        public void onReceive(final Context context, final Intent intent) {
            getNetworkStatus(connectionCallback);
        }
    }
}
