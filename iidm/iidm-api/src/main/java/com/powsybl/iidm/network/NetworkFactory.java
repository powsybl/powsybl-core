/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.util.ServiceLoaderCache;

import java.util.Objects;

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
        Objects.requireNonNull(name);
        return new ServiceLoaderCache<>(NetworkFactoryService.class).getServices().stream()
                .filter(s -> s.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new PowsyblException("'" + name + "' IIDM implementation not found"))
                .createNetworkFactory();
    }

    /**
     * Find default {@code NetworkFactory} instance.
     *
     * @return default {@code NetworkFactory} instance.
     */
    static NetworkFactory findDefault() {
        return find(NetworkFactoryConstants.DEFAULT);
    }

    /**
     * @deprecated Use {@link Network#create(String, String)} instead.
     */
    @Deprecated
    static Network create(String id, String sourceFormat) {
        return findDefault().createNetwork(id, sourceFormat);
    }
}
