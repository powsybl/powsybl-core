/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.impl;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class VoltageLevelTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private Substation substation;

    @Before
    public void setUp() {
        network = NetworkFactory.create("test", "test");
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
        assertSame(substation, voltageLevel.getSubstation());

        // setter getter
        voltageLevel.setHighVoltageLimit(300.0);
        assertEquals(300.0, voltageLevel.getHighVoltageLimit(), 0.0);
        voltageLevel.setLowVoltageLimit(200.0);
        assertEquals(200.0, voltageLevel.getLowVoltageLimit(), 0.0);
        voltageLevel.setNominalV(500.0);
        assertEquals(500.0, voltageLevel.getNominalV(), 0.0);
    }

    @Test
    public void invalidNominalV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("nominal voltage is invalid");
        createVoltageLevel("invalid", "invalid", -100.0, 1.0, 2.0);
    }

    @Test
    public void invalidLowVoltageLimit() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("low voltage limit is < 0");
        createVoltageLevel("invalid", "invalid", 100.0, -1.0, 2.0);
    }

    @Test
    public void invalidHighVoltageLimit() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("high voltage limit is < 0");
        createVoltageLevel("invalid", "invalid", 100.0, 1.0, -2.0);
    }

    @Test
    public void inconsistentVoltageLimitRange() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Inconsistent voltage limit range");
        createVoltageLevel("invalid", "invalid", 100.0, 2.0, 1.0);
    }

    @Test
    public void duplicateVoltageLevel() {
        createVoltageLevel("duplicate", "duplicate", 100.0, 2.0, 10.0);
        thrown.expect(PowsyblException.class);
        createVoltageLevel("duplicate", "duplicate", 100.0, 2.0, 10.0);
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
