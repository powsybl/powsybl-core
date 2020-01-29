/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;
import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.List;

/**
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface NetworkFactory {

    /**
     * Create a network.
     *
     * @param id id of the network
     * @param sourceFormat source format
     * @return a network
     */
    Network createNetwork(String id, String sourceFormat);

    /**
     * Find a {@code NetworkFactory} instance base on its name.
     *
     * @param name name of the {@code NetworkFactory}
     * @return {@code NetworkFactory} instance with the given name.
     */
    static NetworkFactory find(String name) {
        return PlatformConfigNamedProvider.Finder.find(
                name, "network", Private.PROVIDERS_SUPPLIERS.get(),
                PlatformConfig.defaultConfig())
                .createNetworkFactory();
    }

    /**
     * Find default {@code NetworkFactory} instance.
     *
     * @return default {@code NetworkFactory} instance.
     */
    static NetworkFactory findDefault() {
        return find(null);
    }

    /**
     * @deprecated Use {@link Network#create(String, String)} instead.
     */
    @Deprecated
    static Network create(String id, String sourceFormat) {
        return findDefault().createNetwork(id, sourceFormat);
    }

    /**
     * @deprecated Do not use. Exists only to have private static state.
     */
    @Deprecated
    static final class Private {
        Private() { }

        private static final Supplier<List<NetworkFactoryService>> PROVIDERS_SUPPLIERS = Suppliers
                .memoize(() -> new ServiceLoaderCache<>(NetworkFactoryService.class)
                        .getServices());
    }
}
