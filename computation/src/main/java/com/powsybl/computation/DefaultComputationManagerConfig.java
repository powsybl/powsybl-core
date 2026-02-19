/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.computation;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.exceptions.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
 */
public class DefaultComputationManagerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultComputationManagerConfig.class);

    /**
     * A String is used here for default computation manager factory to avoid a direct dependency to local computation
     * manager.
     */
    private static final String DEFAULT_SHORT_TIME_EXECUTION_COMPUTATION_MANAGER_FACTORY_CLASS
            = "com.powsybl.computation.local.LocalComputationManagerFactory";

    private final Class<? extends ComputationManagerFactory> shortTimeExecutionComputationManagerFactoryClass;

    private final Class<? extends ComputationManagerFactory> longTimeExecutionComputationManagerFactoryClass;

    public DefaultComputationManagerConfig(Class<? extends ComputationManagerFactory> shortTimeExecutionComputationManagerFactoryClass,
                                           Class<? extends ComputationManagerFactory> longTimeExecutionComputationManagerFactoryClass) {
        this.shortTimeExecutionComputationManagerFactoryClass = Objects.requireNonNull(shortTimeExecutionComputationManagerFactoryClass);
        this.longTimeExecutionComputationManagerFactoryClass = longTimeExecutionComputationManagerFactoryClass;
    }

    public static DefaultComputationManagerConfig load() {
        return load(PlatformConfig.defaultConfig());
    }

    @SuppressWarnings("unchecked")
    public static DefaultComputationManagerConfig load(PlatformConfig platformConfig) {
        Objects.requireNonNull(platformConfig);
        DefaultComputationManagerConfig config = platformConfig.getOptionalModuleConfig("default-computation-manager")
                .map(moduleConfig -> {
                    Class<? extends ComputationManagerFactory> shortTimeExecutionComputationManagerFactoryClass = moduleConfig.getClassProperty("short-time-execution-computation-manager-factory", ComputationManagerFactory.class);
                    Class<? extends ComputationManagerFactory> longTimeExecutionComputationManagerFactoryClass = moduleConfig.getClassProperty("long-time-execution-computation-manager-factory", ComputationManagerFactory.class, null);
                    return new DefaultComputationManagerConfig(shortTimeExecutionComputationManagerFactoryClass, longTimeExecutionComputationManagerFactoryClass);
                })
                .orElseGet(() -> {
                    Class<? extends ComputationManagerFactory> shortTimeExecutionComputationManagerFactoryClass;
                    try {
                        shortTimeExecutionComputationManagerFactoryClass = (Class<? extends ComputationManagerFactory>) Class.forName(DEFAULT_SHORT_TIME_EXECUTION_COMPUTATION_MANAGER_FACTORY_CLASS);
                    } catch (ClassNotFoundException e) {
                        throw new UncheckedClassNotFoundException(e);
                    } catch (ClassCastException e) {
                        throw new UncheckedClassCastExceptionException(e);
                    }
                    return new DefaultComputationManagerConfig(shortTimeExecutionComputationManagerFactoryClass, null);
                });
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info(config.toString());
        }
        return config;
    }

    private ComputationManager createComputationManager(Class<? extends ComputationManagerFactory> computationManagerFactoryClass) {
        try {
            return new LazyCreatedComputationManager(computationManagerFactoryClass.getDeclaredConstructor().newInstance());
        } catch (InstantiationException e) {
            throw new UncheckedInstantiationException(e);
        } catch (IllegalAccessException e) {
            throw new UncheckedIllegalAccessException(e);
        } catch (NoSuchMethodException e) {
            throw new UncheckedNoSuchMethodException(e);
        } catch (InvocationTargetException e) {
            throw new UncheckedInvocationTargetException(e);
        }
    }

    public ComputationManager createShortTimeExecutionComputationManager() {
        return createComputationManager(shortTimeExecutionComputationManagerFactoryClass);
    }

    public ComputationManager createLongTimeExecutionComputationManager() {
        return longTimeExecutionComputationManagerFactoryClass == null ?
            createShortTimeExecutionComputationManager() :
            createComputationManager(longTimeExecutionComputationManagerFactoryClass);
    }

    @Override
    public String toString() {
        String str = "DefaultComputationManagerConfig(shortTimeExecutionComputationManagerFactoryClass=" + shortTimeExecutionComputationManagerFactoryClass.getName()
                + ", longTimeExecutionComputationManagerFactoryClass=";
        str += Objects.requireNonNullElse(longTimeExecutionComputationManagerFactoryClass, shortTimeExecutionComputationManagerFactoryClass).getName();
        str += ")";
        return str;
    }
}
