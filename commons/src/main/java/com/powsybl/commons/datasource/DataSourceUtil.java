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

        // Get the file information
        FileInformation fileInformation = new FileInformation(fileName);

        return fileInformation.getBaseName();
    }

    static DataSource createDataSource(Path directory, String archiveFileName, String baseName, String sourceFormat, ArchiveFormat archiveFormat, CompressionFormat compressionFormat, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(baseName);

        // Create the datasource
        return new DataSourceBuilder()
            .withDirectory(directory)
            .withArchiveFileName(archiveFileName)
            .withBaseName(baseName)
            .withSourceFormat(sourceFormat)
            .withArchiveFormat(archiveFormat)
            .withCompressionFormat(compressionFormat)
            .withObserver(observer)
            .build();
    }

    static DataSource createDataSource(Path directory, String baseName, String sourceFormat, ArchiveFormat archiveFormat, CompressionFormat compressionFormat, DataSourceObserver observer) {
        return createDataSource(directory, null, baseName, sourceFormat, archiveFormat, compressionFormat, observer);
    }

    static DataSource createDataSource(Path directory, String baseName, CompressionFormat compressionFormat, DataSourceObserver observer) {
        return createDataSource(directory, null, baseName, "", null, compressionFormat, observer);
    }

    static DataSource createDataSource(Path directory, String fileName, DataSourceObserver observer) {

        // Get the file information
        FileInformation fileInformation = new FileInformation(fileName);

        // Datasource creation
        return fileInformation.getArchiveFormat() == null ?
            createDataSource(directory, null, fileInformation.getBaseName(), fileInformation.getSourceFormat(), fileInformation.getArchiveFormat(), fileInformation.getCompressionFormat(), observer) :
            createDataSource(directory, fileName, "", "", fileInformation.getArchiveFormat(), fileInformation.getCompressionFormat(), observer);
    }

    static DataSource createDataSource(Path directory, String fileName, String baseName, DataSourceObserver observer) {

        // Get the file information
        FileInformation fileInformation = new FileInformation(fileName);

        // Datasource creation
        return createDataSource(directory, fileName, baseName, fileInformation.getSourceFormat(), fileInformation.getArchiveFormat(), fileInformation.getCompressionFormat(), observer);
    }

    static DataSource createDataSource(Path directory, String fileName, String baseName) {
        return createDataSource(directory, fileName, baseName, null);
    }

    static DataSource createDataSource(Path directory, String fileName) {
        return createDataSource(directory, fileName, (DataSourceObserver) null);
    }
}
