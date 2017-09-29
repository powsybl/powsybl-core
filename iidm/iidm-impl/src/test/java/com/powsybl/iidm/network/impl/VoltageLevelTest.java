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
                                                .setNominalV(200.0f)
                                                .setLowVoltageLimit(100.0f)
                                                .setHighVoltageLimit(200.0f)
                                            .add();
        assertEquals(200.0f, voltageLevel.getNominalV(), 0.0f);
        assertEquals(100.0f, voltageLevel.getLowVoltageLimit(), 0.0f);
        assertEquals(200.0f, voltageLevel.getHighVoltageLimit(), 0.0f);
        assertEquals(ContainerType.VOLTAGE_LEVEL, voltageLevel.getContainerType());
        assertSame(substation, voltageLevel.getSubstation());

        // setter getter
        voltageLevel.setHighVoltageLimit(300.0f);
        assertEquals(300.0f, voltageLevel.getHighVoltageLimit(), 0.0f);
        voltageLevel.setLowVoltageLimit(200.0f);
        assertEquals(200.0f, voltageLevel.getLowVoltageLimit(), 0.0f);
        voltageLevel.setNominalV(500.0f);
        assertEquals(500.0f, voltageLevel.getNominalV(), 0.0f);
    }

    @Test
    public void invalidNominalV() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("nominal voltage is invalid");
        createVoltageLevel("invalid", "invalid", -100.0f, 1.0f, 2.0f);
    }

    @Test
    public void invalidLowVoltageLimit() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("low voltage limit is < 0");
        createVoltageLevel("invalid", "invalid", 100.0f, -1.0f, 2.0f);
    }

    @Test
    public void invalidHighVoltageLimit() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("high voltage limit is < 0");
        createVoltageLevel("invalid", "invalid", 100.0f, 1.0f, -2.0f);
    }

    @Test
    public void inconsistentVoltageLimitRange() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("Inconsistent voltage limit range");
        createVoltageLevel("invalid", "invalid", 100.0f, 2.0f, 1.0f);
    }

    @Test
    public void duplicateVoltageLevel() {
        createVoltageLevel("duplicate", "duplicate", 100.0f, 2.0f, 10.0f);
        thrown.expect(PowsyblException.class);
        createVoltageLevel("duplicate", "duplicate", 100.0f, 2.0f, 10.0f);
    }

    private void createVoltageLevel(String id, String name, float v, float low, float high) {
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
