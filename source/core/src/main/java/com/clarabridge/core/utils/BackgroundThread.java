package com.clarabridge.core.utils;

import android.os.HandlerThread;

public final class BackgroundThread {

    private static BackgroundThread INSTANCE;

    public static HandlerThread get() {
        synchronized (BackgroundThread.class) {
            if (INSTANCE == null) { //if there is no instance available... create new one
                INSTANCE = new BackgroundThread();
            }
        }
        return INSTANCE.thread();
    }

    private final HandlerThread handlerThread = new HandlerThread("background-thread");

    private HandlerThread thread() {
        if (!handlerThread.isAlive()) {
            handlerThread.start();
        }
        return handlerThread;
    }
}
