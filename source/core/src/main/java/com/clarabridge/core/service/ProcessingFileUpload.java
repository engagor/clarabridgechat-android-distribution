package com.clarabridge.core.service;

import com.clarabridge.core.Message;
import com.clarabridge.core.ClarabridgeChatCallback;

class ProcessingFileUpload {
    private Message message;
    private ClarabridgeChatCallback<Message> callback;

    ProcessingFileUpload(Message message, ClarabridgeChatCallback<Message> callback) {
        this.message = message;
        this.callback = callback;
    }

    public Message getMessage() {
        return message;
    }

    public ClarabridgeChatCallback<Message> getCallback() {
        return callback;
    }
}
