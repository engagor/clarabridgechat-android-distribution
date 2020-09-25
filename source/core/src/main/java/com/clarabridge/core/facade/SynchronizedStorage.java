package com.clarabridge.core.facade;

import androidx.annotation.Nullable;

/**
 * A {@link BaseStorage} decorator that synchronizes reading, writing, and removing.
 */
class SynchronizedStorage implements BaseStorage {

    private BaseStorage storage;
    private String namespace;

    /**
     * Constructs an instance of {@link SynchronizedStorage} to decorate the given {@link BaseStorage}.
     *
     * @param storage the storage implementation to be decorated
     * @param namespace the namespace of this storage mechanism to prevent unintended deadlock
     */
    SynchronizedStorage(BaseStorage storage, String namespace) {
        this.storage = storage;
        this.namespace = namespace;
    }

    @Override
    public void put(String key, @Nullable Object data) {
        synchronized ((key + namespace).intern()) {
            storage.put(key, data);
        }
    }

    @Nullable
    @Override
    public <T> T get(String key, Class<T> clazz) {
        synchronized ((key + namespace).intern()) {
            return storage.get(key, clazz);
        }
    }

    @Override
    public void remove(String key) {
        synchronized ((key + namespace).intern()) {
            storage.remove(key);
        }
    }

    @Override
    public void clear() {
        storage.clear();
    }
}
