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
        bat.setLocalTargetV(90.0);
        bat.newVoltageRegulation()
            .withMode(RegulationMode.VOLTAGE)
            .build();

        Battery bat2 = network.getBattery("BAT2");
        assertNotNull(bat2);
        bat2.newVoltageRegulation()
            .withTargetValue(100.0)
            .withMode(RegulationMode.VOLTAGE)
            .withTerminal(gen.getTerminal())
            .withRegulating(false)
            .build();
    }

    @Test
    void testWithExpiredExtension() throws IOException {
        // 1.17: last version before the extension "removal"
        Network network2 = allFormatsRoundTripTest(network, "voltageRegulationRoundTripRef.xml", IidmVersion.V_1_17);
        assertRemovedExtension(network2);
    }

    private static void assertRemovedExtension(Network network2) {
        Battery battery = network2.getBattery("BAT");
        VoltageRegulationExtension voltageRegulationExtensionXml = battery.getExtension(VoltageRegulationExtension.class);
        assertNull(voltageRegulationExtensionXml);
        VoltageRegulation voltageRegulation = battery.getVoltageRegulation();
        assertNotNull(voltageRegulation);
        assertTrue(Double.isNaN(voltageRegulation.getTargetValue()));
        assertEquals(90.0, battery.getLocalTargetV(), 0);
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation.getMode());
        assertTrue(battery.isRegulating());
        assertTrue(voltageRegulation.isRegulating());
        assertNull(voltageRegulation.getTerminal());

        Battery battery2 = network2.getBattery("BAT2");
        VoltageRegulationExtension voltageRegulationExtensionXml2 = battery2.getExtension(VoltageRegulationExtension.class);
        assertNull(voltageRegulationExtensionXml2);
        VoltageRegulation voltageRegulation2 = battery2.getVoltageRegulation();
        assertNotNull(voltageRegulation2);
        assertEquals(100.0, voltageRegulation2.getTargetValue(), 0.01);
        assertTrue(Double.isNaN(battery2.getLocalTargetV()));
        assertEquals(RegulationMode.VOLTAGE, voltageRegulation2.getMode());
        assertFalse(battery2.isRegulating());
        assertFalse(voltageRegulation2.isRegulating());
        assertSame(network2.getGenerator("GEN").getTerminal(), voltageRegulation2.getTerminal());
    }

    @Test
    void testOlderVersion() throws IOException {
        // Round trip with default extension version (v1_1)
        Network network2 = allFormatsRoundTripTxtTest(network, "voltageRegulationRoundTripRef.xml", IidmVersion.V_1_12);
        assertRemovedExtension(network2);

        // Import then export with version v1_12
        ExportOptions exportOptions = new ExportOptions();
        // exportOptions.addExtensionVersion(VoltageRegulation.NAME, "1.12"); //fixme samir
        Network network3 = allFormatsRoundTripTxtTest(network, "voltageRegulationCompatibilityVersion.xml", IidmVersion.V_1_12, exportOptions);
        assertRemovedExtension(network3);
    }
}
