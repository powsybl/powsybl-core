/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.afs;

import com.google.auto.service.AutoService;
import com.powsybl.afs.ServiceExtension;
import com.powsybl.commons.config.ComponentDefaultConfig;
import com.powsybl.security.SecurityAnalysisFactory;
import com.powsybl.security.SecurityAnalysisFactoryImpl;

import java.util.Objects;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(ServiceExtension.class)
public class LocalSecurityAnalysisRunningServiceExtension implements ServiceExtension<SecurityAnalysisRunningService> {

    private final SecurityAnalysisFactory factory;

    public LocalSecurityAnalysisRunningServiceExtension() {
        this(ComponentDefaultConfig.load().newFactoryImpl(SecurityAnalysisFactory.class, SecurityAnalysisFactoryImpl.class));
    }

    public LocalSecurityAnalysisRunningServiceExtension(SecurityAnalysisFactory factory) {
        this.factory = Objects.requireNonNull(factory);
    }

    @Override
    public ServiceKey<SecurityAnalysisRunningService> getServiceKey() {
        return new ServiceKey<>(SecurityAnalysisRunningService.class, false);
    }

    @Override
    public SecurityAnalysisRunningService createService() {
        return new LocalSecurityAnalysisRunningService(factory);
    }
}
