/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PlatformConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformConfig.class);

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

    private static Path[] getDefaultConfigDir(FileSystem fileSystem) {
        Objects.requireNonNull(fileSystem);
        String directories = System.getProperty("powsybl.config.dirs", System.getProperty("itools.config.dir"));
        Path[] configDirs = null;
        if (directories != null) {
            configDirs = Arrays.stream(directories.split(":"))
                    .map(PlatformEnv::substitute)
                    .map(fileSystem::getPath)
                    .toArray(Path[]::new);
        }
        if (configDirs == null || configDirs.length == 0) {
            configDirs = new Path[] {fileSystem.getPath(System.getProperty("user.home"), ".itools") };
        }
        return configDirs;
    }

    private static ModuleConfigRepository loadModuleRepository(Path[] configDirs, String configName) {
        List<ModuleConfigRepository> repositories = Arrays.stream(configDirs)
                .map(configDir -> loadModuleRepository(configDir, configName))
                .collect(Collectors.toList());
        return new StackedModuleConfigRepository(repositories);
    }

    private static ModuleConfigRepository loadModuleRepository(Path configDir, String configName) {
        Path yamlConfigFile = configDir.resolve(configName + ".yml");
        if (Files.exists(yamlConfigFile)) {
            LOGGER.info("Platform configuration defined by YAML file {}", yamlConfigFile);
            return new YamlModuleConfigRepository(yamlConfigFile);
        } else {
            Path xmlConfigFile = configDir.resolve(configName + ".xml");
            if (Files.exists(xmlConfigFile)) {
                LOGGER.info("Platform configuration defined by XML file {}", xmlConfigFile);
                return new XmlModuleConfigRepository(xmlConfigFile);
            } else {
                LOGGER.info("Platform configuration defined by .properties files of directory {}", configDir);
                return new PropertiesModuleConfigRepository(configDir);
            }
        }
    }

    public static synchronized PlatformConfig defaultConfig() {
        if (defaultConfig == null) {
            FileSystem fileSystem = FileSystems.getDefault();
            Path[] configDirs = getDefaultConfigDir(fileSystem);
            String configName = System.getProperty("powsybl.config.name", System.getProperty("itools.config.name", "config"));

            ModuleConfigRepository repository = loadModuleRepository(configDirs, configName);
            defaultConfig = new PlatformConfig(repository, configDirs[0]);
        }
        return defaultConfig;
    }

    public PlatformConfig(ModuleConfigRepository repository) {
        this(repository, FileSystems.getDefault());
    }

    public PlatformConfig(ModuleConfigRepository repository, FileSystem fileSystem) {
        this(repository, getDefaultConfigDir(fileSystem)[0]);
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
