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
        FileInformation fileInformation = new FileInformation(fileName, false);
        return fileInformation.getBaseName();
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

    /**
     * Creates a {@link DataSource} from the given path. Note that the basename of the created {@link DataSource} is
     * <ul>
     *     <li>the file name if the path is a directory</li>
     *     <li>the base name guessed by {@link FileInformation} if the path is not a directory</li>
     * </ul>
     */
    static DataSource createDataSource(Path path, DataSourceObserver observer) {
        Objects.requireNonNull(path);

        DataSourceBuilder dataSourceBuilder = new DataSourceBuilder().withObserver(observer);
        if (Files.isDirectory(path)) {
            dataSourceBuilder.withDirectory(path)
                .withBaseName(path.getFileName().toString()) // note that we use the full directory name here instead of a parsed representation using FileInformation
                .withAllFiles(true);
        } else {
            Path absFile = path.toAbsolutePath();
            String fileName = absFile.getFileName().toString();

            // Get the file information
            FileInformation fileInformation = new FileInformation(fileName);

            dataSourceBuilder
                .withDirectory(absFile.getParent())
                .withArchiveFileName(fileName)
                .withBaseName(fileInformation.getBaseName())
                .withDataExtension(fileInformation.getDataExtension())
                .withCompressionFormat(fileInformation.getCompressionFormat())
                .withArchiveFormat(fileInformation.getArchiveFormat());
        }

        return dataSourceBuilder.build();
    }
}
