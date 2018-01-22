/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local;

import com.powsybl.afs.AppFileSystem;
import com.powsybl.afs.local.storage.LocalAppStorage;
import com.powsybl.afs.local.storage.LocalFileScanner;
import com.powsybl.afs.local.storage.LocalFolderScanner;
import com.powsybl.commons.util.ServiceLoaderCache;
import com.powsybl.computation.ComputationManager;

import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalAppFileSystem extends AppFileSystem {

    public LocalAppFileSystem(LocalAppFileSystemConfig config, ComputationManager computationManager) {
        this(config,
                new ServiceLoaderCache<>(LocalFileScanner.class).getServices(),
                new ServiceLoaderCache<>(LocalFolderScanner.class).getServices(),
                computationManager);
    }

    public LocalAppFileSystem(LocalAppFileSystemConfig config, List<LocalFileScanner> fileScanners,
                              List<LocalFolderScanner> folderScanners, ComputationManager computationManager) {
        super(config.getDriveName(),
                config.isRemotelyAccessible(),
                new LocalAppStorage(config.getRootDir(), config.getDriveName(), fileScanners, folderScanners, computationManager));
    }
}
