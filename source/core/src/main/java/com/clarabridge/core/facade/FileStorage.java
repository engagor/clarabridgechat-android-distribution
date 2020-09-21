package com.clarabridge.core.facade;

import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.clarabridge.core.Logger;

/**
 * Storage implementation for storing object types to a file
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
class FileStorage implements BaseStorage {

    private static final String LOG_TAG = "FileStorage";

    private File directory;
    private Serialization serializer;
    private FileOperators fileOperators;

    /**
     * Constructs an instance of {@link FileStorage}
     *
     * @param directory The directory {@link File} to be maintained by this instance
     * @param serializer An instance of {@link Serialization}
     * @param fileOperators An instance of {@link FileOperators} for retrieving dependencies used for file I/O
     */
    FileStorage(
            File directory,
            Serialization serializer,
            FileOperators fileOperators) {
        this.directory = directory;
        this.serializer = serializer;
        this.fileOperators = fileOperators;
    }

    @Override
    public void put(String key, @Nullable Object data) {
        String value = serializer.serialize(data);
        try {
            FileWriter fileWriter = fileOperators.getFileWriter(getFile(key));
            fileWriter.write(value);
            fileWriter.close();
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Unable to write data to file (%s)", key);
        }
    }

    @Nullable
    @Override
    public <T> T get(String key, Class<T> clazz) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            FileReader fileReader = fileOperators.getFileReader(getFile(key));
            int character = fileReader.read();
            while (character != -1) {
                stringBuilder.append((char) character);
                character = fileReader.read();
            }
        } catch (IOException e) {
            Logger.e(LOG_TAG, "Unable to read data from file (%s)", key);
            return null;
        }
        return serializer.deserialize(stringBuilder.toString(), clazz);
    }

    @Override
    public void remove(String key) {
        getFile(key).delete();
    }

    @Override
    public void clear() {
        recursiveClear(directory);
    }

    /**
     * Recursively deletes all files and directories within the current file scope
     *
     * @param currentFile the file from which we are operating down
     */
    private void recursiveClear(File currentFile) {
        if (!currentFile.isDirectory()) {
            currentFile.delete();
            return;
        }
        File[] files = currentFile.listFiles();
        if (files != null && files.length > 0) {
            for (File nestedFile: files) {
                recursiveClear(nestedFile);
            }
        }
        currentFile.delete();
    }

    /**
     * Gets an instance of {@link File} with the given key as its name. Will retrieve
     * an existing file or create a new one if there is none.
     *
     * @param key The name of the file
     * @return An existing file if any, or a new file instance
     */
    @VisibleForTesting
    File getFile(String key) {
        if (!directory.isDirectory()) {
            directory.mkdirs();
        }

        File[] files = directory.listFiles();
        if (files != null && files.length > 0) {
            for (File file: files) {
                if (file.getName().equals(key)) {
                    return file;
                }
            }
        }

        return new File(directory.getPath(), key);
    }
}
