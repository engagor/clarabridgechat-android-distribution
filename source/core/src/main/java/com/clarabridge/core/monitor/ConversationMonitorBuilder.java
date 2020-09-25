package com.clarabridge.core.monitor;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.saulpower.fayeclient.FayeClient;

import com.clarabridge.core.AuthenticationCallback;
import com.clarabridge.core.AuthenticationDelegate;
import com.clarabridge.core.AuthenticationError;
import com.clarabridge.core.Logger;
import com.clarabridge.core.facade.PersistenceFacade;
import com.clarabridge.core.facade.Serialization;
import com.clarabridge.core.facade.impl.LocalGsonSerializer;
import com.clarabridge.core.utils.StringUtils;

import static androidx.annotation.VisibleForTesting.NONE;

public class ConversationMonitorBuilder {

    private static final AuthenticationDelegate EMPTY_AUTH_DELEGATE = new AuthenticationDelegate() {
        @Override
        public void onInvalidAuth(AuthenticationError error, AuthenticationCallback callback) {
            //ignored
        }
    };

    private static final String TAG = "ConversationMonBuilder";
    private String appId;
    private String clientId;
    private String userId;
    private String host;
    private String sessionToken;
    private ConversationMonitor.Delegate delegate;
    private Serialization serializer;
    private FayeClient fayeClient;
    private PersistenceFacade persistenceFacade;
    @NonNull
    private AuthenticationDelegate authenticationDelegate = EMPTY_AUTH_DELEGATE;
    private int maxConnectionAttempts;
    private long retryInterval;

    public ConversationMonitorBuilder() {
    }

    public ConversationMonitor build() {
        if (StringUtils.anyEmpty(appId, userId, clientId, host) || delegate == null || persistenceFacade == null) {
            Logger.e(TAG, "Could not build ConversationMonitor because one or more required properties were null:"
                    + "\n\tappId = " + appId
                    + "\n\tuserId = " + userId
                    + "\n\tclientId = " + clientId
                    + "\n\thost = " + host
                    + "\n\tdelegate = " + delegate);
            return null;
        }

        if (serializer == null) {
            serializer = new LocalGsonSerializer();
        }

        return new ConversationMonitor(serializer, appId, userId, clientId, persistenceFacade, sessionToken,
                host, fayeClient, delegate, authenticationDelegate, maxConnectionAttempts, retryInterval);
    }

    public ConversationMonitorBuilder setAppId(final String appId) {
        this.appId = appId;
        return this;
    }

    public ConversationMonitorBuilder setUserId(final String userId) {
        this.userId = userId;
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

    public ConversationMonitorBuilder setAuthenticationDelegate(
            @Nullable AuthenticationDelegate authenticationDelegate) {
        if (authenticationDelegate != null) {
            this.authenticationDelegate = authenticationDelegate;
        }
        return this;
    }

    public ConversationMonitorBuilder setPersistenceFacade(PersistenceFacade persistenceFacade) {
        this.persistenceFacade = persistenceFacade;
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

