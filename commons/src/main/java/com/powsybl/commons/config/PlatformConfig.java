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
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class PlatformConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlatformConfig.class);

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
            List<PlatformConfigProvider> providers = Lists.newArrayList(ServiceLoader.load(PlatformConfigProvider.class, PlatformConfig.class.getClassLoader()));
            if (providers.isEmpty()) {
                LOGGER.error("Platform configuration provider not found. For tests, consider using TestPlatformConfigProvider in powsybl-config-test. Otherwise, consider using ClassicPlatformConfigProvider from powsybl-config-classic.");
                throw new PowsyblException("Platform configuration provider not found");
            }
            if (providers.size() > 1) {
                LOGGER.error("Multiple platform configuration providers found: {}", providers);
                throw new PowsyblException("Multiple platform configuration providers found");
            }
            PlatformConfigProvider p = providers.get(0);
            LOGGER.info("Using platform configuration provider {}", p.getName());
            defaultConfig = p.getPlatformConfig();
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
