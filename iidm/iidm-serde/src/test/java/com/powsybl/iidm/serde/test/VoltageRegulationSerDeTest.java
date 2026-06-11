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
import com.powsybl.iidm.serde.ExportOptions;
import com.powsybl.iidm.serde.IidmSerDeConstants;
import com.powsybl.iidm.serde.IidmVersion;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
class VoltageRegulationSerDeTest extends AbstractIidmSerDeTest {

    private static Network network;

    @BeforeAll
    static void init() {
        network = BatteryNetworkFactory.create();
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
    }

    @Test
    void test() throws IOException {
        Network network2 = allFormatsRoundTripTest(network, "voltageRegulationRoundTripRef.xml", IidmSerDeConstants.CURRENT_IIDM_VERSION);
        assertExtension(network2);
    }

    private static void assertExtension(Network network2) {
        VoltageRegulation voltageRegulationXml = network2.getBattery("BAT").getExtension(VoltageRegulation.class);
        assertNotNull(voltageRegulationXml);
        assertEquals(100.0, voltageRegulationXml.getTargetV(), 0);
        assertTrue(voltageRegulationXml.isVoltageRegulatorOn());
        assertEquals(VoltageRegulation.NAME, voltageRegulationXml.getName());

        VoltageRegulation voltageRegulationXml2 = network2.getBattery("BAT2").getExtension(VoltageRegulation.class);
        assertNotNull(voltageRegulationXml);
        assertSame(network2.getGenerator("GEN").getTerminal(), voltageRegulationXml2.getRegulatingTerminal());
    }

    @Test
    void testOlderVersion() throws IOException {
        // Round trip with default extension version (v1_1)
        Network network2 = allFormatsRoundTripTest(network, "voltageRegulationRoundTripRef.xml", IidmVersion.V_1_12);
        assertExtension(network2);

        // Import then export with version v1_12
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.addExtensionVersion(VoltageRegulation.NAME, "1.12");
        Network network3 = allFormatsRoundTripTest(network, "voltageRegulationCompatibilityVersion.xml", IidmVersion.V_1_12, exportOptions);
        assertExtension(network3);
    }
}
