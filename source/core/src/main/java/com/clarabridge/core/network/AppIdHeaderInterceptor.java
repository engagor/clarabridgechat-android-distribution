package com.clarabridge.core.network;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import com.clarabridge.core.Settings;
import com.clarabridge.core.facade.PersistenceFacade;
import okhttp3.Interceptor;

/**
 * An {@link Interceptor} that adds the x-smooch-appid header to HTTP requests.
 * <p>
 * The header value is based on the value read from {@link Settings} before each request.
 */
public class AppIdHeaderInterceptor extends HeaderInterceptor {

    private final PersistenceFacade persistenceFacade;

    @Inject
    public AppIdHeaderInterceptor(PersistenceFacade persistenceFacade) {
        super("x-smooch-appid");
        this.persistenceFacade = persistenceFacade;
    }

    @Nullable
    @Override
    String getHeaderValue() {
        return persistenceFacade.getAppId();
    }
}
