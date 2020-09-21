package com.clarabridge.core.network;

import android.support.annotation.Nullable;

import javax.inject.Inject;

import com.clarabridge.core.utils.ApplicationInfo;
import com.clarabridge.core.utils.DeviceInfo;
import okhttp3.Interceptor;

/**
 * An {@link Interceptor} that adds the User-Agent header to HTTP requests.
 * <p>
 * The header value is based on values read from {@link ApplicationInfo} and {@link DeviceInfo}
 * when this class is instantiated.
 */
public class UserAgentHeaderInterceptor extends HeaderInterceptor {

    private final String userAgent;

    @Inject
    public UserAgentHeaderInterceptor(ApplicationInfo applicationInfo, DeviceInfo deviceInfo) {
        super("User-Agent");
        this.userAgent = String.format("%s/%s (%s %s; %s %s)",
                applicationInfo.getName(),
                applicationInfo.getVersion(),
                deviceInfo.getManufacturer(),
                deviceInfo.getModel(),
                deviceInfo.getOperatingSystem(),
                deviceInfo.getOperatingSystemVersion());
    }

    @Nullable
    @Override
    String getHeaderValue() {
        return userAgent;
    }
}
