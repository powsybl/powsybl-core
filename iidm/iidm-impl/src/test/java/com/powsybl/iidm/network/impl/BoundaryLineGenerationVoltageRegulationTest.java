/**
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.iidm.network.BoundaryLine;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.ValidationException;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.iidm.network.regulation.RegulationMode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BoundaryLineGenerationVoltageRegulationTest {

    private BoundaryLine boundaryLine;

    @BeforeEach
    void setUp() {
        Network network = Network.create("test", "test");
        VoltageLevel vl = network.newSubstation().setId("S").add()
                .newVoltageLevel().setId("VL").setNominalV(400).setTopologyKind(TopologyKind.BUS_BREAKER).add();
        vl.getBusBreakerView().newBus().setId("B").add();
        boundaryLine = vl.newBoundaryLine()
                .setId("BL")
                .setR(1.0)
                .setX(1.0)
                .setP0(0.0)
                .setQ0(0.0)
                .setPairingKey("code")
                .setBus("B")
                .setConnectableBus("B")
                .newGeneration()
                    .setTargetP(0.0)
                    .setLocalTargetQ(0.0)
                    .setLocalTargetV(400.0)
                .add()
                .add();
    }

    @Test
    void shouldRejectTerminalOnBoundaryLineGenerationRegulation() {
        BoundaryLine.Generation generation = boundaryLine.getGeneration();
        generation.setLocalTargetV(400.0);
        var adder = generation.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(true)
                .withTerminal(boundaryLine.getTerminal())
                .withTargetValue(400.0);
        assertThrows(ValidationException.class, adder::build);
    }

    @Test
    void shouldRejectTargetValueOnBoundaryLineGenerationRegulation() {
        BoundaryLine.Generation generation = boundaryLine.getGeneration();
        generation.setLocalTargetV(400.0);
        var adder = generation.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(true)
                .withTargetValue(400.0);
        assertThrows(ValidationException.class, adder::build);
    }

    @Test
    void shouldAcceptLocalVoltageRegulation() {
        BoundaryLine.Generation generation = boundaryLine.getGeneration();
        generation.setLocalTargetV(400.0);
        generation.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE)
                .withRegulating(true)
                .build();
        assertNotNull(generation.getVoltageRegulation());
        assertTrue(generation.getVoltageRegulation().isRegulating());
        assertEquals(RegulationMode.VOLTAGE, generation.getVoltageRegulation().getMode());
        assertEquals(400.0, generation.getLocalTargetV(), 0.0);
    }

    @Test
    void shouldRejectRemoteOnlyModeOnBoundaryLineGeneration() {
        BoundaryLine.Generation generation = boundaryLine.getGeneration();
        generation.setLocalTargetV(400.0);
        var adder = generation.newVoltageRegulation()
                .withMode(RegulationMode.VOLTAGE_PER_REACTIVE_POWER)
                .withRegulating(true);
        assertThrows(ValidationException.class, adder::build);
    }
}
