/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.local.storage;

import com.powsybl.computation.ComputationManager;

import java.nio.file.Path;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalFolderScannerContext {

    private final Path rootDir;

    private final String fileSystemName;

    private final ComputationManager computationManager;

    public LocalFolderScannerContext(Path rootDir, String fileSystemName, ComputationManager computationManager) {
        this.rootDir = rootDir;
        this.fileSystemName = fileSystemName;
        this.computationManager = computationManager;
    }

    public Path getRootDir() {
        return rootDir;
    }

    public String getFileSystemName() {
        return fileSystemName;
    }

    public ComputationManager getComputationManager() {
        return computationManager;
    }
}
