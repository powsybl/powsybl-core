/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local.storage;

import com.google.auto.service.AutoService;
import com.powsybl.commons.datasource.ReadOnlyDataSource;
import com.powsybl.iidm.import_.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(LocalFileScanner.class)
public class LocalCaseScanner implements LocalFileScanner {

    private final ImportConfig importConfig;

    private final ImportersLoader importersLoader;

    public LocalCaseScanner() {
        this(ImportConfig.load(), new ImportersServiceLoader());
    }

    public LocalCaseScanner(ImportConfig importConfig, ImportersLoader importersLoader) {
        this.importConfig = Objects.requireNonNull(importConfig);
        this.importersLoader = Objects.requireNonNull(importersLoader);
    }

    @Override
    public LocalFile scanFile(Path path, LocalFileScannerContext context) {
        if (Files.isRegularFile(path)) {
            ReadOnlyDataSource dataSource = Importers.createDataSource(path);
            for (Importer importer : Importers.list(importersLoader, context.getComputationManager(), importConfig)) {
                if (importer.exists(dataSource)) {
                    return new LocalCase(path, importer);
                }
            }
        }
        return null;
    }
}
