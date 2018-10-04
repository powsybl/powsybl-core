/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs.local;

import com.google.auto.service.AutoService;
import com.google.common.base.Supplier;
import com.powsybl.afs.ServiceCreationContext;
import com.powsybl.afs.ServiceExtension;
import com.powsybl.commons.config.ComponentDefaultConfig;
import com.powsybl.security.SecurityAnalysisFactory;
import com.powsybl.security.SecurityAnalysisFactoryImpl;
import com.powsybl.security.afs.SecurityAnalysisRunningService;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ServiceExtension.class)
public class LocalSecurityAnalysisRunningServiceExtension implements ServiceExtension<SecurityAnalysisRunningService> {

    private final Supplier<SecurityAnalysisFactory> factorySupplier;

    public LocalSecurityAnalysisRunningServiceExtension() {
        this(() -> ComponentDefaultConfig.load().newFactoryImpl(SecurityAnalysisFactory.class, SecurityAnalysisFactoryImpl.class));
    }

    public LocalSecurityAnalysisRunningServiceExtension(Supplier<SecurityAnalysisFactory> factorySupplier) {
        this.factorySupplier = Objects.requireNonNull(factorySupplier);
    }

    @Override
    public ServiceKey<SecurityAnalysisRunningService> getServiceKey() {
        return new ServiceKey<>(SecurityAnalysisRunningService.class, false);
    }

    @Override
    public SecurityAnalysisRunningService createService(ServiceCreationContext context) {
        return new LocalSecurityAnalysisRunningService(factorySupplier);
    }
}
