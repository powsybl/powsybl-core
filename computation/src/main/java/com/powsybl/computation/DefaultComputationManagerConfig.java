/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.computation;

import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.VersionConfig;
import com.powsybl.commons.exceptions.UncheckedClassNotFoundException;
import com.powsybl.commons.exceptions.UncheckedIllegalAccessException;
import com.powsybl.commons.exceptions.UncheckedInstantiationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public class DefaultComputationManagerConfig implements Versionable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultComputationManagerConfig.class);

    private static final String CONFIG_MODULE_NAME = "default-computation-manager";

    /**
     * A String is used here for default computation manager factory to avoid a direct dependency to local computation
     * manager.
     */
    private static final String DEFAULT_SHORT_TIME_EXECUTION_COMPUTATION_MANAGER_FACTORY_CLASS
            = "com.powsybl.computation.local.LocalComputationManagerFactory";

    private static final String DEFAULT_CONFIG_VERSION = "1.0";

    private VersionConfig version = new VersionConfig(DEFAULT_CONFIG_VERSION);

    private final Class<? extends ComputationManagerFactory> shortTimeExecutionComputationManagerFactoryClass;

    private final Class<? extends ComputationManagerFactory> longTimeExecutionComputationManagerFactoryClass;

    public DefaultComputationManagerConfig(Class<? extends ComputationManagerFactory> shortTimeExecutionComputationManagerFactoryClass,
                                           Class<? extends ComputationManagerFactory> longTimeExecutionComputationManagerFactoryClass) {
        this.shortTimeExecutionComputationManagerFactoryClass = Objects.requireNonNull(shortTimeExecutionComputationManagerFactoryClass);
        this.longTimeExecutionComputationManagerFactoryClass = longTimeExecutionComputationManagerFactoryClass;
    }

    public DefaultComputationManagerConfig(VersionConfig version, Class<? extends ComputationManagerFactory> shortTimeExecutionComputationManagerFactoryClass,
                                           Class<? extends ComputationManagerFactory> longTimeExecutionComputationManagerFactoryClass) {
        this(shortTimeExecutionComputationManagerFactoryClass, longTimeExecutionComputationManagerFactoryClass);
        this.version = version;
    }

    public static DefaultComputationManagerConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    public static DefaultComputationManagerConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);

        return platformConfig.getOptionalModuleConfig(CONFIG_MODULE_NAME)
                .map(moduleConfig -> {
                    Class<? extends ComputationManagerFactory> shortTimeExecutionComputationManagerFactoryClass = moduleConfig.getClassProperty("short-time-execution-computation-manager-factory", ComputationManagerFactory.class);
                    Class<? extends ComputationManagerFactory> longTimeExecutionComputationManagerFactoryClass = moduleConfig.getClassProperty("long-time-execution-computation-manager-factory", ComputationManagerFactory.class, null);
                    DefaultComputationManagerConfig config = moduleConfig.getOptionalStringProperty("version")
                            .map(v -> new DefaultComputationManagerConfig(new VersionConfig(v), shortTimeExecutionComputationManagerFactoryClass, longTimeExecutionComputationManagerFactoryClass))
                            .orElseGet(() -> new DefaultComputationManagerConfig(shortTimeExecutionComputationManagerFactoryClass, longTimeExecutionComputationManagerFactoryClass));
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info(config.toString());
                    }
                    return config;
                }).orElseGet(() -> {
                    Class<? extends ComputationManagerFactory> shortTimeExecutionComputationManagerFactoryClass;
                    try {
                        shortTimeExecutionComputationManagerFactoryClass = (Class<? extends ComputationManagerFactory>) Class.forName(DEFAULT_SHORT_TIME_EXECUTION_COMPUTATION_MANAGER_FACTORY_CLASS);
                    } catch (ClassNotFoundException e) {
                        throw new UncheckedClassNotFoundException(e);
                    }
                    DefaultComputationManagerConfig config = new DefaultComputationManagerConfig(shortTimeExecutionComputationManagerFactoryClass, null);
                    if (LOGGER.isInfoEnabled()) {
                        LOGGER.info(config.toString());
                    }
                    return config;
                });
    }

    public ComputationManager createShortTimeExecutionComputationManager() {
        try {
            return new LazyCreatedComputationManager(shortTimeExecutionComputationManagerFactoryClass.newInstance());
        } catch (InstantiationException e) {
            throw new UncheckedInstantiationException(e);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException(e);
        }
    }

    public ComputationManager createLongTimeExecutionComputationManager() {
        if (longTimeExecutionComputationManagerFactoryClass != null) {
            try {
                return new LazyCreatedComputationManager(longTimeExecutionComputationManagerFactoryClass.newInstance());
            } catch (InstantiationException e) {
                throw new UncheckedInstantiationException(e);
            } catch (IllegalAccessException e) {
                throw new UncheckedIllegalAccessException(e);
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String str = "DefaultComputationManagerConfig(shortTimeExecutionComputationManagerFactoryClass=" + shortTimeExecutionComputationManagerFactoryClass.getName();
        if (longTimeExecutionComputationManagerFactoryClass != null) {
            str += ", longTimeExecutionComputationManagerFactoryClass=" + longTimeExecutionComputationManagerFactoryClass.getName();
        }
        str += ")";
        return str;
    }

    @Override
    public String getName() {
        return CONFIG_MODULE_NAME;
    }

    @Override
    public String getVersion() {
        return this.version.toString();
    }
}
