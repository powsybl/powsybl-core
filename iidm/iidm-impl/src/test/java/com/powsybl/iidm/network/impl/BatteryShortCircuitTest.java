/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Battery;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.extensions.BatteryShortCircuit;
import com.powsybl.iidm.network.extensions.BatteryShortCircuitAdder;
import com.powsybl.iidm.network.test.BatteryNetworkFactory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.fr>}
 */
class BatteryShortCircuitTest {

    @Test
    void test() {
        Network network = BatteryNetworkFactory.create();
        Battery bat = network.getBattery("BAT");
        assertNotNull(bat);
        bat.newExtension(BatteryShortCircuitAdder.class)
            .withDirectTransX(1.0f)
            .withDirectSubtransX(2.0f)
            .withStepUpTransformerX(3.0f)
            .add();
        BatteryShortCircuit batteryShortCircuits = bat.getExtension(BatteryShortCircuit.class);
        assertEquals("batteryShortCircuit", batteryShortCircuits.getName());
        assertEquals(1.0f, batteryShortCircuits.getDirectTransX(), 0.01d);
        assertEquals(2.0f, batteryShortCircuits.getDirectSubtransX(), 0.01d);
        assertEquals(3.0f, batteryShortCircuits.getStepUpTransformerX(), 0.01d);
        batteryShortCircuits.setDirectTransX(2.0f);
        assertEquals(2.0f, batteryShortCircuits.getDirectTransX(), 0.01d);
        batteryShortCircuits.setDirectSubtransX(3.0f);
        assertEquals(3.0f, batteryShortCircuits.getDirectSubtransX(), 0.01d);
        batteryShortCircuits.setStepUpTransformerX(4.0f);
        assertEquals(4.0f, batteryShortCircuits.getStepUpTransformerX(), 0.01d);
    }

    @Test
    void testNaNDirectTransX() {
        Network network = BatteryNetworkFactory.create();
        Battery battery = network.getBattery("BAT");
        assertNotNull(battery);
        BatteryShortCircuitAdder adder = battery.newExtension(BatteryShortCircuitAdder.class)
            .withDirectTransX(Double.NaN);
        PowsyblException e = assertThrows(PowsyblException.class, adder::add);
        assertEquals("Undefined directTransX", e.getMessage());

        battery.newExtension(BatteryShortCircuitAdder.class)
            .withDirectTransX(1.0f)
            .add();
        BatteryShortCircuit batteryShortCircuits = battery.getExtension(BatteryShortCircuit.class);
        PowsyblException e1 = assertThrows(PowsyblException.class, () -> batteryShortCircuits.setDirectTransX(Double.NaN));
        assertEquals("Undefined directTransX", e1.getMessage());
    }
}
