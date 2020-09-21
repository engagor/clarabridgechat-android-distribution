package com.clarabridge.core;

/**
 * Upload status of a Message.
 */
public enum MessageUploadStatus {

    /**
     * A user message that has not yet finished uploading.
     */
    UNSENT,

    /**
     * A user message that failed to upload.
     */
    FAILED,

    /**
     * A user message that was successfully uploaded.
     */
    SENT,

    /**
     * A message that did not originate from the current user.
     */
    NOT_USER_MESSAGE,

    ;
}

