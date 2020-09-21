package com.clarabridge.core.model;

/**
 * Model class containing details about this client of ClarabridgeChat.
 */
public class ClientInfoDto {

    private final String os;
    private final String osVersion;
    private final String devicePlatform;
    private final String appName;
    private final String carrier;
    private final String appId;
    private final String sdkVersion;
    private final String vendor;
    private final String installer;

    private ClientInfoDto(
            String os,
            String osVersion,
            String devicePlatform,
            String appName,
            String carrier,
            String appId,
            String sdkVersion,
            String vendor,
            String installer) {
        this.os = os;
        this.osVersion = osVersion;
        this.devicePlatform = devicePlatform;
        this.appName = appName;
        this.carrier = carrier;
        this.appId = appId;
        this.sdkVersion = sdkVersion;
        this.vendor = vendor;
        this.installer = installer;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getOs() {
        return os;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getDevicePlatform() {
        return devicePlatform;
    }

    public String getAppName() {
        return appName;
    }

    public String getCarrier() {
        return carrier;
    }

    public String getAppId() {
        return appId;
    }

    public String getSdkVersion() {
        return sdkVersion;
    }

    public String getVendor() {
        return vendor;
    }

    public String getInstaller() {
        return installer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClientInfoDto that = (ClientInfoDto) o;

        if (os != null ? !os.equals(that.os) : that.os != null) {
            return false;
        }
        if (osVersion != null ? !osVersion.equals(that.osVersion) : that.osVersion != null) {
            return false;
        }
        if (devicePlatform != null ? !devicePlatform.equals(that.devicePlatform) : that.devicePlatform != null) {
            return false;
        }
        if (appName != null ? !appName.equals(that.appName) : that.appName != null) {
            return false;
        }
        if (carrier != null ? !carrier.equals(that.carrier) : that.carrier != null) {
            return false;
        }
        if (appId != null ? !appId.equals(that.appId) : that.appId != null) {
            return false;
        }
        if (sdkVersion != null ? !sdkVersion.equals(that.sdkVersion) : that.sdkVersion != null) {
            return false;
        }
        if (vendor != null ? !vendor.equals(that.vendor) : that.vendor != null) {
            return false;
        }
        return installer != null ? installer.equals(that.installer) : that.installer == null;
    }

    @Override
    public int hashCode() {
        int result = os != null ? os.hashCode() : 0;
        result = 31 * result + (osVersion != null ? osVersion.hashCode() : 0);
        result = 31 * result + (devicePlatform != null ? devicePlatform.hashCode() : 0);
        result = 31 * result + (appName != null ? appName.hashCode() : 0);
        result = 31 * result + (carrier != null ? carrier.hashCode() : 0);
        result = 31 * result + (appId != null ? appId.hashCode() : 0);
        result = 31 * result + (sdkVersion != null ? sdkVersion.hashCode() : 0);
        result = 31 * result + (vendor != null ? vendor.hashCode() : 0);
        result = 31 * result + (installer != null ? installer.hashCode() : 0);
        return result;
    }

    public static class Builder {
        private String os;
        private String osVersion;
        private String devicePlatform;
        private String appName;
        private String carrier;
        private String appId;
        private String sdkVersion;
        private String vendor;
        private String installer;

        private Builder() {
            // Intentionally empty
        }

        public ClientInfoDto build() {
            return new ClientInfoDto(
                    os,
                    osVersion,
                    devicePlatform,
                    appName,
                    carrier,
                    appId,
                    sdkVersion,
                    vendor,
                    installer);
        }

        public Builder withOs(String os) {
            this.os = os;
            return this;
        }

        public Builder withOsVersion(String osVersion) {
            this.osVersion = osVersion;
            return this;
        }

        public Builder withDevicePlatform(String devicePlatform) {
            this.devicePlatform = devicePlatform;
            return this;
        }

        public Builder withAppName(String appName) {
            this.appName = appName;
            return this;
        }

        public Builder withCarrier(String carrier) {
            this.carrier = carrier;
            return this;
        }

        public Builder withAppId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder withSdkVersion(String sdkVersion) {
            this.sdkVersion = sdkVersion;
            return this;
        }

        public Builder withVendor(String vendor) {
            this.vendor = vendor;
            return this;
        }

        public Builder withInstaller(String installer) {
            this.installer = installer;
            return this;
        }
    }
}
