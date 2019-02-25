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
import java.util.*;
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

    /**
     * Returns the list of default config directories: they are defined by the system properties
     * "powsybl.config.dirs" or "itools.config.dir".
     * If none is defined, it defaults to the single directory ${HOME}/.itools.
     */
    private static Path[] getDefaultConfigDirs(FileSystem fileSystem) {
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

    /**
     * Loads a {@link ModuleConfigRepository} from the list of specified config directories.
     * Configuration properties values encountered first in the list of directories
     * take precedence over the values defined in subsequent directories.
     * Configuration properties encountered in environment variables take precedence
     * over the values defined in config directories.
     */
    private static ModuleConfigRepository loadModuleRepository(Path[] configDirs, String configName) {
        List<ModuleConfigRepository> repositoriesFromPath = Arrays.stream(configDirs)
                .map(configDir -> loadModuleRepository(configDir, configName))
                .collect(Collectors.toList());
        List<ModuleConfigRepository> repositories = new ArrayList<>();
        repositories.add(new EnvironmentModuleConfigRepository(System.getenv(), FileSystems.getDefault()));
        repositories.addAll(repositoriesFromPath);
        return new StackedModuleConfigRepository(repositories);
    }

    /**
     * Loads a {@link ModuleConfigRepository} from a single directory.
     * Reads from yaml file if it exists, else from xml file, else from properties file.
     */
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

    /**
     * Loads the default {@link ModuleConfigRepository}.
     * Configuration properties are read from environment variables and from the list of directories
     * defined by the system properties "powsybl.config.dirs" or "itools.config.dir", or by default from ${HOME}/.itools.
     * Configuration properties values from environment variables
     * take precedence over the values defined in config directories.
     * Configuration properties values encountered first in the list of directories
     * take precedence over the values defined in subsequent directories.
     */
    public static ModuleConfigRepository loadDefaultModuleRepository() {
        FileSystem fileSystem = FileSystems.getDefault();
        Path[] configDirs = getDefaultConfigDirs(fileSystem);
        String configName = System.getProperty("powsybl.config.name", System.getProperty("itools.config.name", "config"));

        return loadModuleRepository(configDirs, configName);
    }

    public static synchronized PlatformConfig defaultConfig() {
        if (defaultConfig == null) {
            FileSystem fileSystem = FileSystems.getDefault();
            Path[] configDirs = getDefaultConfigDirs(fileSystem);
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
        this(repository, getDefaultConfigDirs(fileSystem)[0]);
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

    public Optional<ModuleConfig> getOptionalModuleConfig(String name) {
        return repository.getModuleConfig(name);
    }
}
