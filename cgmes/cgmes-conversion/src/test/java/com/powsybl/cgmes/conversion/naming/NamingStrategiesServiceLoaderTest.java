/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.cgmes.conversion.naming;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.powsybl.cgmes.conversion.export.CgmesExportContext.DEFAULT_UUID_NAMESPACE;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Coline Piloquet {@literal <coline.piloquet at rte-france.com>}
 */
class NamingStrategiesServiceLoaderTest {

    private NamingStrategiesServiceLoader loader;

    @BeforeEach
    void setUp() {
        loader = new NamingStrategiesServiceLoader();
    }

    @Test
    void testFindAllProviders() {
        List<NamingStrategyProvider> providers = loader.findAllProviders();
        assertNotNull(providers);
        assertFalse(providers.isEmpty());
        assertTrue(providers.stream().anyMatch(p -> NamingStrategyFactory.IDENTITY.equals(p.getName())));
        assertTrue(providers.stream().anyMatch(p -> NamingStrategyFactory.CGMES.equals(p.getName())));
    }

    @Test
    void testFindProviderByNameAndFactory() {
        Optional<NamingStrategyProvider> identityProv = loader.findProviderByName(NamingStrategyFactory.IDENTITY);
        assertTrue(identityProv.isPresent());

        // Test factory creation via provider
        NamingStrategy s1 = NamingStrategyFactory.create(NamingStrategyFactory.IDENTITY, DEFAULT_UUID_NAMESPACE);
        assertEquals(NamingStrategyFactory.IDENTITY, s1.getName());

        NamingStrategy s2 = NamingStrategyFactory.create(NamingStrategyFactory.CGMES, DEFAULT_UUID_NAMESPACE);
        assertEquals(NamingStrategyFactory.CGMES, s2.getName());

        assertFalse(loader.findProviderByName("NotFound").isPresent());
        assertFalse(loader.findProviderByName(null).isPresent());
        assertFalse(loader.findProviderByName(" ").isPresent());
    }
}
