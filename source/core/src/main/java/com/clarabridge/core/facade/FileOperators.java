package com.clarabridge.core.facade;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.inject.Inject;

/**
 * Wrapper for providing classes used to operate on {@link File} objects.
 */
class FileOperators {

    @Inject
    FileOperators() {
        // Intentionally empty
    }

    /**
     * Gets an instance of {@link FileWriter} for writing data to the given {@link File} object
     *
     * @param file The {@link File} object to be written to
     * @return an instance of {@link FileWriter}
     * @throws IOException if the file could not be found or modified
     */
    FileWriter getFileWriter(File file) throws IOException {
        return new FileWriter(file);
    }

    /**
     * Gets an instance of {@link FileReader} for reading from the given file
     *
     * @param file The {@link File} object to read from
     * @return an instance of {@link FileReader}
     * @throws FileNotFoundException if the file could not be found
     */
    FileReader getFileReader(File file) throws FileNotFoundException {
        return new FileReader(file);
    }

}
