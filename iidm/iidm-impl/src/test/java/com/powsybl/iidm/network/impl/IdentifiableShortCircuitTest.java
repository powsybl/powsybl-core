/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuitAdder;
import com.powsybl.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Coline Piloquet {@literal <coline.piloquet@rte-france.com>}
 */
class IdentifiableShortCircuitTest {
    @Test
    void test() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel voltageLevel = network.getVoltageLevel("VLLOAD");
        assertNotNull(voltageLevel);
        voltageLevel.newExtension(IdentifiableShortCircuitAdder.class)
                .withIpMin(1000)
                .withIpMax(2000)
                .add();
        IdentifiableShortCircuit identifiableShortCircuit = voltageLevel.getExtension(IdentifiableShortCircuit.class);
        assertEquals(1000, identifiableShortCircuit.getIpMin(), 0);
        assertEquals(2000, identifiableShortCircuit.getIpMax(), 0);
        identifiableShortCircuit.setIpMax(1500);
        identifiableShortCircuit.setIpMin(900);
        assertEquals(900, identifiableShortCircuit.getIpMin(), 0);
        assertEquals(1500, identifiableShortCircuit.getIpMax(), 0);
    }

    @Test
    void testWithoutIp() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel voltageLevel = network.getVoltageLevel("VLLOAD");
        assertNotNull(voltageLevel);
        PowsyblException e = assertThrows(PowsyblException.class, () -> voltageLevel.newExtension(IdentifiableShortCircuitAdder.class).withIpMin(Double.NaN).withIpMax(Double.NaN).add());
        assertEquals("Undefined ipMax", e.getMessage());
    }

    @Test
    void testWithoutIpMin() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel voltageLevel = network.getVoltageLevel("VLLOAD");
        assertNotNull(voltageLevel);
        voltageLevel.newExtension(IdentifiableShortCircuitAdder.class)
                .withIpMax(1000)
                .add();
        IdentifiableShortCircuit identifiableShortCircuit = voltageLevel.getExtension(IdentifiableShortCircuit.class);
        assertTrue(Double.isNaN(identifiableShortCircuit.getIpMin()));
        assertEquals(1000, identifiableShortCircuit.getIpMax(), 0);

    }
}
