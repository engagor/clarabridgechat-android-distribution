package com.clarabridge.core.network;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import com.clarabridge.core.service.ServiceSettings;
import okhttp3.Interceptor;

/**
 * An {@link Interceptor} that adds the x-smooch-push header to HTTP requests.
 * <p>
 * The header value is based on values read from {@link ServiceSettings} before each request.
 */
public class PushHeaderInterceptor extends HeaderInterceptor {

    private final ServiceSettings serviceSettings;

    @Inject
    public PushHeaderInterceptor(ServiceSettings serviceSettings) {
        super("x-smooch-push");
        this.serviceSettings = serviceSettings;
    }

    @Nullable
    @Override
    String getHeaderValue() {
        return serviceSettings.getFirebaseCloudMessagingToken() != null ? "enabled" : "disabled";
    }
}
