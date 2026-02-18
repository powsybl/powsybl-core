/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.test;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.removed.VoltageRegulationExtension;
import com.powsybl.iidm.network.regulation.RegulationMode;
import com.powsybl.iidm.network.regulation.VoltageRegulation;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.ExportOptions;
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
        bat.newVoltageRegulation()
            .withTargetValue(100.0)
            .withMode(RegulationMode.VOLTAGE)
            .build();

        Battery bat2 = network.getBattery("BAT2");
        assertNotNull(bat2);
        bat2.newVoltageRegulation()
            .withTargetValue(100.0)
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(gen.getTerminal())
            .build();
    }

    @Test
    void test() throws IOException {
        Network network2 = allFormatsRoundTripTest(network, "voltageRegulationRoundTripRef.xml", IidmVersion.V_1_15);
        assertRemovedExtension(network2);
    }

    private static void assertRemovedExtension(Network network2) {
        Battery battery = network2.getBattery("BAT");
        VoltageRegulationExtension voltageRegulationExtensionXml = battery.getExtension(VoltageRegulationExtension.class);
        assertNull(voltageRegulationExtensionXml);
        VoltageRegulation voltageRegulation = battery.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertEquals(100.0, voltageRegulation.getTargetValue(), 0);
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertNull(voltageRegulation.getTerminal());

        Battery battery2 = network2.getBattery("BAT2");
        VoltageRegulationExtension voltageRegulationExtensionXml2 = battery2.getExtension(VoltageRegulationExtension.class);
        assertNull(voltageRegulationExtensionXml2);
        VoltageRegulation voltageRegulation2 = battery2.getVoltageRegulation();
        assertNotNull(voltageRegulation2);
        assertSame(network2.getGenerator("GEN").getTerminal(), voltageRegulation2.getTerminal());
    }

    @Test
    void testOlderVersion() throws IOException {
        // Round trip with default extension version (v1_1)
        Network network2 = allFormatsRoundTripTest(network, "voltageRegulationRoundTripRef.xml", IidmVersion.V_1_12);
        assertRemovedExtension(network2);

        // Import then export with version v1_12
        ExportOptions exportOptions = new ExportOptions();
        exportOptions.addExtensionVersion(VoltageRegulationExtension.NAME, "1.12");
        Network network3 = allFormatsRoundTripTest(network, "voltageRegulationCompatibilityVersion.xml", IidmVersion.V_1_12, exportOptions);
        assertRemovedExtension(network3);
    }
}
