/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.commons.io;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.xml.parsers.ParserConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public abstract class PlatformConfig {

    public static final Path CONFIG_DIR;

    public static final Path CACHE_DIR;

    private static final String CONFIG_NAME;

    private static PlatformConfig defaultConfig;

    private static CacheManager defaultCacheManager;

    static {
        String iteslaConfigDir = System.getProperty("itesla.config.dir");
        if (iteslaConfigDir != null) {
            CONFIG_DIR = Paths.get(iteslaConfigDir);
        } else {
            CONFIG_DIR = Paths.get(System.getProperty("user.home"), ".itesla");
        }
        try {
            Files.createDirectories(CONFIG_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        CONFIG_NAME = System.getProperty("itesla.config.name");

        String iteslaCacheDir = System.getProperty("itesla.cache.dir");
        if (iteslaCacheDir != null) {
            CACHE_DIR = Paths.get(iteslaCacheDir);
        } else {
            CACHE_DIR = Paths.get(System.getProperty("user.home"), ".cache", "itesla");
        }
        try {
            Files.createDirectories(CACHE_DIR);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void setDefaultConfig(PlatformConfig defaultConfig) {
        PlatformConfig.defaultConfig = defaultConfig;
    }

    public static synchronized PlatformConfig defaultConfig() {
        if (defaultConfig == null) {
            if (CONFIG_NAME != null) {
                try {
                    defaultConfig = new XmlPlatformConfig(CONFIG_DIR, CONFIG_NAME, FileSystems.getDefault());
                } catch (IOException | ParserConfigurationException | SAXException e) {
                    throw new RuntimeException(e);
                }
            } else {
                defaultConfig = new PropertiesPlatformConfig(CONFIG_DIR, FileSystems.getDefault());
            }
        }
        return defaultConfig;
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

    public abstract boolean moduleExists(String name);

    public abstract ModuleConfig getModuleConfig(String name);

    public abstract ModuleConfig getModuleConfigIfExists(String name);
}
