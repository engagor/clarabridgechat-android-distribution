package com.clarabridge.core.network;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import com.clarabridge.core.facade.PersistenceFacade;
import com.clarabridge.core.utils.StringUtils;
import okhttp3.Credentials;
import okhttp3.Interceptor;

/**
 * An {@link Interceptor} that adds the Authorization header to HTTP requests made to the stripe API.
 * <p>
 * The header value is based on the value read from {@link PersistenceFacade} before each request.
 */
public class StripeAuthorizationHeaderInterceptor extends HeaderInterceptor {

    private final PersistenceFacade persistenceFacade;

    @Inject
    public StripeAuthorizationHeaderInterceptor(PersistenceFacade persistenceFacade) {
        super("Authorization");
        this.persistenceFacade = persistenceFacade;
    }

    @Nullable
    @Override
    String getHeaderValue() {
        String stripeKey = persistenceFacade.getStripeKey();

        if (!StringUtils.isEmpty(stripeKey)) {
            return Credentials.basic(stripeKey, "");
        }

        return null;
    }
}
