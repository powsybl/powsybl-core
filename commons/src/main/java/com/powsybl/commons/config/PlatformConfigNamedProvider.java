/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.google.common.collect.Lists;
import com.powsybl.commons.PowsyblException;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * A provider that can be loaded by by Java's ServiceLoader based on its name
 * present in an entry in the PlatformConfig.
 *
 * @author Jon Harper {@literal <jon.harper at rte-france.com>}
 */
public interface PlatformConfigNamedProvider {

    /**
     * Get the name.
     *
     * @return the name
     */
    String getName();

    /**
     * Get the Provider name used for identifying this provider in the
     * PlatformConfig. Defaults to getName(). Override this method only if getName() is
     * already implemented and returns the wrong name.
     *
     * @return the name
     */
    default String getPlatformConfigName() {
        return getName();
    }

    /**
     * A utility class to find providers in the {@link PlatformConfig} by their
     * names configured in standard fields. the find* methods use the standard
     * fields while the find*BackwardsCompatible methods also look in the legacy
     * fields.
     *
     * @author Jon harper {@literal <jon.harper at rte-france.com>}
     * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
     */
    final class Finder {

        private Finder() {
        }

        private static final String DEFAULT_SERVICE_IMPL_NAME_PROPERTY = "default-impl-name";

        private static final Map<Class<? extends PlatformConfigNamedProvider>, List<? extends PlatformConfigNamedProvider>> PROVIDERS = new ConcurrentHashMap<>();

        /**
         * Find the default provider configured in the standard field of
         * {@code moduleName} in {@code platformConfig} among the {@code providers}
         * arguments based on its name.
         *
         * @return the provider
         */
        public static <T extends PlatformConfigNamedProvider> T findDefault(String moduleName,
                Class<T> clazz, PlatformConfig platformConfig) {
            return find(null, moduleName,
                List.of(DEFAULT_SERVICE_IMPL_NAME_PROPERTY), clazz,
                    platformConfig);
        }

        /**
         * Find the provider among the {@code providers} based on its {@code name}, or
         * if {@code name} is null find the default provider like @{link findDefault}
         *
         * @return the provider
         */
        public static <T extends PlatformConfigNamedProvider> T find(String name, String moduleName,
                Class<T> clazz, PlatformConfig platformConfig) {
            return find(name, moduleName,
                List.of(DEFAULT_SERVICE_IMPL_NAME_PROPERTY), clazz,
                    platformConfig);
        }

        private static Optional<String> getOptionalFirstProperty(ModuleConfig moduleConfig,
                List<String> propertyNames) {
            return propertyNames.stream()
                    .map(moduleConfig::getOptionalStringProperty)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst();
        }

        @SuppressWarnings("unchecked")
        private static <K, V, T extends V> T alwaysSameComputeIfAbsent(
                Map<K, V> map, K key,
                Function<? super K, T> mappingFunction) {
            // Casting to (T) is safe if we awlays pass the same T argument for a given key
            return (T) map.computeIfAbsent(key, mappingFunction);
        }

        private static <T extends PlatformConfigNamedProvider> T find(String name,
                String moduleName, List<String> propertyNames, Class<T> clazz,
                PlatformConfig platformConfig) {
            List<T> providers = alwaysSameComputeIfAbsent(PROVIDERS, clazz,
                k -> Lists.newArrayList(ServiceLoader.load(clazz, PlatformConfigNamedProvider.class.getClassLoader())));
            return find(name, moduleName, propertyNames, providers, platformConfig, clazz);
        }

        // package private for tests
        static <T extends PlatformConfigNamedProvider> T find(String name,
                String moduleName, List<String> propertyNames, List<T> providers,
                PlatformConfig platformConfig, Class<T> clazz) {
            Objects.requireNonNull(moduleName);
            Objects.requireNonNull(propertyNames);
            Objects.requireNonNull(providers);
            Objects.requireNonNull(platformConfig);
            Objects.requireNonNull(clazz);

            if (providers.isEmpty()) {
                throw new PowsyblException("No " + clazz.getSimpleName() + " providers found");
            }

            // if no implementation name is provided through the API we look for information
            // in platform configuration
            String finalName = name != null ? name
                    : platformConfig.getOptionalModuleConfig(moduleName)
                            .flatMap(mc -> getOptionalFirstProperty(mc, propertyNames))
                            .orElse(null);
            T provider;
            if (providers.size() == 1 && finalName == null) {
                // no information to select the implementation but only one provider, so we can
                // use it by default (that is the most common use case)
                provider = providers.get(0);
            } else {
                if (providers.size() > 1 && finalName == null) {
                    // several providers and no information to select which one to choose, we can
                    // only throw an exception
                    List<String> providerNames = providers.stream()
                            .map(PlatformConfigNamedProvider::getPlatformConfigName)
                            .toList();
                    throw new PowsyblException(
                            "Several " + clazz.getSimpleName() + " implementations found (" + providerNames
                                    + "), you must add configuration in PlatformConfig's module \""
                                    + moduleName + "\" to select the implementation");
                }
                provider = providers.stream()
                        .filter(p -> p.getPlatformConfigName().equals(finalName)).findFirst()
                        .orElseThrow(() -> new PowsyblException(
                                clazz.getSimpleName() + " '" + finalName + "' not found"));
            }

            return provider;
        }

    }

}
