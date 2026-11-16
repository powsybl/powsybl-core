/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.naming;

import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.List;
import java.util.Optional;

/**
 * Service loader to discover all CGMES NamingStrategy providers
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public class NamingStrategiesServiceLoader {

    private static final ServiceLoaderCache<NamingStrategyProvider> NAMING_STRATEGY_PROVIDERS = new ServiceLoaderCache<>(NamingStrategyProvider.class);

    public List<NamingStrategyProvider> findAllProviders() {
        return NAMING_STRATEGY_PROVIDERS.getServices();
    }

    public Optional<NamingStrategyProvider> findProviderByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }
        return findAllProviders().stream()
                .filter(provider -> name.equals(provider.getName()))
                .findFirst();
    }
}
