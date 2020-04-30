package com.powsybl.commons.datastore;

import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Objects;

import com.google.common.io.Files;

public interface DataStoreUtil {

    /*
    static DataStore createDataStore(Path directory, String basename, CompressionFormat compressionExtension, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(basename);

        if (compressionExtension == null) {
            return new FileDataSource(directory, basename, observer);
        } else {
            switch (compressionExtension) {
                case GZIP:
                    return new GzFileDataSource(directory, basename, observer);
                case BZIP2:
                    return new Bzip2FileDataSource(directory, basename, observer);
                case ZIP:
                    return new ZipFileDataSource(directory, basename, observer);
                default:
                    throw new AssertionError("Unexpected CompressionFormat value: " + compressionExtension);
            }
        }
    }
    */

    static DataStore createDataStore(Path directory) throws NotDirectoryException {
        Objects.requireNonNull(directory);
        return new DirectoryDataStore(directory);
        /*
        if (fileNameOrBaseName.endsWith(".zip")) {
            return null;
            //return new ZipFileDataSource(directory, getBaseName(fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 4)), observer);
        } else if (fileNameOrBaseName.endsWith(".gz")) {
            return null;
            //return new GzFileDataSource(directory, getBaseName(fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 3)), observer);
        } else if (fileNameOrBaseName.endsWith(".bz2")) {
            return null;
            //return new Bzip2FileDataSource(directory, getBaseName(fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 4)), observer);
        } else {
            return new DirectoryDataStore(directory);
        }
        */
    }

    static String getBasename(String fileName) {
        return Files.getNameWithoutExtension(fileName);
    }
}
