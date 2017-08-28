/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class ShuntCompensatorTest {

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Network network;
    private VoltageLevel voltageLevel;

    @Before
    public void setUp() {
        network = NoEquipmentNetworkFactory.create();
        voltageLevel = network.getVoltageLevel("vl1");
    }

    @Test
    public void baseTest() {
        // adder
        ShuntCompensator shuntCompensator = voltageLevel
                                        .newShunt()
                                            .setId("shunt")
                                            .setName("shuntName")
                                            .setConnectableBus("busA")
                                            .setbPerSection(5.0f)
                                            .setCurrentSectionCount(6)
                                            .setMaximumSectionCount(10)
                                        .add();
        assertEquals(ConnectableType.SHUNT_COMPENSATOR, shuntCompensator.getType());
        assertEquals("shuntName", shuntCompensator.getName());
        assertEquals("shunt", shuntCompensator.getId());
        assertEquals(5.0f, shuntCompensator.getbPerSection(), 0.0f);
        assertEquals(6, shuntCompensator.getCurrentSectionCount());
        assertEquals(10, shuntCompensator.getMaximumSectionCount());

        // setter getter
        try {
            shuntCompensator.setbPerSection(0.0f);
            fail();
        } catch (ValidationException ignored) {
        }
        shuntCompensator.setbPerSection(1.0f);
        assertEquals(1.0f, shuntCompensator.getbPerSection(), 0.0f);

        try {
            shuntCompensator.setCurrentSectionCount(-1);
            fail();
        } catch (ValidationException ignored) {
        }
        try {
            // max = 10 , current could not be 20
            shuntCompensator.setCurrentSectionCount(20);
            fail();
        } catch (ValidationException ignored) {
        }
        shuntCompensator.setCurrentSectionCount(6);
        assertEquals(6, shuntCompensator.getCurrentSectionCount());

        try {
            shuntCompensator.setMaximumSectionCount(1);
            fail();
        } catch (ValidationException ignored) {
        }
        shuntCompensator.setMaximumSectionCount(20);
        assertEquals(20, shuntCompensator.getMaximumSectionCount());

        // remove
        int count = network.getShuntCount();
        shuntCompensator.remove();
        assertNull(network.getShunt("shunt"));
        assertNotNull(shuntCompensator);
        assertEquals(count - 1, network.getShuntCount());
    }

    @Test
    public void invalidbPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("susceptance per section is invalid");
        createShunt("invalid", "invalid", Float.NaN, 5, 10);
    }

    @Test
    public void invalidZerobPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("susceptance per section is equal to zero");
        createShunt("invalid", "invalid", 0.0f, 5, 10);
    }

    @Test
    public void invalidNegativeMaxPerSection() {
        thrown.expect(ValidationException.class);
        thrown.expectMessage("should be greater than 0");
        createShunt("invalid", "invalid", 2.0f, 0, -1);
    }

    private void createShunt(String id, String name, float bPerSection, int currentSectionCount, int maxSectionCount) {
        voltageLevel.newShunt()
                    .setId(id)
                    .setName(name)
                    .setConnectableBus("busA")
                    .setbPerSection(bPerSection)
                    .setCurrentSectionCount(currentSectionCount)
                    .setMaximumSectionCount(maxSectionCount)
                .add();
    }
}
