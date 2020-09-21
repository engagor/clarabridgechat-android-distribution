package com.clarabridge.core.monitor;

import android.support.annotation.VisibleForTesting;

import com.saulpower.fayeclient.FayeClient;

import com.clarabridge.core.Logger;
import com.clarabridge.core.facade.Serialization;
import com.clarabridge.core.facade.impl.LocalGsonSerializer;
import com.clarabridge.core.utils.StringUtils;

import static android.support.annotation.VisibleForTesting.NONE;

public class ConversationMonitorBuilder {
    private static final String TAG = "ConversationMonBuilder";
    private String appId;
    private String clientId;
    private String appUserId;
    private String host;
    private String jwt;
    private String sessionToken;
    private ConversationMonitor.Delegate delegate;
    private Serialization serializer;
    private FayeClient fayeClient;
    private int maxConnectionAttempts;
    private long retryInterval;

    public ConversationMonitorBuilder() {
    }

    public ConversationMonitor build() {
        if (StringUtils.anyEmpty(appId, appUserId, clientId, host) || delegate == null) {
            Logger.e(TAG, "Could not build ConversationMonitor because one or more required properties were null:"
                    + "\n\tappId = " + appId
                    + "\n\tappUserId = " + appUserId
                    + "\n\tclientId = " + clientId
                    + "\n\thost = " + host
                    + "\n\tdelegate = " + delegate);
            return null;
        }

        if (serializer == null) {
            serializer = new LocalGsonSerializer();
        }

        return new ConversationMonitor(serializer, appId, appUserId, clientId, jwt, sessionToken,
                host, fayeClient, delegate, maxConnectionAttempts, retryInterval);
    }

    public ConversationMonitorBuilder setAppId(final String appId) {
        this.appId = appId;
        return this;
    }

    public ConversationMonitorBuilder setAppUserId(final String appUserId) {
        this.appUserId = appUserId;
        return this;
    }

    public ConversationMonitorBuilder setDelegate(final ConversationMonitor.Delegate delegate) {
        this.delegate = delegate;
        return this;
    }

    public ConversationMonitorBuilder setHost(final String host) {
        this.host = host;
        return this;
    }

    public ConversationMonitorBuilder setSerializer(final Serialization serializer) {
        this.serializer = serializer;
        return this;
    }

    public ConversationMonitorBuilder setJwt(final String jwt) {
        this.jwt = jwt;
        return this;
    }

    public ConversationMonitorBuilder setSessionToken(final String sessionToken) {
        this.sessionToken = sessionToken;
        return this;
    }

    public ConversationMonitorBuilder setClientId(final String clientId) {
        this.clientId = clientId;
        return this;
    }

    public ConversationMonitorBuilder setRetryInterval(final long retryInterval) {
        this.retryInterval = retryInterval;
        return this;
    }

    public ConversationMonitorBuilder setMaxConnectionAttempts(final int maxConnectionAttempts) {
        this.maxConnectionAttempts = maxConnectionAttempts;
        return this;
    }

    @VisibleForTesting(otherwise = NONE)
    ConversationMonitorBuilder setFayeClient(final FayeClient fayeClient) {
        this.fayeClient = fayeClient;
        return this;
    }
}

