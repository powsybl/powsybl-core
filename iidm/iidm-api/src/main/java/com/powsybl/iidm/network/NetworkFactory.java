/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import com.powsybl.commons.PowsyblException;
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
     * Get default {@code NetworkFactory} instance. A unique implementation of {@code NetworkFactoryService} configured
     * for {@code ServiceLoader} is supposed to be found in the classpath.
     *
     * @return default {@code NetworkFactory} instance.
     */
    static NetworkFactory getDefault() {
        List<NetworkFactoryService> services = new ServiceLoaderCache<>(NetworkFactoryService.class).getServices();
        if (services.isEmpty()) {
            throw new PowsyblException("No IIDM implementation found");
        }
        if (services.size() > 1) {
            throw new PowsyblException("Multiple IIDM implementations found");
        }
        return services.get(0).createNetworkFactory();
    }

    /**
     * @deprecated Use {@link Network#create(String, String)} instead.
     */
    @Deprecated
    static Network create(String id, String sourceFormat) {
        return getDefault().createNetwork(id, sourceFormat);
    }
}
