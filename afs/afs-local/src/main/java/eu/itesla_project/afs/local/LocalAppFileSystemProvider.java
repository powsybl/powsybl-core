/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.afs.local;

import com.google.auto.service.AutoService;
import eu.itesla_project.afs.AppFileSystem;
import eu.itesla_project.afs.AppFileSystemProvider;
import eu.itesla_project.computation.ComputationManager;
import eu.itesla_project.iidm.import_.ImportConfig;
import eu.itesla_project.iidm.import_.ImportersLoader;
import eu.itesla_project.iidm.import_.ImportersServiceLoader;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(AppFileSystemProvider.class)
public class LocalAppFileSystemProvider implements AppFileSystemProvider {

    private final List<LocalAppFileSystemConfig> configs;

    private final ImportConfig importConfig;

    private final ImportersLoader importersLoader;

    public LocalAppFileSystemProvider() {
        this(LocalAppFileSystemConfig.load(), ImportConfig.load(), new ImportersServiceLoader());
    }

    public LocalAppFileSystemProvider(List<LocalAppFileSystemConfig> configs, ImportConfig importConfig,
                                      ImportersLoader importersLoader) {
        this.configs = Objects.requireNonNull(configs);
        this.importConfig = Objects.requireNonNull(importConfig);
        this.importersLoader = Objects.requireNonNull(importersLoader);
    }

    @Override
    public List<AppFileSystem> getFileSystems(ComputationManager computationManager) {
        return configs.stream()
                .map(config -> new LocalAppFileSystem(config, computationManager, importConfig, importersLoader))
                .collect(Collectors.toList());
    }
}
