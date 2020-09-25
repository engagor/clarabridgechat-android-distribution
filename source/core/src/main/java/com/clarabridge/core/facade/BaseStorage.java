package com.clarabridge.core.facade;

import androidx.annotation.Nullable;

/**
 * Base for all storage implementations
 */
interface BaseStorage {

    /**
     * Stores an {@link Object} against the given key
     *
     * @param key the key for identifying the data
     * @param data the data to be stored
     */
    void put(String key, @Nullable Object data);

    /**
     * Gets data stored by the given key, and deserializes it to the given {@link Class}
     *
     * @param key the key for identifying the data
     * @param clazz the type for deserialization
     * @return the stored data, or null if the type was invalid or nothing was stored
     */
    @Nullable
    <T> T get(String key, Class<T> clazz);

    /**
     * Removes the data stored by the given key
     *
     * @param key the key for identifying the data
     */
    void remove(String key);

    /**
     * Removes all data held by this instance of {@link SharedPreferencesStorage}
     */
    void clear();
}
