/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.List;
import java.util.Optional;

/**
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
public class NamingStrategiesServiceLoader {

    private static final ServiceLoaderCache<NamingStrategy> NAMING_STRATEGY_CACHE = new ServiceLoaderCache<>(NamingStrategy.class);

    public List<NamingStrategy> findAllNamingStrategies() {
        return NAMING_STRATEGY_CACHE.getServices();
    }

    public Optional<NamingStrategy> findNamingStrategyByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Optional.empty();
        }

        return findAllNamingStrategies().stream()
                .filter(strategy -> name.equals(strategy.getName()))
                .findFirst();
    }
}
