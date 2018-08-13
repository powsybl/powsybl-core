/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.io.CacheManager;
import com.powsybl.commons.io.FileUtil;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PlatformConfig {

    private static PlatformConfig defaultConfig;

    private static CacheManager defaultCacheManager;

    protected final FileSystem fileSystem;

    protected final Path configDir;

    protected final Path cacheDir;

    protected final ModuleConfigContainer container;

    public static synchronized void setDefaultConfig(PlatformConfig defaultConfig) {
        PlatformConfig.defaultConfig = defaultConfig;
    }

    public static synchronized PlatformConfig defaultConfig() {
        if (defaultConfig == null) {
            FileSystem fileSystem = FileSystems.getDefault();
            Path configDir = getDefaultConfigDir(fileSystem);
            Path cacheDir = getDefaultCacheDir(fileSystem);
            String configName = System.getProperty("itools.config.name", "config");

            defaultConfig = YamlPlatformConfig.create(fileSystem, configDir, cacheDir, configName)
                    .orElseGet(() -> XmlPlatformConfig.create(fileSystem, configDir, cacheDir, configName)
                            .orElseGet(() -> new PropertiesPlatformConfig(fileSystem, configDir, cacheDir)));
        }
        return defaultConfig;
    }

    public static synchronized void setDefaultCacheManager(CacheManager defaultCacheManager) {
        PlatformConfig.defaultCacheManager = defaultCacheManager;
    }

    public static synchronized CacheManager defaultCacheManager() {
        if (defaultCacheManager == null) {
            defaultCacheManager = new CacheManager(defaultConfig().cacheDir);
        }
        return defaultCacheManager;
    }

    protected PlatformConfig(FileSystem fileSystem, ModuleConfigContainer container) {
        this(fileSystem, getDefaultConfigDir(fileSystem), getDefaultCacheDir(fileSystem), container);
    }

    protected PlatformConfig(FileSystem fileSystem, Path configDir, Path cacheDir, ModuleConfigContainer container) {
        this.fileSystem = Objects.requireNonNull(fileSystem);
        this.configDir = FileUtil.createDirectory(configDir);
        this.cacheDir = FileUtil.createDirectory(cacheDir);
        this.container = Objects.requireNonNull(container);
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public Path getConfigDir() {
        return configDir;
    }

    public Path getCacheDir() {
        return cacheDir;
    }

    public boolean moduleExists(String name) {
        return container.moduleExists(name);
    }

    public ModuleConfig getModuleConfig(String name) {
        return container.getModuleConfig(name);
    }

    public ModuleConfig getModuleConfigIfExists(String name) {
        return container.getModuleConfigIfExists(name);
    }

    public Optional<ModuleConfig> getOptionalModuleConfig(String name) {
        return Optional.ofNullable(container.getModuleConfigIfExists(name));
    }

    static Path getDefaultConfigDir(FileSystem fileSystem) {
        return getDirectory(fileSystem, "itools.config.dir", ".itools");
    }

    static Path getDefaultCacheDir(FileSystem fileSystem) {
        return getDirectory(fileSystem, "itools.cache.dir", ".cache", "itools");
    }

    private static Path getDirectory(FileSystem fileSystem, String propertyName, String... folders) {
        Objects.requireNonNull(fileSystem);
        Objects.requireNonNull(propertyName);
        Objects.requireNonNull(folders);

        Path directory;

        String directoryName = System.getProperty(propertyName);
        if (directoryName != null) {
            directory = fileSystem.getPath(directoryName);
        } else {
            directory = fileSystem.getPath(System.getProperty("user.home"), folders);
        }

        return directory;

    }
}
