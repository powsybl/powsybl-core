/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local;

import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalComputationConfig {

    private static final String CONFIG_MODULE_NAME = "computation-local";

    private static final Path DEFAULT_LOCAL_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

    private static final int DEFAULT_AVAILABLE_CORE = 1;

    private final Path localDir;

    private final int availableCore;

    public static LocalComputationConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static LocalComputationConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        Path localDir = DEFAULT_LOCAL_DIR;
        int availableCore = DEFAULT_AVAILABLE_CORE;
        if (platformConfig.moduleExists(CONFIG_MODULE_NAME)) {
            ModuleConfig config = platformConfig.getModuleConfig(CONFIG_MODULE_NAME);
            localDir = config.getPathProperty("tmpDir", DEFAULT_LOCAL_DIR);
            availableCore = config.getIntProperty("availableCore", DEFAULT_AVAILABLE_CORE);
        }
        if (availableCore <= 0) {
            availableCore = Runtime.getRuntime().availableProcessors();
        }
        return new LocalComputationConfig(localDir, availableCore);
    }

    public LocalComputationConfig(Path localDir) {
        this(localDir, DEFAULT_AVAILABLE_CORE);
    }

    public LocalComputationConfig(Path localDir, int availableCore) {
        this.localDir = localDir;
        this.availableCore = availableCore;
    }

    public Path getLocalDir() {
        return localDir;
    }

    public int getAvailableCore() {
        return availableCore;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [localDir=" + localDir +
                ", availableCore=" + availableCore +
                "]";
    }
}
