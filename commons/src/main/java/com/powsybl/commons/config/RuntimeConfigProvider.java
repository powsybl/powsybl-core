/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.commons.config;

import java.util.Optional;

/**
 * Contributes configuration values that apply when the user has not defined them.
 *
 * <p>Implementations are discovered with {@link java.util.ServiceLoader}. The values they
 * provide sit below the user configuration files and above the default values hardcoded in
 * the Java code, so a value set by the user always wins. Several implementations can be
 * present at the same time: they are all used, ordered by {@link #getPriority()}.
 *
 * <p>Use this to ship the defaults of an application in a jar, rather than forcing a
 * configuration file onto every user. Implement it directly when the values have to be
 * computed at runtime; to serve static values, package a {@code com/powsybl/config/config.yml}
 * resource instead and let {@link ClasspathRuntimeConfigProvider} pick it up.
 *
 * @author Fabrice Buscaylet {@literal <fabrice.buscaylet at artelys.com>}
 */
public interface RuntimeConfigProvider {

    /**
     * Identifies this provider in logs and error messages. It is not a selector: the user
     * never has to name a provider in their configuration.
     */
    String getName();

    /**
     * Ranks this provider against the other runtime configuration providers. Higher wins.
     * Providers of equal priority are ordered by name, so the resulting configuration never
     * depends on the order of the classpath.
     */
    default int getPriority() {
        return 0;
    }

    /**
     * Returns the configuration contributed by this provider, or {@link Optional#empty()}
     * when it has nothing to contribute. Normally called once per JVM, inside the memoization
     * of {@link PlatformConfig#defaultConfig()} — but that is a property of that caller, not a
     * guarantee of this interface: a direct caller of {@link RuntimeConfigRepositories#load()}
     * may invoke it more than once.
     */
    Optional<ModuleConfigRepository> getModuleConfigRepository();
}
