/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local;

import com.google.auto.service.AutoService;
import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.AppFileSystemProvider;
import com.powsybl.afs.AppFileSystemProviderContext;
import com.powsybl.afs.local.storage.LocalFileScanner;
import com.powsybl.afs.local.storage.LocalFolderScanner;
import com.powsybl.commons.util.ServiceLoaderCache;

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
    public List<AppFileSystem> getFileSystems(AppFileSystemProviderContext context) {
        Objects.requireNonNull(context);
        return configs.stream()
                .map(config -> new LocalAppFileSystem(config, fileScanners, folderScanners, context.getComputationManager()))
                .collect(Collectors.toList());
    }
}
