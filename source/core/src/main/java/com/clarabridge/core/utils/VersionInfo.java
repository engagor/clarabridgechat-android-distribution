package com.clarabridge.core.utils;

import javax.inject.Inject;

import com.clarabridge.core.BuildConfig;

public final class VersionInfo {

    @Inject
    public VersionInfo() {
    }

    public String getVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public String getVendorId() {
        return BuildConfig.VENDOR_ID;
    }
}

