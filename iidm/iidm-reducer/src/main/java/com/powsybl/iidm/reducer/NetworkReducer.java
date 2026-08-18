/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.reducer;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.iidm.network.Network;

/**
 * Warning: a NetworkReducer must not be considered reusable nor thread-safe.
 *
 * @author Mathieu Bague {@literal <mathieu.bague at rte-france.com>}
 */
public interface NetworkReducer {

    static DefaultNetworkReducerBuilder builder() {
        return new DefaultNetworkReducerBuilder();
    }

    /**
     * Get a network reducer provider named {@code name}. In the case of a null {@code name}, the default
     * implementation is used.
     *
     * @param name name of the network reducer implementation, null if we want to use the default one
     * @return a network reducer provider named {@code name}
     */
    static NetworkReducerProvider find(String name) {
        return PlatformConfigNamedProvider.Finder
                .find(name, "network-reducer", NetworkReducerProvider.class,
                        PlatformConfig.defaultConfig());
    }

    /**
     * Get the default network reducer provider.
     *
     * @throws com.powsybl.commons.PowsyblException in case we cannot find a default implementation
     * @return the default network reducer provider
     */
    static NetworkReducerProvider find() {
        return find(null);
    }

    default void reduce(Network network) {
        reduce(network, ReductionParameters.getDefault());
    }

    void reduce(Network network, ReductionParameters parameters);
}
