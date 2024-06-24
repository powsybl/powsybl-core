/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public interface DataSourceUtil {

    static String getFileName(String baseName, String suffix, String ext) {
        Objects.requireNonNull(baseName);
        return baseName + (suffix != null ? suffix : "") + (ext == null || ext.isEmpty() ? "" : "." + ext);
    }

    static OpenOption[] getOpenOptions(boolean append) {
        OpenOption[] defaultOpenOptions = {StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE};
        OpenOption[] appendOpenOptions = {StandardOpenOption.APPEND, StandardOpenOption.WRITE};

        return append ? appendOpenOptions : defaultOpenOptions;
    }

    static String getBaseName(Path file) {
        return getBaseName(file.getFileName().toString());
    }

    static String getBaseName(String fileName) {
        Objects.requireNonNull(fileName);
        int pos = fileName.indexOf('.'); // find first dot in case of double extension (.xml.gz)
        return pos == -1 ? fileName : fileName.substring(0, pos);
    }

    static String getMainExtension(String fileName) {
        Objects.requireNonNull(fileName);
        int pos = fileName.indexOf('.'); // find first dot in case of double extension (.xml.gz)
        return pos == -1 ? "" : fileName.substring(pos + 1);
    }

    static DataSource createDataSource(Path directory, String basename, CompressionFormat compressionExtension, DataSourceObserver observer) {
        return createDataSource(directory, basename, "", compressionExtension, observer);
    }

    static DataSource createDataSource(Path directory, String basename, String mainExtension, CompressionFormat compressionExtension, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(basename);

        if (compressionExtension == null) {
            return new FileDataSource(directory, basename, mainExtension, observer);
        } else {
            switch (compressionExtension) {
                case BZIP2:
                    return new Bzip2FileDataSource(directory, basename, mainExtension, observer);
                case GZIP:
                    return new GzFileDataSource(directory, basename, mainExtension, observer);
                case XZ:
                    return new XZFileDataSource(directory, basename, mainExtension, observer);
                case ZIP:
                    return new ZipFileDataSource(directory, basename + ".zip", basename, mainExtension, observer);
                case ZSTD:
                    return new ZstdFileDataSource(directory, basename, mainExtension, observer);
                default:
                    throw new IllegalStateException("Unexpected CompressionFormat value: " + compressionExtension);
            }
        }
    }

    static DataSource createDataSource(Path directory, String fileNameOrBaseName, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(fileNameOrBaseName);

        if (fileNameOrBaseName.endsWith(".zst")) {
            String filenameWithoutCompressionExtension = fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 4);
            return new ZstdFileDataSource(directory, getBaseName(filenameWithoutCompressionExtension), getMainExtension(filenameWithoutCompressionExtension), observer);
        } else if (fileNameOrBaseName.endsWith(".zip")) {
            String filenameWithoutCompressionExtension = fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 4);
            return new ZipFileDataSource(directory, filenameWithoutCompressionExtension + ".zip", getBaseName(filenameWithoutCompressionExtension), getMainExtension(filenameWithoutCompressionExtension), observer);
        } else if (fileNameOrBaseName.endsWith(".xz")) {
            String filenameWithoutCompressionExtension = fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 3);
            return new XZFileDataSource(directory, getBaseName(filenameWithoutCompressionExtension), getMainExtension(filenameWithoutCompressionExtension), observer);
        } else if (fileNameOrBaseName.endsWith(".gz")) {
            String filenameWithoutCompressionExtension = fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 3);
            return new GzFileDataSource(directory, getBaseName(filenameWithoutCompressionExtension), getMainExtension(filenameWithoutCompressionExtension), observer);
        } else if (fileNameOrBaseName.endsWith(".bz2")) {
            String filenameWithoutCompressionExtension = fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 4);
            return new Bzip2FileDataSource(directory, getBaseName(filenameWithoutCompressionExtension), getMainExtension(filenameWithoutCompressionExtension), observer);
        } else {
            return new FileDataSource(directory, getBaseName(fileNameOrBaseName), getMainExtension(fileNameOrBaseName), observer);
        }
    }

}
