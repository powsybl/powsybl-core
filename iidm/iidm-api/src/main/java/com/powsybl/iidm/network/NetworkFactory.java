/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;

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
     * Create a network as the result of the merge of the given networks.
     *
     * @param id id of the network
     * @param networks the networks to merge
     * @return the merged network
     */
    Network createByMerging(String id, Network... networks);

    /**
     * Find a {@code NetworkFactory} instance base on its name.
     *
     * @param name name of the {@code NetworkFactory}
     * @return {@code NetworkFactory} instance with the given name.
     */
    static NetworkFactory find(String name) {
        return PlatformConfigNamedProvider.Finder.find(
                name, "network", NetworkFactoryService.class,
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

}
