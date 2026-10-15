/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import com.powsybl.commons.PowsyblException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * Discovers the {@link RuntimeConfigProvider} implementations of the classpath and stacks
 * them into a single {@link ModuleConfigRepository}.
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public final class RuntimeConfigRepositories {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeConfigRepositories.class);

    /**
     * Set this system property to {@code false} to ignore every runtime configuration
     * provider, which is the way to check whether a surprising value comes from a jar.
     *
     * <p>Following {@link Boolean#parseBoolean(String)} semantics, the layer is enabled unless
     * this property is set to exactly {@code false} (case-insensitive); any other value,
     * including a typo such as {@code 1}, leaves the layer enabled.
     */
    public static final String ENABLED_PROPERTY = "powsybl.config.runtime.enabled";

    private RuntimeConfigRepositories() {
    }

    /**
     * Returns the configuration contributed by the providers of the classpath, or
     * {@link Optional#empty()} when there is none.
     */
    public static Optional<ModuleConfigRepository> load() {
        if (!isEnabled()) {
            LOGGER.info("Runtime configuration is disabled by system property {}", ENABLED_PROPERTY);
            return Optional.empty();
        }
        return load(ServiceLoader.load(RuntimeConfigProvider.class, RuntimeConfigRepositories.class.getClassLoader())
                .stream()
                .map(ServiceLoader.Provider::get)
                .toList());
    }

    static Optional<ModuleConfigRepository> load(List<RuntimeConfigProvider> providers) {
        Objects.requireNonNull(providers);

        if (!isEnabled()) {
            LOGGER.info("Runtime configuration is disabled by system property {}", ENABLED_PROPERTY);
            return Optional.empty();
        }

        List<ModuleConfigRepository> repositories = new ArrayList<>();
        List<String> contributors = new ArrayList<>();
        List<String> names = new ArrayList<>();
        for (RuntimeConfigProvider provider : sortByDecreasingPrecedence(providers)) {
            getRepository(provider).ifPresent(repository -> {
                names.add(provider.getName());
                repositories.add(repository);
                contributors.add(provider.getName() + " (priority " + provider.getPriority() + ")");
            });
        }

        if (repositories.isEmpty()) {
            return Optional.empty();
        }
        warnOverlappingProperties(names, repositories);
        LOGGER.info("Runtime configuration provided by, in decreasing order of precedence: {}", contributors);
        return Optional.of(new StackedModuleConfigRepository(repositories));
    }

    /**
     * Logs a warning for each property defined by more than one of the given repositories, which must be
     * in decreasing order of precedence. The names identify the repositories in the message (provider
     * names, resource URLs...).
     */
    static void warnOverlappingProperties(List<String> names, List<ModuleConfigRepository> repositories) {
        findOverlappingProperties(names, repositories).forEach(o -> LOGGER.warn(
                "Runtime configuration property '{}.{}' is defined by both '{}' and '{}'; '{}' takes precedence",
                o.moduleName(), o.propertyName(), o.winner(), o.loser(), o.winner()));
    }

    /**
     * A property defined by two providers: {@code winner} has precedence over {@code loser}.
     */
    record PropertyOverlap(String moduleName, String propertyName, String winner, String loser) {
    }

    /**
     * Diagnostics only. Both lists are parallel and in decreasing order of precedence, so the first
     * owner recorded for a property is the one that wins. Each property is visited once, hence linear
     * in the size of the configuration. A repository that cannot enumerate its modules contributes
     * nothing, and a failure while inspecting one is logged and ignored: it never affects resolution.
     */
    static List<PropertyOverlap> findOverlappingProperties(List<String> providerNames,
                                                           List<ModuleConfigRepository> repositories) {
        List<PropertyOverlap> overlaps = new ArrayList<>();
        Map<PropertyKey, String> ownerByProperty = new HashMap<>();
        for (int i = 0; i < repositories.size(); i++) {
            String providerName = providerNames.get(i);
            ModuleConfigRepository repository = repositories.get(i);
            try {
                for (String moduleName : repository.getModuleNames()) {
                    Optional<ModuleConfig> moduleConfig = repository.getModuleConfig(moduleName);
                    if (moduleConfig.isEmpty()) {
                        continue;
                    }
                    for (String propertyName : moduleConfig.get().getPropertyNames()) {
                        String winner = ownerByProperty.putIfAbsent(new PropertyKey(moduleName, propertyName), providerName);
                        if (winner != null) {
                            overlaps.add(new PropertyOverlap(moduleName, propertyName, winner, providerName));
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.debug("Could not check provider '{}' for overlapping properties", providerName, e);
            }
        }
        return overlaps;
    }

    private record PropertyKey(String module, String property) {
    }

    private static List<RuntimeConfigProvider> sortByDecreasingPrecedence(List<RuntimeConfigProvider> providers) {
        return providers.stream()
                .sorted(Comparator.comparingInt(RuntimeConfigProvider::getPriority).reversed()
                        .thenComparing(RuntimeConfigRepositories::requireName))
                .toList();
    }

    private static String requireName(RuntimeConfigProvider provider) {
        String name = provider.getName();
        if (name == null) {
            throw new PowsyblException(
                    "Runtime configuration provider '" + provider.getClass().getName() + "' returned a null name");
        }
        return name;
    }

    private static Optional<ModuleConfigRepository> getRepository(RuntimeConfigProvider provider) {
        try {
            return provider.getModuleConfigRepository();
        } catch (Exception e) {
            throw new PowsyblException("Runtime configuration provider '" + provider.getName() + "' has failed", e);
        }
    }

    private static boolean isEnabled() {
        return Boolean.parseBoolean(System.getProperty(ENABLED_PROPERTY, "true"));
    }
}
