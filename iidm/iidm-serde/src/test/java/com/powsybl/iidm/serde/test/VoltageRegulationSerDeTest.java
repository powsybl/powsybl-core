/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.test;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.VoltageRegulation;
import com.powsybl.iidm.network.extensions.VoltageRegulationAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.IidmSerDeConstants;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
class VoltageRegulationSerDeTest extends AbstractIidmSerDeTest {
    @Test
    void test() throws IOException {
        Network network = BatteryNetworkFactory.create();
        Generator gen = network.getGenerator("GEN");

        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);
        bat.newExtension(VoltageRegulationAdder.class)
            .withVoltageRegulatorOn(true)
            .withTargetV(100.0)
            .add();

        Battery bat2 = network.getBattery("BAT2");
        assertNotNull(bat2);
        bat2.newExtension(VoltageRegulationAdder.class)
            .withRegulatingTerminal(gen.getTerminal())
            .withVoltageRegulatorOn(true)
            .withTargetV(100)
            .add();

        Network network2 = allFormatsRoundTripTest(network, "voltageRegulationRoundTripRef.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION);

        VoltageRegulation voltageRegulationXml = network2.getBattery("BAT").getExtension(VoltageRegulation.class);
        assertNotNull(voltageRegulationXml);
        assertEquals(100.0, voltageRegulationXml.getTargetV(), 0);
        assertTrue(voltageRegulationXml.isVoltageRegulatorOn());
        assertEquals(VoltageRegulation.NAME, voltageRegulationXml.getName());

        VoltageRegulation voltageRegulationXml2 = network2.getBattery("BAT2").getExtension(VoltageRegulation.class);
        assertNotNull(voltageRegulationXml);
        assertSame(network2.getGenerator("GEN").getTerminal(), voltageRegulationXml2.getRegulatingTerminal());
    }
}
