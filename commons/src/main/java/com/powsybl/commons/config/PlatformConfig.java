/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.io.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PlatformConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformConfig.class);

    private static final String KEY = "powsybl.config.provider";

    private static PlatformConfig defaultConfig;

    protected final Path configDir;

    protected final Supplier<ModuleConfigRepository> repositorySupplier;

    /**
     * @deprecated Directly pass <code>PlatformConfig</code> instance to the code you want to test.
     */
    @Deprecated
    public static synchronized void setDefaultConfig(PlatformConfig defaultConfig) {
        PlatformConfig.defaultConfig = defaultConfig;
    }

    /**
     * Loads a {@link ModuleConfigRepository} from a single directory.
     * Reads from yaml file if it exists, else from xml file, else from properties file.
     */
    public static ModuleConfigRepository loadModuleRepository(Path configDir, String configName) {
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
            String configName = System.getProperty(KEY);
            if (configName != null) {
                List<PlatformConfigProvider> platformConfigProviders = Lists
                        .newArrayList(ServiceLoader.load(PlatformConfigProvider.class));
                Optional<PlatformConfigProvider> foundProvider = platformConfigProviders.stream()
                        .filter(platformConfigProvider -> configName.equals(platformConfigProvider.getName()))
                        .findFirst();
                if (!foundProvider.isPresent()) {
                    if (LOGGER.isErrorEnabled()) {
                        List<String> available = platformConfigProviders.stream().map(PlatformConfigProvider::getName)
                                .collect(Collectors.toList());
                        LOGGER.error("Requested platform configuration provider {} = {} not found; available: {}", KEY,
                                configName, available);
                    }
                } else {
                    LOGGER.info("Using platform configuration provider {} = {}", KEY, configName);
                    defaultConfig = foundProvider.get().getPlatformConfig();
                }
            }
            if (defaultConfig == null) {
                LOGGER.info("Using default platform configuration provider {}",
                        ClassicPlatformConfigProvider.class.getSimpleName());
                defaultConfig = new ClassicPlatformConfigProvider().getPlatformConfig();
            }
        }
        return defaultConfig;
    }

    public PlatformConfig(ModuleConfigRepository repository, Path configDir) {
        this(() -> repository, configDir);
    }

    protected PlatformConfig(Supplier<ModuleConfigRepository> repositorySupplier, Path configDir) {
        this.repositorySupplier = Suppliers.memoize(Objects.requireNonNull(repositorySupplier));
        this.configDir = FileUtil.createDirectory(configDir);
    }

    public Path getConfigDir() {
        return configDir;
    }

    protected ModuleConfigRepository getRepository() {
        return Objects.requireNonNull(repositorySupplier.get());
    }

    public boolean moduleExists(String name) {
        return getRepository().moduleExists(name);
    }

    public ModuleConfig getModuleConfig(String name) {
        return getRepository().getModuleConfig(name).orElseThrow(() -> new PowsyblException("Module " + name + " not found"));
    }

    public Optional<ModuleConfig> getOptionalModuleConfig(String name) {
        return getRepository().getModuleConfig(name);
    }
}
