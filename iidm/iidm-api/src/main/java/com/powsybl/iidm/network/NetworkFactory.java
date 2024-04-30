/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.config.PlatformConfig;
import com.powsybl.commons.config.PlatformConfigNamedProvider;

/**
 *
 * @author Geoffroy Jamgotchian {@literal <geoffroy.jamgotchian at rte-france.com>}
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
     * Create a network as the result of the merge of the given networks. Each given network is represented as a
     * subnetwork in the resulting network. As a result of that merge, the given networks are empty at the end of the
     * call.
     *
     * @param id id of the network
     * @param networks the networks to merge
     * @return the merged network
     */
    Network merge(String id, Network... networks);

    /**
     * Create a network as the result of the merge of the given networks. Each given network is represented as a
     * subnetwork in the resulting network. As a result of that merge, the given networks are empty at the end of the
     * call. Note that, as no id is given, the id of the network created is generated.
     *
     * @param networks the networks to merge
     * @return the merged network
     */
    Network merge(Network... networks);

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
    @Deprecated(since = "2.6.0")
    static Network create(String id, String sourceFormat) {
        return findDefault().createNetwork(id, sourceFormat);
    }

}
