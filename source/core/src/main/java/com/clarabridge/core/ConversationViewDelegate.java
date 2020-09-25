package com.clarabridge.core;

import android.content.Intent;
import androidx.annotation.NonNull;

/**
 * Delegate for events related to the conversation view.
 * <p>
 * Creating a delegate is optional, and may be used to receive callbacks when certain events
 * happen in the conversation view.
 * <p>
 * Callbacks are guaranteed to be called from the main thread.
 */
public interface ConversationViewDelegate {

    /**
     * Notifies the delegate that a call to start an activity has been made
     *
     * @param intent A copy of the description of the activity that will start
     */
    void onStartActivityCalled(@NonNull Intent intent);

    /**
     * Notifies the delegate that permissions for this app have been requested
     *
     * @param permissions The list of permissions requested
     */
    void onRequestPermissionsCalled(@NonNull String[] permissions);

    /**
     * Notifies the ConverstationListActivity that the conversation activity create intent
     * should be sent to the interceptor
     *
     * @return true if you want to intercept the conversation activity create intent
     */
    boolean shouldCreateCustomConversationFlow();

    /**
     * Called when a new conversation is selected
     */
    void onCreateConversationClick();
}
