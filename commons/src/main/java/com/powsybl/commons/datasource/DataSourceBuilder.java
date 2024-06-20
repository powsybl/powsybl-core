/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import com.powsybl.commons.PowsyblException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class DataSourceBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceBuilder.class);
    private Path directory;
    private String baseName;
    private String archiveFileName;
    private CompressionFormat compressionFormat;
    private ArchiveFormat archiveFormat;
    private String sourceFormatExtension = "";
    private DataSourceObserver observer;

    DataSourceBuilder withDirectory(Path directory) {
        this.directory = directory;
        return this;
    }

    DataSourceBuilder withBaseName(String baseName) {
        this.baseName = baseName;
        return this;
    }

    DataSourceBuilder withArchiveFileName(String archiveFileName) {
        this.archiveFileName = archiveFileName;
        return this;
    }

    DataSourceBuilder withCompressionFormat(CompressionFormat compressionFormat) {
        this.compressionFormat = compressionFormat;
        return this;
    }

    DataSourceBuilder withArchiveFormat(ArchiveFormat archiveFormat) {
        this.archiveFormat = archiveFormat;
        return this;
    }

    DataSourceBuilder withSourceFormatExtension(String sourceFormatExtension) {
        this.sourceFormatExtension = sourceFormatExtension;
        return this;
    }

    DataSourceBuilder withObserver(DataSourceObserver observer) {
        this.observer = observer;
        return this;
    }

    DataSource build() {
        // Check the mandatory parameters
        buildChecks();

        // Create the datasource
        if (compressionFormat == CompressionFormat.ZIP || archiveFormat == ArchiveFormat.ZIP) {
            return buildZip();
        } else if (archiveFormat == ArchiveFormat.TAR) {
            return archiveFileName == null ?
                new TarArchiveDataSource(directory, baseName, compressionFormat, sourceFormatExtension, observer) :
                new TarArchiveDataSource(directory, archiveFileName, baseName, compressionFormat, sourceFormatExtension, observer);
        } else if (compressionFormat == null) {
            return new DirectoryDataSource(directory, baseName, sourceFormatExtension, observer);
        } else {
            return switch (compressionFormat) {
                case BZIP2 -> new Bzip2DirectoryDataSource(directory, baseName, sourceFormatExtension, observer);
                case GZIP -> new GzDirectoryDataSource(directory, baseName, sourceFormatExtension, observer);
                case XZ -> new XZDirectoryDataSource(directory, baseName, sourceFormatExtension, observer);
                case ZSTD -> new ZstdDirectoryDataSource(directory, baseName, sourceFormatExtension, observer);
                default -> {
                    LOGGER.warn("Unsupported compression format {}", compressionFormat);
                    yield new DirectoryDataSource(directory, baseName, sourceFormatExtension, observer);
                }
            };
        }
    }

    private void buildChecks() {
        if (directory == null) {
            throw new PowsyblException("Datasource directory cannot be null");
        }
        if (!Files.isDirectory(directory)) {
            throw new PowsyblException("Datasource directory has to be a directory");
        }
        if (baseName == null) {
            throw new PowsyblException("Datasource baseName cannot be null");
        }
        if (sourceFormatExtension == null) {
            throw new PowsyblException("Source format cannot be null");
        }
    }

    private DataSource buildZip() {
        if (compressionFormat != null && archiveFormat != null && !(compressionFormat == CompressionFormat.ZIP && archiveFormat == ArchiveFormat.ZIP)) {
            throw new PowsyblException(String.format("Incoherence between compression format %s and archive format %s", compressionFormat, archiveFormat));
        }
        return archiveFileName == null ?
            new ZipArchiveDataSource(directory, baseName, sourceFormatExtension, observer) :
            new ZipArchiveDataSource(directory, archiveFileName, baseName, sourceFormatExtension, observer);
    }
}
