package com.clarabridge.core.facade;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.clarabridge.core.di.SdkScope;
import com.clarabridge.core.model.AppUserDto;
import com.clarabridge.core.model.ConversationDto;
import com.clarabridge.core.model.ConversationsListResponseDto;
import com.clarabridge.core.model.RetryConfigurationDto;
import com.clarabridge.core.model.UserSettingsDto;
import com.clarabridge.core.utils.StringUtils;

@SdkScope
public class PersistenceFacade {

    /**
     * Used for determining which storage mechanism should be used for storing items
     */
    private enum Type {
        SHARED_PREFERENCES,
        FILE,
    }

    private static final String RETRY_CONFIGURATION_KEY = "retryConfiguration";
    private static final String USER_SETTINGS_KEY = "userSettings";
    private static final String APP_USER_LOCAL_KEY = "appUserLocal";
    private static final String APP_USER_KEY = "appUser";
    private static final String JWT_KEY = "jwt";
    private static final String SESSION_TOKEN_KEY = "sessionToken";
    private static final String APP_USER_ID_KEY = "appUserId";
    private static final String CONVERSATIONS_LIST_KEY = "conversationsList";

    private static final String TEMPORARY_STORAGE_NAME = "temp_storage";

    private final SharedPreferencesStorageFactory sharedPreferencesStorageFactory;
    private final FileStorageFactory fileStorageFactory;

    private PersistenceCache cache;

    @Nullable
    private String integrationId;
    private String appId;
    private String userId;
    @Nullable
    private String stripeKey;

    /**
     * Constructs an instance of {@link PersistenceFacade}.
     *
     * @param sharedPreferencesStorageFactory an instance of {@link SharedPreferencesStorageFactory}
     * @param fileStorageFactory              an instance of {@link FileStorageFactory}
     */
    @Inject
    public PersistenceFacade(
            final SharedPreferencesStorageFactory sharedPreferencesStorageFactory,
            final FileStorageFactory fileStorageFactory) {
        this.sharedPreferencesStorageFactory = sharedPreferencesStorageFactory;
        this.fileStorageFactory = fileStorageFactory;
        this.cache = PersistenceCache.create();
    }

    /**
     * Constructs an instance of {@link PersistenceFacade}. This constructor should only be used for
     * testing. See {@link #PersistenceFacade(SharedPreferencesStorageFactory, FileStorageFactory)}
     *
     * @param context       an instance of {@link Context}
     * @param serialization an instance of {@link Serialization}
     */
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public PersistenceFacade(
            final Context context,
            final Serialization serialization) {
        this.sharedPreferencesStorageFactory = new SharedPreferencesStorageFactory(context, serialization);
        this.fileStorageFactory = new FileStorageFactory(context, serialization, new FileOperators());
        this.cache = PersistenceCache.create();
    }

    //region In-memory storage

    /**
     * @return The stored app ID
     */
    @Nullable
    public String getAppId() {
        return appId;
    }

    /**
     * Stores an app ID
     *
     * @param appId the ID to be stored
     */
    public void setAppId(final String appId) {
        this.cache = PersistenceCache.create();
        if (!StringUtils.isEmpty(appId)) {
            this.appId = appId;
            this.userId = getUserId();
        }
    }

    /**
     * @return The stored integration ID
     */
    @Nullable
    public String getIntegrationId() {
        return integrationId;
    }

    /**
     * Stores an integration ID
     *
     * @param integrationId the ID to be stored
     */
    public void setIntegrationId(@Nullable final String integrationId) {
        this.integrationId = integrationId;
    }

    /**
     * @return The stored Stripe key
     */
    @Nullable
    public String getStripeKey() {
        return stripeKey;
    }

    /**
     * Stores the provided Stripe key
     *
     * @param stripeKey the key to be stored
     */
    public void setStripeKey(@Nullable String stripeKey) {
        this.stripeKey = stripeKey;
    }
    //endregion

    // region Unscoped storage
    public RetryConfigurationDto getRetryConfiguration() {
        return getPersistence(StorageScope.UNSCOPED, Type.FILE)
                .get(RETRY_CONFIGURATION_KEY, RetryConfigurationDto.class);
    }

    public void saveRetryConfiguration(@NonNull final RetryConfigurationDto entity) {
        getPersistence(StorageScope.UNSCOPED, Type.FILE).put(RETRY_CONFIGURATION_KEY, entity);
    }
    // endregion

    // region App Scope storage
    public String getJwt() {
        return getPersistence(StorageScope.APP_ID, Type.SHARED_PREFERENCES).get(JWT_KEY, String.class);
    }

    public void saveJwt(final String jwt) {
        getPersistence(StorageScope.APP_ID, Type.SHARED_PREFERENCES).put(JWT_KEY, sanitizeJwt(jwt));
    }

    /**
     * Removes illegal characters from the JWT such as trailing/leading whitespace and line breaks.
     *
     * @param jwt the JWT to be sanitized
     * @return the sanitized JWT, or null if the given JWT was null
     */
    @Nullable
    private String sanitizeJwt(@Nullable String jwt) {
        if (jwt == null) {
            return null;
        }
        return jwt.replace("\n", "")
                .replace("\r", "")
                .trim();
    }

    @Nullable
    public String getUserId() {
        return getPersistence(StorageScope.APP_ID, Type.SHARED_PREFERENCES).get(APP_USER_ID_KEY, String.class);
    }

    public void saveUserId(@Nullable final String userId) {
        if (userId == null) {
            clearByScope(StorageScope.USER_ID);
        }
        this.cache = PersistenceCache.create();
        this.userId = userId;
        getPersistence(StorageScope.APP_ID, Type.SHARED_PREFERENCES).put(APP_USER_ID_KEY, userId);
    }

    @Nullable
    public String getSessionToken() {
        return getPersistence(StorageScope.APP_ID, Type.SHARED_PREFERENCES).get(SESSION_TOKEN_KEY, String.class);
    }

    public void saveSessionToken(@Nullable final String sessionToken) {
        getPersistence(StorageScope.APP_ID, Type.SHARED_PREFERENCES).put(SESSION_TOKEN_KEY, sessionToken);
    }

    @Nullable
    public UserSettingsDto getUserSettings() {
        return getPersistence(StorageScope.APP_ID, Type.FILE).get(USER_SETTINGS_KEY, UserSettingsDto.class);
    }

    public void saveUserSettings(@Nullable final UserSettingsDto entity) {
        getPersistence(StorageScope.APP_ID, Type.FILE).put(USER_SETTINGS_KEY, entity);
    }

    @Nullable
    public AppUserDto getAppUserLocal() {
        return getPersistence(StorageScope.APP_ID, Type.SHARED_PREFERENCES).get(APP_USER_LOCAL_KEY, AppUserDto.class);
    }

    public void saveAppUserLocal(@Nullable final AppUserDto entity) {
        getPersistence(StorageScope.APP_ID, Type.SHARED_PREFERENCES).put(APP_USER_LOCAL_KEY, entity);
    }

    @Nullable
    public AppUserDto getAppUserRemote() {
        return getPersistence(StorageScope.APP_ID, Type.SHARED_PREFERENCES).get(APP_USER_KEY, AppUserDto.class);
    }

    public void saveAppUserRemote(@Nullable final AppUserDto entity) {
        getPersistence(StorageScope.APP_ID, Type.SHARED_PREFERENCES).put(APP_USER_KEY, entity);
    }
    //endregion

    //region User Scope storage

    /**
     * @return the stored list of conversations or an empty list if the result was null
     */
    @NonNull
    public List<ConversationDto> getConversationsList() {
        List<ConversationDto> conversationsList = cache.getConversationsList();
        if (conversationsList.isEmpty()) {
            conversationsList = getConversationListFromFileStorage();
            cache.saveConversationsList(conversationsList);
        }
        return conversationsList;
    }

    /**
     * @param toIndex The index in which we want to get the list of conversations according to it.
     * @return the stored list of conversations withing a given offset or an empty list if the result was null
     */
    @NonNull
    public List<ConversationDto> getConversationsList(int toIndex) {
        List<ConversationDto> conversationsList = cache.getConversationsList(toIndex);
        return conversationsList.isEmpty() ? new ArrayList<ConversationDto>() : conversationsList;
    }

    /**
     * Save the given conversation list in storage
     *
     * @param conversationsList the list of {@link ConversationDto} to be stored
     */
    public void saveConversationsList(final List<ConversationDto> conversationsList) {
        // Only the first most updated 10 conversations will be stored in the local file storage and all the list
        // will be cached on memory.
        cache.saveConversationsList(conversationsList);
        ConversationsListResponseDto persistedList =
                new ConversationsListResponseDto(cache.getConversationsList(10));
        getPersistence(StorageScope.USER_ID, Type.FILE).put(CONVERSATIONS_LIST_KEY, persistedList);
    }

    /**
     * Remove specific conversation from conversation list from on-memory cache and file storage.
     *
     * @param conversationId the conversation ID.
     */
    public void removeConversationFromConversationList(String conversationId) {
        if (!cache.getConversationsList().isEmpty()) {
            List<ConversationDto> cacheConversationList = cache.getConversationsList();
            for (ConversationDto conversation : cacheConversationList) {
                if (StringUtils.isEqual(conversation.getId(), conversationId)) {
                    cache.removeConversationFromConversationList(conversation);

                    ConversationsListResponseDto persistedList =
                            new ConversationsListResponseDto(cache.getConversationsList());
                    getPersistence(StorageScope.USER_ID, Type.FILE).put(CONVERSATIONS_LIST_KEY, persistedList);
                    break;
                }
            }
        }
    }

    /**
     * Retrieves a conversation entity stored by the given conversation ID
     *
     * @param conversationId the ID of the conversation to be retrieved
     * @return the stored {@link ConversationDto}
     */
    @Nullable
    public ConversationDto getConversationById(final String conversationId) {
        if (conversationId == null) {
            return null;
        }

        return getPersistence(StorageScope.USER_ID, Type.FILE).get(conversationId, ConversationDto.class);
    }

    /**
     * Saves the given conversation entity by the given conversation ID
     *
     * @param conversationId the ID of the conversation being stored
     * @param entity         the {@link ConversationDto} to be stored
     */
    public void saveConversationById(final String conversationId, final ConversationDto entity) {
        getPersistence(StorageScope.USER_ID, Type.FILE).put(conversationId, entity);
    }

    /**
     * Updates the given conversation entity by the given conversation ID
     *
     * @param conversationId the ID of the conversation being stored
     * @param entity         the {@link ConversationDto} to be stored
     */
    public void updateConversation(final String conversationId, final ConversationDto entity) {
        if (!cache.getConversationsList().isEmpty()) {
            List<ConversationDto> cacheConversationList = cache.getConversationsList();
            for (ConversationDto conversation : cacheConversationList) {
                if (StringUtils.isEqual(conversation.getId(), conversationId)) {
                    cache.removeConversationFromConversationList(conversation);
                    cache.addConversationToConversationList(entity);
                    saveConversationById(conversationId, entity);
                    break;
                }
            }
        }
    }

    /**
     * Updates the given conversation entity by the given conversation ID
     *
     * @param conversationId the ID of the conversation being stored
     * @param entity         the {@link ConversationDto} to be stored
     */
    public void addConversation(final String conversationId, final ConversationDto entity) {
        boolean updated = false;
        if (!cache.getConversationsList().isEmpty()) {
            List<ConversationDto> cacheConversationList = cache.getConversationsList();
            for (ConversationDto conversation : cacheConversationList) {
                if (StringUtils.isEqual(conversation.getId(), conversationId)) {
                    cache.removeConversationFromConversationList(conversation);
                    cache.addConversationToConversationList(entity);
                    saveConversationById(conversationId, entity);
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                cache.addConversationToConversationList(entity);
                saveConversationById(conversationId, entity);
            }
        }
    }

    /**
     * Saves the hasMore we got from {@link com.clarabridge.core.model.ConversationsPaginationResponseDto} once
     *
     * @param hasMore is the boolean that indicates if there is more conversations to be fetched or not.
     */
    public void setHasMoreConversations(boolean hasMore) {
        cache.setHasMoreConversations(hasMore);
    }

    /**
     * @return if there is more conversations to be fetched or not.
     */
    public boolean isHasMoreConversations() {
        return cache.isHasMoreConversations();
    }
    // endregion

    // region Clear storage

    /**
     * Clear the conversation list that is cached on-memory
     */
    public void clearInMemoryCachedConversationList() {
        cache.clearConversationList();
    }

    /**
     * Clears all storage for all scopes
     */
    public void clearAll() {
        cache = PersistenceCache.create();
        clearByScope(StorageScope.UNSCOPED);
        clearByScope(StorageScope.APP_ID);
        clearByScope(StorageScope.USER_ID);
    }

    /**
     * Clears all storage for the given {@link StorageScope}
     *
     * @param scope the scope to be cleared
     */
    public void clearByScope(@NonNull StorageScope scope) {
        cache = PersistenceCache.create();
        for (Type type : Type.values()) {
            getPersistence(scope, type).clear();
        }
    }
    // endregion

    /**
     * Only the first most updated 10 conversations will be stored in the local file storage so this method must only
     * return the first most updated 10 conversations.
     *
     * @return the conversation list stored in the local file storage.
     */
    private List<ConversationDto> getConversationListFromFileStorage() {
        ConversationsListResponseDto persistedList = getPersistence(StorageScope.USER_ID, Type.FILE)
                .get(CONVERSATIONS_LIST_KEY, ConversationsListResponseDto.class);
        return (persistedList != null && persistedList.getConversations() != null)
                ? persistedList.getConversations()
                : new ArrayList<ConversationDto>();
    }

    /**
     * Retrieve an instance of {@link BaseStorage} appropriate for the {@link StorageScope}
     *
     * @param scope The desired {@link StorageScope} for this item to be stored
     * @param type  The {@link Type} of storage mechanism to be used
     * @return an instance of {@link BaseStorage}
     */
    @NonNull
    private BaseStorage getPersistence(
            @NonNull final StorageScope scope,
            @NonNull final Type type) {

        String storageName = getStorageName(scope);

        if (type == Type.SHARED_PREFERENCES) {
            return sharedPreferencesStorageFactory.create(storageName);
        } else {
            return fileStorageFactory.create(storageName);
        }
    }

    /**
     * Gets the name for accessing storage based on the {@link StorageScope} provided and the current
     * state of the SDK.
     *
     * @param scope the scope of storage to be accessed
     * @return the storage name to be used
     */
    @NonNull
    private String getStorageName(@NonNull final StorageScope scope) {
        switch (scope) {
            case UNSCOPED:
                return StorageScope.UNSCOPED.getDirectoryName();
            case USER_ID:
                return appId + ":" + StringUtils.encode(userId != null ? userId : TEMPORARY_STORAGE_NAME);
            case APP_ID:
                return appId != null ? appId : "null";
            default:
                // This should never be reached
                return null;
        }
    }
}

