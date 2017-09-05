/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.local.storage;

import com.google.auto.service.AutoService;
import eu.itesla_project.commons.config.ComponentDefaultConfig;
import eu.itesla_project.commons.datasource.ReadOnlyDataSource;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.import_.ImportConfig;
import eu.itesla_project.iidm.import_.Importer;
import eu.itesla_project.iidm.import_.Importers;
import eu.itesla_project.iidm.import_.ImportersLoader;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(LocalFileStorageExtension.class)
public class LocalCaseStorageExtension implements LocalFileStorageExtension {

    private final ImportConfig importConfig;

    private final ImportersLoader importersLoader;

    public LocalCaseStorageExtension() {
        this(ImportConfig.load(), ComponentDefaultConfig.load().newFactoryImpl(ImportersLoader.class));
    }

    public LocalCaseStorageExtension(ImportConfig importConfig, ImportersLoader importersLoader) {
        this.importConfig = Objects.requireNonNull(importConfig);
        this.importersLoader = Objects.requireNonNull(importersLoader);
    }

    @Override
    public LocalFileStorage scan(Path path, ComputationManager computationManager) {
        if (Files.isRegularFile(path)) {
            ReadOnlyDataSource dataSource = Importers.createDataSource(path);
            for (Importer importer : Importers.list(importersLoader, computationManager, importConfig)) {
                if (importer.exists(dataSource)) {
                    return new LocalCaseStorage(path, importer);
                }
            }
        }
        return null;
    }
}
