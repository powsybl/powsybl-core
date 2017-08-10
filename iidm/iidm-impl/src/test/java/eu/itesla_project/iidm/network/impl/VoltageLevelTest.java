/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package eu.itesla_project.iidm.network.impl;

import eu.itesla_project.iidm.network.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class VoltageLevelTest {

    @Test
    public void testSetterGetter() {
        Network network = NetworkFactory.create("test", "test");
        Substation substation = network.newSubstation()
                                    .setCountry(Country.AF).setTso("tso").setName("sub").setId("subId").add();
        VoltageLevel voltageLevel = substation.newVoltageLevel()
                                                .setTopologyKind(TopologyKind.BUS_BREAKER)
                                                .setId("bbVL")
                                                .setName("bbVL_name")
                                                .setNominalV(200.0f).add();
        assertEquals(200.0f, voltageLevel.getNominalV(), 0.0f);
        assertEquals(ContainerType.VOLTAGE_LEVEL, voltageLevel.getContainerType());

        try {
            voltageLevel.setHighVoltageLimit(-10.0f);
            fail();
        } catch (ValidationException ignored) {
        }
        voltageLevel.setHighVoltageLimit(300.0f);
        assertEquals(300.0f, voltageLevel.getHighVoltageLimit(), 0.0f);
        try {
            voltageLevel.setLowVoltageLimit(-1.0f);
            fail();
        } catch (ValidationException ignored) {
        }
        try {
            voltageLevel.setLowVoltageLimit(500.0f);
            fail();
        } catch (ValidationException ignored) {
        }
        voltageLevel.setLowVoltageLimit(200.0f);
        assertEquals(200.0f, voltageLevel.getLowVoltageLimit(), 0.0f);
        voltageLevel.setNominalV(500.0f);
        assertEquals(500.0f, voltageLevel.getNominalV(), 0.0f);
    }
}