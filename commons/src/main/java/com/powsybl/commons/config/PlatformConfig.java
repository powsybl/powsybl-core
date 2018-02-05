/**
 * Copyright (c) 2016-2018, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.exceptions.UncheckedParserConfigurationException;
import com.powsybl.commons.exceptions.UncheckedSaxException;
import com.powsybl.commons.io.CacheManager;
import com.powsybl.commons.io.FileUtil;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Objects;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Teofil Calin BANC <teofil-calin.banc at rte-france.com>
 */
public class PlatformConfig {

    @Deprecated
    public static final Path CONFIG_DIR;

    @Deprecated
    public static final Path CACHE_DIR;

    @Deprecated
    private static final String CONFIG_NAME;

    private static PlatformConfig defaultConfig;

    private static CacheManager defaultCacheManager;

    protected final FileSystem fileSystem;

    protected final Path configDir;

    protected final Path cacheDir;

    protected final ModuleConfigContainer container;

    static {
        CONFIG_DIR = FileUtil.createDirectory(getDefaultConfigDir(FileSystems.getDefault()));

        CONFIG_NAME = System.getProperty("itools.config.name");

        CACHE_DIR = FileUtil.createDirectory(getDefaultCacheDir(FileSystems.getDefault()));
    }

    public static synchronized void setDefaultConfig(PlatformConfig defaultConfig) {
        PlatformConfig.defaultConfig = defaultConfig;
    }

    public static synchronized PlatformConfig defaultConfig() {
        if (defaultConfig == null) {
            FileSystem fileSystem = FileSystems.getDefault();
            Path configDir = getDefaultConfigDir(fileSystem);
            Path cacheDir = getDefaultCacheDir(fileSystem);

            String configName = System.getProperty("itools.config.name");
            if (configName != null) {
                try {
                    defaultConfig = new XmlPlatformConfig(fileSystem, configDir, cacheDir, configName);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                } catch (SAXException e) {
                    throw new UncheckedSaxException(e);
                } catch (ParserConfigurationException e) {
                    throw new UncheckedParserConfigurationException(e);
                }
            } else {
                defaultConfig = new PropertiesPlatformConfig(fileSystem, configDir, cacheDir);
            }
        }
        return defaultConfig;
    }

    public static synchronized PlatformConfig customConfig(Path configFile) {
        return customConfig(FileSystems.getDefault(), configFile);
    }

    public static synchronized PlatformConfig customConfig(FileSystem fileSystem, Path configFile) {
        Objects.requireNonNull(fileSystem);
        Objects.requireNonNull(configFile);

        Path configDir = getCustomConfigDir(fileSystem, configFile);
        Path cacheDir = getCustomCacheDir(fileSystem, configFile);
        String configName = getFileName(configFile);

        PlatformConfig customnPlatformConfig = null;
        if (configName != null) {
            try {
                customnPlatformConfig = new XmlPlatformConfig(fileSystem, configDir, cacheDir, configName);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (SAXException e) {
                throw new UncheckedSaxException(e);
            } catch (ParserConfigurationException e) {
                throw new UncheckedParserConfigurationException(e);
            }
        }
        return customnPlatformConfig;
    }

    public static synchronized PlatformConfig configInputStream(InputStream configInputStream) {
        Objects.requireNonNull(configInputStream);

        FileSystem fileSystem = FileSystems.getDefault();
        Path configDir = getDefaultConfigDir(fileSystem);
        Path cacheDir = getDefaultCacheDir(fileSystem);

        PlatformConfig customnPlatformConfig = null;
        if (configInputStream != null) {
            try {
                customnPlatformConfig = new XmlPlatformConfig(fileSystem, configDir, cacheDir, configInputStream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            } catch (SAXException e) {
                throw new UncheckedSaxException(e);
            } catch (ParserConfigurationException e) {
                throw new UncheckedParserConfigurationException(e);
            }
        }
        return customnPlatformConfig;
    }

    public static synchronized void setDefaultCacheManager(CacheManager defaultCacheManager) {
        PlatformConfig.defaultCacheManager = defaultCacheManager;
    }

    public static synchronized CacheManager defaultCacheManager() {
        if (defaultCacheManager == null) {
            defaultCacheManager = new CacheManager(CACHE_DIR);
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

    static Path getDefaultConfigDir(FileSystem fileSystem) {
        return getDirectory(fileSystem, "itools.config.dir", ".itools");
    }


    static Path getDefaultCacheDir(FileSystem fileSystem) {
        return getDirectory(fileSystem, "itools.cache.dir", ".cache", "itools");
    }

    static Path getCustomConfigDir(FileSystem fileSystem, Path configFile) {
        return getDirectory(fileSystem, configFile);
    }

    static Path getCustomCacheDir(FileSystem fileSystem, Path configFile) {
        return getDirectory(fileSystem, configFile);
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

    private static Path getDirectory(FileSystem fileSystem, Path configFile) {
        Objects.requireNonNull(fileSystem);
        Objects.requireNonNull(configFile);

        String fileNamePlusPath  = configFile.toString();

        String directoryName = fileNamePlusPath.substring(0, fileNamePlusPath.lastIndexOf("/") + 1);

        return fileSystem.getPath(directoryName);
    }

    private static String getFileName(Path configFile) {
        Objects.requireNonNull(configFile);

        String fileNamePlusPath  = configFile.toString();

        String fileName = fileNamePlusPath.substring(fileNamePlusPath.lastIndexOf('/') + 1);

        return fileName.substring(0, fileName.indexOf("."));
    }
}
