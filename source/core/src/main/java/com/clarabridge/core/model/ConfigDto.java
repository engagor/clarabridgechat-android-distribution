package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ConfigDto implements Serializable {

    @SerializedName("app")
    private AppDto app;

    @SerializedName("baseUrl")
    private BaseUrlDto baseUrl;

    @SerializedName("restRetryPolicy")
    private RetryConfigurationDto retryConfiguration;

    @SerializedName("integrations")
    private List<IntegrationDto> integrations;

    public AppDto getApp() {
        return app;
    }

    public void setApp(AppDto app) {
        this.app = app;
    }

    public BaseUrlDto getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(BaseUrlDto baseUrl) {
        this.baseUrl = baseUrl;
    }

    public RetryConfigurationDto getRetryConfiguration() {
        return retryConfiguration;
    }

    public void setRetryConfiguration(RetryConfigurationDto retryConfiguration) {
        this.retryConfiguration = retryConfiguration;
    }

    public List<IntegrationDto> getIntegrations() {
        if (integrations == null) {
            integrations = new ArrayList<>();
        }
        return integrations;
    }

    public void setIntegrations(List<IntegrationDto> integrations) {
        this.integrations = integrations;
    }
}
