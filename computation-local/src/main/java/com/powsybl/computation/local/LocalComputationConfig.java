/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation.local;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.ConfigurationException;
import com.powsybl.commons.config.ModuleConfig;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.VersionConfig;

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
public class LocalComputationConfig implements Versionable {

    private static final String CONFIG_MODULE_NAME = "computation-local";

    static final String DEFAULT_LOCAL_DIR = System.getProperty("java.io.tmpdir");

    private static final int DEFAULT_AVAILABLE_CORE = 1;

    private static final String DEFAULT_CONFIG_VERSION = "1.0";

    private final VersionConfig version;

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
        VersionConfig version = new VersionConfig(DEFAULT_CONFIG_VERSION);
        Path localDir = getDefaultLocalDir(fileSystem);
        int availableCore = DEFAULT_AVAILABLE_CORE;
        if (platformConfig.moduleExists(CONFIG_MODULE_NAME)) {
            ModuleConfig config = platformConfig.getModuleConfig(CONFIG_MODULE_NAME);
            version = config.hasProperty("version") ? new VersionConfig(config.getStringProperty("version")) : version;
            if (version.equalsOrIsNewerThan("1.1")) {
                localDir = getTmpDir(config, "tmp-dir")
                        .orElse(localDir);
                availableCore = config.getOptionalIntProperty("available-core")
                        .orElse(availableCore);
            } else {
                localDir = getTmpDir(config, "tmpDir")
                        .orElse(localDir);
                availableCore = config.getOptionalIntProperty("availableCore")
                        .orElse(availableCore);
            }
        }
        if (availableCore <= 0) {
            availableCore = Runtime.getRuntime().availableProcessors();
        }
        return new LocalComputationConfig(version, localDir, availableCore);
    }

    public LocalComputationConfig(Path localDir) {
        this(new VersionConfig(DEFAULT_CONFIG_VERSION), localDir, DEFAULT_AVAILABLE_CORE);
    }

    public LocalComputationConfig(Path localDir, int availableCore) {
        this(new VersionConfig(DEFAULT_CONFIG_VERSION), localDir, availableCore);
    }

    public LocalComputationConfig(VersionConfig version, Path localDir, int availableCore) {
        this.version = version;
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

    @Override
    public String getName() {
        return CONFIG_MODULE_NAME;
    }

    @Override
    public String getVersion() {
        return this.version.toString();
    }
}
