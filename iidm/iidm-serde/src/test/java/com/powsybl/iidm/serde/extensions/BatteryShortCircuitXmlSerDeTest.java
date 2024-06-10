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
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
class BatteryShortCircuitXmlSerDeTest extends AbstractIidmSerDeTest {

    @Test
    void test() throws IOException {
        Network network = BatteryNetworkFactory.create();
        network.setCaseDate(ZonedDateTime.parse("2017-06-25T17:43:00.000+01:00"));
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);
        bat.newExtension(BatteryShortCircuitAdder.class)
            .withDirectTransX(1.0)
            .withDirectSubtransX(1.0)
            .withStepUpTransformerX(1.0)
            .add();
        BatteryShortCircuit batteryShortCircuits = bat.getExtension(BatteryShortCircuit.class);

        Network network2 = allFormatsRoundTripTest(network, "/shortcircuits/batteryShortCircuitRef.xml");

        Battery bat2 = network2.getBattery("BAT");
        assertNotNull(bat2);
        BatteryShortCircuit batteryShortCircuits2 = bat2.getExtension(BatteryShortCircuit.class);
        assertNotNull(batteryShortCircuits2);

        assertEquals(batteryShortCircuits.getDirectTransX(), batteryShortCircuits2.getDirectTransX(), 0f);
        assertEquals(batteryShortCircuits.getDirectSubtransX(), batteryShortCircuits2.getDirectSubtransX(), 0f);
        assertEquals(batteryShortCircuits.getStepUpTransformerX(), batteryShortCircuits2.getStepUpTransformerX(), 0f);
    }
}
