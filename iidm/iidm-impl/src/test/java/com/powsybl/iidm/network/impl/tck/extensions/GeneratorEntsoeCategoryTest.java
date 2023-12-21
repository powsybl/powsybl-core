/*
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package com.powsybl.iidm.network.impl.tck.extensions;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategory;
import com.powsybl.iidm.network.extensions.GeneratorEntsoeCategoryAdder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Nicolas Rol {@literal <nicolas.rol at rte-france.com>}
 */
class GeneratorEntsoeCategoryTest {

    @Test
    void testExtension() {
        Network network = Network.create("test", "test");
        Substation s = network.newSubstation()
            .setId("S")
            .setCountry(Country.FR)
            .add();
        VoltageLevel vl = s.newVoltageLevel()
            .setId("VL")
            .setNominalV(400)
            .setTopologyKind(TopologyKind.BUS_BREAKER)
            .add();
        vl.getBusBreakerView().newBus()
            .setId("B")
            .add();
        Generator generator = vl.newGenerator()
            .setId("G1")
            .setBus("B")
            .setMaxP(100)
            .setMinP(50)
            .setTargetP(100)
            .setTargetV(400)
            .setVoltageRegulatorOn(true)
            .add();

        GeneratorEntsoeCategory generatorEntsoeCategory = generator.newExtension(GeneratorEntsoeCategoryAdder.class)
            .withCode(10)
            .add();
        assertEquals(10, generatorEntsoeCategory.getCode());

        // Wrong code
        IllegalArgumentException e0 = assertThrows(IllegalArgumentException.class, () -> generatorEntsoeCategory.setCode(-1));
        assertEquals("Bad generator ENTSO-E code -1 for generator G1",
            e0.getMessage());

        // Right code
        generatorEntsoeCategory.setCode(5);
        assertEquals(5, generatorEntsoeCategory.getCode());
    }
}
