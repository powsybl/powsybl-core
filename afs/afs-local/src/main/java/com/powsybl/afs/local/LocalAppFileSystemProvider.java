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
import eu.itesla_project.afs.local.storage.LocalFileScanner;
import eu.itesla_project.afs.local.storage.LocalFolderScanner;
import eu.itesla_project.commons.util.ServiceLoaderCache;
import eu.itesla_project.computation.ComputationManager;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(AppFileSystemProvider.class)
public class LocalAppFileSystemProvider implements AppFileSystemProvider {

    private final List<LocalAppFileSystemConfig> configs;

    private final List<LocalFileScanner> fileScanners;

    private final List<LocalFolderScanner> folderScanners;

    public LocalAppFileSystemProvider() {
        this(LocalAppFileSystemConfig.load(), new ServiceLoaderCache<>(LocalFileScanner.class).getServices(),
                new ServiceLoaderCache<>(LocalFolderScanner.class).getServices());
    }

    public LocalAppFileSystemProvider(List<LocalAppFileSystemConfig> configs, List<LocalFileScanner> fileScanners,
                                      List<LocalFolderScanner> folderScanners) {
        this.configs = Objects.requireNonNull(configs);
        this.fileScanners = Objects.requireNonNull(fileScanners);
        this.folderScanners = Objects.requireNonNull(folderScanners);
    }

    @Override
    public List<AppFileSystem> getFileSystems(ComputationManager computationManager) {
        return configs.stream()
                .map(config -> new LocalAppFileSystem(config, fileScanners, folderScanners, computationManager))
                .collect(Collectors.toList());
    }
}
