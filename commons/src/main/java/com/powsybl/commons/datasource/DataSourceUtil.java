/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.powsybl.commons.PowsyblException;

import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * This class consists of {@code static} utility methods for creating new data sources. The methods here are limited to
 * creating data sources of the classes implemented in powsybl-core.
 *
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

    static String guessBaseName(Path file) {
        return guessBaseName(file.getFileName().toString());
    }

    static String guessBaseName(String fileName) {
        Objects.requireNonNull(fileName);

        // Get the file information
        FileInformation fileInformation = new FileInformation(fileName);

        return fileInformation.getBaseName();
    }

    /**
     * Create an archive-based DataSource based on the provided base name, source format, archive format and compression
     * format
     * @param directory Path to the folder where the file is located
     * @param archiveFileName Archive file name
     * @param baseName base name for the files in the data source
     * @param sourceFormatExtension Data format extension
     * @param archiveFormat Archive format
     * @param compressionFormat Compression format
     * @param observer Observer
     * @return the created datasource
     */
    private static DataSource createArchiveDataSource(Path directory, String archiveFileName, String baseName, String sourceFormatExtension, ArchiveFormat archiveFormat, CompressionFormat compressionFormat, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(baseName);

        // Create the datasource
        return new DataSourceBuilder()
            .withDirectory(directory)
            .withArchiveFileName(archiveFileName)
            .withBaseName(baseName)
            .withSourceFormatExtension(sourceFormatExtension)
            .withArchiveFormat(archiveFormat)
            .withCompressionFormat(compressionFormat)
            .withObserver(observer)
            .build();
    }

    /**
     * Create an archive-based DataSource based on the provided base name, source format, archive format and compression
     * format
     * @param directory Path to the folder where the file is located
     * @param baseName base name for the files in the data source
     * @param sourceFormatExtension Data format extension
     * @param archiveFormat Archive format
     * @param compressionFormat Compression format
     * @param observer Observer
     * @return the created datasource
     */
    static DataSource createArchiveDataSource(Path directory, String baseName, String sourceFormatExtension, ArchiveFormat archiveFormat, CompressionFormat compressionFormat, DataSourceObserver observer) {
        return createArchiveDataSource(directory, null, baseName, sourceFormatExtension, archiveFormat, compressionFormat, observer);
    }

    /**
     * Create an archive-based DataSource based on the provided base name, source format, archive format and compression
     * format
     * @param directory Path to the folder where the file is located
     * @param archiveFileName Archive file name
     * @param baseName base name for the files in the data source
     * @param sourceFormatExtension Data format extension
     * @param observer Observer
     * @return the created datasource
     */
    static DataSource createArchiveDataSource(Path directory, String archiveFileName, String baseName, String sourceFormatExtension, DataSourceObserver observer) {

        // Get the file information
        FileInformation fileInformation = new FileInformation(archiveFileName);

        return createArchiveDataSource(directory, archiveFileName, baseName, sourceFormatExtension, fileInformation.getArchiveFormat(), fileInformation.getCompressionFormat(), observer);
    }

    /**
     * Create an archive-based DataSource based on the provided file name, base name and source format
     * @param directory Path to the folder where the file is located
     * @param archiveFileName Archive file name
     * @param baseName base name for the files in the data source
     * @param sourceFormatExtension Data format extension
     * @return the created datasource
     */
    static DataSource createArchiveDataSource(Path directory, String archiveFileName, String baseName, String sourceFormatExtension) {
        return createArchiveDataSource(directory, archiveFileName, baseName, sourceFormatExtension, null);
    }

    /**
     * Create a directory-based DataSource based on the provided base name, source format and compression format
     * @param directory Path to the folder where the file is located
     * @param baseName base name for the files in the data source
     * @param sourceFormatExtension Data format extension
     * @param compressionFormat Compression format
     * @param observer Observer
     * @return the created datasource
     */
    static DataSource createDirectoryDataSource(Path directory, String baseName, String sourceFormatExtension, CompressionFormat compressionFormat, DataSourceObserver observer) {
        Objects.requireNonNull(directory);
        Objects.requireNonNull(baseName);

        // Create the datasource
        return new DataSourceBuilder()
            .withDirectory(directory)
            .withBaseName(baseName)
            .withSourceFormatExtension(sourceFormatExtension)
            .withCompressionFormat(compressionFormat)
            .withObserver(observer)
            .build();
    }

    /**
     * Create a directory-based DataSource based on the provided base name and source format
     * @param directory Path to the folder where the file is located
     * @param baseName base name for the files in the data source
     * @param sourceFormatExtension Data format extension
     * @param observer Observer
     * @return the created datasource
     */
    static DataSource createDirectoryDataSource(Path directory, String baseName, String sourceFormatExtension, DataSourceObserver observer) {
        return createDirectoryDataSource(directory, baseName, sourceFormatExtension, null, observer);
    }

    /**
     * Create a directory-based DataSource based on the provided base name and source format
     * @param directory Path to the folder where the file is located
     * @param baseName base name for the files in the data source
     * @param sourceFormatExtension Data format extension
     * @return the created datasource
     */
    static DataSource createDirectoryDataSource(Path directory, String baseName, String sourceFormatExtension) {
        return createDirectoryDataSource(directory, baseName, sourceFormatExtension, null, null);
    }

    /**
     * Create a directory-based DataSource based on the base name
     * @param directory Path to the folder where the file is located
     * @param baseName base name for the files in the data source
     * @return the created datasource
     */
    static DataSource createDirectoryDataSource(Path directory, String baseName) {
        return createDirectoryDataSource(directory, baseName, "", null, null);
    }

    /**
     * Create a DataSource based on a filename
     * <ul>
     *     <li>Case 1: the file is not an archive so the datasource base name and source format is based on its name</li>
     *     <li>Case 2: the file is an archive, so the datasource base name and source format are not base on it in order
     *     to decorrelate the name of the archive from the name of the files inside</li>
     * </ul>
     * @param directory Path to the folder where the file is located
     * @param fileName Name of the file
     * @param observer Observer
     * @return the created datasource
     */
    static DataSource createDataSource(Path directory, String fileName, DataSourceObserver observer) {

        // Get the file information
        FileInformation fileInformation = new FileInformation(fileName);

        // Datasource creation
        return fileInformation.getArchiveFormat() == null ?
            createDirectoryDataSource(directory, fileInformation.getBaseName(), fileInformation.getSourceFormatExtension(), fileInformation.getCompressionFormat(), observer) :
            createArchiveDataSource(directory, fileName, "", "", fileInformation.getArchiveFormat(), fileInformation.getCompressionFormat(), observer);
    }

    /**
     * Create a DataSource based on a filename
     * <ul>
     *     <li>Case 1: the file is not an archive so the datasource base name and source format is based on its name</li>
     *     <li>Case 2: the file is an archive, so the datasource base name and source format are not base on it in order
     *     to decorrelate the name of the archive from the name of the files inside</li>
     * </ul>
     * @param directory Path to the folder where the file is located
     * @param fileName Name of the file
     * @return the created datasource
     */
    static DataSource createDataSource(Path directory, String fileName) {
        return createDataSource(directory, fileName, null);
    }

    /**
     * Create a DataSource based on a path
     * <ul>
     *     <li>Case A: the path corresponds to a directory so the base name and source format extensions are empty</li>
     *     <li>Case B-1: the path corresponds to a file and the file is not an archive so the datasource base name and
     *     source format is based on its name</li>
     *     <li>Case B-2: the path corresponds to a file and the file is an archive, so the datasource base name and
     *     source format are not base on it in order to decorrelate the name of the archive from the name of the files
     *     inside</li>
     * </ul>
     * @param filePath Path to the folder where the file is located
     * @return the created datasource
     */
    static DataSource createDataSource(Path path) {
        if (!Files.exists(path)) {
            throw new PowsyblException(String.format("Path %s should exist to create a data source", path));
        } else if (Files.isDirectory(path)) {
            return createDirectoryDataSource(path, "");
        } else {
            return createDataSource(path.getParent(), path.getFileName().toString(), null);
        }

    }
}
