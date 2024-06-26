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

    static String getBaseExtension(String fileName) {
        Objects.requireNonNull(fileName);
        int firstpos = fileName.indexOf('.'); // find first dot in case of double extension (.xml.gz)
        int secondpos = fileName.indexOf('.', firstpos + 1); // find second dot in case of double extension (.xml.gz)
        if (secondpos == -1) {
            secondpos = fileName.length();
        }
        return firstpos == -1 ? "" : fileName.substring(firstpos + 1, secondpos);
    }

    static DataSource createDataSource(Path directory, String basename, CompressionFormat compressionExtension, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(basename);

        if (compressionExtension == null) {
            return new FileDataSource(directory, basename, observer);
        } else {
            switch (compressionExtension) {
                case BZIP2:
                    return new Bzip2FileDataSource(directory, basename, observer);
                case GZIP:
                    return new GzFileDataSource(directory, basename, observer);
                case XZ:
                    return new XZFileDataSource(directory, basename, observer);
                case ZIP:
                    return new ZipFileDataSource(directory, basename, observer);
                case ZSTD:
                    return new ZstdFileDataSource(directory, basename, observer);
                default:
                    throw new IllegalStateException("Unexpected CompressionFormat value: " + compressionExtension);
            }
        }
    }

    static DataSource createDataSource(Path directory, String fileNameOrBaseName, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(fileNameOrBaseName);

        if (fileNameOrBaseName.endsWith(".zst")) {
            String fileNameWithoutCompressionExtension = fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 4);
            return new ZstdFileDataSource(directory, getBaseName(fileNameWithoutCompressionExtension), getBaseExtension(fileNameWithoutCompressionExtension), observer);
        } else if (fileNameOrBaseName.endsWith(".zip")) {
            //TODO may be useful to allow to have mainExtension for zips.
            //It is Not as important as others because zips are supposed to contain only related files.
            //And we need to rework the constructors into static methods to disambiguate
            //betweenzipfilename + basename or basename + baseextension
            //This would allow something like network.xiidm.zip vs network.zip
            return new ZipFileDataSource(directory, fileNameOrBaseName, getBaseName(fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 4)), observer);
        } else if (fileNameOrBaseName.endsWith(".xz")) {
            String fileNameWithoutCompressionExtension = fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 3);
            return new XZFileDataSource(directory, getBaseName(fileNameWithoutCompressionExtension), getBaseExtension(fileNameWithoutCompressionExtension), observer);
        } else if (fileNameOrBaseName.endsWith(".gz")) {
            String fileNameWithoutCompressionExtension = fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 3);
            return new GzFileDataSource(directory, getBaseName(fileNameWithoutCompressionExtension), getBaseExtension(fileNameWithoutCompressionExtension), observer);
        } else if (fileNameOrBaseName.endsWith(".bz2")) {
            String fileNameWithoutCompressionExtension = fileNameOrBaseName.substring(0, fileNameOrBaseName.length() - 4);
            return new Bzip2FileDataSource(directory, getBaseName(fileNameWithoutCompressionExtension), getBaseExtension(fileNameWithoutCompressionExtension), observer);
        } else {
            return new FileDataSource(directory, getBaseName(fileNameOrBaseName), getBaseExtension(fileNameOrBaseName), observer);
        }
    }
}
