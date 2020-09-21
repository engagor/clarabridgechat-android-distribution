package com.clarabridge.core.model;

/**
 * Model class containing details about this client of ClarabridgeChat.
 */
public class ClientDto {

    private final String id;
    private final String integrationId;
    private final String platform;
    private final String appVersion;
    private final ClientInfoDto info;
    private final String pushNotificationToken;

    private ClientDto(
            String id,
            String integrationId,
            String platform,
            String appVersion,
            ClientInfoDto info,
            String pushNotificationToken) {
        this.id = id;
        this.integrationId = integrationId;
        this.platform = platform;
        this.appVersion = appVersion;
        this.info = info;
        this.pushNotificationToken = pushNotificationToken;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getId() {
        return id;
    }

    public String getIntegrationId() {
        return integrationId;
    }

    public String getPlatform() {
        return platform;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public ClientInfoDto getInfo() {
        return info;
    }

    public String getPushNotificationToken() {
        return pushNotificationToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ClientDto clientDto = (ClientDto) o;

        if (id != null ? !id.equals(clientDto.id) : clientDto.id != null) {
            return false;
        }
        if (integrationId != null ? !integrationId.equals(clientDto.integrationId) : clientDto.integrationId != null) {
            return false;
        }
        if (platform != null ? !platform.equals(clientDto.platform) : clientDto.platform != null) {
            return false;
        }
        if (appVersion != null ? !appVersion.equals(clientDto.appVersion) : clientDto.appVersion != null) {
            return false;
        }
        if (info != null ? !info.equals(clientDto.info) : clientDto.info != null) {
            return false;
        }
        return pushNotificationToken != null
                ? pushNotificationToken.equals(clientDto.pushNotificationToken)
                : clientDto.pushNotificationToken == null;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (integrationId != null ? integrationId.hashCode() : 0);
        result = 31 * result + (platform != null ? platform.hashCode() : 0);
        result = 31 * result + (appVersion != null ? appVersion.hashCode() : 0);
        result = 31 * result + (info != null ? info.hashCode() : 0);
        result = 31 * result + (pushNotificationToken != null ? pushNotificationToken.hashCode() : 0);
        return result;
    }

    public static class Builder {
        private String id;
        private String integrationId;
        private String platform;
        private String appVersion;
        private ClientInfoDto info;
        private String pushNotificationToken;

        private Builder() {
            // Intentionally empty
        }

        public ClientDto build() {
            return new ClientDto(
                    id,
                    integrationId,
                    platform,
                    appVersion,
                    info,
                    pushNotificationToken);
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withIntegrationId(String integrationId) {
            this.integrationId = integrationId;
            return this;
        }

        public Builder withPlatform(String platform) {
            this.platform = platform;
            return this;
        }

        public Builder withAppVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        public Builder withClientInfo(ClientInfoDto clientInfoDto) {
            this.info = clientInfoDto;
            return this;
        }

        public Builder withPushNotificationToken(String pushNotificationToken) {
            this.pushNotificationToken = pushNotificationToken;
            return this;
        }
    }
}
