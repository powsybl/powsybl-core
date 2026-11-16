/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */
package com.powsybl.iidm.network.tck;

import com.google.common.collect.Iterables;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractVoltageLevelTest {

    private static final String DUPLICATE = "duplicate";

    private static final String INVALID = "invalid";

    private Network network;
    private Substation substation;

    @BeforeEach
    public void setUp() {
        network = Network.create("test", "test");
        substation = network.newSubstation()
                                .setCountry(Country.AF)
                                .setTso("tso")
                                .setName("sub")
                                .setId("subId")
                            .add();
    }

    @Test
    public void baseTests() {
        // adder
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                                                .setTopologyKind(TopologyKind.BUS_BREAKER)
                                                .setId("bbVL")
                                                .setName("bbVL_name")
                                                .setNominalV(200.0)
                                                .setLowVoltageLimit(100.0)
                                                .setHighVoltageLimit(200.0)
                                            .add();
        assertEquals(200.0, voltageLevel.getNominalV(), 0.0);
        assertEquals(100.0, voltageLevel.getLowVoltageLimit(), 0.0);
        assertEquals(200.0, voltageLevel.getHighVoltageLimit(), 0.0);
        assertEquals(ContainerType.VOLTAGE_LEVEL, voltageLevel.getContainerType());
        assertSame(substation, voltageLevel.getSubstation().orElse(null));

        // setter getter
        voltageLevel.setHighVoltageLimit(300.0);
        assertEquals(300.0, voltageLevel.getHighVoltageLimit(), 0.0);
        voltageLevel.setLowVoltageLimit(200.0);
        assertEquals(200.0, voltageLevel.getLowVoltageLimit(), 0.0);
        voltageLevel.setNominalV(500.0);
        assertEquals(500.0, voltageLevel.getNominalV(), 0.0);

        // Remove
        voltageLevel.remove();    // <---- TODO HERE
        assertNotNull(voltageLevel);
        try {
            voltageLevel.getSubstation();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access substation of removed voltage level bbVL", e.getMessage());
        }
        try {
            voltageLevel.getNetwork();
            fail();
        } catch (PowsyblException e) {
            assertEquals("Cannot access network of removed voltage level bbVL", e.getMessage());
        }
        assertNull(network.getVoltageLevel("bbVL"));
    }

    @Test
    public void testWithoutSubstation() {
        network.newVoltageLevel()
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .setId("bbVL")
                .setName("bbVL_name")
                .setNominalV(200.0)
                .setLowVoltageLimit(100.0)
                .setHighVoltageLimit(200.0)
                .add();
        VoltageLevel voltageLevel = network.getVoltageLevel("bbVL");
        assertNotNull(voltageLevel);
        assertEquals(200.0, voltageLevel.getNominalV(), 0.0);
        assertEquals(100.0, voltageLevel.getLowVoltageLimit(), 0.0);
        assertEquals(200.0, voltageLevel.getHighVoltageLimit(), 0.0);
        assertEquals(ContainerType.VOLTAGE_LEVEL, voltageLevel.getContainerType());
        assertTrue(voltageLevel.getSubstation().isEmpty());

        assertTrue(Iterables.isEmpty(voltageLevel.getConnectables()));
        voltageLevel.getBusBreakerView().newBus().setId("bbVL_1").add();
        Load load = voltageLevel.newLoad()
                .setId("LOAD")
                .setBus("bbVL_1")
                .setP0(600.0)
                .setQ0(200.0)
                .add();
        assertEquals(1, Iterables.size(voltageLevel.getConnectables()));
        assertTrue(Iterables.contains(voltageLevel.getConnectables(), load));
    }

    @Test
    public void invalidNominalV() {
        ValidationException e = assertThrows(ValidationException.class, () -> createVoltageLevel(INVALID, INVALID, -100.0, 1.0, 2.0));
        assertTrue(e.getMessage().contains("nominal voltage is invalid"));
    }

    @Test
    public void invalidLowVoltageLimit() {
        ValidationException e = assertThrows(ValidationException.class, () -> createVoltageLevel(INVALID, INVALID, 100.0, -1.0, 2.0));
        assertTrue(e.getMessage().contains("low voltage limit is < 0"));
    }

    @Test
    public void invalidHighVoltageLimit() {
        ValidationException e = assertThrows(ValidationException.class, () -> createVoltageLevel(INVALID, INVALID, 100.0, 1.0, -2.0));
        assertTrue(e.getMessage().contains("high voltage limit is < 0"));
    }

    @Test
    public void inconsistentVoltageLimitRange() {
        ValidationException e = assertThrows(ValidationException.class, () -> createVoltageLevel(INVALID, INVALID, 100.0, 2.0, 1.0));
        assertTrue(e.getMessage().contains("Inconsistent voltage limit range"));
    }

    @Test
    public void duplicateVoltageLevel() {
        createVoltageLevel(DUPLICATE, DUPLICATE, 100.0, 2.0, 10.0);
        assertThrows(PowsyblException.class, () -> createVoltageLevel(DUPLICATE, DUPLICATE, 100.0, 2.0, 10.0));
    }

    private void createVoltageLevel(String id, String name, double v, double low, double high) {
        substation.newVoltageLevel()
                    .setId(id)
                    .setName(name)
                    .setTopologyKind(TopologyKind.BUS_BREAKER)
                    .setNominalV(v)
                    .setLowVoltageLimit(low)
                    .setHighVoltageLimit(high)
                .add();
    }

}
