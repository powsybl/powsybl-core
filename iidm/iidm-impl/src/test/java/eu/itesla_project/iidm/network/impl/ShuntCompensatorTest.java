/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;
import eu.itesla_project.iidm.network.test.EurostagTutorialExample1Factory;
import org.junit.Test;

import static org.junit.Assert.*;

public class ShuntCompensatorTest {

    @Test
    public void testSetterGetter() {
        Network network = EurostagTutorialExample1Factory.create();
        VoltageLevel voltageLevel = network.getVoltageLevel("VLHV1");
        String shuntCompensatorName = "shuntCompensatorName";
        String shuntCompensatorId = "id_s";
        float bPerSection = 3.0f;
        int currentSectionCount = 5;
        int maxSectionCount = 10;
        ShuntCompensator shuntCompensator = voltageLevel
                                        .newShunt()
                                            .setName(shuntCompensatorName)
                                            .setId(shuntCompensatorId)
                                            .setConnectableBus("NHV1")
                                            .setbPerSection(bPerSection)
                                            .setCurrentSectionCount(currentSectionCount)
                                            .setMaximumSectionCount(maxSectionCount)
                                        .add();
        assertEquals(ConnectableType.SHUNT_COMPENSATOR, shuntCompensator.getType());
        assertEquals(shuntCompensatorName, shuntCompensator.getName());
        assertEquals(shuntCompensatorId, shuntCompensator.getId());
        assertEquals(bPerSection , shuntCompensator.getbPerSection(), 0.0f);
        assertEquals(currentSectionCount, shuntCompensator.getCurrentSectionCount());
        assertEquals(maxSectionCount, shuntCompensator.getMaximumSectionCount());

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
    }
}
