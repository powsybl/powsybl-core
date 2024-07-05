/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import java.nio.file.Files;
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

    static DataSource createDataSource(Path directory, String basename, CompressionFormat compressionExtension, DataSourceObserver observer) {
        return createDataSource(directory, basename, null, compressionExtension, observer);
    }

    static DataSource createDataSource(Path directory, String basename, String dataExtension, CompressionFormat compressionFormat, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(basename);

        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder()
            .withDirectory(directory)
            .withBaseName(basename)
            .withDataExtension(dataExtension)
            .withCompressionFormat(compressionFormat)
            .withObserver(observer);

        // If a zip compression is asked
        if (compressionFormat == CompressionFormat.ZIP) {
            dataSourceBuilder.withArchiveFormat(ArchiveFormat.ZIP);
        }

        return dataSourceBuilder.build();
    }

    static DataSource createDataSource(Path directory, String fileNameOrBaseName, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(fileNameOrBaseName);

        // Get the file information
        FileInformation fileInformation = new FileInformation(fileNameOrBaseName);

        // If the file in the directory is in fact a nested directory,
        // we consider it as a curated directory and it should behave like an archive
        // (for example listNames should list everything, not filter by basename)
        Path maybeCuratedDirectory = directory.resolve(fileNameOrBaseName);
        boolean isCuratedDirectory = Files.isDirectory(maybeCuratedDirectory);

        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder()
            .withDirectory(isCuratedDirectory ? maybeCuratedDirectory : directory)
            .withArchiveFileName(fileNameOrBaseName)
            .withBaseName(fileInformation.getBaseName())
            .withDataExtension(fileInformation.getDataExtension())
            .withCompressionFormat(fileInformation.getCompressionFormat())
            .withArchiveFormat(fileInformation.getArchiveFormat())
            .withIsCuratedDirectory(isCuratedDirectory)
            .withObserver(observer);

        return dataSourceBuilder.build();
    }
}
