package com.clarabridge.core.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.clarabridge.core.annotation.LocalField;
import com.clarabridge.core.utils.StringUtils;

public class AppUserDto implements Serializable {
    @SerializedName("_id")
    private String appUserId;
    @SerializedName("userId")
    private String userId;
    @SerializedName("givenName")
    private String firstName;
    @SerializedName("surname")
    private String lastName;
    @SerializedName("email")
    private String email;
    @SerializedName("signedUpAt")
    private String signedUpAt;
    @SerializedName("properties")
    private Map<String, Object> properties;
    @SerializedName("hasPaymentInfo")
    private boolean hasPaymentInfo;
    @LocalField
    @SerializedName("modified")
    private Boolean modified;

    public void update(AppUserDto rhs) {
        this.appUserId = rhs.appUserId;
        this.userId = rhs.userId;
        this.firstName = rhs.firstName;
        this.lastName = rhs.lastName;
        this.email = rhs.email;
        this.signedUpAt = rhs.signedUpAt;
        this.properties = rhs.properties;
        this.hasPaymentInfo = rhs.hasPaymentInfo;
    }

    // Merge modifiable properties, preserving rhs
    public void merge(AppUserDto rhs) {
        if (rhs.firstName != null && !StringUtils.isEqual(firstName, rhs.firstName)) {
            firstName = rhs.firstName;
        }

        if (rhs.lastName != null && !StringUtils.isEqual(lastName, rhs.lastName)) {
            lastName = rhs.lastName;
        }

        if (rhs.email != null && !StringUtils.isEqual(email, rhs.email)) {
            email = rhs.email;
        }

        if (rhs.signedUpAt != null && !StringUtils.isEqual(signedUpAt, rhs.signedUpAt)) {
            signedUpAt = rhs.signedUpAt;
        }

        mergeProperties(rhs.properties);
    }

    public String getAppUserId() {
        return appUserId;
    }

    public void setAppUserId(String appUserId) {
        this.appUserId = appUserId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSignedUpAt() {
        return signedUpAt;
    }

    public void setSignedUpAt(String signedUpAt) {
        this.signedUpAt = signedUpAt;
    }

    public Map<String, Object> getProperties() {
        if (properties == null) {
            properties = new HashMap<>();
        }

        return properties;
    }

    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }

    public Boolean getModified() {
        if (modified == null) {
            modified = false;
        }

        return modified;
    }

    public void setModified(Boolean modified) {
        this.modified = modified;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public boolean getHasPaymentInfo() {
        return hasPaymentInfo;
    }

    public void setHasPaymentInfo(boolean hasPaymentInfo) {
        this.hasPaymentInfo = hasPaymentInfo;
    }

    private void mergeProperties(Map<String, Object> survivingProps) {
        final Map<String, Object> mergedProps = new HashMap<>();

        if (properties != null) {
            // Add remote properties
            for (final Map.Entry<String, Object> entry : properties.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();

                if (value != null) {
                    mergedProps.put(key, value);
                }
            }
        }

        if (survivingProps != null) {
            // Override any keys with local properties if present
            for (final Map.Entry<String, Object> entry : survivingProps.entrySet()) {
                final String key = entry.getKey();
                final Object value = entry.getValue();

                if (value != null) {
                    mergedProps.put(key, value);
                }
            }
        }

        properties = mergedProps;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AppUserDto that = (AppUserDto) o;

        if (hasPaymentInfo != that.hasPaymentInfo) {
            return false;
        }
        if (appUserId != null ? !appUserId.equals(that.appUserId) : that.appUserId != null) {
            return false;
        }
        if (userId != null ? !userId.equals(that.userId) : that.userId != null) {
            return false;
        }
        if (firstName != null ? !firstName.equals(that.firstName) : that.firstName != null) {
            return false;
        }
        if (lastName != null ? !lastName.equals(that.lastName) : that.lastName != null) {
            return false;
        }
        if (email != null ? !email.equals(that.email) : that.email != null) {
            return false;
        }
        if (signedUpAt != null ? !signedUpAt.equals(that.signedUpAt) : that.signedUpAt != null) {
            return false;
        }
        if (properties != null ? !properties.equals(that.properties) : that.properties != null) {
            return false;
        }
        return modified != null ? modified.equals(that.modified) : that.modified == null;
    }

    @Override
    public int hashCode() {
        int result = appUserId != null ? appUserId.hashCode() : 0;
        result = 31 * result + (userId != null ? userId.hashCode() : 0);
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (signedUpAt != null ? signedUpAt.hashCode() : 0);
        result = 31 * result + (properties != null ? properties.hashCode() : 0);
        result = 31 * result + (hasPaymentInfo ? 1 : 0);
        result = 31 * result + (modified != null ? modified.hashCode() : 0);
        return result;
    }
}

