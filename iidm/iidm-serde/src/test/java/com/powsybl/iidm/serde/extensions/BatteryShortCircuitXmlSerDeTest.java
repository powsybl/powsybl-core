/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.serde.extensions;

import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.iidm.network.extensions.BatteryShortCircuitAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import com.powsybl.iidm.serde.AbstractIidmSerDeTest;
import com.powsybl.iidm.serde.ExportOptions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
class BatteryShortCircuitXmlSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void roundTripTest() throws IOException {
        NetworkData networkData = getNetworkData();
        Network network = networkData.network();
        BatteryShortCircuit batteryShortCircuit = networkData.batteryShortCircuit();

        Network network2 = allFormatsRoundTripTest(network, "/shortcircuits/batteryShortCircuitRef_V1_0.xml");

        Battery bat2 = network2.getBattery("BAT");
        assertNotNull(bat2);
        BatteryShortCircuit batteryShortCircuits2 = bat2.getExtension(BatteryShortCircuit.class);
        assertNotNull(batteryShortCircuits2);

        assertEquals(batteryShortCircuit.getDirectTransX(), batteryShortCircuits2.getDirectTransX(), 0.001d);
        assertEquals(batteryShortCircuit.getDirectSubtransX(), batteryShortCircuits2.getDirectSubtransX(), 0.001d);
        assertEquals(batteryShortCircuit.getStepUpTransformerX(), batteryShortCircuits2.getStepUpTransformerX(), 0.001d);
    }

    @Test
    void roundTripTestV01() throws IOException {
        NetworkData networkData = getNetworkData();
        Network network = networkData.network();
        BatteryShortCircuit batteryShortCircuit = networkData.batteryShortCircuit();

        // Use an extension version which serialization name is not the default
        ExportOptions exportOptions = new ExportOptions()
                .addExtensionVersion(BatteryShortCircuit.NAME, "0.1");
        Network network2 = allFormatsRoundTripTest(network, "/shortcircuits/batteryShortCircuitRef_V0_1.xml", exportOptions);

        Battery bat2 = network2.getBattery("BAT");
        assertNotNull(bat2);
        BatteryShortCircuit batteryShortCircuits2 = bat2.getExtension(BatteryShortCircuit.class);
        assertNotNull(batteryShortCircuits2);

        assertEquals(batteryShortCircuit.getDirectTransX(), batteryShortCircuits2.getDirectTransX(), 0.001d);
        assertEquals(batteryShortCircuit.getStepUpTransformerX(), batteryShortCircuits2.getStepUpTransformerX(), 0.001d);
        assertTrue(Double.isNaN(batteryShortCircuits2.getDirectSubtransX())); // This attribute is not exported in V0.1
    }

    private static NetworkData getNetworkData() {
        Network network = BatteryNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);
        bat.newExtension(BatteryShortCircuitAdder.class)
                .withDirectTransX(1.0)
                .withDirectSubtransX(2.0)
                .withStepUpTransformerX(3.0)
                .add();
        BatteryShortCircuit batteryShortCircuits = bat.getExtension(BatteryShortCircuit.class);
        return new NetworkData(network, batteryShortCircuits);
    }

    private record NetworkData(Network network, BatteryShortCircuit batteryShortCircuit) {
    }
}
