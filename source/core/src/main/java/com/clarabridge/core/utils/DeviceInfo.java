package com.clarabridge.core.utils;

import android.content.Context;
import android.os.Build;
import androidx.annotation.Nullable;
import android.telephony.TelephonyManager;

import javax.inject.Inject;

/**
 * A helper class that provides information about the device.
 */
public final class DeviceInfo {

    @Nullable
    private final TelephonyManager telephonyManager;

    @Inject
    public DeviceInfo(Context context) {
        this.telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    public String getCarrierName() {
        String name = null;

        if (telephonyManager != null) {
            name = telephonyManager.getNetworkOperatorName();
        }

        return StringUtils.emptyIfNull(name);
    }

    public String getManufacturer() {
        return StringUtils.emptyIfNull(Build.MANUFACTURER);
    }

    public String getModel() {
        return StringUtils.emptyIfNull(Build.MODEL);
    }

    public String getOperatingSystem() {
        return "Android";
    }

    public String getPlatform() {
        return "android";
    }

    public String getOperatingSystemVersion() {
        return StringUtils.emptyIfNull(Build.VERSION.RELEASE);
    }
}

