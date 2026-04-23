/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.contingency;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Samir Romdhani {@literal <samir.romdhani_externe at rte-france.com>}
 */
class JsonContingenciesProviderFactoryTest {

    @Test
    void createReturnsEmptyProvider() {
        //Given
        JsonContingenciesProviderFactory factory = new JsonContingenciesProviderFactory();
        Network network = EurostagTutorialExample1Factory.create();
        //When
        List<Contingency> contingencies = factory.create().getContingencies(network);
        //Then
        assertEquals(0, contingencies.size());
    }

    @Test
    void createFromPathReadsJsonContingencyList(@TempDir Path tmpDir) throws Exception {
        //Given
        Path jsonFile = tmpDir.resolve("contingencies.json");
        try (InputStream is = getClass().getResourceAsStream("/contingencies.json")) {
            assertNotNull(is);
            Files.copy(is, jsonFile);
        }
        JsonContingenciesProviderFactory factory = new JsonContingenciesProviderFactory();
        Network network = EurostagTutorialExample1Factory.create();
        //When
        List<Contingency> contingencies = factory.create(jsonFile).getContingencies(network);
        //Then
        assertEquals(2, contingencies.size());
        assertEquals("contingency", contingencies.get(0).getId());
        assertEquals("contingency2", contingencies.get(1).getId());
    }

    @Test
    void createFromStreamReadsJsonContingencyList() throws Exception {
        //Given
        JsonContingenciesProviderFactory factory = new JsonContingenciesProviderFactory();
        Network network = EurostagTutorialExample1Factory.create();
        try (InputStream is = getClass().getResourceAsStream("/contingencies.json")) {
            //When
            List<Contingency> contingencies = factory.create(is).getContingencies(network);
            //Then
            assertEquals(2, contingencies.size());
            assertEquals("contingency", contingencies.get(0).getId());
            assertEquals("contingency2", contingencies.get(1).getId());
        }
    }
}


