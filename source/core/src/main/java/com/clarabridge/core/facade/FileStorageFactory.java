package com.clarabridge.core.facade;

import android.content.Context;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import com.clarabridge.core.di.SdkScope;

/**
 * Factory for creating instances of {@link FileStorage}
 */
@SdkScope
class FileStorageFactory {

    @SuppressWarnings("FieldCanBeLocal")
    private static String FILE_STORAGE_NAMESPACE = "-file-storage";

    private Context context;
    private Serialization serializer;
    private FileOperators fileOperators;
    private Map<String, BaseStorage> fileStorageMap;

    /**
     * Creates an instance of {@link FileStorageFactory}
     *
     * @param context       an instance of {@link Context} for retrieving the app internal files directory
     * @param serializer    an instance of {@link Serialization}
     *                      for serialising objects
     * @param fileOperators an instance of {@link FileOperators}
     */
    @Inject
    FileStorageFactory(
            Context context,
            Serialization serializer,
            FileOperators fileOperators) {
        this.context = context;
        this.serializer = serializer;
        this.fileOperators = fileOperators;
        this.fileStorageMap = new HashMap<>();
    }

    /**
     * Retrieves a cached instance of {@link FileStorage} if one exists for the given key, or creates
     * and caches a new instance.
     *
     * @param key The key for accessing an instance of {@link FileStorage}
     * @return an instance of {@link FileStorage}
     */
    BaseStorage create(String key) {
        if (!fileStorageMap.containsKey(key)) {
            File directory = new File(context.getFilesDir(), key);
            FileStorage fileStorage = new FileStorage(
                    directory,
                    serializer,
                    fileOperators
            );
            SynchronizedStorage synchronizedStorage = new SynchronizedStorage(
                    fileStorage,
                    FILE_STORAGE_NAMESPACE
            );
            fileStorageMap.put(key, synchronizedStorage);
        }
        return fileStorageMap.get(key);
    }
}
