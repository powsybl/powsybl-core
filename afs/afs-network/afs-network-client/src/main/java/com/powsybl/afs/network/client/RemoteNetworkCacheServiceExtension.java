/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.network.client;

import com.google.auto.service.AutoService;
import com.google.common.base.Suppliers;
import com.powsybl.afs.ServiceExtension;
import com.powsybl.afs.ext.base.NetworkCacheService;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ServiceExtension.class)
public class RemoteNetworkCacheServiceExtension implements ServiceExtension<NetworkCacheService> {

    @Override
    public ServiceKey<NetworkCacheService> getServiceKey() {
        return new ServiceKey<>(NetworkCacheService.class, true);
    }

    @Override
    public RemoteNetworkCacheService createService() {
        return new RemoteNetworkCacheService(Suppliers.memoize(RemoteNetworkCacheServiceConfig::load));
    }
}
