/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.FictitiousSwitchFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractSwitchNodeBreakerTest {

    private Network network;
    private VoltageLevel voltageLevel;

    @BeforeEach
    public void initNetwork() {
        network = FictitiousSwitchFactory.create();
        voltageLevel = network.getVoltageLevel("C");
    }

    @Test
    public void addSwitchWithSameNodeAtBothEnds() {
        int newNode = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
        ValidationException e = assertThrows(ValidationException.class, () -> voltageLevel.getNodeBreakerView().newSwitch()
            .setId("Sw1")
            .setNode1(newNode)
            .setNode2(newNode)
            .setKind(SwitchKind.BREAKER)
            .add());
        assertEquals("Switch 'Sw1': same node at both ends", e.getMessage());
    }

    @Test
    public void addSwitchWithNullKind() {
        int newNode1 = voltageLevel.getNodeBreakerView().getMaximumNodeIndex() + 1;
        int newNode2 = newNode1 + 1;
        ValidationException e = assertThrows(ValidationException.class, () -> voltageLevel.getNodeBreakerView().newSwitch()
            .setId("Sw1")
            .setNode1(newNode1)
            .setNode2(newNode2)
            .add());
        assertEquals("Switch 'Sw1': kind is not set", e.getMessage());
    }

    @Test
    public void switchWithSolvedOpen() {
        Switch disconnector = network.getSwitch("R");
        disconnector.setSolvedOpen(true);

        assertTrue(disconnector.isSolvedOpen());
        assertEquals(Optional.of(true), disconnector.findSolvedOpen());

        disconnector.unsetSolvedOpen();
        assertNull(disconnector.isSolvedOpen());
        assertFalse(disconnector.findSolvedOpen().isPresent());
    }
}
