/**
 * Copyright (c) 2020, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A provider that can be loaded by by Java's ServiceLoader based on its name
 * present in an entry in the PlatformConfig.
 *
 * @author Jon Harper <jon.harper at rte-france.com>
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
     * @author Jon harper <jon.harper at rte-france.com>
     * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
     */
    static final class Finder {

        private Finder() {
        }

        private static final String DEFAULT_SERVICE_IMPL_NAME_PROPERTY = "default-impl-name";
        private static final String LEGACY_SERVICE_IMPL_NAME_PROPERTY = "default";

        /**
         * Find the default provider configured in the standard field of
         * {@code moduleName} in {@code platformConfig} among the {@code providers}
         * arguments based on its name.
         *
         * @return the provider
         */
        public static <T extends PlatformConfigNamedProvider> T findDefault(String moduleName,
                List<T> providers, PlatformConfig platformConfig) {
            return find(null, moduleName, Arrays.asList(DEFAULT_SERVICE_IMPL_NAME_PROPERTY),
                    providers, platformConfig);
        }

        /**
         * Find the provider among the {@code providers} based on its {@code name}, or
         * if {@code name} is null find the default provider like @{link findDefault}
         *
         * @return the provider
         */
        public static <T extends PlatformConfigNamedProvider> T find(String name, String moduleName,
                List<T> providers, PlatformConfig platformConfig) {
            return find(name, moduleName, Arrays.asList(DEFAULT_SERVICE_IMPL_NAME_PROPERTY),
                    providers, platformConfig);
        }

        /**
         * Find the default provider configured in the standard field or the legacy
         * field of {@code moduleName} in {@code platformConfig} among the
         * {@code providers} arguments based on its name.
         *
         * @deprecated Use {@link #findDefault} instead
         *
         * @return the provider
         */
        @Deprecated
        public static <T extends PlatformConfigNamedProvider> T findDefaultBackwardsCompatible(
                String moduleName, List<T> providers, PlatformConfig platformConfig) {
            return find(null, moduleName, Arrays.asList(DEFAULT_SERVICE_IMPL_NAME_PROPERTY,
                    LEGACY_SERVICE_IMPL_NAME_PROPERTY), providers, platformConfig);
        }

        /**
         * Find the provider among the {@code providers} based on its {@code name}, or
         * if {@code name} is null find the default provider like @{link
         * findDefaultBackwardsCompatible}
         *
         * @deprecated Use {@link #find} instead
         *
         * @return the provider
         */
        @Deprecated
        public static <T extends PlatformConfigNamedProvider> T findBackwardsCompatible(String name,
                String moduleName, List<T> providers, PlatformConfig platformConfig) {
            return find(name, moduleName, Arrays.asList(DEFAULT_SERVICE_IMPL_NAME_PROPERTY,
                    LEGACY_SERVICE_IMPL_NAME_PROPERTY), providers, platformConfig);
        }

        private static Optional<String> getOptionalFirstProperty(ModuleConfig moduleConfig,
                List<String> propertyNames) {
            return propertyNames.stream().map(moduleConfig::getOptionalStringProperty)
                    .filter(Optional::isPresent).map(Optional::get).findFirst();
        }

        private static <T extends PlatformConfigNamedProvider> T find(String name,
                String moduleName, List<String> propertyNames, List<T> providers,
                PlatformConfig platformConfig) {
            Objects.requireNonNull(providers);
            Objects.requireNonNull(platformConfig);

            if (providers.isEmpty()) {
                throw new PowsyblException("No " + moduleName + " providers found");
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
                // use it by default
                // (that is be the most common use case)
                provider = providers.get(0);
            } else {
                if (providers.size() > 1 && finalName == null) {
                    // several providers and no information to select which one to choose, we can
                    // only throw
                    // an exception
                    List<String> loadFlowNames = providers.stream()
                            .map(PlatformConfigNamedProvider::getPlatformConfigName)
                            .collect(Collectors.toList());
                    throw new PowsyblException(
                            "Several loadflow implementations found (" + loadFlowNames
                                    + "), you must add configuration to select the implementation");
                }
                provider = providers.stream()
                        .filter(p -> p.getPlatformConfigName().equals(finalName)).findFirst()
                        .orElseThrow(() -> new PowsyblException(
                                "Loadflow '" + finalName + "' not found"));
            }

            return provider;
        }

    }

}
