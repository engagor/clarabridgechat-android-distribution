package com.clarabridge.core;

import android.support.annotation.Nullable;

/**
 * The settings used to initialise the SDK.
 */
public class Settings {

    private final String integrationId;
    @Nullable
    private final String authCode;
    private boolean firebaseCloudMessagingAutoRegistrationEnabled;
    @Nullable
    private String serviceBaseUrl;
    @Nullable
    private String region;
    @Nullable
    private String fileProviderAuthorities;
    @Nullable
    private String mapsApiKey;
    @Nullable
    private AuthenticationDelegate authenticationDelegate;

    /**
     * Initializes a {@link Settings} with the given app id and integration id.
     *
     * @param integrationId A valid integration id retrieved from the ClarabridgeChat web portal
     */
    public Settings(final String integrationId) {
        this(integrationId, null);
    }

    /**
     * Initializes a {@link Settings} instance with the given app id, integration id and auth code
     *
     * @param integrationId A valid integration id retrieved from the ClarabridgeChat web portal
     * @param authCode      A valid auth code retrieved from the ClarabridgeChat API to authenticate as an
     *                      existing user
     */
    public Settings(final String integrationId, @Nullable final String authCode) {
        this.integrationId = integrationId;
        this.authCode = authCode;

        this.firebaseCloudMessagingAutoRegistrationEnabled = true;
    }

    /**
     * The integration id corresponding to your SDK integration.
     * <p>
     * Integrations id are issued on the ClarabridgeChat web portal. This value may only be set once, and must
     * be set at init time.
     *
     * @return the integration id
     */
    public String getIntegrationId() {
        return integrationId;
    }

    /**
     * The auth code being used to authenticate as an existing user.
     * <p>
     * An auth code can be retrieved from the ClarabridgeChat API. This value may only be set once, and must
     * be set at init time.
     *
     * @return the auth code if provided on init, null otherwise
     */
    @Nullable
    public String getAuthCode() {
        return authCode;
    }

    /**
     * A boolean property that indicates whether ClarabridgeChat should manage the Firebase Cloud Messaging
     * registration.
     * <p>
     * The default value is true.
     *
     * @return true if Firebase Cloud Messaging auto registration is enabled, false otherwise
     */
    public boolean isFirebaseCloudMessagingAutoRegistrationEnabled() {
        return firebaseCloudMessagingAutoRegistrationEnabled;
    }

    /**
     * A boolean property that indicates whether ClarabridgeChat should manage the Firebase Cloud Messaging
     * registration.
     * <p>
     * The default value is true.
     *
     * @param isEnabled if Firebase Cloud Messaging auto registration should be enabled
     */
    public void setFirebaseCloudMessagingAutoRegistrationEnabled(boolean isEnabled) {
        this.firebaseCloudMessagingAutoRegistrationEnabled = isEnabled;
    }

    /**
     * A String that signifies which authority ClarabridgeChat will use to provide files to the CAMERA application.
     * <p>
     * This should only be set when you have a provider defined in your AndroidManifest.
     *
     * @return the authorities string
     */
    @Nullable
    public String getFileProviderAuthorities() {
        return this.fileProviderAuthorities;
    }

    /**
     * A String that signifies which authority ClarabridgeChat will use to provide files to the CAMERA application.
     * <p>
     * This need only be set when you have a provider defined in the AndroidManifest.
     *
     * @param fileProviderAuthorities the authorities string set in your provider tag
     */
    public void setFileProviderAuthorities(@Nullable final String fileProviderAuthorities) {
        this.fileProviderAuthorities = fileProviderAuthorities;
    }

    /**
     * Google Maps API key to use to display a location preview for Location messages. If null or empty, a
     * text representation of the location is shown instead.
     *
     * @return the Google Maps API key
     */
    @Nullable
    public String getMapsApiKey() {
        return mapsApiKey;
    }

    /**
     * Google Maps API key to use to display a location preview for Location messages. If null or empty, a
     * text representation of the location is shown instead.
     *
     * @param mapsApiKey the Google Maps API key
     */
    public void setMapsApiKey(@Nullable String mapsApiKey) {
        this.mapsApiKey = mapsApiKey;
    }

    @Nullable
    public String getServiceBaseUrl() {
        return this.serviceBaseUrl;
    }

    public void setServiceBaseUrl(@Nullable String serviceBaseUrl) {
        this.serviceBaseUrl = serviceBaseUrl;
    }

    /**
     * A String that determines which region to connect to.
     *
     * @return the region identifier
     */
    @Nullable
    public String getRegion() {
        return this.region;
    }

    /**
     * A String that determines which region to connect to.
     * <p>
     * Leave unspecified to use the default region (US). Set to "eu-1" to use the EU region.
     *
     * @param region the region identifier
     */
    public void setRegion(@Nullable String region) {
        this.region = region;
    }

    /**
     * A delegate to be notified about a failed request due to invalid credentials.
     *
     * @return an implementation of {@link AuthenticationDelegate}
     */
    @Nullable
    public AuthenticationDelegate getAuthenticationDelegate() {
        return authenticationDelegate;
    }

    /**
     * A delegate to be notified about a failed request due to invalid credentials.
     *
     * @param authenticationDelegate an implementation of {@link AuthenticationDelegate}
     */
    public void setAuthenticationDelegate(@Nullable AuthenticationDelegate authenticationDelegate) {
        this.authenticationDelegate = authenticationDelegate;
    }
}
