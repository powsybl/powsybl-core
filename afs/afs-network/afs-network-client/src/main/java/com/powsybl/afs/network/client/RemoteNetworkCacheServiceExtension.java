/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.afs.network.client;

import com.google.auto.service.AutoService;
import com.google.common.base.Supplier;
import com.powsybl.afs.ServiceCreationContext;
import com.powsybl.afs.ServiceExtension;
import com.powsybl.afs.ext.base.NetworkCacheService;
import com.powsybl.afs.ws.client.utils.RemoteServiceConfig;

import java.util.Objects;
import java.util.Optional;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ServiceExtension.class)
public class RemoteNetworkCacheServiceExtension implements ServiceExtension<NetworkCacheService> {

    private final Supplier<Optional<RemoteServiceConfig>> configSupplier;

    public RemoteNetworkCacheServiceExtension() {
        this(RemoteServiceConfig::load);
    }

    public RemoteNetworkCacheServiceExtension(Supplier<Optional<RemoteServiceConfig>> configSupplier) {
        this.configSupplier = Objects.requireNonNull(configSupplier);
    }

    @Override
    public ServiceKey<NetworkCacheService> getServiceKey() {
        return new ServiceKey<>(NetworkCacheService.class, true);
    }

    @Override
    public NetworkCacheService createService(ServiceCreationContext context) {
        return new RemoteNetworkCacheService(configSupplier, context.getToken());
    }
}
