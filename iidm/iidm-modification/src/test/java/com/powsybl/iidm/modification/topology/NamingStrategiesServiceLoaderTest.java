/**
 * Copyright (c) 2025, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.modification.topology;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Ghazwa Rehili {@literal <ghazwa.rehili at rte-france.com>}
 */
class NamingStrategiesServiceLoaderTest {

    public static final String DEFAULT = "Default";
    private NamingStrategiesServiceLoader loader;

    @BeforeEach
    void setUp() {
        loader = new NamingStrategiesServiceLoader();
    }

    @Test
    void testFindAllNamingStrategies() {
        List<NamingStrategy> strategies = loader.findAllNamingStrategies();

        assertNotNull(strategies);
        assertFalse(strategies.isEmpty());

        assertTrue(strategies.stream().anyMatch(s -> DEFAULT.equals(s.getName())));
    }

    @Test
    void testFindNamingStrategyByName() {
        Optional<NamingStrategy> found = loader.findNamingStrategyByName(DEFAULT);
        assertTrue(found.isPresent());
        assertEquals(DEFAULT, found.get().getName());

        assertFalse(loader.findNamingStrategyByName("NonExistent").isPresent());
        assertFalse(loader.findNamingStrategyByName(null).isPresent());
        assertFalse(loader.findNamingStrategyByName(" ").isPresent());
    }
}
