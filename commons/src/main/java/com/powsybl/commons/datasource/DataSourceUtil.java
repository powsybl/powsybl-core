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

    static String getBaseName(Path file) {
        return getBaseName(file.getFileName().toString());
    }

    static String getBaseName(String fileName) {
        Objects.requireNonNull(fileName);

        // Get the file information
        FileInformation fileInformation = new FileInformation(fileName);

        return fileInformation.getBaseName();
    }

    /**
     * Create an archive-based DataSource based on the provided base name, source format extension, archive format and compression
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
     * Create an archive-based DataSource based on the provided base name, source format extension, archive format and compression
     * format
     * @param archivePath Path to the archive file
     * @param baseName base name for the files in the data source
     * @param sourceFormatExtension Data format extension
     * @param archiveFormat Archive format
     * @param compressionFormat Compression format
     * @param observer Observer
     * @return the created datasource
     */
    static DataSource createFilteredArchiveDataSource(Path archivePath, ArchiveFormat archiveFormat, CompressionFormat compressionFormat, String baseName, String sourceFormatExtension, DataSourceObserver observer) {
        return createArchiveDataSource(archivePath.getParent(), archivePath.getFileName().toString(), baseName, sourceFormatExtension, archiveFormat, compressionFormat, observer);
    }

    /**
     * Create an archive-based DataSource based on the provided base name, source format extension, archive format and compression
     * format
     * @param archivePath Path to the archive file
     * @param baseName base name for the files in the data source
     * @param sourceFormatExtension Data format extension
     * @param archiveFormat Archive format
     * @param compressionFormat Compression format
     * @return the created datasource
     */
    static DataSource createFilteredArchiveDataSource(Path archivePath, ArchiveFormat archiveFormat, CompressionFormat compressionFormat, String baseName, String sourceFormatExtension) {
        return createArchiveDataSource(archivePath.getParent(), archivePath.getFileName().toString(), baseName, sourceFormatExtension, archiveFormat, compressionFormat, null);
    }

    /**
     * Create an archive-based DataSource based on the provided base name, source format extension, archive format and compression
     * format
     * @param archivePath Path to the archive file
     * @param baseName base name for the files in the data source
     * @param sourceFormatExtension Data format extension
     * @param observer Observer
     * @return the created datasource
     */
    static DataSource createFilteredArchiveDataSource(Path archivePath, String baseName, String sourceFormatExtension, DataSourceObserver observer) {

        // Get the file information
        FileInformation fileInformation = new FileInformation(archivePath.getFileName().toString());

        return createFilteredArchiveDataSource(archivePath, fileInformation.getArchiveFormat(), fileInformation.getCompressionFormat(), baseName, sourceFormatExtension, observer);
    }

    /**
     * Create an archive-based DataSource based on the provided file name, base name and source format extension
     * @param archivePath Path to the archive file
     * @param baseName base name for the files in the data source
     * @param sourceFormatExtension Data format extension
     * @return the created datasource
     */
    static DataSource createFilteredArchiveDataSource(Path archivePath, String baseName, String sourceFormatExtension) {
        return createFilteredArchiveDataSource(archivePath, baseName, sourceFormatExtension, null);
    }

    /**
     * Create an archive-based DataSource based on the provided file name, base name and source format extension
     * @param archivePath Path to the archive file
     * @param sourceFormatExtension Data format extension
     * @param observer Observer
     * @return the created datasource
     */
    static DataSource createExtensionFilteredArchiveDataSource(Path archivePath, String sourceFormatExtension, DataSourceObserver observer) {
        return createFilteredArchiveDataSource(archivePath, "", sourceFormatExtension, observer);
    }

    /**
     * Create an archive-based DataSource based on the provided file name, base name and source format extension
     * @param archivePath Path to the archive file
     * @param sourceFormatExtension Data format extension
     * @return the created datasource
     */
    static DataSource createExtensionFilteredArchiveDataSource(Path archivePath, String sourceFormatExtension) {
        return createFilteredArchiveDataSource(archivePath, "", sourceFormatExtension, null);
    }

    /**
     * Create an archive-based DataSource based on the provided file name, base name and source format extension
     * @param archivePath Path to the archive file
     * @param baseName Base name
     * @param observer Observer
     * @return the created datasource
     */
    static DataSource createBaseNameFilteredArchiveDataSource(Path archivePath, String baseName, DataSourceObserver observer) {
        return createFilteredArchiveDataSource(archivePath, baseName, "", observer);
    }

    /**
     * Create an archive-based DataSource based on the provided file name, base name and source format extension
     * @param archivePath Path to the archive file
     * @param baseName Base name
     * @return the created datasource
     */
    static DataSource createBaseNameFilteredArchiveDataSource(Path archivePath, String baseName) {
        return createFilteredArchiveDataSource(archivePath, baseName, "", null);
    }

    /**
     * Create an archive-based DataSource based on the provided file name, base name and source format extension
     * @param archivePath Path to the archive file
     * @param observer Observer
     * @return the created datasource
     */
    static DataSource createArchiveDataSource(Path archivePath, DataSourceObserver observer) {
        return createFilteredArchiveDataSource(archivePath, "", "", observer);
    }

    /**
     * Create an archive-based DataSource based on the provided file name, base name and source format extension
     * @param archivePath Path to the archive file
     * @return the created datasource
     */
    static DataSource createArchiveDataSource(Path archivePath) {
        return createFilteredArchiveDataSource(archivePath, "", "", null);
    }

    /**
     * Create a directory-based DataSource based on the provided base name, source format extension and compression format
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
     * Create a directory-based DataSource based on the provided base name, source format extension and compression format
     * @param directory Path to the folder where the file is located
     * @param baseName base name for the files in the data source
     * @param sourceFormatExtension Data format extension
     * @param compressionFormat Compression format
     * @return the created datasource
     */
    static DataSource createDirectoryDataSource(Path directory, String baseName, String sourceFormatExtension, CompressionFormat compressionFormat) {
        return createDirectoryDataSource(directory, baseName, sourceFormatExtension, compressionFormat, null);
    }

    /**
     * Create a directory-based DataSource based on the provided base name and source format extension
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
     * Create a directory-based DataSource based on the provided base name and source format extension
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
    static DataSource createDirectoryDataSource(Path directory, String baseName, DataSourceObserver observer) {
        return createDirectoryDataSource(directory, baseName, "", null, observer);
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
     * @param path Path to the folder where the file is located
     * @param observer Observer
     * @return the created datasource
     */
    static DataSource createDataSource(Path path, DataSourceObserver observer) {
        if (!Files.exists(path)) {
            throw new PowsyblException(String.format("Path %s should exist to create a data source", path));
        } else if (Files.isDirectory(path)) {
            String baseName = path.getFileName().toString();
            baseName = baseName.endsWith("/") ? baseName.substring(0, baseName.length() - 1) : baseName;
            return createDirectoryDataSource(path, baseName, observer);
        } else {
            return createDataSource(path.getParent(), path.getFileName().toString(), observer);
        }
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
     * @param path Path to the folder where the file is located
     * @return the created datasource
     */
    static DataSource createDataSource(Path path) {
        return createDataSource(path, (DataSourceObserver) null);
    }
}
