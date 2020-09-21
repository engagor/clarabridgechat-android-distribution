package com.clarabridge.core.service;

import android.os.Binder;

public class ClarabridgeChatServiceBinder extends Binder {
    private final ClarabridgeChatService service;

    public ClarabridgeChatServiceBinder(final ClarabridgeChatService service) {
        this.service = service;
    }

    public ClarabridgeChatService getService() {
        return service;
    }
}

