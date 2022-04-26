/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.commons.config;

import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Optional;
import java.util.ServiceLoader;

import static org.junit.Assert.assertEquals;

/**
 * @author Florian Dupuy <florian.dupuy at rte-france.com>
 */
public class EmptyPlatformConfigTest {

    @Test
    public void test() {
        ServiceLoader<PlatformConfigProvider> mockedSl = Mockito.mock(ServiceLoader.class);
        Mockito.when(mockedSl.iterator()).thenReturn(Collections.emptyIterator());

        try (MockedStatic<?> sl = Mockito.mockStatic(ServiceLoader.class)) {
            sl.when(() -> ServiceLoader.load(PlatformConfigProvider.class, PlatformConfig.class.getClassLoader())).thenReturn(mockedSl);
            assertEquals(Optional.empty(), PlatformConfig.defaultConfig().getConfigDir());
            assertEquals(Optional.empty(), PlatformConfig.defaultConfig().getOptionalModuleConfig("any"));
            assertEquals("com.powsybl.commons.config.PlatformConfig$EmptyModuleConfigRepository", PlatformConfig.defaultConfig().getRepository().getClass().getName());
        }

        // Reset platform config in order not to interfere in other unit tests
        PlatformConfig.setDefaultConfig(null);
    }

}
