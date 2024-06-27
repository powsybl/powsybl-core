/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
abstract class AbstractFileSystemDataSource extends AbstractReadOnlyDataSource implements DataSource {
    final Path directory;
    final String baseName;
    final CompressionFormat compressionFormat;
    final ArchiveFormat archiveFormat;
    final String mainExtension;
    final DataSourceObserver observer;

    /**
     *
     * @param directory Directory in which is located the data
     * @param baseName Base of the filenames that will be used
     * @param compressionFormat Compression format of the data
     * @param mainExtension Extension corresponding to the data source (.iidm, .cgmes, .xml, etc.)
     * @param observer Data source observer
     */
    AbstractFileSystemDataSource(Path directory, String baseName,
                                 CompressionFormat compressionFormat, ArchiveFormat archiveFormat,
                                 String mainExtension,
                                 DataSourceObserver observer) {
        this.directory = Objects.requireNonNull(directory);
        this.baseName = Objects.requireNonNull(baseName);
        this.compressionFormat = compressionFormat;
        this.archiveFormat = archiveFormat;
        this.mainExtension = Objects.requireNonNull(mainExtension);
        this.observer = observer;
    }

    public Path getDirectory() {
        return this.directory;
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    public CompressionFormat getCompressionFormat() {
        return compressionFormat;
    }

    String getCompressionExtension() {
        return compressionFormat == null ? "" : "." + compressionFormat.getExtension();
    }

    public ArchiveFormat getArchiveFormat() {
        return archiveFormat;
    }

    @Override
    public String getMainExtension() {
        return mainExtension;
    }

    public DataSourceObserver getObserver() {
        return observer;
    }
}
