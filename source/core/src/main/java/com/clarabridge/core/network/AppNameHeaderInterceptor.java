package com.clarabridge.core.network;

import androidx.annotation.Nullable;

import javax.inject.Inject;

import com.clarabridge.core.utils.ApplicationInfo;
import okhttp3.Interceptor;

/**
 * An {@link Interceptor} that adds the x-smooch-appname header to HTTP requests.
 * <p>
 * The header value is based on values read from {@link ApplicationInfo} when this class is
 * instantiated.
 */
public class AppNameHeaderInterceptor extends HeaderInterceptor {

    private final String appName;

    @Inject
    public AppNameHeaderInterceptor(ApplicationInfo applicationInfo) {
        super("x-smooch-appname");
        this.appName = applicationInfo.getName();
    }

    @Nullable
    @Override
    String getHeaderValue() {
        return appName;
    }
}
