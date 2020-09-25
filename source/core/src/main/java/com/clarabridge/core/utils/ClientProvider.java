package com.clarabridge.core.utils;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import javax.inject.Inject;

import com.clarabridge.core.Settings;
import com.clarabridge.core.di.SdkScope;
import com.clarabridge.core.model.ClientDto;
import com.clarabridge.core.model.ClientInfoDto;
import com.clarabridge.core.service.ServiceSettings;

/**
 * A service class that be used to build {@link ClientDto} instances.
 */
@SdkScope
public class ClientProvider {

    private final ApplicationInfo applicationInfo;
    private final DeviceInfo deviceInfo;
    private final VersionInfo versionInfo;
    private final Settings settings;
    private final ServiceSettings serviceSettings;

    @Nullable
    @VisibleForTesting
    ClientDto cachedClient;

    @Inject
    public ClientProvider(
            ApplicationInfo applicationInfo,
            DeviceInfo deviceInfo,
            VersionInfo versionInfo,
            Settings settings,
            ServiceSettings serviceSettings) {
        this.applicationInfo = applicationInfo;
        this.deviceInfo = deviceInfo;
        this.versionInfo = versionInfo;
        this.settings = settings;
        this.serviceSettings = serviceSettings;
    }

    /**
     * @return an instance of {@link ClientDto} with metadata about this ClarabridgeChat client
     */
    public ClientDto buildClient() {
        if (shouldBuildNewInstance()) {
            cachedClient = buildNewInstance();
        }

        return cachedClient;
    }

    @VisibleForTesting
    boolean shouldBuildNewInstance() {
        return cachedClient == null
                || !StringUtils.isEqual(cachedClient.getId(), serviceSettings.getClientId())
                || !StringUtils.isEqual(cachedClient.getPushNotificationToken(),
                serviceSettings.getFirebaseCloudMessagingToken());
    }

    @VisibleForTesting
    ClientDto buildNewInstance() {
        ClientInfoDto clientInfoDto = ClientInfoDto.builder()
                .withOs(deviceInfo.getOperatingSystem())
                .withOsVersion(deviceInfo.getOperatingSystemVersion())
                .withDevicePlatform(String.format("%s %s", deviceInfo.getManufacturer(), deviceInfo.getModel()))
                .withAppName(applicationInfo.getName())
                .withCarrier(deviceInfo.getCarrierName())
                .withAppId(applicationInfo.getPackageName())
                .withSdkVersion(versionInfo.getVersion())
                .withVendor(versionInfo.getVendorId())
                .withInstaller(applicationInfo.getInstallerPackageName())
                .build();

        return ClientDto.builder()
                .withId(serviceSettings.getClientId())
                .withIntegrationId(settings.getIntegrationId())
                .withPlatform(deviceInfo.getPlatform())
                .withAppVersion(applicationInfo.getVersion())
                .withClientInfo(clientInfoDto)
                .withPushNotificationToken(serviceSettings.getFirebaseCloudMessagingToken())
                .build();
    }
}
