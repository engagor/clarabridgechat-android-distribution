package com.clarabridge.core.facade;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.clarabridge.core.di.SdkScope;

/**
 * Factory for creating instances of {@link SharedPreferencesStorageFactory}
 */
@SdkScope
class SharedPreferencesStorageFactory {

    private Map<String, SharedPreferencesStorage> sharedPreferencesStorageMap;
    private Context context;
    private Serialization serializer;

    /**
     * Creates an instance of {@link FileStorageFactory}
     *
     * @param context    an instance of {@link Context} for retrieving an instance of {@link SharedPreferences}
     * @param serializer an instance of {@link Serialization}
     *                   for serialising objects
     */
    @Inject
    SharedPreferencesStorageFactory(
            Context context,
            Serialization serializer) {
        this.context = context;
        this.serializer = serializer;
        this.sharedPreferencesStorageMap = new HashMap<>();
    }

    /**
     * Retrieves a cached instance of {@link SharedPreferencesStorage} if one exists for the given key,
     * or creates and caches a new instance.
     *
     * @param key The key for accessing an instance of {@link SharedPreferencesStorage}
     * @return an instance of {@link SharedPreferencesStorage}
     */
    SharedPreferencesStorage create(String key) {
        if (!sharedPreferencesStorageMap.containsKey(key)) {
            SharedPreferencesStorage storage = new SharedPreferencesStorage(
                    context.getSharedPreferences(key, Context.MODE_PRIVATE),
                    serializer
            );
            sharedPreferencesStorageMap.put(key, storage);
        }
        return sharedPreferencesStorageMap.get(key);
    }
}
