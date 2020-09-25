package com.clarabridge.core.facade;

import android.content.SharedPreferences;
import androidx.annotation.Nullable;

/**
 * Storage implementation for storing primitive values
 */
class SharedPreferencesStorage implements BaseStorage {

    private static final String LOG_TAG = "SharedPreferencesStorage";

    private SharedPreferences sharedPreferences;
    private Serialization serializer;

    /**
     * Constructs an instance of {@link SharedPreferencesStorage} to use {@link SharedPreferences} for persistence
     *
     * @param sharedPreferences an instance of {@link SharedPreferences}
     */
    SharedPreferencesStorage(
            SharedPreferences sharedPreferences,
            Serialization serializer) {
        this.sharedPreferences = sharedPreferences;
        this.serializer = serializer;
    }

    @Override
    public void put(String key, @Nullable Object data) {
        String value = serializer.serialize(data);
        sharedPreferences.edit().putString(key, value).apply();
    }

    @Nullable
    @Override
    public <T> T get(String key, Class<T> clazz) {
        String value = sharedPreferences.getString(key, null);
        return serializer.deserialize(value, clazz);
    }

    @Override
    public void remove(String key) {
        sharedPreferences.edit().remove(key).apply();
    }

    @Override
    public void clear() {
        sharedPreferences.edit().clear().apply();
    }

}
