/**
 * Copyright (c) 2016, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.exceptions.UncheckedIllegalAccessException;
import com.powsybl.commons.exceptions.UncheckedInstantiationException;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 * @author Mathieu Bague <mathieu.bague at rte-france.com>
 */
public interface ComponentDefaultConfig {

    static ComponentDefaultConfig load() {
        return Impl.getDefaultConfig();
    }

    static ComponentDefaultConfig load(PlatformConfig platformConfig) {
        return platformConfig.getOptionalModuleConfig(Impl.CONFIG_MODULE_NAME_V_1_1)
                .map(moduleConfig -> new Impl(new ConfigVersion("1.1"), moduleConfig))
                .orElseGet(() -> platformConfig.getOptionalModuleConfig(Impl.CONFIG_MODULE_NAME_V_1_0)
                        .map(moduleConfig -> new Impl(new ConfigVersion("1.0"), moduleConfig))
                        .orElse(null));

    }

    class Impl implements ComponentDefaultConfig, Versionable {

        private static final String CONFIG_MODULE_NAME_V_1_0 = "componentDefaultConfig";

        private static final String CONFIG_MODULE_NAME_V_1_1 = "component-default-config";

        /**
         * Lazily intialized config from the default platform config.
         */
        private static ComponentDefaultConfig defaultConfig;

        private ConfigVersion version = new ConfigVersion(CONFIG_MODULE_NAME_V_1_0);

        private static synchronized ComponentDefaultConfig getDefaultConfig() {
            if (defaultConfig == null) {
                defaultConfig = ComponentDefaultConfig.load(PlatformConfig.defaultConfig());
            }
            return defaultConfig;
        }

        private final ModuleConfig config;

        public Impl(ModuleConfig config) {
            this.config = config;
        }

        public Impl(ConfigVersion version, ModuleConfig config) {
            this(config);
            this.version = version;
        }

        @Override
        public <T> Class<? extends T> findFactoryImplClass(Class<T> factoryBaseClass) {
            Objects.requireNonNull(factoryBaseClass);
            String propertyName = factoryBaseClass.getSimpleName();
            if (config == null) {
                throw new PowsyblException("Property " + propertyName + " is not set");
            }
            return config.getClassProperty(propertyName, factoryBaseClass);
        }

        @Override
        public <T, U extends T> Class<? extends T> findFactoryImplClass(Class<T> factoryBaseClass, Class<U> defaultFactoryImplClass) {
            Objects.requireNonNull(factoryBaseClass);
            Objects.requireNonNull(defaultFactoryImplClass);
            String propertyName = factoryBaseClass.getSimpleName();
            return config != null ? config.getClassProperty(propertyName, factoryBaseClass, defaultFactoryImplClass)
                    : defaultFactoryImplClass;
        }

        @Override
        public <T> T newFactoryImpl(Class<T> factoryBaseClass) {
            try {
                return findFactoryImplClass(factoryBaseClass).newInstance();
            } catch (IllegalAccessException e) {
                throw new UncheckedIllegalAccessException(e);
            } catch (InstantiationException e) {
                throw new UncheckedInstantiationException(e);
            }
        }

        @Override
        public <T, U extends T> T newFactoryImpl(Class<T> factoryBaseClass, Class<U> defaultFactoryImplClass) {
            try {
                return findFactoryImplClass(factoryBaseClass, defaultFactoryImplClass).newInstance();
            } catch (IllegalAccessException e) {
                throw new UncheckedIllegalAccessException(e);
            } catch (InstantiationException e) {
                throw new UncheckedInstantiationException(e);
            }
        }

        @Override
        public String getName() {
            if (version.equalsOrIsNewerThan("1.1")) {
                return CONFIG_MODULE_NAME_V_1_1;
            }
            return CONFIG_MODULE_NAME_V_1_0;
        }

        @Override
        public String getVersion() {
            return version.toString();
        }
    }

    <T> Class<? extends T> findFactoryImplClass(Class<T> factoryBaseClass);

    <T, U extends T> Class<? extends T> findFactoryImplClass(Class<T> factoryBaseClass, Class<U> defaultFactoryImplClass);

    <T> T newFactoryImpl(Class<T> factoryBaseClass);

    <T, U extends T> T newFactoryImpl(Class<T> factoryBaseClass, Class<U> defaultFactoryImplClass);
}
