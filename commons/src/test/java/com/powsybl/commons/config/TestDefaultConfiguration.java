/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import com.google.auto.service.AutoService;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static org.mockito.Matchers.any;

/**
 * @author Miora Ralambotiana <miora.ralambotiana at rte-france.com>
 */
@AutoService(DefaultConfiguration.class)
public class TestDefaultConfiguration implements DefaultConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDefaultConfiguration.class);

    @Override
    public PlatformConfig get() {
        LOGGER.info("An empty mocked configuration is used for tests");
        PlatformConfig platformConfig = Mockito.mock(PlatformConfig.class);
        Mockito.when(platformConfig.getOptionalModuleConfig(any(String.class))).thenReturn(Optional.empty());
        return platformConfig;
    }
}
