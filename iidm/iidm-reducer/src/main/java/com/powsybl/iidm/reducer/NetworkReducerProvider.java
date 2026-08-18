/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.google.common.collect.Lists;
import com.powsybl.commons.Versionable;
import com.powsybl.commons.config.PlatformConfigNamedProvider;

import java.util.List;
import java.util.ServiceLoader;

/**
 * Service Provider Interface for network reducer implementations.
 *
 * <p>A network reducer provider is required to implement the main method {@link #create}, in charge of building
 * a {@link NetworkReducer} instance able to reduce a network for a given {@link NetworkPredicate} and
 * {@link ReductionParameters}.
 *
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
public interface NetworkReducerProvider extends Versionable, PlatformConfigNamedProvider {

    static List<NetworkReducerProvider> findAll() {
        return Lists.newArrayList(ServiceLoader.load(NetworkReducerProvider.class, NetworkReducerProvider.class.getClassLoader()));
    }

    /**
     * Create a network reducer for the given {@code predicate} and {@code parameters}.
     *
     * @param predicate the predicate used to select the elements of the network to keep
     * @param parameters the reduction parameters
     * @return a new {@link NetworkReducer}
     */
    NetworkReducer create(NetworkPredicate predicate, ReductionParameters parameters);

    /**
     * Create a network reducer for the given {@code predicate} and default reduction parameters.
     *
     * @param predicate the predicate used to select the elements of the network to keep
     * @return a new {@link NetworkReducer}
     */
    default NetworkReducer create(NetworkPredicate predicate) {
        return create(predicate, ReductionParameters.getDefault());
    }
}
