/*
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.datasource;

import java.nio.file.Path;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
public abstract class AbstractArchiveDataSource extends AbstractFileSystemDataSource {

    private final String archiveFileName;
    final ArchiveFormat archiveFormat;

    AbstractArchiveDataSource(Path directory, String archiveFileName, String baseName, String mainExtension, CompressionFormat compressionFormat, ArchiveFormat archiveFormat, DataSourceObserver observer) {
        super(directory, baseName, mainExtension, compressionFormat, observer);
        this.archiveFileName = archiveFileName;
        this.archiveFormat = archiveFormat;
    }

    protected Path getArchiveFilePath() {
        return directory.resolve(archiveFileName);
    }
}
