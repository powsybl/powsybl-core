/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import com.powsybl.commons.PowsyblException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

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
     * @deprecated Directly pass <code>PlatformConfig</code> instance to the code you want to test.
     */
    @Deprecated(since = "2.2.0")
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
        this.repositorySupplier = Suppliers.memoize(Objects.requireNonNull(repositorySupplier));
        this.configDir = configDir;
    }

    public Optional<Path> getConfigDir() {
        return Optional.ofNullable(configDir);
    }

    protected ModuleConfigRepository getRepository() {
        return Objects.requireNonNull(repositorySupplier.get());
    }

    /**
     * @deprecated Use the <code>Optional</code> returned by {@link #getOptionalModuleConfig(String)}
     */
    @Deprecated(since = "4.9.0")
    public boolean moduleExists(String name) {
        return getOptionalModuleConfig(name).isPresent();
    }

    /**
     * @deprecated Use {@link #getOptionalModuleConfig(String)} instead
     */
    @Deprecated(since = "4.9.0")
    public ModuleConfig getModuleConfig(String name) {
        return getRepository().getModuleConfig(name).orElseThrow(() -> new PowsyblException("Module " + name + " not found"));
    }

    public Optional<ModuleConfig> getOptionalModuleConfig(String name) {
        return getRepository().getModuleConfig(name);
    }

    private static final class EmptyModuleConfigRepository implements ModuleConfigRepository {
        @Override
        public Optional<ModuleConfig> getModuleConfig(String name) {
            return Optional.empty();
        }
    }
}
