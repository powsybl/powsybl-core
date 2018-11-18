/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local;

import com.powsybl.commons.config.ConfigurationException;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class LocalComputationConfig {

    private static final String CONFIG_MODULE_NAME = "computation-local";

    static final String DEFAULT_LOCAL_DIR = System.getProperty("java.io.tmpdir");

    private static final int DEFAULT_AVAILABLE_CORE = 1;

    private final Path localDir;

    private final int availableCore;

    public static LocalComputationConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static LocalComputationConfig load(PlatformConfig platformConfig) {
        return load(platformConfig, FileSystems.getDefault());
    }

    private static Path getDefaultLocalDir(FileSystem fileSystem) {
        return fileSystem.getPath(DEFAULT_LOCAL_DIR);
    }

    private static Optional<Path> getTmpDir(ModuleConfig config, String name) {
        return config.getOptionalPathListProperty(name)
                .map(paths -> {
                    if (paths.isEmpty()) {
                        throw new ConfigurationException("Empty tmp dir list");
                    }
                    List<Path> checkedPaths = paths.stream().filter(Files::exists).collect(Collectors.toList());
                    if (checkedPaths.isEmpty()) {
                        throw new ConfigurationException("None of the tmp dir path of the list exist");
                    }
                    return checkedPaths.get(0);
                });
    }

    public static LocalComputationConfig load(PlatformConfig platformConfig, FileSystem fileSystem) {
        Objects.requireNonNull(platformConfig);

        Path localDir = getDefaultLocalDir(fileSystem);
        int availableCore = DEFAULT_AVAILABLE_CORE;
        if (platformConfig.moduleExists(CONFIG_MODULE_NAME)) {
            ModuleConfig config = platformConfig.getModuleConfig(CONFIG_MODULE_NAME);
            localDir = getTmpDir(config, "tmp-dir")
                           .orElseGet(() -> getTmpDir(config, "tmpDir")
                                                .orElseGet(() -> getDefaultLocalDir(fileSystem)));
            availableCore = config.getOptionalIntProperty("available-core")
                                  .orElseGet(() -> config.getOptionalIntProperty("availableCore")
                                                         .orElse(DEFAULT_AVAILABLE_CORE));
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
