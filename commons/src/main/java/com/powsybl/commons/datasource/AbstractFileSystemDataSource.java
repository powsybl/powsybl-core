/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
abstract class AbstractFileSystemDataSource implements DataSource {
    final Path directory;
    final String baseName;
    final String mainExtension;
    final CompressionFormat compressionFormat;
    final DataSourceObserver observer;

    /**
     *
     * @param directory Directory in which is located the data
     * @param baseName Base of the filenames that will be used
     * @param observer Data source observer
     */
    AbstractFileSystemDataSource(Path directory, String baseName,
                                 String mainExtension,
                                 CompressionFormat compressionFormat,
                                 DataSourceObserver observer) {
        this.directory = Objects.requireNonNull(directory);
        this.baseName = Objects.requireNonNull(baseName);
        this.mainExtension = mainExtension;
        this.compressionFormat = compressionFormat;
        this.observer = observer;
    }

    @Override
    public boolean isMainExtension(String ext) {
        return mainExtension == null || mainExtension.isEmpty() || mainExtension.equals(ext);
    }

    public Path getDirectory() {
        return this.directory;
    }

    @Override
    public String getBaseName() {
        return baseName;
    }

    @Override
    public String getMainExtension() {
        return mainExtension;
    }

    public CompressionFormat getCompressionFormat() {
        return compressionFormat;
    }

    String getCompressionExtension() {
        return compressionFormat == null ? "" : "." + compressionFormat.getExtension();
    }

    public DataSourceObserver getObserver() {
        return observer;
    }

    @Override
    public boolean exists(String suffix, String ext) throws IOException {
        return exists(DataSourceUtil.getFileName(baseName, suffix, ext));
    }
}
