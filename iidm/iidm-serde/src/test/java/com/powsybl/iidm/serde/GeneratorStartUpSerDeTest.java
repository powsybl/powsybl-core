/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Etienne Lesot {@literal <etienne.lesot at rte-france.com>}
 */
class GeneratorStartUpSerDeTest {

    @Test
    void testCompatibility() throws URISyntaxException {
        Path path = Paths.get(Objects.requireNonNull(getClass().getClassLoader().getResource("generatorStartUp.xml")).toURI());
        Network network = NetworkSerDe.read(path);
        GeneratorStartup generatorStartup = network.getGenerator("GEN").getExtension(GeneratorStartup.class);
        assertNotNull(generatorStartup);
        assertEquals(600.0, generatorStartup.getPlannedActivePowerSetpoint());
        assertEquals(5.0, generatorStartup.getStartupCost());
        assertEquals(10.0, generatorStartup.getMarginalCost());
        // planned outage rate is greater than 1 so it is set to 1
        assertEquals(1, generatorStartup.getPlannedOutageRate());
        // forced outage rate is smaller than 0 so it is set to 0
        assertEquals(0, generatorStartup.getForcedOutageRate());
    }
}
