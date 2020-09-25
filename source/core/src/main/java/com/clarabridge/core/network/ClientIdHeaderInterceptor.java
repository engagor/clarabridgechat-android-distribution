package com.clarabridge.core.network;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import com.clarabridge.core.service.ServiceSettings;
import okhttp3.Interceptor;

/**
 * An {@link Interceptor} that adds the x-smooch-clientid header to HTTP requests.
 * <p>
 * The header value is based on values read from {@link com.clarabridge.core.service.ServiceSettings}
 * before each request.
 */
public class ClientIdHeaderInterceptor extends HeaderInterceptor {

    private final ServiceSettings serviceSettings;

    @Inject
    public ClientIdHeaderInterceptor(ServiceSettings serviceSettings) {
        super("x-smooch-clientid");
        this.serviceSettings = serviceSettings;
    }

    @Nullable
    @Override
    String getHeaderValue() {
        return serviceSettings.getClientId();
    }
}
