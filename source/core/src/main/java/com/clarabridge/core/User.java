package com.clarabridge.core;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;

import java.util.Date;
import java.util.Map;

import com.clarabridge.core.model.AppUserDto;
import com.clarabridge.core.utils.DateUtils;
import com.clarabridge.core.utils.StringUtils;

/**
 * Object representing the current user.
 */
public class User {
    private static final int MAX_PROPERTY_KEY_SIZE = 100;
    private static final int MAX_PROPERTY_VALUE_SIZE = 800;
    private static final String TAG = "User";

    private static User sharedInstance = null;

    /**
     * Returns the object representing the current user.
     *
     * @return The current user
     */
    @NonNull
    public static User getCurrentUser() {
        synchronized (User.class) {
            if (sharedInstance == null) {
                sharedInstance = new User();
            }

            return sharedInstance;
        }
    }

    /**
     * The user's email, to be used to display a gravatar.
     *
     * @param email The email
     */
    public void setEmail(@Nullable String email) {
        AppUserDto appUser = getLocalUser();

        if (appUser == null) {
            logError();
            return;
        }

        if (!StringUtils.isEqual(appUser.getEmail(), email)) {
            appUser.setEmail(email);
            appUser.setModified(true);

            sync();
        }
    }

    /**
     * The user's first name, to be used as part of the display name when sending messages.
     *
     * @param firstName The first name
     */
    public void setFirstName(@Nullable String firstName) {
        AppUserDto appUser = getLocalUser();

        if (appUser == null) {
            logError();
            return;
        }

        if (!StringUtils.isEqual(appUser.getFirstName(), firstName)) {
            appUser.setFirstName(firstName);
            appUser.setModified(true);

            sync();
        }
    }

    /**
     * The user's last name, to be used as part of the display name when sending messages.
     *
     * @param lastName The last name
     */
    public void setLastName(@Nullable String lastName) {
        AppUserDto appUser = getLocalUser();

        if (appUser == null) {
            logError();
            return;
        }

        if (!StringUtils.isEqual(appUser.getLastName(), lastName)) {
            appUser.setLastName(lastName);
            appUser.setModified(true);

            sync();
        }
    }

    /**
     * The date the user started using your service
     *
     * @param signedUpAt The sign up date
     */
    public void setSignedUpAt(@Nullable Date signedUpAt) {
        AppUserDto appUser = getLocalUser();

        if (appUser == null) {
            logError();
            return;
        }

        String iso = signedUpAt != null ? DateUtils.toIso(signedUpAt) : null;

        if (!StringUtils.isEqual(appUser.getSignedUpAt(), iso)) {
            appUser.setSignedUpAt(iso);
            appUser.setModified(true);

            sync();
        }
    }

    /**
     * Adds custom metadata to the user. This info is used to provide more context around who a user is.
     * <p>
     * Values must be of type String, Integer, Long, Float, Double, or Date; any other type will be converted
     * to String using the toString() method.
     * <p>
     * Changes to user metadata are uploaded in batches at regular intervals, when the app is sent
     * to the background, or when a message is sent. This API is additive, and subsequent calls will
     * override values for the provided keys.
     *
     * @param metadata Map of metadata
     */
    public void addMetadata(@NonNull Map<String, Object> metadata) {
        AppUserDto appUser = getLocalUser();

        if (appUser == null) {
            logError();
            return;
        }

        Map<String, Object> currentMetadata = appUser.getMetadata();

        for (Map.Entry<String, Object> prop : metadata.entrySet()) {
            String key = prop.getKey();

            if (key.length() <= MAX_PROPERTY_KEY_SIZE) {
                boolean modified;

                Object rawValue = prop.getValue();
                Object value;

                if (rawValue == null) {
                    value = null;
                } else if (rawValue instanceof Boolean
                        || rawValue instanceof Integer
                        || rawValue instanceof Long
                        || rawValue instanceof Float
                        || rawValue instanceof Double) {
                    value = rawValue;
                } else if (rawValue instanceof Date) {
                    value = DateUtils.toIso((Date) prop.getValue());
                } else {
                    String stringValue = prop.getValue().toString();

                    if (stringValue.length() <= MAX_PROPERTY_VALUE_SIZE) {
                        value = stringValue;
                    } else {
                        value = stringValue.substring(0, MAX_PROPERTY_VALUE_SIZE);
                    }
                }

                if (value == null) {
                    modified = currentMetadata.put(key, null) != null;
                } else {
                    Object replaced = currentMetadata.put(key, value);
                    modified = !value.equals(replaced);
                }

                if (modified) {
                    appUser.setModified(true);
                }
            }
        }

        sync();
    }

    /**
     * String representing the user's email
     *
     * @return The user's email
     */
    @Nullable
    public String getEmail() {
        AppUserDto appUser = getMergedUser();

        if (appUser == null) {
            logError();
            return null;
        }

        return appUser.getEmail();
    }

    /**
     * String representing the user's first name
     *
     * @return The user's first name
     */
    @Nullable
    public String getFirstName() {
        AppUserDto appUser = getMergedUser();

        if (appUser == null) {
            logError();
            return null;
        }

        return appUser.getFirstName();
    }

    /**
     * String representing the user's last name
     *
     * @return The user's last name
     */
    @Nullable
    public String getLastName() {
        AppUserDto appUser = getMergedUser();

        if (appUser == null) {
            logError();
            return null;
        }

        return appUser.getLastName();
    }

    /**
     * Date representing the date the user signed up
     *
     * @return The date the user signed up
     */
    @Nullable
    public Date getSignedUpAt() {
        AppUserDto appUser = getMergedUser();

        if (appUser == null) {
            logError();
            return null;
        }

        return DateUtils.fromIso(appUser.getSignedUpAt());
    }

    /**
     * Map representing the user's custom metadata
     *
     * @return The user's custom metadata
     */
    @Nullable
    public Map<String, Object> getMetadata() {
        AppUserDto appUser = getMergedUser();

        if (appUser == null) {
            logError();
            return null;
        }

        return appUser.getMetadata();
    }

    /**
     * String representing the user's externalId. This property is set by calling
     * {@link ClarabridgeChat#login(String, String, ClarabridgeChatCallback)}.
     * <p>
     * Unlike <code>userId</code>, this value is assigned and managed by the developer, and is
     * used to identify a user across devices and app installations.
     *
     * @return The user's externalId
     */
    @Nullable
    public String getExternalId() {
        AppUserDto appUser = getMergedUser();

        if (appUser == null) {
            logError();
            return null;
        }

        return appUser.getExternalId();
    }


    /**
     * The assigned userId for this user.
     * <p>
     * Unlike {@link #getExternalId()}, this property is set automatically by ClarabridgeChat, and is not configurable.
     * This is analogous to <code>appUser._id</code> in the REST API response. A user is assigned an
     * <code>userId</code> once they start a conversation or when an existing user is logged in
     * by calling {@link ClarabridgeChat#login(String, String, ClarabridgeChatCallback)}.
     *
     * @return The user's userId
     */
    @Nullable
    public String getUserId() {
        AppUserDto appUser = getMergedUser();

        if (appUser == null) {
            logError();
            return null;
        }

        return appUser.getUserId();
    }

    /**
     * Boolean describing whether or not the user has payment information stored
     *
     * @return Boolean indicating whether or not the User has payment info stored
     */
    public boolean hasPaymentInfo() {
        AppUserDto appUser = getMergedUser();

        if (appUser == null) {
            logError();
            return false;
        }

        return appUser.getHasPaymentInfo();
    }

    @Nullable
    AppUserDto getRemoteUser() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();

        if (clarabridgeChatInternal != null) {
            return clarabridgeChatInternal.getRemoteUser();
        }

        return null;
    }

    @Nullable
    private AppUserDto getLocalUser() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();

        if (clarabridgeChatInternal != null) {
            return clarabridgeChatInternal.getLocalUser();
        }

        return null;
    }

    @Nullable
    private AppUserDto getMergedUser() {
        AppUserDto localAppUser = getLocalUser();
        AppUserDto remoteAppUser = getRemoteUser();

        if (localAppUser != null && remoteAppUser != null) {
            AppUserDto mergedAppUser = new AppUserDto();

            mergedAppUser.update(remoteAppUser);
            mergedAppUser.merge(localAppUser);

            return mergedAppUser;
        }

        return null;
    }

    private void sync() {
        ClarabridgeChatInternal clarabridgeChatInternal = ClarabridgeChat.getInstance();

        if (clarabridgeChatInternal != null) {
            clarabridgeChatInternal.syncAppUser();
        }
    }

    private void logError() {
        Log.e(TAG, "You must initialize before setting user metadata. Ignoring.");
    }
}

