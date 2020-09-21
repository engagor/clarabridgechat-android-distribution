package com.clarabridge.core.network;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.IOException;
import java.text.Normalizer;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * An {@link Interceptor} to add a header to HTTP requests.
 * <p>
 * The header name is defined when this class is instantiated and won't change.
 * <p>
 * The header value is set by {@link #getHeaderValue()} and is only added to the request if it is
 * not null, otherwise the header is not added at all.
 */
abstract class HeaderInterceptor implements Interceptor {

    @NonNull
    private final String headerName;

    HeaderInterceptor(@NonNull String headerName) {
        this.headerName = headerName;
    }

    /**
     * @return the header name defined when this class was instantiated, without modifications
     */
    @NonNull
    final String getHeaderName() {
        return headerName;
    }

    /**
     * @return the header value to be set by this {@link Interceptor} in every HTTP request
     */
    @Nullable
    abstract String getHeaderValue();

    @NonNull
    @Override
    public final Response intercept(@NonNull Chain chain) throws IOException {
        Request.Builder newRequest = chain.request().newBuilder();
        String headerValue = getHeaderValue();

        if (headerValue != null) {
            newRequest.addHeader(getHeaderName(), normalizeHeaderValue(headerValue));
        }

        return chain.proceed(newRequest.build());
    }

    private String normalizeHeaderValue(@NonNull String headerValue) {
        return Normalizer.normalize(headerValue, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "");
    }
}
