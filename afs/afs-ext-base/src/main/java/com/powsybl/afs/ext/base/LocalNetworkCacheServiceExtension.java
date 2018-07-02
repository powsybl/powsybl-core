/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.ext.base;

import com.google.auto.service.AutoService;
import com.powsybl.afs.ServiceCreationContext;
import com.powsybl.afs.ServiceExtension;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ServiceExtension.class)
public class LocalNetworkCacheServiceExtension implements ServiceExtension<NetworkCacheService> {

    @Override
    public ServiceKey<NetworkCacheService> getServiceKey() {
        return new ServiceKey<>(NetworkCacheService.class, false);
    }

    @Override
    public NetworkCacheService createService(ServiceCreationContext context) {
        return new LocalNetworkCacheService();
    }
}
