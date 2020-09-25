package com.clarabridge.core;

import android.content.Intent;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;

/**
 * Abstract class that implements {@link ConversationViewDelegate}
 *
 */
public abstract class ConversationViewDelegateAdapter implements ConversationViewDelegate {

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStartActivityCalled(@NonNull Intent intent) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRequestPermissionsCalled(@NonNull String[] permissions) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean shouldCreateCustomConversationFlow() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateConversationClick() {

    }
}
