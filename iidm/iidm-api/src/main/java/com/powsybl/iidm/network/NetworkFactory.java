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
 * To create a new empty network:
 *<pre>
 *    Network n = NetworkFactory.create("test");
 *</pre>
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class NetworkFactory {

    private static final ServiceLoaderCache<NetworkFactoryService> LOADER
            = new ServiceLoaderCache(NetworkFactoryService.class);

    private NetworkFactory() {
    }

    public static Network create(String id, String sourceFormat) {
        List<NetworkFactoryService> services = LOADER.getServices();
        if (services.isEmpty()) {
            throw new PowsyblException("No IIDM implementation found");
        }
        return services.get(0).createNetwork(id, sourceFormat);
    }
}
