package com.clarabridge.core.service;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.UUID;

import javax.inject.Inject;

import com.clarabridge.core.di.SdkScope;
import com.clarabridge.core.utils.HashDigest;
import com.clarabridge.core.utils.StringUtils;

import static androidx.annotation.VisibleForTesting.NONE;

@SdkScope
public class ServiceSettings {
    private static final String PREFS = "ClarabridgeChatServiceSettings";
    private Context context;
    private String baseUrl;
    private String region;

    @Nullable
    private String clientId;

    @Inject
    public ServiceSettings(final Context context) {
        this.context = context;
    }

    public void clear() {
        final SharedPreferences.Editor editor = getSettings().edit();

        editor.clear();
        editor.apply();
    }

    public String getLegacyDeviceId() {
        final String defaultValue = null;

        return getSettings().getString("deviceId", defaultValue);
    }

    @VisibleForTesting(otherwise = NONE)
    public void setLegacyDeviceId(String value) {
        final SharedPreferences.Editor editor = getSettings().edit();

        if (value == null) {
            editor.remove("deviceId");
        } else {
            editor.putString("deviceId", value);
        }

        editor.apply();
    }

    public void clearLegacyDeviceId() {
        final SharedPreferences.Editor editor = getSettings().edit();
        editor.remove("deviceId");
        editor.apply();
    }

    public String getClientId() {
        if (clientId != null) {
            return clientId;
        }

        clientId = getSettings().getString("clientId", HashDigest.sha1(UUID.randomUUID().toString()));
        setClientId(clientId);

        return clientId;
    }

    public void setClientId(final String value) {
        clientId = value;
        final SharedPreferences.Editor editor = getSettings().edit();

        if (value == null) {
            editor.remove("clientId");
        } else {
            editor.putString("clientId", value);
        }

        editor.apply();
    }

    public String getFirebaseCloudMessagingToken() {
        final String defaultValue = null;

        return getSettings().getString("firebaseCloudMessagingToken", defaultValue);
    }

    public void setFirebaseCloudMessagingToken(final String value) {
        final SharedPreferences.Editor editor = getSettings().edit();

        if (value == null) {
            editor.remove("firebaseCloudMessagingToken");
        } else {
            editor.putString("firebaseCloudMessagingToken", value);
        }

        editor.apply();
    }

    public String getBaseUrl(@NonNull String integrationId) {
        if (!StringUtils.isEmpty(baseUrl)) {
            return baseUrl;
        } else if (!StringUtils.isEmpty(region)) {
            return String.format("https://%s.config.%s.smooch.io", integrationId, region);
        } else {
            return String.format("https://%s.config.smooch.io", integrationId);
        }
    }

    public void setBaseUrl(final String value) {
        baseUrl = value;
    }

    public void setRegion(final String region) {
        this.region = region;
    }

    public int getRefreshDelayInSecs() {
        final int defaultValue = 30;

        return getSettings().getInt("refreshDelayInSecs", defaultValue);
    }

    public void setRefreshDelayInSecs(final Integer value) {
        final SharedPreferences.Editor editor = getSettings().edit();

        if (value == null) {
            editor.remove("refreshDelayInSecs");
        } else {
            editor.putInt("refreshDelayInSecs", value);
        }

        editor.apply();
    }

    public int getSyncDelayInSecs() {
        final int defaultValue = 60;

        return getSettings().getInt("syncDelayInSecs", defaultValue);
    }

    public void setSyncDelayInSecs(final Integer value) {
        final SharedPreferences.Editor editor = getSettings().edit();

        if (value == null) {
            editor.remove("syncDelayInSecs");
        } else {
            editor.putInt("syncDelayInSecs", value);
        }

        editor.apply();
    }

    private SharedPreferences getSettings() {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}

