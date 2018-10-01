/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.FileUtil;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PlatformConfig {

    private static PlatformConfig defaultConfig;

    protected final Path configDir;

    protected final ModuleConfigRepository repository;

    /**
     * @deprecated Directly pass <code>PlatformConfig</code> instance to the code you want to test.
     */
    @Deprecated
    public static synchronized void setDefaultConfig(PlatformConfig defaultConfig) {
        PlatformConfig.defaultConfig = defaultConfig;
    }

    private static Path getDefaultConfigDir(FileSystem fileSystem) {
        Objects.requireNonNull(fileSystem);
        String directoryName = System.getProperty("itools.config.dir");
        if (directoryName != null) {
            return fileSystem.getPath(directoryName);
        } else {
            return fileSystem.getPath(System.getProperty("user.home"), ".itools");
        }
    }

    public static synchronized PlatformConfig defaultConfig() {
        if (defaultConfig == null) {
            FileSystem fileSystem = FileSystems.getDefault();
            Path configDir = getDefaultConfigDir(fileSystem);
            String configName = System.getProperty("itools.config.name", "config");

            ModuleConfigRepository repository;
            Path yamlConfigFile = configDir.resolve(configName + ".yml");
            if (Files.exists(yamlConfigFile)) {
                repository = new YamlModuleConfigRepository(yamlConfigFile);
            } else {
                Path xmlConfigFile = configDir.resolve(configName + ".xml");
                if (Files.exists(xmlConfigFile)) {
                    repository = new XmlModuleConfigRepository(xmlConfigFile);
                } else {
                    repository = new PropertiesModuleConfigRepository(configDir);
                }
            }
            defaultConfig = new PlatformConfig(repository, configDir);
        }
        return defaultConfig;
    }

    public PlatformConfig(ModuleConfigRepository repository) {
        this(repository, FileSystems.getDefault());
    }

    public PlatformConfig(ModuleConfigRepository repository, FileSystem fileSystem) {
        this(repository, getDefaultConfigDir(fileSystem));
    }

    protected PlatformConfig(ModuleConfigRepository repository, Path configDir) {
        this.repository = Objects.requireNonNull(repository);
        this.configDir = FileUtil.createDirectory(configDir);
    }

    public Path getConfigDir() {
        return configDir;
    }

    public boolean moduleExists(String name) {
        return repository.moduleExists(name);
    }

    public ModuleConfig getModuleConfig(String name) {
        return repository.getModuleConfig(name).orElseThrow(() -> new PowsyblException("Module " + name + " not found"));
    }

    /**
     * @deprecated Use {{@link #getOptionalModuleConfig(String)}} instead.
     */
    @Deprecated
    public ModuleConfig getModuleConfigIfExists(String name) {
        return repository.getModuleConfig(name).orElse(null);
    }

    public Optional<ModuleConfig> getOptionalModuleConfig(String name) {
        return repository.getModuleConfig(name);
    }
}
