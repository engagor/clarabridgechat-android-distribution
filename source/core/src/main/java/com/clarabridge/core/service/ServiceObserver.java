package com.clarabridge.core.service;

public interface ServiceObserver {

    /**
     * Invoked when the {@link ClarabridgeChatService} is resumed
     */
    void onServiceResumed();

}
