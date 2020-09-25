package com.clarabridge.core.network;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import com.clarabridge.core.utils.VersionInfo;
import okhttp3.Interceptor;

/**
 * An {@link Interceptor} that adds the x-smooch-sdk header to HTTP requests.
 * <p>
 * The header value is based on values read from {@link com.clarabridge.core.utils.VersionInfo} when
 * this class is instantiated.
 */
public class SdkHeaderInterceptor extends HeaderInterceptor {

    private final String sdk;

    @Inject
    public SdkHeaderInterceptor(VersionInfo versionInfo) {
        super("x-smooch-sdk");
        this.sdk = String.format("android/%s/%s",
                versionInfo.getVendorId(), versionInfo.getVersion());
    }

    @Nullable
    @Override
    String getHeaderValue() {
        return sdk;
    }
}
