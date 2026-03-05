/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.powsybl.commons.PowsyblException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Supplier;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class PlatformConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformConfig.class);

    private static PlatformConfig defaultConfig;

    protected final Path configDir;

    protected final Supplier<ModuleConfigRepository> repositorySupplier;

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
            List<PlatformConfigProvider> providers = Lists.newArrayList(ServiceLoader.load(PlatformConfigProvider.class, PlatformConfig.class.getClassLoader()));
            if (providers.isEmpty()) {
                LOGGER.info("Platform configuration provider not found. In order to customize the platform configuration, consider using powsybl-config-classic artifact, or powsybl-config-test for tests.");
                defaultConfig = new PlatformConfig(new EmptyModuleConfigRepository(), null);
            } else {
                if (providers.size() > 1) {
                    LOGGER.error("Multiple platform configuration providers found: {}", providers);
                    throw new PowsyblException("Multiple platform configuration providers found");
                }
                PlatformConfigProvider p = providers.get(0);
                LOGGER.info("Using platform configuration provider {}", p.getName());
                defaultConfig = p.getPlatformConfig();
            }
        }
        return defaultConfig;
    }

    public PlatformConfig(ModuleConfigRepository repository, Path configDir) {
        this(() -> repository, configDir);
    }

    protected PlatformConfig(Supplier<ModuleConfigRepository> repositorySupplier, Path configDir) {
        Objects.requireNonNull(repositorySupplier);
        this.repositorySupplier = Suppliers.memoize(repositorySupplier::get);
        this.configDir = configDir;
    }

    public Optional<Path> getConfigDir() {
        return Optional.ofNullable(configDir);
    }

    protected ModuleConfigRepository getRepository() {
        return Objects.requireNonNull(repositorySupplier.get());
    }

    public Optional<ModuleConfig> getOptionalModuleConfig(String name) {
        return getRepository().getModuleConfig(name);
    }

    public int getIntProperty(String moduleName, String propertyName, int defaultValue) {
        return getOptionalModuleConfig(moduleName)
                .map(moduleConfig -> moduleConfig.getIntProperty(propertyName, defaultValue))
                .orElse(defaultValue);
    }

    public float getFloatProperty(String moduleName, String propertyName, float defaultValue) {
        return getOptionalModuleConfig(moduleName)
                .flatMap(moduleConfig -> moduleConfig.getOptionalFloatProperty(propertyName))
                .orElse(defaultValue);
    }

    public double getDoubleProperty(String moduleName, String propertyName, double defaultValue) {
        return getOptionalModuleConfig(moduleName)
                .map(moduleConfig -> moduleConfig.getDoubleProperty(propertyName, defaultValue))
                .orElse(defaultValue);
    }

    public Long getLongProperty(String moduleName, String propertyName, long defaultValue) {
        return getOptionalModuleConfig(moduleName)
                .map(moduleConfig -> moduleConfig.getLongProperty(propertyName, defaultValue))
                .orElse(defaultValue);
    }

    public String getStringProperty(String moduleName, String propertyName, String defaultValue) {
        return getOptionalModuleConfig(moduleName)
                .flatMap(moduleConfig -> moduleConfig.getOptionalStringProperty(propertyName))
                .orElse(defaultValue);
    }

    public List<String> getStringListProperty(String moduleName, String propertyName, List<String> defaultValue) {
        return getOptionalModuleConfig(moduleName)
                .flatMap(moduleConfig -> moduleConfig.getOptionalStringListProperty(propertyName))
                .orElse(defaultValue);
    }

    public Boolean getBooleanProperty(String moduleName, String propertyName, boolean defaultValue) {
        return getOptionalModuleConfig(moduleName)
                .flatMap(moduleConfig -> moduleConfig.getOptionalBooleanProperty(propertyName))
                .orElse(defaultValue);
    }

    public <E extends Enum<E>> E getEnumProperty(String moduleName, String propertyName, E defaultValue, Class<E> clazz) {
        return getOptionalModuleConfig(moduleName)
                .flatMap(moduleConfig -> moduleConfig.getOptionalEnumProperty(propertyName, clazz))
                .orElse(defaultValue);
    }

    public <E extends Enum<E>> Set<E> getEnumProperty(String moduleName, String propertyName, Set<E> defaultValue, Class<E> clazz) {
        return getOptionalModuleConfig(moduleName)
                .flatMap(moduleConfig -> moduleConfig.getOptionalEnumSetProperty(propertyName, clazz))
                .orElse(defaultValue);
    }

    public Path getPathProperty(String moduleName, String propertyName, Path defaultValue) {
        return getOptionalModuleConfig(moduleName)
                .flatMap(moduleConfig -> moduleConfig.getOptionalPathProperty(propertyName))
                .orElse(defaultValue);
    }

    public List<Path> getPathListProperty(String moduleName, String propertyName, List<Path> defaultValue) {
        return getOptionalModuleConfig(moduleName)
                .flatMap(moduleConfig -> moduleConfig.getOptionalPathListProperty(propertyName))
                .orElse(defaultValue);
    }

    public <T> Class<? extends T> getClassProperty(String moduleName, String propertyName, Class<? extends T> defaultValue, Class<T> subClass) {
        return getOptionalModuleConfig(moduleName)
                .flatMap(moduleConfig -> moduleConfig.getOptionalClassProperty(propertyName, subClass))
                .orElse(defaultValue);
    }

    public ZonedDateTime getZonedDateTimeProperty(String moduleName, String propertyName, ZonedDateTime defaultValue) {
        return getOptionalModuleConfig(moduleName)
                .flatMap(moduleConfig -> moduleConfig.getOptionalDateTimeProperty(propertyName))
                .orElse(defaultValue);
    }

    private static final class EmptyModuleConfigRepository implements ModuleConfigRepository {
        @Override
        public Optional<ModuleConfig> getModuleConfig(String name) {
            return Optional.empty();
        }
    }
}
